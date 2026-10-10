import { Card, Empty } from 'antd';
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { SERIES, STATUS_COLORS, toneOf } from '../utils/constants';

const AXIS = { fontSize: 12, fill: '#6b6b68' };
const GRID = '#ececea';

const tooltipStyle = { borderRadius: 8, border: '1px solid #e5e5e2', boxShadow: '0 4px 14px rgba(0,0,0,.08)', fontSize: 12 };

/** Single-series bar chart (magnitude) — one hue, thin rounded bars, recessive grid, hover tooltip. */
export function BarCard({ title, data, height = 260, color = SERIES[0], horizontal = false, extra, unit = '' }) {
  return (
    <Card title={title} extra={extra} className="chart-card" styles={{ body: { padding: '12px 16px 8px' } }}>
      {!data?.length ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} style={{ height: height - 40 }} />
      ) : (
        <ResponsiveContainer width="100%" height={height}>
          {horizontal ? (
            <BarChart data={data} layout="vertical" margin={{ left: 8, right: 24, top: 4, bottom: 4 }} barCategoryGap={6}>
              <CartesianGrid horizontal={false} stroke={GRID} />
              <XAxis type="number" tick={AXIS} axisLine={false} tickLine={false} allowDecimals={false} />
              <YAxis type="category" dataKey="name" tick={AXIS} axisLine={false} tickLine={false} width={150} />
              <Tooltip cursor={{ fill: 'rgba(0,0,0,.04)' }} contentStyle={tooltipStyle} formatter={(v) => [`${v}${unit}`, title]} />
              <Bar isAnimationActive={false} dataKey="value" fill={color} radius={[0, 4, 4, 0]} maxBarSize={18} />
            </BarChart>
          ) : (
            <BarChart data={data} margin={{ left: -12, right: 8, top: 8, bottom: 4 }}>
              <CartesianGrid vertical={false} stroke={GRID} />
              <XAxis dataKey="name" tick={AXIS} axisLine={{ stroke: GRID }} tickLine={false} interval={0} angle={data.length > 6 ? -25 : 0}
                textAnchor={data.length > 6 ? 'end' : 'middle'} height={data.length > 6 ? 60 : 30} />
              <YAxis tick={AXIS} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip cursor={{ fill: 'rgba(0,0,0,.04)' }} contentStyle={tooltipStyle} formatter={(v) => [`${v}${unit}`, title]} />
              <Bar isAnimationActive={false} dataKey="value" fill={color} radius={[4, 4, 0, 0]} maxBarSize={36} />
            </BarChart>
          )}
        </ResponsiveContainer>
      )}
    </Card>
  );
}

/**
 * Part-to-whole donut. Categorical slots in fixed order; with useStatus the reserved status
 * colours are used instead. Legend is always shown so identity never relies on colour alone.
 */
export function DonutCard({ title, data, height = 260, useStatus = false, extra }) {
  const total = (data || []).reduce((s, d) => s + d.value, 0);
  return (
    <Card title={title} extra={extra} className="chart-card" styles={{ body: { padding: '12px 16px 8px' } }}>
      {!total ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} style={{ height: height - 40 }} />
      ) : (
        <ResponsiveContainer width="100%" height={height}>
          <PieChart>
            <Pie isAnimationActive={false} data={data} dataKey="value" nameKey="name" cx="40%" innerRadius="58%" outerRadius="82%" paddingAngle={1.5} stroke="#fff" strokeWidth={2}>
              {data.map((d, i) => (
                <Cell key={d.key || d.name} fill={useStatus ? STATUS_COLORS[toneOf(d.key)] : SERIES[i % SERIES.length]} />
              ))}
            </Pie>
            <Tooltip contentStyle={tooltipStyle} formatter={(v, n) => [`${v} (${Math.round((v / total) * 100)}%)`, n]} />
            <Legend iconType="circle" iconSize={8} layout="vertical" align="right" verticalAlign="middle"
              formatter={(v, entry) => <span style={{ color: '#3d3d3a', fontSize: 12 }}>{v} · {entry.payload.value}</span>} />
            <text x="40%" y="50%" textAnchor="middle" dominantBaseline="middle" style={{ fontSize: 22, fontWeight: 600, fill: '#1f1f1d' }}>
              {total}
            </text>
          </PieChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
