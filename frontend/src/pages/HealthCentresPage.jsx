import { useEffect, useState } from 'react';
import { Col, Descriptions, Row, Select, Tabs, Tag } from 'antd';
import { HomeOutlined } from '@ant-design/icons';
import api from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import StatusTag from '../components/StatusTag';
import { E, VILLAGES, humanize } from '../utils/constants';

const centreRef = { name: 'centreId', label: 'Health centre', type: 'remote', endpoint: '/health-centres', required: true };

const centreFields = [
  { name: 'name', label: 'Centre name', required: true },
  { name: 'code', label: 'Code', help: 'Auto-generated if left blank' },
  { name: 'type', label: 'Type', type: 'select', options: E.centreType, required: true },
  { name: 'status', label: 'Status', type: 'select', options: E.centreStatus },
  { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true },
  { name: 'block', label: 'Block' },
  { name: 'district', label: 'District' },
  { name: 'address', label: 'Address' },
  { name: 'phone', label: 'Phone' },
  { name: 'email', label: 'Email' },
  { name: 'inChargeName', label: 'In-charge' },
  { name: 'bedCapacity', label: 'Bed capacity', type: 'number', min: 0, span: 6 },
  { name: 'populationCovered', label: 'Population covered', type: 'number', min: 0, span: 6 },
  { name: 'latitude', label: 'Latitude', type: 'number', step: 0.0001, span: 6 },
  { name: 'longitude', label: 'Longitude', type: 'number', step: 0.0001, span: 6 },
];

function CentreFilter({ value, onChange, centres }) {
  return <Select allowClear placeholder="All centres" style={{ width: 220 }} value={value} onChange={onChange} options={centres.map((c) => ({ value: c.id, label: c.name }))} />;
}

export default function HealthCentresPage() {
  const [stats, setStats] = useState({});
  const [centres, setCentres] = useState([]);
  const [centreId, setCentreId] = useState();
  const [tick, setTick] = useState(0);
  const params = centreId ? { centreId } : undefined;

  useEffect(() => {
    api.get('/health-centres/stats').then(({ data }) => setStats(data)).catch(() => {});
    api.get('/health-centres').then(({ data }) => setCentres(data)).catch(() => {});
  }, [tick]);

  const refresh = () => setTick((t) => t + 1);
  const filter = <CentreFilter value={centreId} onChange={setCentreId} centres={centres} />;

  return (
    <>
      <PageHeader icon={<HomeOutlined />} title="Health Centres" subtitle="Rural health centres, facilities, healthcare workers, services and operating schedules" />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={6}><StatCard title="Health centres" value={stats.totalCentres} footer={Object.entries(stats.centresByType || {}).map(([k, v]) => `${v} ${humanize(k)}`).join(' · ')} /></Col>
        <Col xs={12} md={6}><StatCard title="Healthcare workers" value={stats.totalWorkers} color="#2a78d6" footer={`${stats.activeWorkers ?? 0} active · ${stats.workersOnLeave ?? 0} on leave`} /></Col>
        <Col xs={12} md={6}><StatCard title="Facilities" value={stats.totalFacilities} color="#1baf7a" footer={`${(stats.facilitiesByStatus?.UNDER_MAINTENANCE ?? 0) + (stats.facilitiesByStatus?.UNAVAILABLE ?? 0)} need attention`} /></Col>
        <Col xs={12} md={6}><StatCard title="Services offered" value={stats.totalServices} color="#6250d6" footer="Across all centres" /></Col>
      </Row>
      <Tabs className="module-tabs" items={[
        { key: 'centres', label: 'Centres', children: (
          <CrudPage endpoint="/health-centres" entityName="Health centre" permission="centres" fields={centreFields} onChanged={refresh}
            searchKeys={['name', 'code', 'village', 'inChargeName']} filters={[{ name: 'type', label: 'Type', options: E.centreType }, { name: 'status', label: 'Status', options: E.centreStatus }]}
            defaults={{ status: 'OPERATIONAL', block: 'Rampur Block', district: 'Anandpur' }}
            statusOptions={(r) => ({ url: `/health-centres/${r.id}/status`, options: E.centreStatus.filter((s) => s !== r.status) })}
            detail={(c) => (
              <Descriptions column={1} bordered size="small">
                {['code', 'name', 'type', 'village', 'block', 'district', 'address', 'phone', 'email', 'inChargeName', 'bedCapacity', 'populationCovered'].map((k) => (
                  <Descriptions.Item key={k} label={humanize(k.replace(/([A-Z])/g, '_$1'))}>{k === 'type' ? humanize(c[k]) : c[k] ?? '—'}</Descriptions.Item>
                ))}
                <Descriptions.Item label="Status"><StatusTag value={c.status} /></Descriptions.Item>
                <Descriptions.Item label="Location">{c.latitude}, {c.longitude}</Descriptions.Item>
              </Descriptions>
            )}
            columns={[col.text('Code', 'code', { width: 100 }), { title: 'Centre', dataIndex: 'name', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{humanize(r.type)}</div></div> },
              col.text('Village', 'village'), col.text('In-charge', 'inChargeName'), col.number('Beds', 'bedCapacity', { width: 80 }), col.number('Population', 'populationCovered'), col.text('Phone', 'phone'), col.status('Status', 'status')]} />
        ) },
        { key: 'workers', label: 'Healthcare workers', children: (
          <CrudPage endpoint="/health-workers" entityName="Health worker" permission="centres" params={params} extra={filter} refreshKey={tick}
            searchKeys={['name', 'specialization', 'phone', 'centreName']} filters={[{ name: 'role', label: 'Role', options: E.workerRole }, { name: 'status', label: 'Status', options: E.workerStatus }]}
            defaults={{ status: 'ACTIVE' }}
            statusOptions={(r) => ({ url: `/health-workers/${r.id}/status`, options: E.workerStatus.filter((s) => s !== r.status) })}
            fields={[centreRef, { name: 'name', label: 'Full name', required: true }, { name: 'role', label: 'Role', type: 'select', options: E.workerRole, required: true },
              { name: 'specialization', label: 'Specialization' }, { name: 'qualification', label: 'Qualification' }, { name: 'registrationNumber', label: 'Registration no.' },
              { name: 'phone', label: 'Phone' }, { name: 'email', label: 'Email' }, { name: 'joiningDate', label: 'Joining date', type: 'date' },
              { name: 'villagesCovered', label: 'Villages covered' }, { name: 'status', label: 'Status', type: 'select', options: E.workerStatus }]}
            columns={[{ title: 'Name', dataIndex: 'name', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{r.qualification}</div></div> },
              { title: 'Role', dataIndex: 'role', render: (v) => <Tag color="geekblue">{humanize(v)}</Tag> }, col.text('Specialization', 'specialization'),
              col.text('Centre', 'centreName'), col.text('Phone', 'phone'), col.status('Status', 'status')]} />
        ) },
        { key: 'facilities', label: 'Facilities', children: (
          <CrudPage endpoint="/facilities" entityName="Facility" permission="centres" params={params} extra={filter} refreshKey={tick} onChanged={refresh}
            searchKeys={['name', 'centreName', 'notes']} filters={[{ name: 'category', label: 'Category', options: E.facilityCategory }, { name: 'status', label: 'Status', options: E.facilityStatus }]}
            defaults={{ status: 'AVAILABLE', quantity: 1 }}
            statusOptions={(r) => ({ url: `/facilities/${r.id}/status`, options: E.facilityStatus.filter((s) => s !== r.status) })}
            fields={[centreRef, { name: 'name', label: 'Facility name', required: true }, { name: 'category', label: 'Category', type: 'select', options: E.facilityCategory, required: true },
              { name: 'quantity', label: 'Quantity', type: 'number', min: 0 }, { name: 'status', label: 'Status', type: 'select', options: E.facilityStatus }, { name: 'notes', label: 'Notes', type: 'textarea', span: 24 }]}
            columns={[col.text('Facility', 'name'), col.enumText('Category', 'category'), col.text('Centre', 'centreName'), col.number('Qty', 'quantity', { width: 70 }), col.status('Status', 'status'), col.text('Notes', 'notes')]} />
        ) },
        { key: 'services', label: 'Services', children: (
          <CrudPage endpoint="/centre-services" entityName="Service" permission="centres" params={params} extra={filter} refreshKey={tick}
            searchKeys={['name', 'centreName']} filters={[{ name: 'category', label: 'Category', options: E.serviceCategory }]}
            defaults={{ available: true, fee: 0 }}
            fields={[centreRef, { name: 'name', label: 'Service name', required: true }, { name: 'category', label: 'Category', type: 'select', options: E.serviceCategory, required: true },
              { name: 'availableDays', label: 'Available days', placeholder: 'e.g. Mon - Sat' }, { name: 'fee', label: 'Fee', type: 'number', unit: '₹', min: 0 },
              { name: 'available', label: 'Currently available', type: 'switch' }, { name: 'description', label: 'Description', type: 'textarea', span: 24 }]}
            columns={[col.text('Service', 'name'), col.enumText('Category', 'category'), col.text('Centre', 'centreName'), col.text('Days', 'availableDays'),
              { title: 'Fee', dataIndex: 'fee', render: (v) => (v ? `₹${v}` : <Tag color="green">Free</Tag>) }, { title: 'Available', dataIndex: 'available', render: (v) => <StatusTag value={v ? 'AVAILABLE' : 'UNAVAILABLE'} /> }]} />
        ) },
        { key: 'schedules', label: 'Operating schedules', children: (
          <CrudPage endpoint="/schedules" entityName="Schedule" permission="centres" params={params} extra={filter} refreshKey={tick} pageSize={14}
            searchKeys={['centreName', 'workerName', 'notes']} filters={[{ name: 'dayOfWeek', label: 'Day', options: E.dayOfWeek }]}
            defaults={{ closed: false }}
            fields={[centreRef, { name: 'dayOfWeek', label: 'Day', type: 'select', options: E.dayOfWeek, required: true },
              { name: 'openTime', label: 'Opens', type: 'time' }, { name: 'closeTime', label: 'Closes', type: 'time' },
              { name: 'workerId', label: 'Duty roster: worker (optional)', type: 'remote', endpoint: '/health-workers', labelOf: (w) => `${w.name} (${humanize(w.role)})` },
              { name: 'closed', label: 'Closed on this day', type: 'switch' }, { name: 'notes', label: 'Notes', span: 24 }]}
            columns={[col.text('Centre', 'centreName'), col.enumText('Day', 'dayOfWeek'), col.time('Opens', 'openTime'), col.time('Closes', 'closeTime'),
              { title: 'Open?', dataIndex: 'closed', render: (v) => <StatusTag value={v ? 'CLOSED' : 'OPERATIONAL'} tone={v ? 'neutral' : 'good'} /> },
              { title: 'Duty worker', dataIndex: 'workerName', render: (v) => v || <span className="muted">Centre hours</span> }, col.text('Notes', 'notes')]} />
        ) },
      ]} />
    </>
  );
}
