import { Result } from 'antd';
import { UsergroupAddOutlined } from '@ant-design/icons';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatusTag from '../components/StatusTag';
import { useAuth } from '../auth/AuthContext';
import { ROLES, VILLAGES, humanize } from '../utils/constants';

export default function UsersPage() {
  const { user } = useAuth();
  if (user?.role !== 'ADMIN') return <Result status="403" title="Administrators only" subTitle="You do not have access to user management." />;
  return (
    <>
      <PageHeader icon={<UsergroupAddOutlined />} title="User Management" subtitle="Accounts and role-based access for healthcare and sanitation personnel" />
      <CrudPage endpoint="/users" entityName="User" permission="users" searchKeys={['username', 'fullName', 'email', 'assignedVillage']}
        defaults={{ role: 'HEALTH_WORKER', active: true }}
        beforeSubmit={(v) => ({ ...v, password: v.password || null })}
        fields={[{ name: 'username', label: 'Username', required: true }, { name: 'password', label: 'Password', help: 'Leave blank to keep the current password when editing', rules: [{ min: 6, message: 'At least 6 characters' }] },
          { name: 'fullName', label: 'Full name', required: true }, { name: 'role', label: 'Role', type: 'select', options: ROLES, required: true },
          { name: 'email', label: 'Email', rules: [{ type: 'email' }] }, { name: 'phone', label: 'Phone' },
          { name: 'assignedVillage', label: 'Assigned village', type: 'select', options: VILLAGES },
          { name: 'healthCentreId', label: 'Health centre', type: 'remote', endpoint: '/health-centres' }, { name: 'active', label: 'Active', type: 'switch' }]}
        columns={[col.text('Username', 'username'), col.text('Name', 'fullName'), { title: 'Role', dataIndex: 'role', render: (v) => humanize(v) },
          col.text('Email', 'email'), col.text('Village', 'assignedVillage'), { title: 'Status', dataIndex: 'active', render: (v) => <StatusTag value={v ? 'ACTIVE' : 'INACTIVE'} /> }]} />
    </>
  );
}
