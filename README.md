# Toast Project

A comprehensive microservices application with the following components:

## Architecture Overview

- **Backend Services**: Java-based REST services using Dagger for dependency injection
- **Message Broker**: Apache Pulsar (dev) and RabbitMQ (prod)
- **Integration**: Apache Camel for message routing and transformation
- **Databases**: PostgreSQL (dev) and DynamoDB (prod)
- **Frontend**: React application with Tailwind CSS, GraphQL, and Storybook

## Project Structure

```
├── backend/                 # Java backend services
│   ├── api-service/        # REST API service
│   ├── notification-service/ # Notification service
│   └── integration-service/ # Camel integration service
├── frontend/               # React frontend application
├── infrastructure/         # Infrastructure configuration
└── docs/                  # Documentation
```

## Prerequisites

- Java 21
- Node.js 18+
- Docker and Docker Compose
- Maven 3.8+
- PostgreSQL (for development)
- AWS CLI (for production)

## Quick Start

### Backend Services

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend Application

```bash
cd frontend
npm install
npm start
```

### Infrastructure

```bash
cd infrastructure
docker-compose up -d
```

## Development

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Frontend**: http://localhost:3000
- **Storybook**: http://localhost:6006
- **Pulsar Admin**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## Production Deployment

See `docs/deployment.md` for production deployment instructions. 