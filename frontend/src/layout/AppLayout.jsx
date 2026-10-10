import { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Avatar, Badge, Button, Drawer, Dropdown, Grid, Layout, Menu, Space, Tag, Typography } from 'antd';
import {
  AlertOutlined, BarChartOutlined, CalendarOutlined, DashboardOutlined, ExperimentOutlined, FileTextOutlined, HomeOutlined,
  LogoutOutlined, MedicineBoxOutlined, MenuOutlined, SafetyCertificateOutlined, TeamOutlined, UserOutlined, UsergroupAddOutlined,
} from '@ant-design/icons';
import { useAuth } from '../auth/AuthContext';
import { humanize } from '../utils/constants';

const { Sider, Header, Content } = Layout;

const NAV = [
  { type: 'group', label: 'Overview', children: [
    { key: '/', icon: <DashboardOutlined />, label: 'Dashboard' },
    { key: '/pending-actions', icon: <AlertOutlined />, label: 'Pending Actions' },
    { key: '/field-reports', icon: <FileTextOutlined />, label: 'Field Reports' },
  ] },
  { type: 'group', label: 'Healthcare', children: [
    { key: '/residents', icon: <TeamOutlined />, label: 'Residents & Patients' },
    { key: '/health-centres', icon: <HomeOutlined />, label: 'Health Centres' },
    { key: '/appointments', icon: <CalendarOutlined />, label: 'Appointments' },
    { key: '/inventory', icon: <MedicineBoxOutlined />, label: 'Medicine Inventory' },
    { key: '/health-camps', icon: <SafetyCertificateOutlined />, label: 'Health Camps' },
  ] },
  { type: 'group', label: 'Public Health', children: [
    { key: '/sanitation', icon: <ExperimentOutlined />, label: 'Sanitation' },
    { key: '/water', icon: <span className="nav-emoji">💧</span>, label: 'Water Monitoring' },
  ] },
  { type: 'group', label: 'Administration', children: [
    { key: '/reports', icon: <BarChartOutlined />, label: 'Reports' },
    { key: '/users', icon: <UsergroupAddOutlined />, label: 'Users', admin: true },
  ] },
];

function Brand() {
  return (
    <div className="brand">
      <div className="brand-logo">✚</div>
      <div>
        <div className="brand-name">HealthHarbor</div>
        <div className="brand-sub">Rural Health & Sanitation</div>
      </div>
    </div>
  );
}

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const screens = Grid.useBreakpoint();
  const [drawer, setDrawer] = useState(false);
  const isMobile = !screens.lg;

  const items = NAV.map((g) => ({ ...g, children: g.children.filter((c) => !c.admin || user?.role === 'ADMIN').map(({ admin, ...c }) => c) })); // eslint-disable-line no-unused-vars
  const selected = '/' + (location.pathname.split('/')[1] || '');

  const menu = (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={[selected]}
      items={items}
      onClick={({ key }) => {
        navigate(key);
        setDrawer(false);
      }}
    />
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {!isMobile && (
        <Sider width={248} style={{ position: 'sticky', top: 0, height: '100vh', overflow: 'auto' }}>
          <Brand />
          {menu}
        </Sider>
      )}
      <Drawer open={drawer} onClose={() => setDrawer(false)} placement="left" width={260} styles={{ body: { padding: 0, background: '#0b3b37' }, header: { display: 'none' } }}>
        <Brand />
        {menu}
      </Drawer>
      <Layout>
        <Header className="app-header">
          <Space>
            {isMobile && <Button type="text" icon={<MenuOutlined />} onClick={() => setDrawer(true)} />}
            <Typography.Text type="secondary" className="header-context">Anandpur District · Rampur Block</Typography.Text>
          </Space>
          <Dropdown
            menu={{ items: [
              { key: 'role', label: <span>Signed in as <b>{humanize(user?.role)}</b></span>, disabled: true },
              { type: 'divider' },
              { key: 'logout', icon: <LogoutOutlined />, label: 'Sign out', onClick: () => { logout(); navigate('/login'); } },
            ] }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Badge dot color="#22c55e" offset={[-4, 28]}>
                <Avatar style={{ background: '#0f766e' }} icon={<UserOutlined />} />
              </Badge>
              {!isMobile && (
                <div style={{ lineHeight: 1.2 }}>
                  <div style={{ fontWeight: 600 }}>{user?.fullName}</div>
                  <Tag color="cyan" style={{ marginTop: 2, fontSize: 11 }}>{humanize(user?.role)}</Tag>
                </div>
              )}
            </Space>
          </Dropdown>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
