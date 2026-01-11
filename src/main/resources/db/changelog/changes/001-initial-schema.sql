--liquibase formatted sql

--changeset yourname:001-create-users-table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       active BOOLEAN DEFAULT TRUE
);

--changeset yourname:002-create-invoices-table
CREATE TABLE invoices (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL REFERENCES users(id),
                          vendor_name VARCHAR(255) NOT NULL,
                          amount DECIMAL(19, 2) NOT NULL,
                          currency VARCHAR(3) DEFAULT 'EUR',
                          description TEXT,
                          s3_file_key VARCHAR(512) NOT NULL,
                          content_hash VARCHAR(64),
                          status VARCHAR(50) NOT NULL,
                          rejection_reason TEXT,
                          version BIGINT DEFAULT 0,
                          created_at TIMESTAMP DEFAULT NOW(),
                          updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_user ON invoices(user_id);

--changeset yourname:003-create-audit-table
CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            invoice_id UUID NOT NULL REFERENCES invoices(id),
                            actor_id UUID REFERENCES users(id),
                            action VARCHAR(50) NOT NULL,
                            old_status VARCHAR(50),
                            new_status VARCHAR(50),
                            comment TEXT,
                            timestamp TIMESTAMP DEFAULT NOW()
);