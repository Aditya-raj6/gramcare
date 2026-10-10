import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import api, { session } from '../api/client';

const AuthContext = createContext(null);

/** Write permissions mirror the API gateway's AccessPolicy (the gateway is the source of truth). */
const WRITE_ROLES = {
  clinical: ['ADMIN', 'DOCTOR', 'HEALTH_WORKER'],
  consultation: ['ADMIN', 'DOCTOR'],
  centres: ['ADMIN'],
  medicines: ['ADMIN', 'PHARMACIST'],
  inventory: ['ADMIN', 'PHARMACIST', 'DOCTOR', 'HEALTH_WORKER'],
  sanitation: ['ADMIN', 'SANITATION_OFFICER', 'HEALTH_WORKER'],
  users: ['ADMIN'],
  reports: ['ADMIN', 'DOCTOR', 'HEALTH_WORKER', 'PHARMACIST', 'SANITATION_OFFICER'],
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(session.user());

  const login = useCallback(async (username, password) => {
    const { data } = await api.post('/auth/login', { username, password });
    session.save(data.token, data.user);
    setUser(data.user);
    return data.user;
  }, []);

  const logout = useCallback(() => {
    session.clear();
    setUser(null);
  }, []);

  const can = useCallback((module) => !!user && (WRITE_ROLES[module] || []).includes(user.role), [user]);

  const value = useMemo(() => ({ user, login, logout, can }), [user, login, logout, can]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
