import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Col, Collapse, Empty, Form, Input, InputNumber, Modal, Row, Select, Space, Steps, Table, Tag, Typography, message } from 'antd';
import { DeleteOutlined, FileTextOutlined, PlusOutlined, ReloadOutlined, SendOutlined, SyncOutlined } from '@ant-design/icons';
import api, { errorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusTag from '../components/StatusTag';
import RemoteSelect from '../components/RemoteSelect';
import { E, PRIORITIES, VILLAGES, humanize, opts } from '../utils/constants';
import { fmtDate, fmtDateTime } from '../utils/format';
import { useAuth } from '../auth/AuthContext';

const TYPE_INFO = {
  SANITATION_MAINTENANCE: { label: 'Sanitation facility needs maintenance', target: 'Sanitation service → maintenance task', color: 'orange' },
  SANITATION_COMPLAINT: { label: 'Sanitation complaint', target: 'Sanitation service → complaint', color: 'gold' },
  MEDICINE_SHORTAGE: { label: 'Medicine / supply shortage', target: 'Inventory service → replenishment request', color: 'red' },
  WATER_ISSUE: { label: 'Water / public-health issue', target: 'Water service → public-health issue', color: 'blue' },
};

function ItemFields({ field, form, remove }) {
  const type = Form.useWatch(['items', field.name, 'itemType'], form);
  const set = (key, value) => form.setFieldValue(['items', field.name, key], value);
  return (
    <Card size="small" style={{ marginBottom: 12, background: '#fafbfa' }}
      title={<Space>Issue #{field.name + 1}{type && <Tag color={TYPE_INFO[type].color}>{TYPE_INFO[type].target}</Tag>}</Space>}
      extra={<Button size="small" type="text" danger icon={<DeleteOutlined />} onClick={() => remove(field.name)} />}>
      <Row gutter={12}>
        <Col xs={24} md={12}>
          <Form.Item name={[field.name, 'itemType']} label="Issue type" rules={[{ required: true }]}>
            <Select options={E.reportItemType.map((t) => ({ value: t, label: TYPE_INFO[t].label }))} />
          </Form.Item>
        </Col>
        <Col xs={24} md={12}>
          <Form.Item name={[field.name, 'priority']} label="Priority">
            <Select options={opts(PRIORITIES)} />
          </Form.Item>
        </Col>
        {(type === 'SANITATION_MAINTENANCE' || type === 'SANITATION_COMPLAINT') && (
          <>
            <Col xs={24} md={12}>
              <Form.Item name={[field.name, 'facilityId']} label="Sanitation facility" rules={type === 'SANITATION_MAINTENANCE' ? [{ required: true }] : []}>
                <RemoteSelect endpoint="/sanitation/facilities" labelOf={(f) => `${f.name} (${f.village})`} onPick={(f) => set('facilityName', f?.name)} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name={[field.name, 'category']} label={type === 'SANITATION_MAINTENANCE' ? 'Maintenance type' : 'Complaint category'}>
                <Select allowClear options={opts(type === 'SANITATION_MAINTENANCE' ? E.maintenanceType : E.complaintCategory)} />
              </Form.Item>
            </Col>
          </>
        )}
        {type === 'MEDICINE_SHORTAGE' && (
          <>
            <Col xs={24} md={9}>
              <Form.Item name={[field.name, 'healthCentreId']} label="Health centre" rules={[{ required: true }]}>
                <RemoteSelect endpoint="/health-centres" onPick={(c) => set('healthCentreName', c?.name)} />
              </Form.Item>
            </Col>
            <Col xs={24} md={9}>
              <Form.Item name={[field.name, 'medicineId']} label="Medicine / supply" rules={[{ required: true }]}>
                <RemoteSelect endpoint="/medicines" labelOf={(m) => `${m.name} ${m.strength || ''}`} onPick={(m) => set('medicineName', m?.name)} />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name={[field.name, 'quantity']} label="Qty needed">
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </>
        )}
        {type === 'WATER_ISSUE' && (
          <>
            <Col xs={24} md={12}>
              <Form.Item name={[field.name, 'waterSourceId']} label="Water source (optional)">
                <RemoteSelect endpoint="/water/sources" labelOf={(s) => `${s.name} (${s.village})`} onPick={(s) => set('waterSourceName', s?.name)} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name={[field.name, 'category']} label="Issue category">
                <Select allowClear options={opts(E.issueCategory)} />
              </Form.Item>
            </Col>
          </>
        )}
        <Col span={24}>
          <Form.Item name={[field.name, 'description']} label="Description" rules={[{ required: true }]}>
            <Input.TextArea rows={2} placeholder="What did you observe?" />
          </Form.Item>
        </Col>
        {['facilityName', 'healthCentreName', 'medicineName', 'waterSourceName'].map((k) => (
          <Form.Item key={k} name={[field.name, k]} hidden><Input /></Form.Item>
        ))}
      </Row>
    </Card>
  );
}

export default function FieldReportsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [result, setResult] = useState(null);
  const [form] = Form.useForm();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/field-reports');
      setReports(data);
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const submit = async () => {
    const values = await form.validateFields();
    if (!values.items?.length) {
      message.warning('Add at least one issue to the report');
      return;
    }
    setSaving(true);
    try {
      const { data } = await api.post('/field-reports', values);
      setResult(data);
      setOpen(false);
      load();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  const retry = async (id) => {
    try {
      await api.post(`/field-reports/${id}/retry`);
      message.success('Routing retried');
      load();
    } catch (e) {
      message.error(errorMessage(e));
    }
  };

  const itemColumns = [
    { title: 'Issue', dataIndex: 'itemType', render: (v) => <Tag color={TYPE_INFO[v].color}>{TYPE_INFO[v].label}</Tag> },
    { title: 'Details', dataIndex: 'description', render: (v, r) => (
      <div style={{ maxWidth: 360 }}>
        <div>{v}</div>
        <div className="muted" style={{ fontSize: 12 }}>{[r.facilityName, r.healthCentreName, r.medicineName && `${r.medicineName} × ${r.quantity}`, r.waterSourceName].filter(Boolean).join(' · ')}</div>
      </div>
    ) },
    { title: 'Priority', dataIndex: 'priority', render: (v) => <StatusTag value={v} /> },
    { title: 'Routed to', dataIndex: 'targetService', render: (v, r) => (
      r.routingStatus === 'ROUTED'
        ? <Space direction="vertical" size={0}><span>{v}</span><Button type="link" size="small" style={{ padding: 0 }} onClick={() => navigate(r.targetPath)}>{r.targetReference}</Button></Space>
        : <Typography.Text type="danger" style={{ fontSize: 12 }}>{r.routingMessage}</Typography.Text>
    ) },
    { title: 'Routing', dataIndex: 'routingStatus', render: (v) => <StatusTag value={v} /> },
    { title: 'Live status', dataIndex: 'targetStatus', render: (v, r) => (r.routingStatus === 'ROUTED' ? <StatusTag value={v} /> : '—') },
  ];

  return (
    <>
      <PageHeader icon={<FileTextOutlined />} title="Field Reports" subtitle="Report sanitation, medicine and water issues from the field — each issue is routed automatically to the responsible service"
        extra={<Space><Button icon={<ReloadOutlined />} onClick={load} loading={loading}>Refresh</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>New field report</Button></Space>} />

      <Card className="crud-card" style={{ marginBottom: 16, height: 'auto' }}>
        <Steps size="small" responsive items={[
          { title: 'Report', description: 'Health worker records one or more issues', status: 'finish' },
          { title: 'Route', description: 'Each issue goes to its owning microservice', status: 'finish' },
          { title: 'Act', description: 'Teams update status in their module', status: 'finish' },
          { title: 'Monitor', description: 'Officer tracks everything in Pending Actions', status: 'finish' },
        ]} />
      </Card>

      {reports.length === 0 && !loading ? (
        <Card className="crud-card"><Empty description="No field reports yet" /></Card>
      ) : (
        <Collapse
          key={reports.length}
          defaultActiveKey={reports.slice(0, 2).map((r) => r.id)}
          items={reports.map((r) => {
            const failed = r.items.filter((i) => i.routingStatus === 'FAILED').length;
            const done = r.items.filter((i) => ['COMPLETED', 'RESOLVED', 'FULFILLED', 'CLOSED'].includes(i.targetStatus)).length;
            return {
              key: r.id,
              label: (
                <Space wrap>
                  <b>{r.reportNumber}</b>
                  <span>{r.village}</span>
                  <span className="muted">· {r.reportedBy} · {fmtDate(r.reportDate)}</span>
                  <Tag>{r.items.length} issue{r.items.length > 1 ? 's' : ''}</Tag>
                  <Tag color={done === r.items.length ? 'success' : 'processing'}>{done}/{r.items.length} closed</Tag>
                  {failed > 0 && <Tag color="error">{failed} routing failed</Tag>}
                </Space>
              ),
              extra: failed > 0 ? <Button size="small" icon={<SyncOutlined />} onClick={(e) => { e.stopPropagation(); retry(r.id); }}>Retry</Button> : null,
              children: (
                <>
                  {r.summary && <Typography.Paragraph type="secondary">{r.summary}</Typography.Paragraph>}
                  <Table size="small" rowKey="id" pagination={false} columns={itemColumns} dataSource={r.items} scroll={{ x: 'max-content' }} />
                  <div className="muted" style={{ fontSize: 12, marginTop: 8 }}>Submitted {fmtDateTime(r.createdAt)} · statuses synced live from owning services</div>
                </>
              ),
            };
          })}
        />
      )}

      <Modal open={open} width={820} title="New field report" okText="Submit & route" okButtonProps={{ icon: <SendOutlined /> }}
        confirmLoading={saving} onOk={submit} onCancel={() => setOpen(false)} destroyOnClose maskClosable={false}>
        <Form form={form} layout="vertical"
          initialValues={{ village: user?.assignedVillage || undefined, reportedBy: user?.fullName, items: [{ itemType: 'SANITATION_MAINTENANCE', priority: 'HIGH' }, { itemType: 'MEDICINE_SHORTAGE', priority: 'HIGH', quantity: 100 }] }}>
          <Row gutter={12}>
            <Col xs={24} md={12}>
              <Form.Item name="village" label="Village" rules={[{ required: true }]}>
                <Select showSearch options={VILLAGES.map((v) => ({ value: v, label: v }))} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="reportedBy" label="Reported by"><Input /></Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item name="summary" label="Visit summary"><Input.TextArea rows={2} placeholder="Optional notes about the field visit" /></Form.Item>
            </Col>
          </Row>
          <Form.List name="items">
            {(fields, { add, remove }) => (
              <>
                {fields.map((f) => <ItemFields key={f.key} field={f} form={form} remove={remove} />)}
                <Button block type="dashed" icon={<PlusOutlined />} onClick={() => add({ priority: 'MEDIUM' })}>Add another issue</Button>
              </>
            )}
          </Form.List>
        </Form>
      </Modal>

      <Modal open={!!result} title={`Report ${result?.reportNumber} submitted`} footer={<Button type="primary" onClick={() => setResult(null)}>Done</Button>} onCancel={() => setResult(null)} width={720}>
        {result && (
          <>
            <Alert type={result.items.some((i) => i.routingStatus === 'FAILED') ? 'warning' : 'success'} showIcon style={{ marginBottom: 12 }}
              message={`${result.items.filter((i) => i.routingStatus === 'ROUTED').length} of ${result.items.length} issues routed to the responsible services`} />
            <Table size="small" rowKey="id" pagination={false} columns={itemColumns.filter((c) => c.dataIndex !== 'description')} dataSource={result.items} />
          </>
        )}
      </Modal>
    </>
  );
}
