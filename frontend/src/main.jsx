import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App as AntApp, ConfigProvider } from 'antd';
import enGB from 'antd/locale/en_GB';
import dayjs from 'dayjs';
import 'dayjs/locale/en-gb';
import App from './App';
import { AuthProvider } from './auth/AuthContext';
import './styles.css';

dayjs.locale('en-gb');

const theme = {
  token: {
    colorPrimary: '#0f766e',
    colorInfo: '#0f766e',
    borderRadius: 8,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    colorBgLayout: '#f4f6f5',
  },
  components: {
    Layout: { siderBg: '#0b3b37', headerBg: '#ffffff', headerPadding: '0 24px' },
    Menu: { darkItemBg: '#0b3b37', darkSubMenuItemBg: '#082e2b', darkItemSelectedBg: '#14b8a6', darkItemColor: '#cfe7e3' },
    Card: { headerFontSize: 15 },
  },
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ConfigProvider theme={theme} locale={enGB}>
      <AntApp>
        <BrowserRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrowserRouter>
      </AntApp>
    </ConfigProvider>
  </React.StrictMode>,
);
