CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(30) NOT NULL
);

CREATE TABLE client_addresses (
    client_id UUID NOT NULL,
    seq INTEGER NOT NULL,
    street1 VARCHAR(255) NOT NULL,
    street2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(30) NOT NULL,
    country VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    CONSTRAINT pk_client_addresses PRIMARY KEY (client_id, seq),
    CONSTRAINT fk_client_addresses_client
        FOREIGN KEY (client_id)
        REFERENCES clients (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_clients_first_name ON clients (LOWER(first_name));
CREATE INDEX idx_clients_last_name ON clients (LOWER(last_name));
CREATE INDEX idx_clients_email ON clients (LOWER(email));
CREATE INDEX idx_clients_phone_number ON clients (LOWER(phone_number));
CREATE INDEX idx_client_addresses_client_id ON client_addresses (client_id);
