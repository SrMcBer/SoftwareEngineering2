#!/bin/bash

# Test Database Setup Script
# This script creates the test database and user for running pytest

set -e  # Exit on error

echo "====================================="
echo "Setting up Test Database"
echo "====================================="

# Database credentials
DB_NAME="test_db"
DB_USER="test_user"
DB_PASS="test_pass"

# Check if PostgreSQL is running
if ! pg_isready -q; then
    echo "Error: PostgreSQL is not running!"
    echo "Please start PostgreSQL first:"
    echo "  macOS (Homebrew): brew services start postgresql"
    echo "  Linux: sudo systemctl start postgresql"
    exit 1
fi

echo "PostgreSQL is running ✓"

# Drop existing test database if it exists
echo "Dropping existing test database (if any)..."
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1 && \
    psql -U postgres -c "DROP DATABASE $DB_NAME" 2>/dev/null || true

# Drop existing test user if exists
echo "Dropping existing test user (if any)..."
psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = '$DB_USER'" | grep -q 1 && \
    psql -U postgres -c "DROP USER $DB_USER" 2>/dev/null || true

# Create test user
echo "Creating test user '$DB_USER'..."
psql -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASS';"

# Create test database
echo "Creating test database '$DB_NAME'..."
psql -U postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"

# Grant privileges
echo "Granting privileges..."
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

# Connect to test database and grant schema privileges
psql -U postgres -d $DB_NAME -c "GRANT ALL ON SCHEMA public TO $DB_USER;"
psql -U postgres -d $DB_NAME -c "GRANT CREATE ON SCHEMA public TO $DB_USER;"

# Enable UUID extension (for gen_random_uuid())
echo "Enabling UUID extension..."
psql -U postgres -d $DB_NAME -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

# Enable CITEXT extension
echo "Enabling CITEXT extension..."
psql -U postgres -d $DB_NAME -c "CREATE EXTENSION IF NOT EXISTS citext;"

echo ""
echo "====================================="
echo "✓ Test database setup complete!"
echo "====================================="
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "Password: $DB_PASS"
echo "Connection String: postgresql://$DB_USER:$DB_PASS@localhost:5432/$DB_NAME"
echo ""
echo "You can now run your tests with: pytest"
echo "====================================="