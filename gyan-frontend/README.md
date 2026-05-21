# Gyan Frontend

This frontend now runs on React and Vite.

## Development

Install dependencies and start the dev server:

```bash
npm install
npm run dev
```

The app runs on `http://localhost:5173` by default. API requests are proxied to `http://localhost:8080`.

## Build

Create a production build with:

```bash
npm run build
```

## API configuration

For local development, the Vite proxy handles backend requests automatically.

For deployed environments, set `VITE_API_BASE_URL` to the backend origin.
