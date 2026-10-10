import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card, Col, Input, Row, Segmented, Select, Space, Table, Tag, Tooltip, message } from 'antd';
import { AlertOutlined, DownloadOutlined, ExportOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import api, { errorMessage } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatusTag from '../components/StatusTag';
import StatusChanger from '../components/StatusChanger';
import StatCard from '../components/StatCard';
import { PRIORITIES, humanize } from '../utils/constants';
import { exportCsv, fmtDate, fmtDateTime } from '../utils/format';
import { useAuth } from '../auth/AuthContext';

const SOURCES = [
  { value: 'sanitation', label: 'Sanitation' },
  { value: 'inventory', label: 'Inventory' },
  { value: 'water', label: 'Water & Public Health' },
  { value: 'healthcamp', label: 'Health Camps' },
  { value: 'appointment', label: 'Appointments' },
  { value: 'healthcenter', label: 'Health Centres' },
];
const SOURCE_LABEL = Object.fromEntries(SOURCES.map((s) => [s.value, s.label]));
const SOURCE_PERMISSION = { sanitation: 'sanitation', water: 'sanitation', inventory: 'inventory', healthcamp: 'clinical', appointment: 'clinical', healthcenter: 'centres' };

/** Consolidated officer view of all outstanding work, routed from every microservice. */
export default function PendingActionsPage() {
  const navigate = useNavigate();
  const { can } = useAuth();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [source, setSource] = useState('all');
  const [priority, setPriority] = useState();
  const [q, setQ] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get('/dashboard/pending-actions');
      setRows(data);
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const visible = useMemo(() => rows.filter((r) => (source === 'all' || r.source === source)
    && (!priority || r.priority === priority)
    && (!q || `${r.title} ${r.refNumber} ${r.village} ${r.category}`.toLowerCase().includes(q.toLowerCase()))), [rows, source, priority, q]);

  const count = (p) => rows.filter((r) => r.priority === p).length;

  const columns = [
    { title: 'Priority', dataIndex: 'priority', width: 120, render: (v) => <StatusTag value={v} />,
      sorter: (a, b) => PRIORITIES.indexOf(b.priority) - PRIORITIES.indexOf(a.priority) },
    { title: 'Reference', dataIndex: 'refNumber', width: 130, render: (v) => <Tag>{v}</Tag> },
    { title: 'Action item', dataIndex: 'title', render: (v, r) => (
      <div style={{ maxWidth: 420 }}>
        <div style={{ fontWeight: 500 }}>{v}</div>
        {r.description && <div className="muted" style={{ fontSize: 12, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.description}</div>}
      </div>
    ) },
    { title: 'Module', dataIndex: 'source', width: 160, render: (v, r) => (
      <div><div>{SOURCE_LABEL[v]}</div><div className="muted" style={{ fontSize: 12 }}>{r.category}</div></div>
    ) },
    { title: 'Location', dataIndex: 'village', width: 170, ellipsis: true },
    { title: 'Status', dataIndex: 'status', width: 150, render: (v) => <StatusTag value={v} /> },
    { title: 'Due', dataIndex: 'dueDate', width: 120, render: fmtDate },
    { title: 'Raised', dataIndex: 'createdAt', width: 160, render: fmtDateTime },
    { title: 'Actions', key: 'a', fixed: 'right', width: 1, render: (_, r) => (
      <Space>
        <Tooltip title={can(SOURCE_PERMISSION[r.source]) ? null : 'Your role cannot update this module'}>
          <span><StatusChanger url={r.statusUrl} options={r.nextStatuses} onDone={load} label="Update" disabled={!can(SOURCE_PERMISSION[r.source])} /></span>
        </Tooltip>
        <Tooltip title="Open module"><Button size="small" type="text" icon={<ExportOutlined />} onClick={() => navigate(r.modulePath)} /></Tooltip>
      </Space>
    ) },
  ];

  return (
    <>
      <PageHeader icon={<AlertOutlined />} title="Pending Actions" subtitle="Consolidated view of outstanding work across healthcare, inventory, sanitation and water services"
        extra={<Space><Button icon={<DownloadOutlined />} onClick={() => exportCsv('pending-actions', columns, visible)}>Export</Button><Button icon={<ReloadOutlined />} loading={loading} onClick={load}>Refresh</Button></Space>} />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        {PRIORITIES.slice().reverse().map((p) => (
          <Col key={p} xs={12} md={6}>
            <StatCard title={`${humanize(p)} priority`} value={count(p)} onClick={() => setPriority(priority === p ? undefined : p)}
              color={{ CRITICAL: '#d03b3b', HIGH: '#ec835a', MEDIUM: '#eda100', LOW: '#2a78d6' }[p]} icon={<AlertOutlined />}
              footer={priority === p ? 'Filter active — click to clear' : 'Click to filter'} />
          </Col>
        ))}
      </Row>
      <Card className="crud-card">
        <div className="crud-toolbar">
          <Segmented value={source} onChange={setSource} options={[{ value: 'all', label: `All (${rows.length})` },
            ...SOURCES.map((s) => ({ value: s.value, label: `${s.label} (${rows.filter((r) => r.source === s.value).length})` }))]} />
          <Space wrap>
            <Select allowClear placeholder="Priority" style={{ width: 140 }} value={priority} onChange={setPriority} options={PRIORITIES.map((p) => ({ value: p, label: humanize(p) }))} />
            <Input allowClear prefix={<SearchOutlined />} placeholder="Search actions…" value={q} onChange={(e) => setQ(e.target.value)} style={{ width: 220 }} />
          </Space>
        </div>
        <Table rowKey={(r) => `${r.source}-${r.category}-${r.refId}`} loading={loading} columns={columns} dataSource={visible} scroll={{ x: 'max-content' }}
          pagination={{ pageSize: 15, showTotal: (t) => `${t} actions` }} />
      </Card>
    </>
  );
}
