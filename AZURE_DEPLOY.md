# Azure Deployment

This project is best deployed on Azure as:

- `gyan-frontend` on Azure Storage Static Website
- backend stack on one Ubuntu VM with Docker Compose

The Azure VM runs:

- Spring Boot backend
- Python embedding service
- PostgreSQL
- Kafka
- Zookeeper
- Elasticsearch

## 1. Create the backend VM

Create an Ubuntu 22.04 VM.

Recommended for this stack:

- at least `4 vCPU / 8 GB RAM`
- `80 GB` managed disk

Open inbound ports:

- `22` for SSH
- `8080` for backend API

Keep Postgres, Kafka, Zookeeper, Elasticsearch, and embedding service internal only.

## 2. Install Docker on the VM

SSH into the VM and run:

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER
```

Reconnect after adding your user to the docker group.

## 3. Clone the project on the VM

```bash
git clone <your-repo-url>
cd gyan
```

## 4. Create runtime env file

```bash
cp .env.azure.example .env
```

Set:

- `POSTGRES_PASSWORD`
- `GROQ_API_KEY`
- `APP_CORS_ALLOWED_ORIGINS`

For `APP_CORS_ALLOWED_ORIGINS`, use your Azure static website URL or your CDN/custom domain.

Example:

```env
APP_CORS_ALLOWED_ORIGINS=https://yourfrontend.z13.web.core.windows.net
```

## 5. Start backend stack

```bash
docker compose -f docker-compose.azure.yml up -d --build
```

Check status:

```bash
docker compose -f docker-compose.azure.yml ps
docker compose -f docker-compose.azure.yml logs -f backend
```

## 6. Deploy frontend to Azure Storage Static Website

Build locally:

```bash
cd gyan-frontend
npm install
npm run build
```

Before building for production, set:

```env
VITE_API_BASE_URL=http://<your-vm-public-ip>:8080
```

Then upload the contents of `gyan-frontend/dist` to the `$web` container in your Azure Storage account.

Enable:

- Static website hosting
- index document: `index.html`
- error document path: `index.html`

## 7. Final URLs

- Frontend: Azure static website URL
- Backend: `http://<your-vm-public-ip>:8080`

## 8. Notes

- Cookies and auth are better over HTTPS. For production, put the backend behind nginx and TLS.
- The frontend is static, so Azure Storage is cheaper than using a second VM.
- Uploaded files are persisted in the `backend_uploads` Docker volume on the VM.
