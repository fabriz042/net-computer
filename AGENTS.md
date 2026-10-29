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

## Service Organization
- Use the same general English section separators in service classes, in this order: `Constants`, `Dependencies`, `Constructor`, `Public operations`, and `Private helpers`. Keep section titles generic; do not use feature-specific titles.

# Frontend

## Tailwind Code Style

Tailwind classes grouped by responsibility:

```tsx
className="w-full py-2 pl-4 pr-10
            text-sm text-gray-900 placeholder:text-gray-400
            border border-gray-300 rounded-lg bg-white
            outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
```

Order: sizing → text/color → border/visual → interaction (transition, hover, focus).

## Component Props

Components must not expose styling props such as `className`, `css`, or `style`. Keep Tailwind styles inside the component; use semantic variants or sizes when customization is needed.
