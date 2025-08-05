#!/bin/bash

# Toast Project Setup Script
# This script sets up the entire Toast project with all components

set -e

echo "🚀 Setting up Toast Project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 21."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt "21" ]; then
        print_error "Java 21 is required. Current version: $java_version"
        exit 1
    fi
    print_success "Java version: $(java -version 2>&1 | head -n 1)"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.8+."
        exit 1
    fi
    print_success "Maven version: $(mvn -version | head -n 1)"
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 18+."
        exit 1
    fi
    print_success "Node.js version: $(node --version)"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker."
        exit 1
    fi
    print_success "Docker version: $(docker --version)"
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose."
        exit 1
    fi
    print_success "Docker Compose version: $(docker-compose --version)"
}

# Setup infrastructure
setup_infrastructure() {
    print_status "Setting up infrastructure services..."
    
    cd infrastructure
    
    # Start infrastructure services
    print_status "Starting Docker services..."
    docker-compose up -d
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Check service health
    print_status "Checking service health..."
    
    # Check PostgreSQL
    if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
        print_success "PostgreSQL is ready"
    else
        print_warning "PostgreSQL is not ready yet"
    fi
    
    # Check RabbitMQ
    if curl -s http://localhost:15672 > /dev/null 2>&1; then
        print_success "RabbitMQ is ready"
    else
        print_warning "RabbitMQ is not ready yet"
    fi
    
    # Check Pulsar
    if curl -s http://localhost:8080/admin/v2/brokers/health > /dev/null 2>&1; then
        print_success "Apache Pulsar is ready"
    else
        print_warning "Apache Pulsar is not ready yet"
    fi
    
    cd ..
}

# Build backend services
build_backend() {
    print_status "Building backend services..."
    
    cd backend
    
    # Clean and install
    print_status "Building with Maven..."
    mvn clean install -DskipTests
    
    print_success "Backend services built successfully"
    cd ..
}

# Setup frontend
setup_frontend() {
    print_status "Setting up frontend application..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing npm dependencies..."
    npm install
    
    # Build Storybook
    print_status "Building Storybook..."
    npm run build-storybook
    
    print_success "Frontend setup completed"
    cd ..
}

# Create environment files
create_env_files() {
    print_status "Creating environment files..."
    
    # Backend environment
    cat > backend/.env << EOF
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/toast_dev
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# AWS Configuration (for production)
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_DYNAMODB_ENDPOINT=https://dynamodb.us-east-1.amazonaws.com

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/

# Pulsar Configuration
PULSAR_SERVICE_URL=pulsar://localhost:6650
EOF

    # Frontend environment
    cat > frontend/.env << EOF
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_GRAPHQL_URL=http://localhost:8080/api/graphql
EOF

    print_success "Environment files created"
}

# Display next steps
show_next_steps() {
    echo ""
    echo "🎉 Toast Project setup completed!"
    echo ""
    echo "📋 Next steps:"
    echo ""
    echo "1. Start the backend services:"
    echo "   cd backend"
    echo "   mvn spring-boot:run -pl api-service"
    echo ""
    echo "2. Start the frontend application:"
    echo "   cd frontend"
    echo "   npm start"
    echo ""
    echo "3. Access the applications:"
    echo "   - Frontend: http://localhost:3000"
    echo "   - API Documentation: http://localhost:8080/api/swagger-ui.html"
    echo "   - Storybook: http://localhost:6006"
    echo "   - RabbitMQ Management: http://localhost:15672"
    echo "   - Pulsar Admin: http://localhost:8080"
    echo "   - Grafana: http://localhost:3001 (admin/admin)"
    echo "   - Prometheus: http://localhost:9090"
    echo ""
    echo "4. For production deployment:"
    echo "   - Update environment variables in .env files"
    echo "   - Use docker-compose.prod.yml for production infrastructure"
    echo ""
    echo "📚 Documentation:"
    echo "   - README.md: Project overview and setup"
    echo "   - docs/: Additional documentation"
    echo ""
}

# Main execution
main() {
    print_status "Starting Toast Project setup..."
    
    check_prerequisites
    setup_infrastructure
    build_backend
    setup_frontend
    create_env_files
    show_next_steps
}

# Run main function
main "$@" 