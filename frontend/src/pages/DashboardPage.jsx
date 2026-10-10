import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Col, List, Row, Skeleton, Space, Table, Tag, Typography } from 'antd';
import {
  AlertOutlined, CalendarOutlined, ExperimentOutlined, HomeOutlined, MedicineBoxOutlined, ReloadOutlined,
  SafetyCertificateOutlined, TeamOutlined, WarningOutlined,
} from '@ant-design/icons';
import api, { errorMessage } from '../api/client';
import StatCard from '../components/StatCard';
import StatusTag from '../components/StatusTag';
import { BarCard, DonutCard } from '../components/Charts';
import { humanize, SERIES } from '../utils/constants';
import { fmtDate, fmtDateTime, toRows } from '../utils/format';
import { useAuth } from '../auth/AuthContext';

const SOURCE_LABEL = { appointment: 'Appointments', healthcamp: 'Health Camps', healthcenter: 'Health Centres', inventory: 'Inventory', sanitation: 'Sanitation', water: 'Water' };

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data: d } = await api.get('/dashboard/summary');
      setData(d);
      setError(null);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    const t = setInterval(load, 60000);
    return () => clearInterval(t);
  }, [load]);

  if (!data) {
    return error ? <Alert type="error" showIcon message="Dashboard unavailable" description={error} action={<Button onClick={load}>Retry</Button>} /> : <Skeleton active paragraph={{ rows: 12 }} />;
  }

  const p = data.patients || {}, hc = data.healthCentres || {}, ap = data.appointments || {}, inv = data.inventory || {};
  const camps = data.healthCamps || {}, san = data.sanitation || {}, wat = data.water || {}, pending = data.pending || {};
  const downServices = Object.entries(data.serviceHealth || {}).filter(([, v]) => v !== 'UP');
  const hour = new Date().getHours();

  return (
    <>
      <div className="hero-banner">
        <div>
          <h2>Good {hour < 12 ? 'morning' : hour < 17 ? 'afternoon' : 'evening'}, {user?.fullName?.split(' ')[0] === 'Dr.' ? user.fullName : user?.fullName?.split(' ')[0]}</h2>
          <p>Community health &amp; sanitation overview · updated {fmtDateTime(data.generatedAt)}</p>
          <div style={{ marginTop: 10 }}>
            {Object.entries(data.serviceHealth || {}).map(([k, v]) => (
              <span key={k} className="service-dot"><i style={{ background: v === 'UP' ? '#4ade80' : '#f87171' }} />{humanize(k.replace(/([A-Z])/g, '_$1'))}</span>
            ))}
          </div>
        </div>
        <Space size={28}>
          <div className="hero-stat"><div className="num">{pending.total ?? 0}</div><div className="lbl">Pending actions</div></div>
          <div className="hero-stat"><div className="num">{pending.byPriority?.CRITICAL ?? 0}</div><div className="lbl">Critical</div></div>
          <Button ghost icon={<ReloadOutlined />} loading={loading} onClick={load}>Refresh</Button>
        </Space>
      </div>

      {downServices.length > 0 && (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }}
          message={`Some services are unreachable: ${downServices.map(([k]) => humanize(k.replace(/([A-Z])/g, '_$1'))).join(', ')}`}
          description="Figures from those modules are temporarily omitted; everything else is live." />
      )}

      <Row gutter={[16, 16]}>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Registered residents" value={p.totalResidents} icon={<TeamOutlined />} footer={`${p.chronicPatients ?? 0} chronic · ${p.pregnantWomen ?? 0} pregnant`} onClick={() => navigate('/residents')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Health centres" value={hc.totalCentres} icon={<HomeOutlined />} color="#2a78d6" footer={`${hc.activeWorkers ?? 0} active staff`} onClick={() => navigate('/health-centres')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Today's appointments" value={ap.todayAppointments} icon={<CalendarOutlined />} color="#6250d6" footer={`${ap.upcoming7Days ?? 0} upcoming this week`} onClick={() => navigate('/appointments')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Stock alerts" value={(inv.lowStock ?? 0) + (inv.outOfStock ?? 0)} icon={<MedicineBoxOutlined />} color="#d03b3b" footer={`${inv.outOfStock ?? 0} out of stock · ${inv.openReplenishments ?? 0} requests open`} onClick={() => navigate('/inventory')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Health camps" value={camps.totalCamps} icon={<SafetyCertificateOutlined />} color="#1baf7a" footer={`${camps.upcomingCamps ?? 0} upcoming · ${camps.openFollowUps ?? 0} follow-ups`} onClick={() => navigate('/health-camps')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Sanitation facilities" value={san.totalFacilities} icon={<ExperimentOutlined />} color="#eb6834" footer={`${san.needsAttention ?? 0} need attention · ${san.openComplaints ?? 0} complaints`} onClick={() => navigate('/sanitation')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Water sources" value={wat.totalSources} icon={<span>💧</span>} color="#2a78d6" footer={`${wat.contaminatedSources ?? 0} contaminated · ${wat.openIssues ?? 0} open issues`} onClick={() => navigate('/water')} /></Col>
        <Col xs={12} md={8} xl={6} xxl={3}><StatCard title="Field reports" value={data.fieldReports?.total} icon={<AlertOutlined />} color="#eda100" footer={`${data.fieldReports?.items ?? 0} issues routed`} onClick={() => navigate('/field-reports')} /></Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} xl={14}>
          <Card className="chart-card" title={<Space><WarningOutlined style={{ color: '#d03b3b' }} />Critical actions requiring attention</Space>}
            extra={<Button type="link" onClick={() => navigate('/pending-actions')}>View all {pending.total}</Button>}>
            <List
              dataSource={pending.critical || []}
              locale={{ emptyText: 'No critical actions — great work!' }}
              renderItem={(a) => (
                <List.Item actions={[<StatusTag key="s" value={a.status} />]}>
                  <List.Item.Meta
                    title={<Space wrap><span className="list-item-title">{a.title}</span><Tag>{a.refNumber}</Tag></Space>}
                    description={<span>{SOURCE_LABEL[a.source]} · {a.category}{a.village ? ` · ${a.village}` : ''}{a.dueDate ? ` · due ${fmtDate(a.dueDate)}` : ''}</span>}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <BarCard title="Pending actions by module" data={toRows(pending.bySource, (k) => SOURCE_LABEL[k] || k)} horizontal height={300} />
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12} xl={8}><DonutCard title="Medicine stock status" data={toRows(inv.stockByStatus, humanize)} useStatus /></Col>
        <Col xs={24} lg={12} xl={8}><DonutCard title="Sanitation cleanliness" data={toRows(san.facilitiesByCleanliness, humanize)} useStatus /></Col>
        <Col xs={24} lg={12} xl={8}><DonutCard title="Water source quality (latest test)" data={toRows(wat.sourcesByQuality, humanize)} useStatus /></Col>
        <Col xs={24} lg={12} xl={12}><BarCard title="Appointments – next & last 14 days" data={toRows(ap.dailyAppointments, (k) => fmtDate(k).slice(0, 6))} color={SERIES[0]} /></Col>
        <Col xs={24} lg={12} xl={12}>
          <Card className="chart-card" title="Low & out-of-stock items" extra={<Button type="link" onClick={() => navigate('/inventory')}>Manage stock</Button>}>
            <Table size="small" rowKey="id" pagination={false} dataSource={data.lowStockItems || []} scroll={{ y: 230 }}
              columns={[
                { title: 'Item', dataIndex: 'medicineName', ellipsis: true },
                { title: 'Centre', dataIndex: 'healthCentreName', ellipsis: true },
                { title: 'Qty', dataIndex: 'quantity', align: 'right', width: 70 },
                { title: 'Status', dataIndex: 'stockStatus', render: (v) => <StatusTag value={v} />, width: 140 },
              ]} />
          </Card>
        </Col>
      </Row>
      <Typography.Paragraph type="secondary" style={{ marginTop: 16, fontSize: 12 }}>
        Data is aggregated live from the patient, health-centre, appointment, inventory, health-camp, sanitation and water microservices via the API gateway.
      </Typography.Paragraph>
    </>
  );
}
