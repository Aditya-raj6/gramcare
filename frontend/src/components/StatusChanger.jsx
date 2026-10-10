import { Button, Dropdown, Input, Modal, message } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import { useState } from 'react';
import api, { errorMessage } from '../api/client';
import { humanize } from '../utils/constants';
import { useAuth } from '../auth/AuthContext';

/** Moves a record along its workflow via PATCH {url} with an optional note. */
export default function StatusChanger({ url, options, onDone, size = 'small', label = 'Update', disabled }) {
  const { user } = useAuth();
  const [pending, setPending] = useState(null);
  const [notes, setNotes] = useState('');
  const [assignedTo, setAssignedTo] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    setLoading(true);
    try {
      await api.patch(url.replace(/^\/api/, ''), { status: pending, notes: notes || null, assignedTo: assignedTo || null, updatedBy: user?.fullName });
      message.success(`Status updated to ${humanize(pending)}`);
      setPending(null);
      setNotes('');
      setAssignedTo('');
      onDone?.();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  if (!options?.length) return null;
  return (
    <>
      <Dropdown
        disabled={disabled}
        trigger={['click']}
        menu={{ items: options.map((o) => ({ key: o, label: humanize(o) })), onClick: ({ key }) => setPending(key) }}
      >
        <Button size={size}>
          {label} <DownOutlined />
        </Button>
      </Dropdown>
      <Modal
        open={!!pending}
        title={`Change status to "${humanize(pending)}"`}
        okText="Confirm"
        confirmLoading={loading}
        onOk={submit}
        onCancel={() => setPending(null)}
        destroyOnClose
      >
        {['ASSIGNED', 'IN_PROGRESS'].includes(pending) && (
          <Input placeholder="Assign to (optional)" value={assignedTo} onChange={(e) => setAssignedTo(e.target.value)} style={{ marginBottom: 12 }} />
        )}
        <Input.TextArea rows={3} placeholder="Notes / remarks (optional)" value={notes} onChange={(e) => setNotes(e.target.value)} />
      </Modal>
    </>
  );
}
