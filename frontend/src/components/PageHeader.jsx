import { Space, Typography } from 'antd';

export default function PageHeader({ title, subtitle, icon, extra }) {
  return (
    <div className="page-header">
      <Space align="center" size={14}>
        {icon && <div className="page-header-icon">{icon}</div>}
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {title}
          </Typography.Title>
          {subtitle && <Typography.Text type="secondary">{subtitle}</Typography.Text>}
        </div>
      </Space>
      {extra && <div>{extra}</div>}
    </div>
  );
}
