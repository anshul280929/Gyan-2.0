# Oracle VM Deployment

This project can be deployed on a single Oracle Cloud VM with Docker Compose.

## 1. Recommended VM

- Ubuntu 22.04 or Oracle Linux
- At least 2 OCPUs and 8 GB RAM if possible
- Open ports:
  - `22` for SSH
  - `80` for frontend
  - `8080` for backend API if needed

## 2. Install Docker

Example for Ubuntu:

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER
```

Reconnect after adding your user to the docker group.

## 3. Clone the project

```bash
git clone <your-repo-url>
cd gyan
```

## 4. Create runtime env file

```bash
cp .env.oracle.example .env
```

Fill in:

- `POSTGRES_PASSWORD`
- `GROQ_API_KEY`

## 5. Start the stack

```bash
docker compose -f docker-compose.oracle.yml up -d --build
```

## 6. Check service status

```bash
docker compose -f docker-compose.oracle.yml ps
docker compose -f docker-compose.oracle.yml logs -f backend
```

## 7. Access

- Frontend: `http://<oracle-vm-public-ip>`
- Backend: `http://<oracle-vm-public-ip>:8080`

## 8. Notes

- Internal services remain private inside the Docker network:
  - Postgres
  - Kafka
  - Zookeeper
  - Elasticsearch
  - Embedding service
- Uploaded files are stored in the `backend_uploads` Docker volume.
- For production hardening, add a reverse proxy, domain, and HTTPS later.
