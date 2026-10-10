import { useEffect, useState } from 'react';
import { Col, Descriptions, Row, Tabs, Tag, Typography } from 'antd';
import api from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import StatusTag from '../components/StatusTag';
import { E, PRIORITIES, VILLAGES, humanize } from '../utils/constants';
import { fmtDate, fmtNumber } from '../utils/format';

const ISSUE_NEXT = { OPEN: ['INVESTIGATING', 'ACTION_TAKEN', 'RESOLVED'], INVESTIGATING: ['ACTION_TAKEN', 'RESOLVED'], ACTION_TAKEN: ['RESOLVED', 'CLOSED'], RESOLVED: ['CLOSED'], CLOSED: [] };
const ACTION_NEXT = { PLANNED: ['IN_PROGRESS', 'COMPLETED', 'CANCELLED'], IN_PROGRESS: ['COMPLETED', 'CANCELLED'], COMPLETED: [], CANCELLED: ['PLANNED'] };
const sourceRef = (required) => ({ name: 'sourceId', label: 'Water source', type: 'remote', endpoint: '/water/sources', labelOf: (s) => `${s.name} (${s.village})`, required });

function TestDetail({ t }) {
  const rows = [['pH', t.ph, '6.5 – 8.5'], ['Turbidity (NTU)', t.turbidity, '≤ 1 (≤ 5 permissible)'], ['TDS (mg/L)', t.tds, '≤ 500 (≤ 2000)'],
    ['Residual chlorine (mg/L)', t.residualChlorine, '0.2 – 1.0'], ['Fluoride (mg/L)', t.fluoride, '≤ 1.0 (≤ 1.5)'], ['Nitrate (mg/L)', t.nitrate, '≤ 45'],
    ['Arsenic (mg/L)', t.arsenic, '≤ 0.01'], ['Iron (mg/L)', t.iron, '≤ 1.0'], ['Total coliform', t.coliformPresent == null ? null : t.coliformPresent ? 'Present' : 'Absent', 'Absent'],
    ['E. coli', t.ecoliPresent == null ? null : t.ecoliPresent ? 'Present' : 'Absent', 'Absent']];
  return (
    <>
      <Descriptions bordered size="small" column={1} style={{ marginBottom: 16 }}>
        <Descriptions.Item label="Source">{t.sourceName} · {t.village}</Descriptions.Item>
        <Descriptions.Item label="Tested">{fmtDate(t.testDate)} by {t.testedBy || '—'} ({humanize(t.method)})</Descriptions.Item>
        <Descriptions.Item label="Result"><StatusTag value={t.overallStatus} /></Descriptions.Item>
        <Descriptions.Item label="Findings">{t.findings}</Descriptions.Item>
      </Descriptions>
      <Descriptions bordered size="small" column={1} title="Parameters vs BIS 10500:2012">
        {rows.map(([k, v, limit]) => <Descriptions.Item key={k} label={k}>{v ?? '—'} <Typography.Text type="secondary" style={{ marginLeft: 8 }}>limit {limit}</Typography.Text></Descriptions.Item>)}
      </Descriptions>
    </>
  );
}

export default function WaterPage() {
  const [stats, setStats] = useState({});
  const [tick, setTick] = useState(0);
  const refresh = () => setTick((t) => t + 1);

  useEffect(() => {
    api.get('/water/stats').then(({ data }) => setStats(data)).catch(() => {});
  }, [tick]);

  return (
    <>
      <PageHeader icon={<span>💧</span>} title="Water & Public Health Monitoring" subtitle="Water-supply information, quality observations, sanitation-related issues and corrective actions" />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={8} xl={4}><StatCard title="Water sources" value={stats.totalSources} footer={`${stats.functionalSources ?? 0} functional`} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Population served" value={fmtNumber(stats.populationServed)} color="#2a78d6" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Contaminated" value={stats.contaminatedSources} color="#d03b3b" footer={`${stats.unsafeSources ?? 0} unsafe on last test`} /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Tests (30 days)" value={stats.testsLast30Days} color="#1baf7a" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Open issues" value={stats.openIssues} color="#eda100" /></Col>
        <Col xs={12} md={8} xl={4}><StatCard title="Corrective actions" value={stats.activeCorrectiveActions} color="#6250d6" footer="Planned / in progress" /></Col>
      </Row>
      <Tabs className="module-tabs" items={[
        { key: 'sources', label: 'Water sources', children: (
          <CrudPage endpoint="/water/sources" entityName="Water source" permission="sanitation" refreshKey={tick} onChanged={refresh}
            searchKeys={['name', 'sourceCode', 'village', 'managedBy']}
            filters={[{ name: 'village', label: 'Village', options: VILLAGES.map((v) => ({ value: v, label: v })) }, { name: 'type', label: 'Type', options: E.waterSourceType }, { name: 'status', label: 'Status', options: E.waterSourceStatus }]}
            defaults={{ status: 'FUNCTIONAL' }}
            statusOptions={(r) => ({ url: `/water/sources/${r.id}/status`, options: E.waterSourceStatus.filter((s) => s !== r.status) })}
            fields={[{ name: 'name', label: 'Source name', required: true, span: 24 }, { name: 'type', label: 'Type', type: 'select', options: E.waterSourceType, required: true },
              { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true }, { name: 'ward', label: 'Ward' }, { name: 'landmark', label: 'Landmark' },
              { name: 'householdsServed', label: 'Households served', type: 'number', min: 0 }, { name: 'populationServed', label: 'Population served', type: 'number', min: 0 },
              { name: 'supplySchedule', label: 'Supply schedule', placeholder: '06:00-08:00 & 17:00-19:00' }, { name: 'supplyHoursPerDay', label: 'Supply hours / day', type: 'number', min: 0, max: 24 },
              { name: 'dailyCapacityLitres', label: 'Daily capacity', type: 'number', unit: 'L', min: 0 }, { name: 'managedBy', label: 'Managed by' },
              { name: 'status', label: 'Status', type: 'select', options: E.waterSourceStatus }, { name: 'lastChlorinatedDate', label: 'Last chlorinated', type: 'date' },
              { name: 'latitude', label: 'Latitude', type: 'number', step: 0.0001 }, { name: 'longitude', label: 'Longitude', type: 'number', step: 0.0001 }]}
            columns={[col.text('Code', 'sourceCode', { width: 100 }), { title: 'Source', dataIndex: 'name', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{humanize(r.type)}</div></div> },
              col.text('Village', 'village'), col.number('Population', 'populationServed'), col.text('Supply', 'supplySchedule'), col.status('Status', 'status', { width: 170 }),
              col.status('Last quality', 'lastQualityStatus', { width: 120 }), col.date('Last tested', 'lastTestedDate')]} />
        ) },
        { key: 'tests', label: 'Quality observations', children: (
          <CrudPage endpoint="/water/tests" entityName="Quality test" permission="sanitation" refreshKey={tick} onChanged={refresh} allowEdit={false}
            searchKeys={['sourceName', 'village', 'testedBy', 'findings']} filters={[{ name: 'overallStatus', label: 'Result', options: E.quality.filter((q) => q !== 'UNTESTED') }]}
            defaults={{ method: 'FIELD_TEST_KIT', coliformPresent: false, ecoliPresent: false }}
            detail={(t) => <TestDetail t={t} />}
            fields={[sourceRef(true), { name: 'testDate', label: 'Test date', type: 'date' }, { name: 'testedBy', label: 'Tested by' },
              { name: 'method', label: 'Method', type: 'select', options: E.testMethod },
              { name: 'ph', label: 'pH', type: 'number', step: 0.1, span: 6 }, { name: 'turbidity', label: 'Turbidity', type: 'number', unit: 'NTU', step: 0.1, span: 6 },
              { name: 'tds', label: 'TDS', type: 'number', unit: 'mg/L', span: 6 }, { name: 'residualChlorine', label: 'Residual Cl', type: 'number', unit: 'mg/L', step: 0.1, span: 6 },
              { name: 'fluoride', label: 'Fluoride', type: 'number', unit: 'mg/L', step: 0.1, span: 6 }, { name: 'nitrate', label: 'Nitrate', type: 'number', unit: 'mg/L', span: 6 },
              { name: 'arsenic', label: 'Arsenic', type: 'number', unit: 'mg/L', step: 0.001, span: 6 }, { name: 'iron', label: 'Iron', type: 'number', unit: 'mg/L', step: 0.1, span: 6 },
              { name: 'coliformPresent', label: 'Total coliform present', type: 'switch' }, { name: 'ecoliPresent', label: 'E. coli present', type: 'switch' },
              { name: 'remarks', label: 'Remarks', type: 'textarea', span: 24 }]}
            columns={[col.date('Date', 'testDate'), col.text('Source', 'sourceName'), col.text('Village', 'village'), col.number('pH', 'ph', { width: 70 }),
              col.number('Turbidity', 'turbidity', { width: 90 }), col.number('TDS', 'tds', { width: 80 }), { title: 'E. coli', dataIndex: 'ecoliPresent', render: (v) => (v ? <Tag color="red">Present</Tag> : <Tag color="green">Absent</Tag>) },
              col.status('Result', 'overallStatus', { width: 110 }), col.text('Findings', 'findings'), { title: 'Issue', dataIndex: 'issueId', render: (v) => (v ? <Tag color="volcano">PHI #{v}</Tag> : '—') }]} />
        ) },
        { key: 'issues', label: 'Public-health issues', children: (
          <CrudPage endpoint="/water/issues" entityName="Issue" permission="sanitation" refreshKey={tick} onChanged={refresh}
            searchKeys={['issueNumber', 'title', 'village', 'sourceName', 'assignedTo']}
            filters={[{ name: 'status', label: 'Status', options: E.issueStatus }, { name: 'category', label: 'Category', options: E.issueCategory }, { name: 'severity', label: 'Severity', options: PRIORITIES }]}
            defaults={{ severity: 'MEDIUM', source: 'MANUAL' }}
            statusOptions={(r) => ({ url: `/water/issues/${r.id}/status`, options: ISSUE_NEXT[r.status] })}
            fields={[{ name: 'title', label: 'Title', required: true, span: 24 }, { name: 'category', label: 'Category', type: 'select', options: E.issueCategory, required: true },
              { name: 'severity', label: 'Severity', type: 'select', options: PRIORITIES }, sourceRef(false), { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true },
              { name: 'affectedPopulation', label: 'Affected population', type: 'number', min: 0 }, { name: 'suspectedCases', label: 'Suspected disease cases', type: 'number', min: 0 },
              { name: 'reportedBy', label: 'Reported by' }, { name: 'assignedTo', label: 'Assigned to' }, { name: 'description', label: 'Description', type: 'textarea', span: 24 }]}
            columns={[col.text('No.', 'issueNumber', { width: 110 }), { title: 'Issue', dataIndex: 'title', render: (v, r) => <div><b>{v}</b><div className="muted" style={{ fontSize: 12 }}>{humanize(r.category)}</div></div> },
              col.text('Village', 'village'), col.status('Severity', 'severity', { width: 120 }), col.number('Affected', 'affectedPopulation'), col.number('Cases', 'suspectedCases', { width: 80 }),
              { title: 'Source', dataIndex: 'source', render: (v) => <Tag color={v === 'FIELD_REPORT' ? 'purple' : v === 'QUALITY_TEST' ? 'volcano' : undefined}>{humanize(v)}</Tag> },
              col.date('Reported', 'reportedDate'), col.status('Status', 'status')]} />
        ) },
        { key: 'actions', label: 'Corrective actions', children: (
          <CrudPage endpoint="/water/actions" entityName="Corrective action" permission="sanitation" refreshKey={tick} onChanged={refresh}
            searchKeys={['issueNumber', 'description', 'assignedTo', 'village']} filters={[{ name: 'status', label: 'Status', options: E.actionStatus }, { name: 'actionType', label: 'Type', options: E.actionType }]}
            defaults={{ status: 'PLANNED' }}
            statusOptions={(r) => ({ url: `/water/actions/${r.id}/status`, options: ACTION_NEXT[r.status] })}
            fields={[{ name: 'issueId', label: 'Public-health issue', type: 'remote', endpoint: '/water/issues', labelOf: (i) => `${i.issueNumber} · ${i.title}`, required: true, span: 24 },
              { name: 'actionType', label: 'Action type', type: 'select', options: E.actionType, required: true }, { name: 'assignedTo', label: 'Assigned to' },
              { name: 'plannedDate', label: 'Planned date', type: 'date' }, { name: 'cost', label: 'Cost', type: 'number', unit: '₹', min: 0 },
              { name: 'description', label: 'Description', type: 'textarea', span: 24, required: true }, { name: 'outcome', label: 'Outcome', type: 'textarea', span: 24 }]}
            columns={[col.text('Issue', 'issueNumber', { width: 110 }), col.enumText('Action', 'actionType'), col.text('Description', 'description'), col.text('Village', 'village'),
              col.text('Assigned to', 'assignedTo'), col.date('Planned', 'plannedDate'), col.date('Completed', 'completedDate'), col.status('Status', 'status')]} />
        ) },
      ]} />
    </>
  );
}
