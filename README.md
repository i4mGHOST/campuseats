# CampusEats System

## Assignment Overview

**CampusEats** is an integrated system design initiative focused on streamlining web transactions, food ordering workflows, and RESTful API interactions across campus environments. This project encompasses comprehensive architectural briefs, REST API request/response logging, and a live web application with various microservices within.

The codebase and accompanying documentation provide a structured blueprint for building, testing, and auditing high-throughput campus service platforms.

---
## Team Details

| Serial Number | Team Member Name | Roll Number |
| :--- | :--- | :--- |
| 1 | SUVOJIT NAG|20251651094 |
| 2 | VANSH PANWAR|20251651101 |
| 3 | AJEET SHAKYA|20251651010 |
| 4 | ANKIT|20251651021 |
| 5 | ANJUL YADAV| 20251651020|

## File Manifest

| File Name | Format | Description |
| :--- | :--- | :--- |
| `README.md` | Markdown | Project overview, directory layout, execution instructions, and component summaries. |
| `docs/brief.md` | Markdown | System design brief detailing domain entities (nouns) and functional operations (verbs) categorized across Student, Vendor, and Administrative service boundaries. |
| `docs/http-log.md` | Markdown | Detailed log of HTTP/1.1 API requests and responses detailing query parameters, headers, status codes, JSON payload projections, and error handling. |
| `docs/network-analysis.md` | Markdown | Comprehensive browser network profile and waterfall evaluation for `wikipedia.org`, measuring payload sizes, TTFB, DOM loading times, and asset transfer bottlenecks. |
| `design/design.pdf` | PDF Document | Comprehensive architectural blueprint, system specifications, and API contract details. |
| `schema/schema.drawio` | Draw.io Diagram | Editable source file for the microservices database schema models. |
| `schema/schema.png` | Image (PNG) | Exported visual diagram illustrating data isolation across all internal services. |
| `schema/schema.sql` | SQL Script | Complete DDL statements to construct the independent database tables for all services. |
| `services/services.drawio` | Draw.io Diagram | Editable source file mapping the microservice boundaries and communication pathways. |
| `services/services.png` | Image (PNG) | Exported visual architecture diagram displaying system orchestration and API gateways. |
| `integration/integration.pdf` | PDF Document | Architectural context, HTTP POST binding details, service catalog entry, and fault translation mapping. |
| `partner/partner.wsdl` | WSDL / XML | Complete contract specifying the external GlobalPay `Charge` operation and data types. |
| `soap/soap-request.xml` | XML Envelope | Sample request payload featuring authentication credentials and payment parameters. |
| `soap/soap-response.xml` | XML Envelope | Sample success response payload returning transaction identifiers and timestamps. |
| `soap/soap-fault.xml` | XML Envelope | Sample fault envelope demonstrating structured XML error messaging (e.g., insufficient funds). |

---

## Directory Structure
📦`campuseats/`</br>
 ┣ 📂[`design`](./design/)</br>
 ┃ ┗ 📜[`design.pdf`](./design/design.pdf)</br>
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
 ┣ 📂[`integration/`](./integration/)</br>
 ┃ ┗ 📜[`integration.pdf`](./integration/integration.pdf)</br>
 ┣ 📂[`partner`](./partner/)</br>
 ┃ ┗ 📜[`partner.wsdl`](./partner/partner.wsdl)</br>
 ┣ 📂[`schema`](./schema/)</br>
 ┃ ┣ 📜[`schema.drawio`](./schema/schema.drawio)</br>
 ┃ ┣ 📜[`schema.png`](./schema/schema.png)</br>
 ┃ ┗ 📜[`schema.sql`](./schema/schema.sql)</br>
 ┣ 📂[`services`](./services/)</br>
 ┃ ┣ 📜[`services.drawio`](./services/services.drawio)</br>
 ┃ ┗ 📜[`services.png`](./services/services.png)</br>
 ┣ 📂[`soap`](./soap/)</br>
 ┃  ┣ 📜[`soap-fault.xml`](./soap/soap-fault.xml)</br>
 ┃  ┣ 📜[`soap-request.xml`](./soap/soap-request.xml)</br>
 ┃  ┗ 📜[`soap-response.xml`](./soap/soap-response.xml)</br>
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

### 4. System Design & Service Orchestration (`design/design.pdf, services/services.drawio, services/services.png`)
The `design.pdf` file serves as the definitive architectural blueprint for the CampusEats platform. It details the microservice bounded contexts, API contract specifications, and the overarching system requirements. The visual representation of these service boundaries, network flows, and orchestrations is provided in `services.drawio` and its exported image equivalent, `services.png`. These diagrams illustrate how the internal services (Accounts, Orders, Payments, Catalogue, Delivery, and Notifications) securely interact to process user transactions.

### 5. Database Schema & Data Isolation (`schema/schema.drawio, schema/schema.png, schema/schema.sql`)
To enforce strict microservice independence, each service maintains its own isolated data store. This distributed data architecture is visually modeled in `schema.drawio` and `schema.png`, detailing the entities and logical relationships within each bounded context. The concrete implementation of these models is provided in `schema.sql`, which contains the complete Data Definition Language (DDL) scripts required to generate the independent tables, indices, and constraints for the entire platform.

### 6. Architectural Strategy & Fault Mapping (`integration/integration.pdf`)
The `integration.pdf` document serves as the technical justification for selecting SOAP for financial transactions, specifically focusing on the `Charge` operation. The document outlines the raw `HTTP POST` binding requirements, provides the modern service registry discovery entry, and specifies the fault mapping logic used to isolate external vendor terminology from internal services. It ensures requests are seamlessly translated into standard CampusEats REST responses, maintaining a clean and decoupled microservice boundary.

### 7. Formal WSDL Service Contract (`partner/partner.wsdl`)
The `partner.wsdl` file establishes the SOAP contract between `CampusEats` and the `GlobalPay` gateway. It defines the required XML schema data types, the message formats for the `Charge` operation, and the physical service endpoint necessary for strong and reliable integration client stubs. This contract strictly incorporates all six essential WSDL elements—types, message, portType, binding, service, and port—to guarantee full protocol compliance. This ensures that all payment payloads are statically validated prior to network transmission.

### 8. Operational SOAP Message Payloads (`soap/soap-request.xml, soap/soap-response.xml, soap/soap-fault.xml`)
The XML message suite, comprising `soap-request.xml`, `soap-response.xml`, and `soap-fault.xml`, demonstrates the concrete operational data exchange expected during live system execution. Together, these files illustrate the precise structure of an outbound charge request containing WS-Security credentials, a successful transaction response with receipt metadata, and a standardized SOAP fault envelope handling a declined payment scenario. These payloads strictly adhere to the custom XML namespaces defined in the contract, showcasing how data like student IDs and order references are serialized.

---