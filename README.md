# Ocean View Resort Management System

A Spring Boot application for managing resort reservations and billing/invoices with a user-friendly web interface.

## Features

- Secure staff login (`username` + `password`)
- Reservation management
  - Add reservation
  - View reservation by reservation number
  - Update reservation
  - Delete reservation
  - View all reservations
- Billing and invoicing
  - Create invoice for a reservation
  - Automatic total calculation (room rate, nights, extras, tax, discount)
  - Record payments (cash/card/bank transfer)
  - Track invoice status (`PENDING`, `PARTIAL`, `PAID`)
  - Print invoice/bill
- Left navigation panel for quick access to:
  - Reservations
  - Billing & Invoices
  - Help Guide
  - Exit System
- Help and Exit pages for staff operations

## Tech Stack

- Java 17+
- Spring Boot 3
- Spring Web + JDBC
- MySQL (runtime)
- H2 (tests)
- Plain HTML/CSS/JavaScript frontend
- Maven

## Project Structure

- Backend source: `src/main/java/com/ocean/resort`
- SQL scripts: `src/main/resources/schema.sql`, `src/main/resources/data.sql`
- Frontend pages: `src/main/resources/static`
- Tests: `src/test/java/com/ocean/resort`

## Default Login

- Username: `admin`
- Password: `admin123`

## Prerequisites

- JDK 17 or newer
- Maven 
- MySQL running locally (default config) if running the app normally

## Configuration

Main config file: `src/main/resources/application.yaml`

Default datasource values:

- URL: `jdbc:mysql://localhost:3306/ocean_view_resort?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- Username: `root`
- Password: `root`

You can override using environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Run the Application

```Terminal

Clean and install maven dependencies: mvn clean install

Run spring boot app: mvn spring-boot:run
```

Open:

- `http://localhost:8080/`

## Run Tests

```Terminal
.\mvn test
```

## Main API Endpoints

### Authentication

- `POST /api/v1/auth/login`

### Reservations

- `POST /api/v1/reservations`
- `GET /api/v1/reservations`
- `GET /api/v1/reservations/{reservationNumber}`
- `PUT /api/v1/reservations/{reservationNumber}`
- `DELETE /api/v1/reservations/{reservationNumber}`

### Billing

- `POST /api/v1/billing/invoices`
- `GET /api/v1/billing/invoices`
- `GET /api/v1/billing/invoices/{invoiceNumber}`
- `POST /api/v1/billing/invoices/{invoiceNumber}/payments`

## Notes

- Database schema and seed data are auto-initialized at startup.
- Reservation and invoice numbers must be unique.
- The UI is responsive and supports desktop/mobile layouts.
