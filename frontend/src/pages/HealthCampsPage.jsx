import { useEffect, useState } from 'react';
import { Col, Descriptions, Row, Select, Statistic, Tabs, Tag } from 'antd';
import { SafetyCertificateOutlined } from '@ant-design/icons';
import api from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import StatusTag from '../components/StatusTag';
import { E, PRIORITIES, VILLAGES, humanize } from '../utils/constants';
import { fmtDate, fmtMoney } from '../utils/format';

const CAMP_NEXT = { PLANNED: ['ONGOING', 'CANCELLED'], ONGOING: ['COMPLETED', 'CANCELLED'], CANCELLED: ['PLANNED'], COMPLETED: [] };
const FU_NEXT = { PENDING: ['IN_PROGRESS', 'COMPLETED', 'MISSED'], IN_PROGRESS: ['COMPLETED', 'MISSED'], MISSED: ['PENDING'], COMPLETED: [] };

const campRef = { name: 'campId', label: 'Health camp', type: 'remote', endpoint: '/health-camps', labelOf: (c) => `${c.name} (${fmtDate(c.startDate)})`, required: true };
const beneficiaryRef = (campId) => ({ name: 'beneficiaryId', label: 'Beneficiary', type: 'remote', endpoint: '/beneficiaries', params: campId ? { campId } : undefined,
  labelOf: (b) => `${b.name} · ${b.age ?? '?'} yrs · ${b.campName}`, required: true, span: 24 });

function CampSummary({ camp }) {
  const [s, setS] = useState(null);
  useEffect(() => {
    api.get(`/health-camps/${camp.id}/summary`).then(({ data }) => setS(data)).catch(() => {});
  }, [camp.id]);
  return (
    <>
      <Row gutter={[12, 12]} style={{ marginBottom: 16 }}>
        {[['Staff', 'staff'], ['Beneficiaries', 'beneficiaries'], ['Screenings', 'screenings'], ['Abnormal', 'abnormalScreenings'], ['Open follow-ups', 'openFollowUps']].map(([l, k]) => (
          <Col key={k} xs={12} md={8}><div className="stat-card" style={{ padding: 12, background: '#fff' }}><Statistic title={l} value={s?.[k] ?? '—'} /></div></Col>
        ))}
        <Col xs={12} md={8}><div className="stat-card" style={{ padding: 12, background: '#fff' }}><Statistic title="Target reach" value={s && camp.targetBeneficiaries ? Math.round((s.beneficiaries / camp.targetBeneficiaries) * 100) : '—'} suffix="%" /></div></Col>
      </Row>
      <Descriptions bordered size="small" column={1}>
        <Descriptions.Item label="Camp">{camp.campCode} · {camp.name}</Descriptions.Item>
        <Descriptions.Item label="Type">{humanize(camp.campType)}</Descriptions.Item>
        <Descriptions.Item label="Where">{camp.venue}, {camp.village}</Descriptions.Item>
        <Descriptions.Item label="When">{fmtDate(camp.startDate)} – {fmtDate(camp.endDate)}</Descriptions.Item>
        <Descriptions.Item label="Organizer">{camp.organizer} {camp.healthCentreName && `· ${camp.healthCentreName}`}</Descriptions.Item>
        <Descriptions.Item label="Budget">{fmtMoney(camp.budget)}</Descriptions.Item>
        <Descriptions.Item label="Status"><StatusTag value={camp.status} /></Descriptions.Item>
        <Descriptions.Item label="Description">{camp.description || '—'}</Descriptions.Item>
        <Descriptions.Item label="Outcome">{camp.outcomeSummary || '—'}</Descriptions.Item>
      </Descriptions>
    </>
  );
}

export default function HealthCampsPage() {
  const [stats, setStats] = useState({});
  const [camps, setCamps] = useState([]);
  const [campId, setCampId] = useState();
  const [tick, setTick] = useState(0);
  const refresh = () => setTick((t) => t + 1);
  const params = campId ? { campId } : undefined;

  useEffect(() => {
    api.get('/health-camps/stats').then(({ data }) => setStats(data)).catch(() => {});
    api.get('/health-camps').then(({ data }) => setCamps(data)).catch(() => {});
  }, [tick]);

  const filter = <Select allowClear placeholder="All camps" style={{ width: 240 }} value={campId} onChange={setCampId} options={camps.map((c) => ({ value: c.id, label: c.name }))} />;

  return (
    <>
      <PageHeader icon={<SafetyCertificateOutlined />} title="Health Camps" subtitle="Plan camps, assign medical staff, register beneficiaries, record screenings and track follow-ups" />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={8} xl={4}><StatCard title="Total camps" value={stats.totalCamps} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Upcoming (30 days)" value={stats.upcomingCamps} color="#2a78d6" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Beneficiaries" value={stats.totalBeneficiaries} color="#1baf7a" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Screenings" value={stats.totalScreenings} color="#6250d6" footer={`${(stats.screeningOutcomes?.ABNORMAL ?? 0) + (stats.screeningOutcomes?.CRITICAL ?? 0)} abnormal`} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Open follow-ups" value={stats.openFollowUps} color="#eda100" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Overdue follow-ups" value={stats.overdueFollowUps} color="#d03b3b" /></Col>
      </Row>
      <Tabs className="module-tabs" items={[
        { key: 'camps', label: 'Camps', children: (
          <CrudPage endpoint="/health-camps" entityName="Health camp" permission="clinical" refreshKey={tick} onChanged={refresh}
            searchKeys={['name', 'campCode', 'village', 'venue']} filters={[{ name: 'status', label: 'Status', options: E.campStatus }, { name: 'campType', label: 'Type', options: E.campType }]}
            defaults={{ campType: 'GENERAL_CHECKUP', organizer: 'Block Health Office, Rampur' }}
            statusOptions={(r) => ({ url: `/health-camps/${r.id}/status`, options: CAMP_NEXT[r.status] })}
            detail={(c) => <CampSummary camp={c} />}
            fields={[{ name: 'name', label: 'Camp name', required: true, span: 24 }, { name: 'campType', label: 'Type', type: 'select', options: E.campType, required: true },
              { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true }, { name: 'venue', label: 'Venue' },
              { name: 'healthCentreId', label: 'Supporting health centre', type: 'remote', endpoint: '/health-centres', onPick: (c, f) => f.setFieldValue('healthCentreName', c?.name) },
              { name: 'healthCentreName', label: 'Centre', hidden: true },
              { name: 'startDate', label: 'Start date', type: 'date', required: true }, { name: 'endDate', label: 'End date', type: 'date' },
              { name: 'organizer', label: 'Organizer' }, { name: 'targetBeneficiaries', label: 'Target beneficiaries', type: 'number', min: 0 },
              { name: 'budget', label: 'Budget', type: 'number', unit: '₹', min: 0 }, { name: 'description', label: 'Description', type: 'textarea', span: 24 }]}
            columns={[col.text('Code', 'campCode', { width: 110 }), { title: 'Camp', dataIndex: 'name', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{humanize(r.campType)}</div></div> },
              col.text('Village', 'village'), col.text('Venue', 'venue'), col.date('Start', 'startDate'), col.date('End', 'endDate'), col.number('Target', 'targetBeneficiaries'), col.status('Status', 'status')]} />
        ) },
        { key: 'staff', label: 'Medical staff', children: (
          <CrudPage endpoint="/camp-staff" entityName="Staff assignment" permission="clinical" params={params} extra={filter} refreshKey={tick}
            searchKeys={['name', 'role', 'assignment', 'campName']} defaults={{ volunteer: false }}
            fields={[campRef, { name: 'workerId', label: 'Link health worker (optional)', type: 'remote', endpoint: '/health-workers', labelOf: (w) => `${w.name} (${humanize(w.role)})`,
              onPick: (w, f) => w && f.setFieldsValue({ name: w.name, role: humanize(w.role), phone: w.phone }) },
              { name: 'name', label: 'Name', required: true }, { name: 'role', label: 'Role', required: true }, { name: 'phone', label: 'Phone' },
              { name: 'assignment', label: 'Station / duty' }, { name: 'volunteer', label: 'Volunteer', type: 'switch' }]}
            columns={[col.text('Name', 'name'), { title: 'Role', dataIndex: 'role', render: (v, r) => <span>{v} {r.volunteer && <Tag>Volunteer</Tag>}</span> }, col.text('Assignment', 'assignment'), col.text('Camp', 'campName'), col.text('Phone', 'phone')]} />
        ) },
        { key: 'beneficiaries', label: 'Beneficiaries', children: (
          <CrudPage endpoint="/beneficiaries" entityName="Beneficiary" permission="clinical" params={params} extra={filter} refreshKey={tick} onChanged={refresh}
            searchKeys={['name', 'village', 'phone', 'campName']} filters={[{ name: 'gender', label: 'Gender', options: E.gender }]}
            fields={[campRef, { name: 'residentId', label: 'Registered resident (optional)', type: 'remote', endpoint: '/residents', labelOf: (r) => `${r.fullName} · ${r.residentCode}`,
              onPick: (r, f) => r && f.setFieldsValue({ name: r.fullName, age: r.age, gender: r.gender, village: r.village, phone: r.phone }) },
              { name: 'name', label: 'Name', required: true }, { name: 'age', label: 'Age', type: 'number', min: 0 }, { name: 'gender', label: 'Gender', type: 'select', options: E.gender },
              { name: 'village', label: 'Village', type: 'select', options: VILLAGES }, { name: 'phone', label: 'Phone' },
              { name: 'servicesReceived', label: 'Services received', type: 'textarea', span: 24 }]}
            columns={[col.text('Name', 'name'), col.number('Age', 'age', { width: 70 }), col.enumText('Gender', 'gender'), col.text('Village', 'village'), col.text('Camp', 'campName'),
              { title: 'Resident link', dataIndex: 'residentId', render: (v) => (v ? <Tag color="green">Linked</Tag> : <Tag>Walk-in</Tag>) }, col.date('Registered', 'registeredOn')]} />
        ) },
        { key: 'screenings', label: 'Screenings', children: (
          <CrudPage endpoint="/screenings" entityName="Screening" permission="clinical" params={params} extra={filter} refreshKey={tick} onChanged={refresh}
            searchKeys={['beneficiaryName', 'resultValue', 'campName', 'screenedBy']}
            filters={[{ name: 'outcome', label: 'Outcome', options: E.screeningOutcome }, { name: 'screeningType', label: 'Type', options: E.screeningType }]}
            defaults={{ outcome: 'NORMAL', referred: false }}
            fields={[beneficiaryRef(campId), { name: 'screeningType', label: 'Screening', type: 'select', options: E.screeningType, required: true },
              { name: 'resultValue', label: 'Result value', placeholder: 'e.g. 150/95 mmHg' }, { name: 'outcome', label: 'Outcome', type: 'select', options: E.screeningOutcome, required: true },
              { name: 'screenedBy', label: 'Screened by' }, { name: 'referred', label: 'Referred (creates follow-up)', type: 'switch' }, { name: 'referredTo', label: 'Referred to' },
              { name: 'notes', label: 'Notes', type: 'textarea', span: 24 }]}
            columns={[col.date('Date', 'screeningDate'), col.text('Beneficiary', 'beneficiaryName'), col.enumText('Screening', 'screeningType'), col.text('Result', 'resultValue'),
              col.status('Outcome', 'outcome', { width: 120 }), { title: 'Referral', dataIndex: 'referred', render: (v, r) => (v ? <Tag color="purple">{r.referredTo || 'Referred'}</Tag> : '—') }, col.text('Camp', 'campName')]} />
        ) },
        { key: 'followups', label: 'Follow-ups', children: (
          <CrudPage endpoint="/follow-ups" entityName="Follow-up" permission="clinical" params={params} extra={filter} refreshKey={tick} onChanged={refresh}
            searchKeys={['beneficiaryName', 'action', 'assignedTo', 'village']}
            filters={[{ name: 'status', label: 'Status', options: E.followUpStatus }, { name: 'priority', label: 'Priority', options: PRIORITIES }]}
            defaults={{ priority: 'MEDIUM', status: 'PENDING' }}
            statusOptions={(r) => ({ url: `/follow-ups/${r.id}/status`, options: FU_NEXT[r.status] })}
            fields={[beneficiaryRef(campId), { name: 'action', label: 'Follow-up action', required: true, span: 24 }, { name: 'dueDate', label: 'Due date', type: 'date', required: true },
              { name: 'priority', label: 'Priority', type: 'select', options: PRIORITIES }, { name: 'assignedTo', label: 'Assigned to' },
              { name: 'outcomeNotes', label: 'Outcome notes', type: 'textarea', span: 24 }]}
            columns={[col.text('Beneficiary', 'beneficiaryName'), col.text('Action', 'action'), col.text('Village', 'village'),
              { title: 'Due', dataIndex: 'dueDate', render: (v, r) => <span>{fmtDate(v)} {r.overdue && <Tag color="red">Overdue</Tag>}</span> },
              col.status('Priority', 'priority', { width: 120 }), col.text('Assigned to', 'assignedTo'), col.status('Status', 'status')]} />
        ) },
      ]} />
    </>
  );
}
