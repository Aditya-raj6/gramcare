import { useEffect, useState } from 'react';
import { Avatar, Card, Col, Descriptions, Row, Space, Table, Tabs, Tag, Typography } from 'antd';
import { TeamOutlined, UserOutlined } from '@ant-design/icons';
import api from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatusTag from '../components/StatusTag';
import { E, VILLAGES, humanize } from '../utils/constants';
import { fmtDate, fmtTime, yesNo } from '../utils/format';

export const residentFields = [
  { name: 'firstName', label: 'First name', required: true },
  { name: 'lastName', label: 'Last name' },
  { name: 'gender', label: 'Gender', type: 'select', options: E.gender, required: true },
  { name: 'dateOfBirth', label: 'Date of birth', type: 'date' },
  { name: 'village', label: 'Village', type: 'select', options: VILLAGES, required: true },
  { name: 'householdNumber', label: 'Household no.' },
  { name: 'address', label: 'Address', span: 24 },
  { name: 'phone', label: 'Phone' },
  { name: 'aadhaarLast4', label: 'Aadhaar (stored as last 4 digits)', help: 'Only the last four digits are retained' },
  { name: 'bloodGroup', label: 'Blood group', type: 'select', options: E.bloodGroup.map((b) => ({ value: b, label: b })) },
  { name: 'status', label: 'Status', type: 'select', options: E.residentStatus },
  { name: 'heightCm', label: 'Height', type: 'number', unit: 'cm' },
  { name: 'weightKg', label: 'Weight', type: 'number', unit: 'kg' },
  { name: 'chronicConditions', label: 'Chronic conditions', type: 'textarea', span: 24, rows: 2 },
  { name: 'allergies', label: 'Allergies', type: 'textarea', rows: 2 },
  { name: 'currentMedications', label: 'Current medications', type: 'textarea', rows: 2 },
  { name: 'pregnant', label: 'Currently pregnant', type: 'switch', span: 6 },
  { name: 'disability', label: 'Person with disability', type: 'switch', span: 6 },
  { name: 'emergencyContactName', label: 'Emergency contact', span: 6 },
  { name: 'emergencyContactPhone', label: 'Emergency phone', span: 6 },
];

const visitFields = [
  { name: 'residentId', label: 'Resident', hidden: true },
  { name: 'visitDate', label: 'Visit date', type: 'date', required: true },
  { name: 'visitType', label: 'Visit type', type: 'select', options: E.visitType, required: true },
  { name: 'healthCentreId', label: 'Health centre', type: 'remote', endpoint: '/health-centres', onPick: (c, f) => f.setFieldValue('healthCentreName', c?.name) },
  { name: 'healthCentreName', label: 'Centre name', hidden: true },
  { name: 'attendedBy', label: 'Attended by' },
  { name: 'reason', label: 'Reason' },
  { name: 'diagnosis', label: 'Diagnosis' },
  { name: 'bloodPressure', label: 'BP (mmHg)', span: 6 },
  { name: 'temperatureC', label: 'Temp', type: 'number', unit: '°C', span: 6 },
  { name: 'pulse', label: 'Pulse', type: 'number', span: 6 },
  { name: 'weightKg', label: 'Weight', type: 'number', unit: 'kg', span: 6 },
  { name: 'notes', label: 'Notes', type: 'textarea', span: 24 },
];

const recordFields = [
  { name: 'residentId', label: 'Resident', hidden: true },
  { name: 'recordType', label: 'Record type', type: 'select', options: E.recordType, required: true },
  { name: 'recordDate', label: 'Date', type: 'date' },
  { name: 'title', label: 'Title', required: true, span: 24 },
  { name: 'description', label: 'Details', type: 'textarea', span: 24, rows: 4 },
  { name: 'recordedBy', label: 'Recorded by' },
  { name: 'healthCentreName', label: 'Health centre' },
];

function ResidentDetail({ resident }) {
  const [appointments, setAppointments] = useState([]);
  useEffect(() => {
    api.get('/appointments', { params: { residentId: resident.id } }).then(({ data }) => setAppointments(data)).catch(() => setAppointments([]));
  }, [resident.id]);

  return (
    <>
      <Card style={{ marginBottom: 16, background: '#f6fbfa' }}>
        <Space size={16} align="start">
          <Avatar size={56} style={{ background: '#0f766e' }} icon={<UserOutlined />} />
          <div>
            <Typography.Title level={4} style={{ margin: 0 }}>{resident.fullName}</Typography.Title>
            <Space wrap style={{ marginTop: 4 }}>
              <Tag>{resident.residentCode}</Tag>
              <span>{humanize(resident.gender)}{resident.age != null ? `, ${resident.age} yrs` : ''}</span>
              <span className="muted">· {resident.village}</span>
              <StatusTag value={resident.status} />
              {resident.pregnant && <Tag color="magenta">Pregnant</Tag>}
              {resident.chronicConditions && <Tag color="volcano">Chronic care</Tag>}
            </Space>
          </div>
        </Space>
      </Card>
      <Tabs items={[
        { key: 'profile', label: 'Profile', children: (
          <Descriptions column={{ xs: 1, md: 2 }} size="small" bordered>
            <Descriptions.Item label="Date of birth">{fmtDate(resident.dateOfBirth)}</Descriptions.Item>
            <Descriptions.Item label="Blood group">{resident.bloodGroup || '—'}</Descriptions.Item>
            <Descriptions.Item label="Phone">{resident.phone || '—'}</Descriptions.Item>
            <Descriptions.Item label="Aadhaar">{resident.aadhaarLast4 ? `XXXX-XXXX-${resident.aadhaarLast4}` : '—'}</Descriptions.Item>
            <Descriptions.Item label="Household">{resident.householdNumber || '—'}</Descriptions.Item>
            <Descriptions.Item label="Address">{resident.address || '—'}</Descriptions.Item>
            <Descriptions.Item label="Height / Weight">{resident.heightCm || '—'} cm / {resident.weightKg || '—'} kg</Descriptions.Item>
            <Descriptions.Item label="Disability">{yesNo(resident.disability)}</Descriptions.Item>
            <Descriptions.Item label="Chronic conditions" span={2}>{resident.chronicConditions || 'None recorded'}</Descriptions.Item>
            <Descriptions.Item label="Allergies" span={2}>{resident.allergies || 'None known'}</Descriptions.Item>
            <Descriptions.Item label="Current medications" span={2}>{resident.currentMedications || '—'}</Descriptions.Item>
            <Descriptions.Item label="Emergency contact" span={2}>{resident.emergencyContactName || '—'} {resident.emergencyContactPhone}</Descriptions.Item>
          </Descriptions>
        ) },
        { key: 'visits', label: 'Visit history', children: (
          <CrudPage endpoint="/visits" entityName="Visit" permission="clinical" params={{ residentId: resident.id }} fields={visitFields} pageSize={5}
            defaults={{ visitType: 'OPD' }}
            columns={[col.date('Date', 'visitDate'), col.enumText('Type', 'visitType'), col.text('Centre', 'healthCentreName'), col.text('Diagnosis', 'diagnosis'),
              col.text('BP', 'bloodPressure', { width: 90 }), col.text('By', 'attendedBy')]} />
        ) },
        { key: 'records', label: 'Medical records', children: (
          <CrudPage endpoint="/medical-records" entityName="Medical record" permission="clinical" params={{ residentId: resident.id }} fields={recordFields} pageSize={5}
            columns={[col.date('Date', 'recordDate'), col.status('Type', 'recordType', { render: (v) => <Tag color="blue">{humanize(v)}</Tag> }),
              col.text('Title', 'title'), col.text('Details', 'description'), col.text('By', 'recordedBy')]} />
        ) },
        { key: 'appointments', label: `Appointments (${appointments.length})`, children: (
          <Table size="small" rowKey="id" dataSource={appointments} pagination={{ pageSize: 5 }} scroll={{ x: 'max-content' }} columns={[
            { title: 'No.', dataIndex: 'appointmentNumber' }, { title: 'Date', dataIndex: 'appointmentDate', render: fmtDate },
            { title: 'Time', dataIndex: 'appointmentTime', render: fmtTime }, { title: 'Type', dataIndex: 'type', render: humanize },
            { title: 'Centre', dataIndex: 'healthCentreName' }, { title: 'Status', dataIndex: 'status', render: (v) => <StatusTag value={v} /> },
          ]} />
        ) },
      ]} />
    </>
  );
}

export default function ResidentsPage() {
  return (
    <>
      <PageHeader icon={<TeamOutlined />} title="Residents & Patients" subtitle="Resident profiles, basic medical information, visit history and healthcare records" />
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <CrudPage
            endpoint="/residents" entityName="Resident" permission="clinical" fields={residentFields} formWidth={860}
            searchKeys={['fullName', 'residentCode', 'phone', 'village', 'householdNumber']}
            filters={[{ name: 'village', label: 'Village', options: VILLAGES.map((v) => ({ value: v, label: v })) }, { name: 'gender', label: 'Gender', options: E.gender }, { name: 'status', label: 'Status', options: E.residentStatus }]}
            defaults={{ status: 'ACTIVE', pregnant: false, disability: false }}
            detail={(r) => <ResidentDetail resident={r} />}
            columns={[
              col.text('Code', 'residentCode', { width: 110 }),
              { title: 'Name', dataIndex: 'fullName', render: (v, r) => <div><div style={{ fontWeight: 500 }}>{v}</div><div className="muted" style={{ fontSize: 12 }}>{humanize(r.gender)}{r.age != null ? `, ${r.age} yrs` : ''}</div></div>, sorter: (a, b) => a.fullName.localeCompare(b.fullName) },
              col.text('Village', 'village'),
              col.text('Phone', 'phone'),
              col.text('Blood', 'bloodGroup', { width: 70 }),
              { title: 'Conditions', dataIndex: 'chronicConditions', ellipsis: true, render: (v, r) => <Space size={4} wrap>{r.pregnant && <Tag color="magenta">Pregnant</Tag>}{v ? <span>{v}</span> : <span className="muted">—</span>}</Space> },
              col.status('Status', 'status', { width: 110 }),
            ]}
          />
        </Col>
      </Row>
    </>
  );
}
