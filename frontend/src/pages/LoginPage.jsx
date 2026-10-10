import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Form, Input, Typography } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { useAuth } from '../auth/AuthContext';
import { errorMessage } from '../api/client';

const DEMO = [
  { role: 'District Health Officer (Admin)', u: 'admin', p: 'admin123' },
  { role: 'Doctor', u: 'doctor', p: 'doctor123' },
  { role: 'ASHA / Health Worker', u: 'worker', p: 'worker123' },
  { role: 'Pharmacist', u: 'pharmacist', p: 'pharma123' },
  { role: 'Sanitation Officer', u: 'sanitation', p: 'sanit123' },
];

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  if (user) return <Navigate to="/" replace />;

  const submit = async ({ username, password }) => {
    setLoading(true);
    setError(null);
    try {
      await login(username, password);
      navigate('/');
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-hero">
        <div className="brand-logo big">✚</div>
        <h1>HealthHarbor</h1>
        <p>Integrated Rural Health &amp; Sanitation Management for primary healthcare, sanitation and public-health teams.</p>
        <ul>
          <li>Residents, health centres, appointments &amp; consultations</li>
          <li>Medicine inventory with automatic replenishment</li>
          <li>Health camps, sanitation facilities &amp; water-quality monitoring</li>
          <li>One consolidated view of every pending action</li>
        </ul>
      </div>
      <div className="login-panel">
        <Card className="login-card" bordered={false}>
          <Typography.Title level={3} style={{ marginTop: 0 }}>Sign in</Typography.Title>
          <Typography.Paragraph type="secondary">Use your department credentials.</Typography.Paragraph>
          {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}
          <Form form={form} layout="vertical" onFinish={submit} requiredMark={false}>
            <Form.Item name="username" label="Username" rules={[{ required: true }]}>
              <Input size="large" prefix={<UserOutlined />} autoComplete="username" />
            </Form.Item>
            <Form.Item name="password" label="Password" rules={[{ required: true }]}>
              <Input.Password size="large" prefix={<LockOutlined />} autoComplete="current-password" />
            </Form.Item>
            <Button type="primary" htmlType="submit" size="large" block loading={loading}>Sign in</Button>
          </Form>
          <div className="demo-accounts">
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>Demo accounts — click to fill</Typography.Text>
            {DEMO.map((d) => (
              <button type="button" key={d.u} className="demo-account" onClick={() => form.setFieldsValue({ username: d.u, password: d.p })}>
                <span>{d.role}</span>
                <code>{d.u} / {d.p}</code>
              </button>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
