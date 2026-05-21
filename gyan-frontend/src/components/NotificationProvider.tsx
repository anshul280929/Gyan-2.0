import { createContext, ReactNode, useCallback, useContext, useMemo, useState } from 'react';

type NotificationVariant = 'success' | 'error' | 'info';

type NotificationItem = {
  id: number;
  message: string;
  variant: NotificationVariant;
};

type NotificationContextValue = {
  notify: (message: string, variant?: NotificationVariant) => void;
};

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);

  const notify = useCallback((message: string, variant: NotificationVariant = 'info') => {
    const id = Date.now() + Math.floor(Math.random() * 1000);

    setNotifications((current) => [...current, { id, message, variant }]);

    window.setTimeout(() => {
      setNotifications((current) => current.filter((item) => item.id !== id));
    }, 4000);
  }, []);

  const value = useMemo(() => ({ notify }), [notify]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <div className="notification-stack">
        {notifications.map((notification) => (
          <div className={`notification-item ${notification.variant}`} key={notification.id}>
            {notification.message}
          </div>
        ))}
      </div>
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  const context = useContext(NotificationContext);

  if (!context) {
    throw new Error('useNotifications must be used within NotificationProvider');
  }

  return context;
}
