import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Card, Col, DatePicker, Drawer, Form, Input, InputNumber, Modal, Popconfirm, Row, Select, Space, Switch, Table, TimePicker, Tooltip, message } from 'antd';
import { DeleteOutlined, DownloadOutlined, EditOutlined, EyeOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import api, { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { humanize, opts } from '../utils/constants';
import { exportCsv, fmtDate, fmtDateTime, fmtTime } from '../utils/format';
import RemoteSelect, { invalidateRemote } from './RemoteSelect';
import StatusTag from './StatusTag';
import StatusChanger from './StatusChanger';

/* ------------------------------------------------------------------ form helpers */

export function toFormValues(record, fields) {
  const v = { ...record };
  fields.forEach((f) => {
    if (v[f.name] === null || v[f.name] === undefined) return;
    if (f.type === 'date') v[f.name] = dayjs(v[f.name]);
    if (f.type === 'time') v[f.name] = dayjs(`2000-01-01T${v[f.name]}`);
  });
  return v;
}

export function fromFormValues(values, fields) {
  const v = { ...values };
  fields.forEach((f) => {
    if (v[f.name] && f.type === 'date') v[f.name] = v[f.name].format('YYYY-MM-DD');
    if (v[f.name] && f.type === 'time') v[f.name] = v[f.name].format('HH:mm:ss');
    if (v[f.name] === '') v[f.name] = null;
  });
  return v;
}

/** Renders one configured form field. Field config: { name, label, type, options, required, span, ... } */
export function FieldInput({ field, form }) {
  const common = { placeholder: field.placeholder, disabled: field.disabled };
  switch (field.type) {
    case 'number':
      return <InputNumber style={{ width: '100%' }} min={field.min} max={field.max} step={field.step} addonAfter={field.unit} {...common} />;
    case 'textarea':
      return <Input.TextArea rows={field.rows || 3} {...common} />;
    case 'select':
      return <Select allowClear showSearch optionFilterProp="label" options={field.options?.[0]?.value !== undefined ? field.options : opts(field.options || [])} mode={field.mode} {...common} />;
    case 'date':
      return <DatePicker style={{ width: '100%' }} format="DD MMM YYYY" disabledDate={field.disabledDate} {...common} />;
    case 'time':
      return <TimePicker style={{ width: '100%' }} format="HH:mm" minuteStep={5} {...common} />;
    case 'switch':
      return <Switch />;
    case 'remote':
      return (
        <RemoteSelect
          endpoint={field.endpoint}
          labelOf={field.labelOf}
          params={field.params}
          placeholder={field.placeholder || `Select ${field.label.toLowerCase()}`}
          disabled={field.disabled}
          onPick={(item) => field.onPick?.(item, form)}
        />
      );
    default:
      return <Input {...common} />;
  }
}

export function FormFields({ fields, form }) {
  return (
    <Row gutter={16}>
      {fields.filter((f) => !f.hidden).map((f) => (
        <Col key={f.name} xs={24} md={f.span || 12}>
          <Form.Item
            name={f.name}
            label={f.label}
            valuePropName={f.type === 'switch' ? 'checked' : 'value'}
            rules={[...(f.required ? [{ required: true, message: `${f.label} is required` }] : []), ...(f.rules || [])]}
            tooltip={f.help}
          >
            <FieldInput field={f} form={form} />
          </Form.Item>
        </Col>
      ))}
      {fields.filter((f) => f.hidden).map((f) => (
        <Form.Item key={f.name} name={f.name} hidden>
          <Input />
        </Form.Item>
      ))}
    </Row>
  );
}

/* ------------------------------------------------------------- column helpers */

export const col = {
  text: (title, dataIndex, extra = {}) => ({ title, dataIndex, ellipsis: true, ...extra }),
  date: (title, dataIndex, extra = {}) => ({ title, dataIndex, render: fmtDate, width: 120, sorter: (a, b) => String(a[dataIndex] || '').localeCompare(String(b[dataIndex] || '')), ...extra }),
  dateTime: (title, dataIndex, extra = {}) => ({ title, dataIndex, render: fmtDateTime, width: 160, ...extra }),
  time: (title, dataIndex) => ({ title, dataIndex, render: fmtTime, width: 80 }),
  status: (title, dataIndex, extra = {}) => ({ title, dataIndex, render: (v) => <StatusTag value={v} />, width: 150, ...extra }),
  enumText: (title, dataIndex, extra = {}) => ({ title, dataIndex, render: (v) => humanize(v), ...extra }),
  number: (title, dataIndex, extra = {}) => ({ title, dataIndex, align: 'right', width: 110, sorter: (a, b) => (a[dataIndex] || 0) - (b[dataIndex] || 0), ...extra }),
};

/* ------------------------------------------------------------------- CrudPage */

/**
 * Configuration-driven list + create/edit/delete screen backed by a CrudController endpoint.
 *
 * Props: endpoint, entityName, columns, fields, permission, filters, searchKeys, params,
 *        statusUrl(record) / statusOptions(record), rowActions(record, reload), detail(record),
 *        defaults, beforeSubmit(values, editing), allowEdit, allowDelete, allowCreate, title, extra.
 */
export default function CrudPage({
  endpoint, entityName, columns, fields = [], permission, filters = [], searchKeys = [], params,
  statusOptions, rowActions, detail, defaults = {}, beforeSubmit, allowEdit = true, allowDelete = true,
  allowCreate = true, title, extra, refreshKey, onChanged, rowKey = 'id', pageSize = 10, formWidth = 760,
}) {
  const { can } = useAuth();
  const writable = permission ? can(permission) : true;
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [filterValues, setFilterValues] = useState({});
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get(endpoint, { params: { ...params, ...filterValues } });
      setRows(data);
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [endpoint, JSON.stringify(params), JSON.stringify(filterValues)]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    load();
  }, [load, refreshKey]);

  const changed = () => {
    invalidateRemote();
    load();
    onChanged?.();
  };

  const visible = useMemo(() => {
    if (!search) return rows;
    const q = search.toLowerCase();
    return rows.filter((r) => searchKeys.some((k) => String(r[k] ?? '').toLowerCase().includes(q)));
  }, [rows, search, searchKeys]);

  const [initial, setInitial] = useState({});

  const openCreate = () => {
    setInitial(toFormValues({ ...defaults, ...params }, fields));
    setEditing({});
  };

  const openEdit = (record) => {
    setInitial(toFormValues(record, fields));
    setEditing(record);
  };

  const submit = async () => {
    const values = await form.validateFields();
    let payload = fromFormValues(values, fields);
    if (beforeSubmit) payload = beforeSubmit(payload, editing);
    setSaving(true);
    try {
      if (editing?.id) {
        await api.put(`${endpoint}/${editing.id}`, payload);
        message.success(`${entityName} updated`);
      } else {
        await api.post(endpoint, payload);
        message.success(`${entityName} created`);
      }
      setEditing(null);
      changed();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (record) => {
    try {
      await api.delete(`${endpoint}/${record.id}`);
      message.success(`${entityName} deleted`);
      changed();
    } catch (e) {
      message.error(errorMessage(e));
    }
  };

  const actionColumn = {
    title: 'Actions',
    key: '__actions',
    fixed: 'right',
    width: 1,
    render: (_, record) => (
      <Space size={4} wrap={false}>
        {detail && (
          <Tooltip title="View details">
            <Button size="small" type="text" icon={<EyeOutlined />} onClick={() => setViewing(record)} />
          </Tooltip>
        )}
        {writable && statusOptions && (() => {
          const s = statusOptions(record);
          return s && s.options?.length ? <StatusChanger url={s.url} options={s.options} onDone={changed} label="Status" /> : null;
        })()}
        {rowActions && rowActions(record, changed, writable)}
        {writable && allowEdit && (
          <Tooltip title="Edit">
            <Button size="small" type="text" icon={<EditOutlined />} onClick={() => openEdit(record)} />
          </Tooltip>
        )}
        {writable && allowDelete && (
          <Popconfirm title={`Delete this ${entityName.toLowerCase()}?`} okButtonProps={{ danger: true }} onConfirm={() => remove(record)}>
            <Button size="small" type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        )}
      </Space>
    ),
  };

  const tableColumns = [...columns, actionColumn];

  return (
    <Card className="crud-card" title={title} styles={{ body: { paddingTop: title ? 12 : 20 } }}>
      <div className="crud-toolbar">
        <Space wrap>
          {searchKeys.length > 0 && (
            <Input allowClear prefix={<SearchOutlined />} placeholder="Search…" value={search} onChange={(e) => setSearch(e.target.value)} style={{ width: 240 }} />
          )}
          {filters.map((f) => (
            <Select
              key={f.name}
              allowClear
              placeholder={f.label}
              style={{ minWidth: f.width || 160 }}
              options={f.options?.[0]?.value !== undefined ? f.options : opts(f.options || [])}
              value={filterValues[f.name]}
              onChange={(v) => setFilterValues((prev) => ({ ...prev, [f.name]: v }))}
            />
          ))}
        </Space>
        <Space wrap>
          {extra}
          <Tooltip title="Refresh">
            <Button icon={<ReloadOutlined />} onClick={load} />
          </Tooltip>
          <Tooltip title="Export CSV">
            <Button icon={<DownloadOutlined />} onClick={() => exportCsv(entityName.toLowerCase().replace(/\s+/g, '-'), columns, visible)} />
          </Tooltip>
          {writable && allowCreate && fields.length > 0 && (
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
              New {entityName}
            </Button>
          )}
        </Space>
      </div>
      <Table
        rowKey={rowKey}
        size="middle"
        loading={loading}
        columns={tableColumns}
        dataSource={visible}
        scroll={{ x: 'max-content' }}
        pagination={{ pageSize, showSizeChanger: true, showTotal: (t) => `${t} records` }}
      />
      <Modal
        open={!!editing}
        title={editing?.id ? `Edit ${entityName}` : `New ${entityName}`}
        width={formWidth}
        okText={editing?.id ? 'Save changes' : 'Create'}
        confirmLoading={saving}
        onOk={submit}
        onCancel={() => setEditing(null)}
        destroyOnClose
        maskClosable={false}
      >
        <Form form={form} layout="vertical" requiredMark="optional" preserve={false} initialValues={initial}>
          <FormFields fields={fields} form={form} />
        </Form>
      </Modal>
      {detail && (
        <Drawer open={!!viewing} width={Math.min(820, window.innerWidth)} title={`${entityName} details`} onClose={() => setViewing(null)} destroyOnClose>
          {viewing && detail(viewing, () => { changed(); })}
        </Drawer>
      )}
    </Card>
  );
}
