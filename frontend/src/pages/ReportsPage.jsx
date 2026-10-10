import { useCallback, useEffect, useState } from 'react';
import { Button, Card, Col, Row, Skeleton, Space, Table, Tabs } from 'antd';
import { BarChartOutlined, DownloadOutlined, PrinterOutlined, ReloadOutlined } from '@ant-design/icons';
import api from '../api/client';
import PageHeader from '../components/PageHeader';
import { BarCard, DonutCard } from '../components/Charts';
import { humanize, SERIES } from '../utils/constants';
import { exportCsv, fmtDate, fmtDateTime, toRows } from '../utils/format';

const villageColumns = [
  { title: 'Village', dataIndex: 'village', render: (v) => <b>{v}</b> },
  { title: 'Registered residents', dataIndex: 'residents', align: 'right' },
  { title: 'Sanitation complaints', dataIndex: 'sanitationComplaints', align: 'right' },
  { title: 'Unsafe water tests', dataIndex: 'unsafeWaterTests', align: 'right', render: (v) => <span style={{ color: v > 0 ? '#d03b3b' : undefined, fontWeight: v > 0 ? 600 : 400 }}>{v}</span> },
  { title: 'Pending actions', dataIndex: 'pendingActions', align: 'right' },
];

export default function ReportsPage() {
  const [s, setS] = useState(null);
  const [villages, setVillages] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [summary, v] = await Promise.all([api.get('/dashboard/summary'), api.get('/dashboard/reports/villages')]);
      setS(summary.data);
      setVillages(v.data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  if (!s) return <Skeleton active paragraph={{ rows: 14 }} />;
  const p = s.patients || {}, ap = s.appointments || {}, inv = s.inventory || {}, camps = s.healthCamps || {}, san = s.sanitation || {}, wat = s.water || {}, hc = s.healthCentres || {};
  const H = humanize;

  return (
    <>
      <PageHeader icon={<BarChartOutlined />} title="Community Health Reports" subtitle={`Consolidated reporting across all services · generated ${fmtDateTime(s.generatedAt)}`}
        extra={<Space><Button icon={<PrinterOutlined />} onClick={() => window.print()}>Print</Button><Button icon={<ReloadOutlined />} loading={loading} onClick={load}>Refresh</Button></Space>} />
      <Card className="crud-card" title="Village-wise health & sanitation summary" style={{ marginBottom: 16 }}
        extra={<Button icon={<DownloadOutlined />} onClick={() => exportCsv('village-report', villageColumns, villages)}>Export CSV</Button>}>
        <Table rowKey="village" size="middle" pagination={false} columns={villageColumns} dataSource={villages} scroll={{ x: 'max-content' }}
          summary={(rows) => (
            <Table.Summary.Row style={{ background: '#fafbfa', fontWeight: 600 }}>
              <Table.Summary.Cell index={0}>Total</Table.Summary.Cell>
              {['residents', 'sanitationComplaints', 'unsafeWaterTests', 'pendingActions'].map((k, i) => (
                <Table.Summary.Cell key={k} index={i + 1} align="right">{rows.reduce((t, r) => t + Number(r[k] || 0), 0)}</Table.Summary.Cell>
              ))}
            </Table.Summary.Row>
          )} />
      </Card>
      <Tabs className="module-tabs" items={[
        { key: 'health', label: 'Healthcare services', children: (
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}><BarCard title="Residents by village" data={toRows(p.byVillage)} /></Col>
            <Col xs={24} lg={12}><BarCard title="Visits by type (all time)" data={toRows(p.visitsByType, H)} horizontal color={SERIES[2]} /></Col>
            <Col xs={24} lg={12}><DonutCard title="Appointments by status" data={toRows(ap.byStatus, H)} useStatus /></Col>
            <Col xs={24} lg={12}><BarCard title="Appointments by centre" data={toRows(ap.byCentre)} horizontal /></Col>
            <Col xs={24} lg={12}><BarCard title="Top diagnoses (consultations)" data={toRows(ap.topDiagnoses)} horizontal color={SERIES[6]} /></Col>
            <Col xs={24} lg={12}><BarCard title="Healthcare workers by role" data={toRows(hc.workersByRole, H)} horizontal color={SERIES[0]} /></Col>
          </Row>
        ) },
        { key: 'inventory', label: 'Medicine availability', children: (
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}><DonutCard title="Stock records by status" data={toRows(inv.stockByStatus, H)} useStatus /></Col>
            <Col xs={24} lg={12}><BarCard title="Units in stock by centre" data={toRows(inv.quantityByCentre)} horizontal /></Col>
            <Col xs={24} lg={12}><BarCard title="Most issued items (30 days)" data={toRows(inv.topIssued)} horizontal color={SERIES[1]} /></Col>
            <Col xs={24} lg={12}><DonutCard title="Replenishment requests by status" data={toRows(inv.replenishmentsByStatus, H)} useStatus /></Col>
          </Row>
        ) },
        { key: 'camps', label: 'Health camps', children: (
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}><BarCard title="Beneficiaries by camp" data={toRows(camps.beneficiariesByCamp)} horizontal color={SERIES[2]} /></Col>
            <Col xs={24} lg={12}><DonutCard title="Screening outcomes" data={toRows(camps.screeningOutcomes, H)} useStatus /></Col>
            <Col xs={24} lg={12}><BarCard title="Abnormal findings by screening type" data={toRows(camps.abnormalByType, H)} horizontal color={SERIES[7]} /></Col>
            <Col xs={24} lg={12}><DonutCard title="Follow-ups by status" data={toRows(camps.followUpsByStatus, H)} useStatus /></Col>
          </Row>
        ) },
        { key: 'sanitation', label: 'Sanitation', children: (
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}><DonutCard title="Facilities by cleanliness" data={toRows(san.facilitiesByCleanliness, H)} useStatus /></Col>
            <Col xs={24} lg={12}><BarCard title="Complaints by category" data={toRows(san.complaintsByCategory, H)} horizontal color={SERIES[1]} /></Col>
            <Col xs={24} lg={12}><DonutCard title="Maintenance tasks by status" data={toRows(san.maintenanceByStatus, H)} useStatus /></Col>
            <Col xs={24} lg={12}><BarCard title="Facilities by type" data={toRows(san.facilitiesByType, H)} horizontal /></Col>
          </Row>
        ) },
        { key: 'water', label: 'Water & public health', children: (
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}><DonutCard title="Quality test results" data={toRows(wat.testResults, H)} useStatus /></Col>
            <Col xs={24} lg={12}><DonutCard title="Sources by operational status" data={toRows(wat.sourcesByStatus, H)} useStatus /></Col>
            <Col xs={24} lg={12}><BarCard title="Public-health issues by category" data={toRows(wat.issuesByCategory, H)} horizontal color={SERIES[7]} /></Col>
            <Col xs={24} lg={12}><BarCard title="Sources by type" data={toRows(wat.sourcesByType, H)} horizontal /></Col>
          </Row>
        ) },
      ]} />
      <div className="muted" style={{ fontSize: 12, marginTop: 12 }}>Report date {fmtDate(new Date())}</div>
    </>
  );
}
