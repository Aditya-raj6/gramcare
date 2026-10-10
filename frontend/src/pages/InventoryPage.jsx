import { useEffect, useState } from 'react';
import { Button, Col, Form, Input, InputNumber, Modal, Popconfirm, Row, Space, Tabs, Tag, Tooltip, message } from 'antd';
import { ExportOutlined, ImportOutlined, MedicineBoxOutlined, StopOutlined } from '@ant-design/icons';
import api, { errorMessage } from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import RemoteSelect, { useRemoteOptions } from '../components/RemoteSelect';
import { E, PRIORITIES, humanize } from '../utils/constants';
import { fmtDate, fmtMoney, fmtNumber } from '../utils/format';

const REQUEST_NEXT = { PENDING: ['APPROVED', 'REJECTED'], APPROVED: ['ORDERED', 'FULFILLED', 'REJECTED'], ORDERED: ['FULFILLED'], REJECTED: ['PENDING'], FULFILLED: [] };

function MovementModal({ mode, item, onClose, onDone }) {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const issue = mode === 'issue';
  const submit = async () => {
    const v = await form.validateFields();
    setSaving(true);
    try {
      const { data } = await api.post(`/stock/${item.id}/${mode}`, v);
      message.success(`${issue ? 'Issued' : 'Received'} ${v.quantity} · balance ${data.quantity}${data.stockStatus !== 'IN_STOCK' ? ` (${humanize(data.stockStatus)} — replenishment raised automatically)` : ''}`);
      onDone();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setSaving(false);
    }
  };
  return (
    <Modal open title={`${issue ? 'Issue' : 'Receive'} · ${item.medicineName} @ ${item.healthCentreName}`} okText={issue ? 'Issue stock' : 'Receive stock'}
      confirmLoading={saving} onOk={submit} onCancel={onClose} destroyOnClose>
      <p className="muted">Batch {item.batchNumber || '—'} · available <b>{item.quantity}</b> · reorder level {item.reorderLevel}</p>
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item name="quantity" label="Quantity" rules={[{ required: true }]}>
          <InputNumber min={1} max={issue ? item.quantity : undefined} style={{ width: '100%' }} />
        </Form.Item>
        {issue && (
          <>
            <Form.Item name="residentId" label="Issued to resident (optional)">
              <RemoteSelect endpoint="/residents" labelOf={(r) => `${r.fullName} · ${r.residentCode}`} onPick={(r) => form.setFieldValue('issuedTo', r?.fullName)} />
            </Form.Item>
            <Form.Item name="issuedTo" label="Issued to (name / camp / ward)" rules={[{ required: true }]}><Input /></Form.Item>
          </>
        )}
        <Form.Item name="reference" label={issue ? 'Reference (prescription / appointment no.)' : 'Reference (invoice / challan no.)'}><Input /></Form.Item>
        <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
      </Form>
    </Modal>
  );
}

export default function InventoryPage() {
  const [stats, setStats] = useState({});
  const [movement, setMovement] = useState(null);
  const [tick, setTick] = useState(0);
  const refresh = () => setTick((t) => t + 1);
  const centreOptions = useRemoteOptions('/health-centres').map(({ value, label }) => ({ value, label }));

  useEffect(() => {
    api.get('/inventory/stats').then(({ data }) => setStats(data)).catch(() => {});
  }, [tick]);

  const writeOff = async (item) => {
    try {
      await api.post(`/stock/${item.id}/write-off`);
      message.success('Expired batch written off');
      refresh();
    } catch (e) {
      message.error(errorMessage(e));
    }
  };

  return (
    <>
      <PageHeader icon={<MedicineBoxOutlined />} title="Medicine & Medical Inventory" subtitle="Medicines, supplies, stock levels, issue records and replenishment requirements" />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={8} xl={4}><StatCard title="Catalogue items" value={stats.catalogueItems} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Stock records" value={stats.stockRecords} color="#2a78d6" footer={`Value ${fmtMoney(stats.stockValue)}`} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Low stock" value={stats.lowStock} color="#eda100" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Out of stock" value={stats.outOfStock} color="#d03b3b" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Expired / expiring" value={`${stats.expired ?? 0} / ${stats.expiringSoon ?? 0}`} color="#ec835a" footer="Expiring within 60 days" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Open requests" value={stats.openReplenishments} color="#6250d6" footer={`${stats.issuesLast30Days ?? 0} issues in 30 days`} /></Col>
      </Row>
      <Tabs className="module-tabs" items={[
        { key: 'stock', label: 'Stock levels', children: (
          <CrudPage endpoint="/stock" entityName="Stock item" permission="inventory" refreshKey={tick} onChanged={refresh}
            searchKeys={['medicineName', 'healthCentreName', 'batchNumber', 'category']}
            filters={[{ name: 'healthCentreId', label: 'Health centre', options: centreOptions },
              { name: 'category', label: 'Category', options: E.itemCategory }]}
            fields={[
              { name: 'medicineId', label: 'Medicine / supply', type: 'remote', endpoint: '/medicines', labelOf: (m) => `${m.name} ${m.strength || ''} (${m.code})`, required: true },
              { name: 'healthCentreId', label: 'Health centre', type: 'remote', endpoint: '/health-centres', required: true, onPick: (c, f) => f.setFieldValue('healthCentreName', c?.name) },
              { name: 'healthCentreName', label: 'Centre', hidden: true },
              { name: 'batchNumber', label: 'Batch no.' }, { name: 'quantity', label: 'Quantity on hand', type: 'number', min: 0, required: true },
              { name: 'reorderLevel', label: 'Reorder level', type: 'number', min: 0, help: 'Defaults to the catalogue reorder level' },
              { name: 'expiryDate', label: 'Expiry date', type: 'date' }, { name: 'unitPrice', label: 'Unit price', type: 'number', unit: '₹', min: 0 },
              { name: 'storageLocation', label: 'Storage location' },
            ]}
            allowDelete={false}
            rowActions={(r, reload, writable) => writable && (
              <Space size={4}>
                <Tooltip title="Issue"><Button size="small" icon={<ExportOutlined />} disabled={r.quantity <= 0 || r.stockStatus === 'EXPIRED'} onClick={() => setMovement({ mode: 'issue', item: r })}>Issue</Button></Tooltip>
                <Tooltip title="Receive"><Button size="small" icon={<ImportOutlined />} onClick={() => setMovement({ mode: 'receive', item: r })} /></Tooltip>
                {r.stockStatus === 'EXPIRED' && r.quantity > 0 && (
                  <Popconfirm title="Write off this expired batch?" onConfirm={() => writeOff(r)}><Button size="small" danger icon={<StopOutlined />} /></Popconfirm>
                )}
              </Space>
            )}
            columns={[
              { title: 'Item', dataIndex: 'medicineName', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{humanize(r.category)} · batch {r.batchNumber || '—'}</div></div>, sorter: (a, b) => a.medicineName.localeCompare(b.medicineName) },
              col.text('Health centre', 'healthCentreName'),
              { title: 'Quantity', dataIndex: 'quantity', align: 'right', render: (v, r) => <span style={{ fontWeight: 600, color: v <= r.reorderLevel ? '#d03b3b' : undefined }}>{fmtNumber(v)}</span>, sorter: (a, b) => a.quantity - b.quantity },
              col.number('Reorder at', 'reorderLevel'),
              { title: 'Expiry', dataIndex: 'expiryDate', render: (v, r) => <span>{fmtDate(v)} {r.expiringSoon && <Tag color="orange">Soon</Tag>}</span> },
              col.status('Stock status', 'stockStatus'),
            ]} />
        ) },
        { key: 'replenishments', label: 'Replenishment requests', children: (
          <CrudPage endpoint="/replenishments" entityName="Replenishment request" permission="inventory" refreshKey={tick} onChanged={refresh}
            searchKeys={['requestNumber', 'medicineName', 'healthCentreName', 'requestedBy', 'reason']}
            filters={[{ name: 'status', label: 'Status', options: E.requestStatus }, { name: 'priority', label: 'Priority', options: PRIORITIES }, { name: 'source', label: 'Source', options: E.requestSource }]}
            defaults={{ priority: 'MEDIUM', source: 'MANUAL' }}
            allowEdit={false}
            statusOptions={(r) => ({ url: `/replenishments/${r.id}/status`, options: REQUEST_NEXT[r.status] })}
            fields={[
              { name: 'medicineId', label: 'Medicine / supply', type: 'remote', endpoint: '/medicines', labelOf: (m) => `${m.name} ${m.strength || ''}`, required: true },
              { name: 'healthCentreId', label: 'Health centre', type: 'remote', endpoint: '/health-centres', required: true, onPick: (c, f) => f.setFieldValue('healthCentreName', c?.name) },
              { name: 'healthCentreName', label: 'Centre', hidden: true },
              { name: 'requestedQuantity', label: 'Quantity', type: 'number', min: 1, required: true },
              { name: 'priority', label: 'Priority', type: 'select', options: PRIORITIES },
              { name: 'requiredBy', label: 'Required by', type: 'date' }, { name: 'requestedBy', label: 'Requested by' },
              { name: 'reason', label: 'Reason', type: 'textarea', span: 24 },
            ]}
            columns={[col.text('Request', 'requestNumber', { width: 110 }), col.text('Item', 'medicineName'), col.text('Centre', 'healthCentreName'),
              col.number('Qty', 'requestedQuantity', { width: 80 }), col.number('Stock then', 'currentStock'), col.status('Priority', 'priority', { width: 120 }),
              { title: 'Source', dataIndex: 'source', render: (v) => <Tag color={v === 'FIELD_REPORT' ? 'purple' : v === 'AUTO_LOW_STOCK' ? 'cyan' : 'default'}>{humanize(v)}</Tag> },
              col.date('Required by', 'requiredBy'), col.text('Requested by', 'requestedBy'), col.status('Status', 'status')]} />
        ) },
        { key: 'catalogue', label: 'Medicine catalogue', children: (
          <CrudPage endpoint="/medicines" entityName="Medicine" permission="medicines" refreshKey={tick}
            searchKeys={['name', 'genericName', 'code', 'manufacturer']} filters={[{ name: 'category', label: 'Category', options: E.itemCategory }]}
            defaults={{ category: 'MEDICINE', essential: true, defaultReorderLevel: 50 }}
            fields={[{ name: 'name', label: 'Name', required: true }, { name: 'genericName', label: 'Generic name' },
              { name: 'category', label: 'Category', type: 'select', options: E.itemCategory, required: true }, { name: 'form', label: 'Form', type: 'select', options: E.dosageForm },
              { name: 'strength', label: 'Strength' }, { name: 'unit', label: 'Unit of issue' }, { name: 'manufacturer', label: 'Manufacturer' },
              { name: 'defaultReorderLevel', label: 'Default reorder level', type: 'number', min: 0 }, { name: 'essential', label: 'Essential medicines list', type: 'switch' },
              { name: 'description', label: 'Description', type: 'textarea', span: 24 }]}
            columns={[col.text('Code', 'code', { width: 110 }), { title: 'Name', dataIndex: 'name', render: (v, r) => <div><b>{v} {r.strength}</b><div className="muted" style={{ fontSize: 12 }}>{r.genericName}</div></div> },
              col.enumText('Category', 'category'), col.enumText('Form', 'form'), col.text('Unit', 'unit'), col.number('Reorder level', 'defaultReorderLevel'),
              { title: 'EML', dataIndex: 'essential', render: (v) => (v ? <Tag color="green">Essential</Tag> : '—') }]} />
        ) },
        { key: 'transactions', label: 'Issue & receipt log', children: (
          <CrudPage endpoint="/stock-transactions" entityName="Transaction" refreshKey={tick} allowCreate={false} allowEdit={false} allowDelete={false} pageSize={15}
            searchKeys={['medicineName', 'healthCentreName', 'issuedTo', 'reference', 'performedBy']}
            filters={[{ name: 'type', label: 'Type', options: E.transactionType }]}
            columns={[col.dateTime('When', 'transactionDate'), { title: 'Type', dataIndex: 'type', render: (v) => <Tag color={{ RECEIPT: 'green', ISSUE: 'blue', ADJUSTMENT: 'gold', EXPIRED_WRITE_OFF: 'red' }[v]}>{humanize(v)}</Tag> },
              col.text('Item', 'medicineName'), col.text('Centre', 'healthCentreName'),
              { title: 'Qty', dataIndex: 'quantity', align: 'right', render: (v) => <b style={{ color: v < 0 ? '#d03b3b' : '#0ca30c' }}>{v > 0 ? `+${v}` : v}</b> },
              col.number('Balance', 'balanceAfter'), col.text('Issued to', 'issuedTo'), col.text('Reference', 'reference'), col.text('By', 'performedBy')]} />
        ) },
      ]} />
      {movement && <MovementModal mode={movement.mode} item={movement.item} onClose={() => setMovement(null)} onDone={() => { setMovement(null); refresh(); }} />}
    </>
  );
}
