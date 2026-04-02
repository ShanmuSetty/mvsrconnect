/* ─────────────────────────────────────────────
   MvsrConnect Service Worker
   Handles Web Push notifications + click routing
   ───────────────────────────────────────────── */

const CACHE_NAME = 'mvsrconnect-v1';

// ── PUSH EVENT ──
// Fired when the backend sends a push message
self.addEventListener('push', function (event) {
  let data = {};

  try {
    data = event.data ? event.data.json() : {};
  } catch (e) {
    data = {
      title: 'MVSR Connect',
      body: event.data ? event.data.text() : 'You have a new notification',
      url: '/',
      icon: '/icon-192.png',
      badge: '/badge-72.png'
    };
  }

  const title = data.title || 'MVSR Connect';
  const options = {
    body: data.body || '',
    icon: data.icon || '/icon-192.png',
    badge: data.badge || '/badge-72.png',
    data: { url: data.url || '/' },
    vibrate: [100, 50, 100],
    requireInteraction: false,
    tag: data.url || 'mvsrconnect',   // group duplicate notifications for same URL
    renotify: true
  };

  event.waitUntil(
    self.registration.showNotification(title, options)
  );
});

// ── NOTIFICATION CLICK ──
// When the user clicks the OS notification, open the relevant page
self.addEventListener('notificationclick', function (event) {
  event.notification.close();

  const targetUrl = (event.notification.data && event.notification.data.url)
    ? event.notification.data.url
    : '/';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function (clientList) {
      // If a tab is already open, focus it and navigate
      for (const client of clientList) {
        if (client.url.includes(self.location.origin) && 'focus' in client) {
          client.focus();
          return client.navigate(targetUrl);
        }
      }
      // Otherwise open a new tab
      if (clients.openWindow) {
        return clients.openWindow(targetUrl);
      }
    })
  );
});

// ── INSTALL + ACTIVATE ──
self.addEventListener('install', function (event) {
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(clients.claim());
});
