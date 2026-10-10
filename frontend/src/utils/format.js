import dayjs from 'dayjs';

export const fmtDate = (v) => (v ? dayjs(v).format('DD MMM YYYY') : '—');
export const fmtDateTime = (v) => (v ? dayjs(v).format('DD MMM YYYY, HH:mm') : '—');
export const fmtTime = (v) => (v ? String(v).slice(0, 5) : '—');
export const fmtNumber = (v) => (v === null || v === undefined ? '—' : Number(v).toLocaleString('en-IN'));
export const fmtMoney = (v) => (v === null || v === undefined ? '—' : `₹${Number(v).toLocaleString('en-IN')}`);
export const yesNo = (v) => (v === true ? 'Yes' : v === false ? 'No' : '—');

/** Converts a { key: count } map into chart rows. */
export const toRows = (map, labeler = (k) => k) =>
  Object.entries(map || {}).map(([key, value]) => ({ key, name: labeler(key), value: Number(value) || 0 }));

/** Downloads rows as CSV using the given columns ({ title, dataIndex }). */
export function exportCsv(filename, columns, rows) {
  const cols = columns.filter((c) => c.dataIndex && typeof c.dataIndex === 'string');
  const esc = (v) => {
    const s = v === null || v === undefined ? '' : typeof v === 'object' ? JSON.stringify(v) : String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
  };
  const lines = [cols.map((c) => esc(typeof c.title === 'string' ? c.title : c.dataIndex)).join(',')];
  rows.forEach((r) => lines.push(cols.map((c) => esc(r[c.dataIndex])).join(',')));
  const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `${filename}-${dayjs().format('YYYYMMDD')}.csv`;
  a.click();
  URL.revokeObjectURL(a.href);
}
