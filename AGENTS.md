# General

## Language Conventions
- **Internal (Development):** 100% **English** (code, variables, files, folders, comments, logs, and git commits).
- **External (End User):** 100% **Spanish** (graphical interface, client-facing error messages, alerts, and catalog/seeders).

# Backend

## Architecture and Package Structure
- **Style:** Domain-Driven Modular Monolith (DDD / Package by Feature).
- **Hierarchical Package Structure:**
  1. **Root Domain (Bounded Context):** Business macro modules (`catalog`, `orders`, `users`, `analytics`, etc.).
  2. **Subdomain / Aggregate:** Subfolders per entity or feature (e.g., `catalog/product`, `catalog/category`).
  3. **Internal Layers per Subdomain:** Each feature encapsulates its own technical layers:
     - `api`: Controllers and DTOs (requests / responses).
     - `domain`: Entities and Value Objects.
     - `repository`: Interfaces and persistence queries (Spring Data JPA).
     - `service` / `application`: Use cases and business logic.
- **Rule:** Do not package by global technical layers (`controllers/`, `services/` at root). Each module must be autonomous.

# Frontend


