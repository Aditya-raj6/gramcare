import { Card } from 'antd';

/** KPI tile: one headline number with a label and optional context line. */
export default function StatCard({ title, value, icon, color = '#0f766e', footer, onClick }) {
  return (
    <Card className="stat-card" hoverable={!!onClick} onClick={onClick} styles={{ body: { padding: 18 } }}>
      <div className="stat-card-row">
        <div>
          <div className="stat-card-title">{title}</div>
          <div className="stat-card-value">{value ?? '—'}</div>
        </div>
        {icon && (
          <div className="stat-card-icon" style={{ color, background: `${color}14` }}>
            {icon}
          </div>
        )}
      </div>
      {footer && <div className="stat-card-footer">{footer}</div>}
    </Card>
  );
}
