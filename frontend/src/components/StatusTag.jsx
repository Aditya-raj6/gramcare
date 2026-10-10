import { Tag } from 'antd';
import { CheckCircleFilled, ClockCircleFilled, ExclamationCircleFilled, InfoCircleFilled, MinusCircleFilled, WarningFilled } from '@ant-design/icons';
import { humanize, toneOf } from '../utils/constants';

const STYLE = {
  good: { color: 'success', icon: <CheckCircleFilled /> },
  info: { color: 'processing', icon: <ClockCircleFilled /> },
  warning: { color: 'warning', icon: <InfoCircleFilled /> },
  serious: { color: 'orange', icon: <WarningFilled /> },
  critical: { color: 'error', icon: <ExclamationCircleFilled /> },
  neutral: { color: 'default', icon: <MinusCircleFilled /> },
};

/** Status chip: colour + icon + label, so state is never conveyed by colour alone. */
export default function StatusTag({ value, tone }) {
  if (value === null || value === undefined || value === '') return <span style={{ color: '#999' }}>—</span>;
  const s = STYLE[tone || toneOf(value)];
  return (
    <Tag color={s.color} icon={s.icon} style={{ marginInlineEnd: 0 }}>
      {humanize(value)}
    </Tag>
  );
}
