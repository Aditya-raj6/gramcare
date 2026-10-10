import { useEffect, useState } from 'react';
import { Button, Col, Form, Input, Modal, Rate, Row, Select, Tabs, Tag, Tooltip, message } from 'antd';
import { ExperimentOutlined, ToolOutlined } from '@ant-design/icons';
import api, { errorMessage } from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import { E, PRIORITIES, VILLAGES, humanize, opts } from '../utils/constants';
import { fmtDate, fmtMoney, yesNo } from '../utils/format';

const TASK_NEXT = { PENDING: ['ASSIGNED', 'IN_PROGRESS', 'CANCELLED'], ASSIGNED: ['IN_PROGRESS', 'PENDING', 'CANCELLED'], IN_PROGRESS: ['COMPLETED', 'CANCELLED'], CANCELLED: ['PENDING'], COMPLETED: [] };
const facilityRef = (required = true) => ({ name: 'facilityId', label: 'Facility', type: 'remote', endpoint: '/sanitation/facilities', labelOf: (f) => `${f.name} (${f.village})`, required });

function ConvertModal({ complaint, onClose, onDone }) {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const submit = async () => {
    const v = await form.validateFields();
    setSaving(true);
    try {
      const { data } = await api.post(`/sanitation/complaints/${complaint.id}/convert-to-task`, v);
      message.success(`Work order ${data.taskNumber} created`);
      onDone();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setSaving(false);
    }
  };
  return (
    <Modal open title={`Create work order from ${complaint.complaintNumber}`} okText="Create maintenance task" confirmLoading={saving} onOk={submit} onCancel={onClose} destroyOnClose>
      <p className="muted">{humanize(complaint.category)} · {complaint.facilityName}<br />{complaint.description}</p>
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item name="maintenanceType" label="Maintenance type" tooltip="Defaults from complaint category if left empty"><Select allowClear options={opts(E.maintenanceType)} /></Form.Item>
        <Form.Item name="assignedTo" label="Assign to"><Input placeholder="Team or person" /></Form.Item>
      </Form>
    </Modal>
  );
}

export default function SanitationPage() {
  const [stats, setStats] = useState({});
  const [converting, setConverting] = useState(null);
  const [tick, setTick] = useState(0);
  const refresh = () => setTick((t) => t + 1);

  useEffect(() => {
    api.get('/sanitation/stats').then(({ data }) => setStats(data)).catch(() => {});
  }, [tick]);

  return (
    <>
      <PageHeader icon={<ExperimentOutlined />} title="Sanitation Facility Management" subtitle="Public sanitation facilities, cleanliness inspections, maintenance activities and complaints" />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={8} xl={4}><StatCard title="Facilities" value={stats.totalFacilities} footer={`${stats.operational ?? 0} operational`} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Need attention" value={stats.needsAttention} color="#d03b3b" footer="Dirty, unusable or non-functional" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Active maintenance" value={stats.activeMaintenance} color="#eb6834" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Open complaints" value={stats.openComplaints} color="#eda100" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Inspections" value={stats.totalInspections} color="#2a78d6" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Clean facilities" value={stats.facilitiesByCleanliness?.CLEAN ?? 0} color="#0ca30c" /></Col>
      </Row>
      <Tabs className="module-tabs" items={[
        { key: 'facilities', label: 'Facilities', children: (
          <CrudPage endpoint="/sanitation/facilities" entityName="Facility" permission="sanitation" refreshKey={tick} onChanged={refresh} formWidth={860}
            searchKeys={['name', 'facilityCode', 'village', 'landmark', 'caretakerName']}
            filters={[{ name: 'village', label: 'Village', options: VILLAGES.map((v) => ({ value: v, label: v })) }, { name: 'type', label: 'Type', options: E.sanitationType },
              { name: 'cleanlinessStatus', label: 'Cleanliness', options: E.cleanliness }]}
            defaults={{ status: 'OPERATIONAL', cleanlinessStatus: 'SATISFACTORY', waterAvailable: true, electricityAvailable: true, accessibleForDisabled: false, genderSeparated: false }}
            statusOptions={(r) => ({ url: `/sanitation/facilities/${r.id}/status`, options: E.operationalStatus.filter((s) => s !== r.status) })}
            fields={[{ name: 'name', label: 'Facility name', required: true, span: 24 }, { name: 'type', label: 'Type', type: 'select', options: E.sanitationType, required: true },
              { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true }, { name: 'ward', label: 'Ward' }, { name: 'landmark', label: 'Landmark' },
              { name: 'capacity', label: 'Capacity (seats)', type: 'number', min: 0 }, { name: 'managedBy', label: 'Managed by' },
              { name: 'cleanlinessStatus', label: 'Cleanliness', type: 'select', options: E.cleanliness }, { name: 'status', label: 'Operational status', type: 'select', options: E.operationalStatus },
              { name: 'caretakerName', label: 'Caretaker' }, { name: 'caretakerPhone', label: 'Caretaker phone' },
              { name: 'latitude', label: 'Latitude', type: 'number', step: 0.0001 }, { name: 'longitude', label: 'Longitude', type: 'number', step: 0.0001 },
              { name: 'waterAvailable', label: 'Water available', type: 'switch', span: 6 }, { name: 'electricityAvailable', label: 'Electricity', type: 'switch', span: 6 },
              { name: 'accessibleForDisabled', label: 'Disabled-accessible', type: 'switch', span: 6 }, { name: 'genderSeparated', label: 'Gender separated', type: 'switch', span: 6 }]}
            columns={[col.text('Code', 'facilityCode', { width: 100 }), { title: 'Facility', dataIndex: 'name', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{humanize(r.type)} · {r.ward}</div></div> },
              col.text('Village', 'village'), col.status('Cleanliness', 'cleanlinessStatus', { width: 130 }), col.status('Status', 'status', { width: 170 }),
              { title: 'Water', dataIndex: 'waterAvailable', render: yesNo, width: 70 }, col.date('Last inspected', 'lastInspectionDate'), col.text('Caretaker', 'caretakerName')]} />
        ) },
        { key: 'inspections', label: 'Cleanliness inspections', children: (
          <CrudPage endpoint="/sanitation/inspections" entityName="Inspection" permission="sanitation" refreshKey={tick} onChanged={refresh} allowEdit={false}
            searchKeys={['facilityName', 'village', 'inspectorName', 'remarks']} filters={[{ name: 'cleanlinessStatus', label: 'Result', options: E.cleanliness }]}
            defaults={{ rating: 3, cleanlinessStatus: 'SATISFACTORY', waterAvailable: true, lightingWorking: true, soapAvailable: true, odourFree: true }}
            fields={[facilityRef(), { name: 'inspectionDate', label: 'Inspection date', type: 'date' }, { name: 'inspectorName', label: 'Inspector' },
              { name: 'rating', label: 'Rating (1-5)', type: 'number', min: 1, max: 5, required: true, help: 'Ratings ≤ 2 automatically raise a cleaning task' },
              { name: 'cleanlinessStatus', label: 'Cleanliness', type: 'select', options: E.cleanliness, required: true },
              { name: 'waterAvailable', label: 'Water', type: 'switch', span: 6 }, { name: 'lightingWorking', label: 'Lighting', type: 'switch', span: 6 },
              { name: 'soapAvailable', label: 'Soap', type: 'switch', span: 6 }, { name: 'odourFree', label: 'Odour free', type: 'switch', span: 6 },
              { name: 'remarks', label: 'Remarks', type: 'textarea', span: 24 }]}
            columns={[col.date('Date', 'inspectionDate'), col.text('Facility', 'facilityName'), col.text('Village', 'village'),
              { title: 'Rating', dataIndex: 'rating', render: (v) => <Rate disabled value={v} style={{ fontSize: 13 }} /> }, col.status('Cleanliness', 'cleanlinessStatus', { width: 130 }),
              col.text('Inspector', 'inspectorName'), { title: 'Auto task', dataIndex: 'maintenanceTaskId', render: (v) => (v ? <Tag color="orange">Task #{v}</Tag> : '—') }, col.text('Remarks', 'remarks')]} />
        ) },
        { key: 'maintenance', label: 'Maintenance', children: (
          <CrudPage endpoint="/sanitation/maintenance" entityName="Maintenance task" permission="sanitation" refreshKey={tick} onChanged={refresh}
            searchKeys={['taskNumber', 'facilityName', 'village', 'assignedTo', 'description']}
            filters={[{ name: 'status', label: 'Status', options: E.taskStatus }, { name: 'priority', label: 'Priority', options: PRIORITIES }, { name: 'source', label: 'Source', options: E.taskSource }]}
            defaults={{ priority: 'MEDIUM', maintenanceType: 'CLEANING', source: 'MANUAL' }}
            statusOptions={(r) => ({ url: `/sanitation/maintenance/${r.id}/status`, options: TASK_NEXT[r.status] })}
            fields={[facilityRef(), { name: 'maintenanceType', label: 'Type', type: 'select', options: E.maintenanceType, required: true },
              { name: 'priority', label: 'Priority', type: 'select', options: PRIORITIES }, { name: 'scheduledDate', label: 'Scheduled date', type: 'date' },
              { name: 'assignedTo', label: 'Assigned to' }, { name: 'reportedBy', label: 'Reported by' },
              { name: 'estimatedCost', label: 'Estimated cost', type: 'number', unit: '₹', min: 0 }, { name: 'actualCost', label: 'Actual cost', type: 'number', unit: '₹', min: 0 },
              { name: 'description', label: 'Work description', type: 'textarea', span: 24, required: true }]}
            columns={[col.text('Task', 'taskNumber', { width: 110 }), { title: 'Work', dataIndex: 'maintenanceType', render: (v, r) => <div><b>{humanize(v)}</b><div className="muted" style={{ fontSize: 12, maxWidth: 300, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.description}</div></div> },
              col.text('Facility', 'facilityName'), col.status('Priority', 'priority', { width: 120 }),
              { title: 'Source', dataIndex: 'source', render: (v) => <Tag color={v === 'FIELD_REPORT' ? 'purple' : undefined}>{humanize(v)}</Tag> },
              col.text('Assigned to', 'assignedTo'), col.date('Scheduled', 'scheduledDate'), { title: 'Cost', dataIndex: 'estimatedCost', render: (v, r) => fmtMoney(r.actualCost ?? v) }, col.status('Status', 'status')]} />
        ) },
        { key: 'complaints', label: 'Complaints', children: (
          <CrudPage endpoint="/sanitation/complaints" entityName="Complaint" permission="sanitation" refreshKey={tick} onChanged={refresh}
            searchKeys={['complaintNumber', 'facilityName', 'village', 'complainantName', 'description']}
            filters={[{ name: 'status', label: 'Status', options: E.complaintStatus }, { name: 'category', label: 'Category', options: E.complaintCategory }]}
            defaults={{ priority: 'MEDIUM', channel: 'CITIZEN' }}
            statusOptions={(r) => ({ url: `/sanitation/complaints/${r.id}/status`, options: E.complaintStatus.filter((s) => s !== r.status) })}
            rowActions={(r, reload, writable) => writable && !r.maintenanceTaskId && r.facilityId && ['OPEN', 'IN_PROGRESS'].includes(r.status) && (
              <Tooltip title="Create maintenance work order"><Button size="small" icon={<ToolOutlined />} onClick={() => setConverting(r)}>Work order</Button></Tooltip>
            )}
            fields={[facilityRef(false), { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true },
              { name: 'category', label: 'Category', type: 'select', options: E.complaintCategory, required: true }, { name: 'priority', label: 'Priority', type: 'select', options: PRIORITIES },
              { name: 'complainantName', label: 'Complainant' }, { name: 'complainantPhone', label: 'Phone' },
              { name: 'channel', label: 'Channel', type: 'select', options: ['CITIZEN', 'HEALTH_WORKER', 'HELPLINE', 'FIELD_REPORT'] },
              { name: 'description', label: 'Description', type: 'textarea', span: 24, required: true }]}
            columns={[col.text('No.', 'complaintNumber', { width: 110 }), col.enumText('Category', 'category'), col.text('Description', 'description'), col.text('Facility', 'facilityName'),
              col.text('Village', 'village'), col.status('Priority', 'priority', { width: 120 }), col.date('Reported', 'reportedDate'),
              { title: 'Work order', dataIndex: 'maintenanceTaskId', render: (v) => (v ? <Tag color="orange">Task #{v}</Tag> : '—') }, col.status('Status', 'status')]} />
        ) },
      ]} />
      {converting && <ConvertModal complaint={converting} onClose={() => setConverting(null)} onDone={() => { setConverting(null); refresh(); }} />}
    </>
  );
}
