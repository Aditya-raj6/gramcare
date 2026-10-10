import { useEffect, useState } from 'react';
import { Button, Col, DatePicker, Descriptions, Divider, Form, Input, InputNumber, Modal, Row, Select, Space, Table, Tabs, Tag, Tooltip, message } from 'antd';
import { CalendarOutlined, MedicineBoxOutlined, MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import api, { errorMessage } from '../api/client';
import CrudPage, { col } from '../components/CrudPage';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import StatusTag from '../components/StatusTag';
import { useAuth } from '../auth/AuthContext';
import { E, humanize, opts } from '../utils/constants';
import { fmtDate, fmtTime } from '../utils/format';

const NEXT = {
  SCHEDULED: ['CHECKED_IN', 'CANCELLED', 'NO_SHOW'],
  CHECKED_IN: ['CANCELLED', 'SCHEDULED'],
  NO_SHOW: ['SCHEDULED'],
  CANCELLED: ['SCHEDULED'],
  COMPLETED: [],
};

const appointmentFields = [
  { name: 'residentId', label: 'Resident / patient', type: 'remote', endpoint: '/residents', labelOf: (r) => `${r.fullName} · ${r.residentCode} · ${r.village}`, required: true, span: 24 },
  { name: 'healthCentreId', label: 'Health centre', type: 'remote', endpoint: '/health-centres', required: true },
  { name: 'workerId', label: 'Doctor / health worker', type: 'remote', endpoint: '/health-workers', labelOf: (w) => `${w.name} (${humanize(w.role)}) · ${w.centreName}` },
  { name: 'appointmentDate', label: 'Date', type: 'date', required: true, disabledDate: (d) => d && d.isBefore(dayjs().startOf('day')) },
  { name: 'appointmentTime', label: 'Time', type: 'time', required: true },
  { name: 'type', label: 'Type', type: 'select', options: E.appointmentType, required: true },
  { name: 'priority', label: 'Priority', type: 'select', options: E.appointmentPriority },
  { name: 'reason', label: 'Reason for visit', type: 'textarea', span: 24, rows: 2 },
  { name: 'notes', label: 'Notes', type: 'textarea', span: 24, rows: 2 },
];

function ConsultationModal({ appointment, onClose, onDone }) {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const { user } = useAuth();

  const submit = async () => {
    const v = await form.validateFields();
    setSaving(true);
    try {
      const payload = { ...v, appointmentId: appointment.id, followUpDate: v.followUpDate ? v.followUpDate.format('YYYY-MM-DD') : null };
      const { data } = await api.post('/consultations', payload);
      message.success(`Consultation saved${data.followUpBooked ? ' · follow-up appointment booked' : ''} · visit added to patient history`);
      onDone();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal open title={`Consultation · ${appointment.residentName} (${appointment.appointmentNumber})`} width={900} okText="Save consultation"
      confirmLoading={saving} onOk={submit} onCancel={onClose} destroyOnClose maskClosable={false}>
      <Form form={form} layout="vertical" preserve={false} initialValues={{ doctorName: appointment.workerName || user?.fullName, treatments: [{ treatmentType: 'MEDICATION' }] }}>
        <Row gutter={12}>
          <Col xs={24} md={12}><Form.Item name="symptoms" label="Presenting symptoms"><Input.TextArea rows={2} /></Form.Item></Col>
          <Col xs={24} md={12}><Form.Item name="diagnosis" label="Diagnosis" rules={[{ required: true }]}><Input.TextArea rows={2} /></Form.Item></Col>
          <Col xs={12} md={5}><Form.Item name="bloodPressure" label="BP (mmHg)"><Input placeholder="120/80" /></Form.Item></Col>
          <Col xs={12} md={5}><Form.Item name="temperatureC" label="Temp (°C)"><InputNumber step={0.1} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={8} md={4}><Form.Item name="pulse" label="Pulse"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={8} md={5}><Form.Item name="spo2" label="SpO₂ (%)"><InputNumber max={100} style={{ width: '100%' }} /></Form.Item></Col>
          <Col xs={8} md={5}><Form.Item name="weightKg" label="Weight (kg)"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
        </Row>
        <Divider orientation="left" plain style={{ margin: '4px 0 12px' }}>Treatment plan</Divider>
        <Form.List name="treatments">
          {(fields, { add, remove }) => (
            <>
              {fields.map((f) => (
                <Row gutter={8} key={f.key} align="middle">
                  <Col xs={24} md={4}><Form.Item name={[f.name, 'treatmentType']}><Select options={opts(E.treatmentType)} /></Form.Item></Col>
                  <Col xs={24} md={6}><Form.Item name={[f.name, 'name']} rules={[{ required: true, message: 'Required' }]}><Input placeholder="Medicine / test / procedure" /></Form.Item></Col>
                  <Col xs={8} md={3}><Form.Item name={[f.name, 'dosage']}><Input placeholder="Dosage" /></Form.Item></Col>
                  <Col xs={8} md={3}><Form.Item name={[f.name, 'frequency']}><Input placeholder="Frequency" /></Form.Item></Col>
                  <Col xs={8} md={3}><Form.Item name={[f.name, 'durationDays']}><InputNumber placeholder="Days" style={{ width: '100%' }} /></Form.Item></Col>
                  <Col xs={20} md={4}><Form.Item name={[f.name, 'instructions']}><Input placeholder="Instructions" /></Form.Item></Col>
                  <Col xs={4} md={1}><Form.Item><MinusCircleOutlined onClick={() => remove(f.name)} style={{ color: '#d03b3b' }} /></Form.Item></Col>
                </Row>
              ))}
              <Button type="dashed" block icon={<PlusOutlined />} onClick={() => add({ treatmentType: 'MEDICATION' })} style={{ marginBottom: 16 }}>Add treatment line</Button>
            </>
          )}
        </Form.List>
        <Row gutter={12}>
          <Col xs={24} md={12}><Form.Item name="clinicalNotes" label="Clinical notes"><Input.TextArea rows={2} /></Form.Item></Col>
          <Col xs={24} md={12}><Form.Item name="advice" label="Advice to patient"><Input.TextArea rows={2} /></Form.Item></Col>
          <Col xs={24} md={12}><Form.Item name="doctorName" label="Consulting doctor"><Input /></Form.Item></Col>
          <Col xs={24} md={12}><Form.Item name="followUpDate" label="Follow-up date" tooltip="A follow-up appointment is booked automatically">
            <DatePicker style={{ width: '100%' }} format="DD MMM YYYY" disabledDate={(d) => d && !d.isAfter(dayjs(), 'day')} />
          </Form.Item></Col>
        </Row>
      </Form>
    </Modal>
  );
}

function ConsultationDetail({ c }) {
  return (
    <>
      <Descriptions bordered size="small" column={{ xs: 1, md: 2 }}>
        <Descriptions.Item label="Patient">{c.residentName}</Descriptions.Item>
        <Descriptions.Item label="Appointment">{c.appointmentNumber}</Descriptions.Item>
        <Descriptions.Item label="Date">{fmtDate(c.consultationDate)}</Descriptions.Item>
        <Descriptions.Item label="Doctor">{c.doctorName}</Descriptions.Item>
        <Descriptions.Item label="Centre" span={2}>{c.healthCentreName}</Descriptions.Item>
        <Descriptions.Item label="Symptoms" span={2}>{c.symptoms || '—'}</Descriptions.Item>
        <Descriptions.Item label="Diagnosis" span={2}><b>{c.diagnosis}</b></Descriptions.Item>
        <Descriptions.Item label="Vitals" span={2}>BP {c.bloodPressure || '—'} · Temp {c.temperatureC ?? '—'}°C · Pulse {c.pulse ?? '—'} · SpO₂ {c.spo2 ?? '—'}% · {c.weightKg ?? '—'} kg</Descriptions.Item>
        <Descriptions.Item label="Clinical notes" span={2}>{c.clinicalNotes || '—'}</Descriptions.Item>
        <Descriptions.Item label="Advice" span={2}>{c.advice || '—'}</Descriptions.Item>
        <Descriptions.Item label="Follow-up" span={2}>{c.followUpDate ? <>{fmtDate(c.followUpDate)} {c.followUpBooked && <Tag color="green">Appointment booked</Tag>}</> : '—'}</Descriptions.Item>
      </Descriptions>
      <Divider orientation="left">Treatment plan</Divider>
      <Table size="small" rowKey={(_, i) => i} pagination={false} dataSource={c.treatments || []} columns={[
        { title: 'Type', dataIndex: 'treatmentType', render: (v) => <Tag>{humanize(v)}</Tag> }, { title: 'Item', dataIndex: 'name' },
        { title: 'Dosage', dataIndex: 'dosage' }, { title: 'Frequency', dataIndex: 'frequency' }, { title: 'Days', dataIndex: 'durationDays' }, { title: 'Instructions', dataIndex: 'instructions' },
      ]} />
    </>
  );
}

export default function AppointmentsPage() {
  const { can } = useAuth();
  const [stats, setStats] = useState({});
  const [consulting, setConsulting] = useState(null);
  const [tick, setTick] = useState(0);
  const refresh = () => setTick((t) => t + 1);

  useEffect(() => {
    api.get('/appointments/stats').then(({ data }) => setStats(data)).catch(() => {});
  }, [tick]);

  return (
    <>
      <PageHeader icon={<CalendarOutlined />} title="Appointments & Consultations" subtitle="Schedule appointments, record consultations and treatment plans" />
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} md={6}><StatCard title="Today" value={stats.todayAppointments} footer="Appointments scheduled for today" /></Col>
        <Col xs={12} md={6}><StatCard title="Next 7 days" value={stats.upcoming7Days} color="#2a78d6" footer="Scheduled & awaiting" /></Col>
        <Col xs={12} md={6}><StatCard title="Consultations" value={stats.totalConsultations} color="#1baf7a" footer="Recorded to date" /></Col>
        <Col xs={12} md={6}><StatCard title="Missed" value={stats.byStatus?.NO_SHOW ?? 0} color="#d03b3b" footer={`${stats.byStatus?.CANCELLED ?? 0} cancelled`} /></Col>
      </Row>
      <Tabs className="module-tabs" items={[
        { key: 'appointments', label: 'Appointments', children: (
          <CrudPage endpoint="/appointments" entityName="Appointment" permission="clinical" fields={appointmentFields} refreshKey={tick} onChanged={refresh}
            searchKeys={['appointmentNumber', 'residentName', 'workerName', 'healthCentreName', 'village']}
            filters={[{ name: 'status', label: 'Status', options: E.appointmentStatus }, { name: 'type', label: 'Type', options: E.appointmentType }]}
            defaults={{ type: 'GENERAL', priority: 'ROUTINE', appointmentDate: dayjs().format('YYYY-MM-DD'), appointmentTime: '10:00:00' }}
            allowDelete={false}
            statusOptions={(r) => ({ url: `/appointments/${r.id}/status`, options: NEXT[r.status] })}
            rowActions={(r) => can('consultation') && ['SCHEDULED', 'CHECKED_IN'].includes(r.status) && (
              <Tooltip title="Record consultation"><Button size="small" type="primary" ghost icon={<MedicineBoxOutlined />} onClick={() => setConsulting(r)}>Consult</Button></Tooltip>
            )}
            columns={[
              col.text('No.', 'appointmentNumber', { width: 110 }),
              { title: 'Date & time', dataIndex: 'appointmentDate', render: (v, r) => <div><b>{fmtDate(v)}</b><div className="muted">{fmtTime(r.appointmentTime)}</div></div>, sorter: (a, b) => `${a.appointmentDate}${a.appointmentTime}`.localeCompare(`${b.appointmentDate}${b.appointmentTime}`) },
              { title: 'Patient', dataIndex: 'residentName', render: (v, r) => <div><div style={{ fontWeight: 500 }}>{v}</div><div className="muted" style={{ fontSize: 12 }}>{r.village}</div></div> },
              { title: 'Type', dataIndex: 'type', render: (v, r) => <Space size={4}><Tag>{humanize(v)}</Tag>{r.priority !== 'ROUTINE' && <StatusTag value={r.priority} />}</Space> },
              col.text('Centre', 'healthCentreName'), col.text('Doctor / worker', 'workerName'), col.text('Reason', 'reason'), col.status('Status', 'status'),
            ]} />
        ) },
        { key: 'consultations', label: 'Consultations', children: (
          <CrudPage endpoint="/consultations" entityName="Consultation" permission="consultation" refreshKey={tick} allowCreate={false} allowEdit={false} allowDelete={false}
            searchKeys={['residentName', 'diagnosis', 'doctorName', 'appointmentNumber']}
            detail={(c) => <ConsultationDetail c={c} />}
            columns={[col.date('Date', 'consultationDate'), col.text('Appointment', 'appointmentNumber', { width: 120 }), col.text('Patient', 'residentName'),
              col.text('Diagnosis', 'diagnosis'), col.text('Doctor', 'doctorName'), col.text('Centre', 'healthCentreName'),
              { title: 'Treatments', dataIndex: 'treatments', render: (t) => `${t?.length || 0} item(s)` }, col.date('Follow-up', 'followUpDate')]} />
        ) },
      ]} />
      {consulting && <ConsultationModal appointment={consulting} onClose={() => setConsulting(null)} onDone={() => { setConsulting(null); refresh(); }} />}
    </>
  );
}
