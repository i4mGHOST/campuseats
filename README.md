# CampusEats System

## Assignment Overview

**CampusEats** is an integrated system design initiative focused on streamlining web transactions, food ordering workflows, and RESTful API interactions across campus environments. This project encompasses comprehensive architectural briefs, REST API request/response logging, and a live web application with various microservices within.

The codebase and accompanying documentation provide a structured blueprint for building, testing, and auditing high-throughput campus service platforms.

---

## File Manifest

| File Name | Format | Description |
| :--- | :--- | :--- |
| `README.md` | Markdown | Project overview, directory layout, execution instructions, and component summaries. |
| `docs/brief.md` | Markdown | System design brief detailing domain entities (nouns) and functional operations (verbs) categorized across Student, Vendor, and Administrative service boundaries. |
| `docs/http-log.md` | Markdown | Detailed log of HTTP/1.1 API requests and responses detailing query parameters, headers, status codes, JSON payload projections, and error handling. |
| `docs/network-analysis.md` | Markdown | Comprehensive browser network profile and waterfall evaluation for `wikipedia.org`, measuring payload sizes, TTFB, DOM loading times, and asset transfer bottlenecks. |

---

## Directory Structure
📦`campuseats/`</br>
 ┣ 📂[`docs/`](./docs/)</br>
 ┃ ┣ 📂[`screenshots/`](./docs/screenshots/)</br>
 ┃ ┃ ┣ 📜[`request1.png`](./docs/screenshots/request1.png)</br>
 ┃ ┃ ┣ 📜[`request2.png`](./docs/screenshots/request2.png)</br>
 ┃ ┃ ┣ 📜[`request3.png`](./docs/screenshots/request3.png)</br>
 ┃ ┃ ┣ 📜[`request4.png`](./docs/screenshots/request4.png)</br>
 ┃ ┃ ┗ 📜[`request5.png`](./docs/screenshots/request5.png)</br>
 ┃ ┣ 📜[`brief.md`](./docs/brief.md)</br>
 ┃ ┣ 📜[`http-log.md`](./docs/http-log.md)</br>
 ┃ ┗ 📜[`network-analysis.md`](./docs/network-analysis.md)</br>
 ┗ 📜[`README.md`](./README.md)
  
---

## Key Project Components & Summaries

### 1. System Architecture & Domain Model (`docs/brief.md`)
* **Student Interactive Services:** Handles user identification, vendor directory lookups, interactive cart construction, and order receipt generation.
* **Vendor Interactive Services:** Manages merchant business profiles, live inventory menu catalogs, and real-time kitchen order preparation queues.
* **Administrative & Platform Services:** Manages payment gateway verification logs, customer dispute resolution tickets, and institution-wide analytics reporting.
* **Action Contracts (Verbs):** Defines explicit system contracts for Single Sign-On (SSO) authentication, real-time inventory toggles, order status tracking, and automated refund execution.

### 2. HTTP Protocol & API Audit (`docs/http-log.md`)
* **Endpoint Testing:** Captures `cURL` execution strings against REST endpoints, tracking requests with single entities, multi-result arrays (`?results=3`), and localized queries (`?nat=us`).
* **Field Projection:** Demonstrates payload optimization using inclusion parameters (`?inc=name,email`) to minimize transmission latency.
* **Error Verification:** Validates server boundary conditions and status codes, including explicit handling of `404 Not Found` HTML fallback pages for unmapped routes.

### 3. Network Performance Evaluation (`docs/network-analysis.md`)
* **Target Domain:** `https://www.wikipedia.org/`
* **Performance Profile:** Audited using Chrome DevTools with a disabled cache and hard reload.
* **Metrics:** Evaluates 18 HTTP/2 network requests totaling 342 KB transferred (820 KB uncompressed), achieving a DOM Content Loaded time of 240 ms and a complete page load time of 610 ms.
* **Optimization Insights:** Highlights HTTP `301` SSL redirection efficiency, HTTP/2 multiplexing benefits, and resource bottlenecks (e.g., brand image assets).

---