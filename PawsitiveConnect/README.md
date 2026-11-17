# **"Pawsitive Connect"**

#### **System Documentation**

### Developers

*Name:* **SUVOJIT NAG** | *Roll No:* **20251651094** (Team Leader)
*Name:* **VANSH PANWAR** | *Roll No:* **20251651101**
*Name:* **AJEET SHAKYA** | *Roll No:* **20251651010**
*Name:* **ANKIT** | *Roll No:* **20251651021**
*Name:* **ANJUL YADAV** | *Roll No:* **20251651020**

## 1. Introduction

**Pawsitive Connect** is an enterprise-grade, full-stack native desktop application engineered to digitize and optimize animal rescue, shelter management, veterinary welfare, and pet adoption workflows. Designed as a unified ecosystem, the platform bridges the gap between stray/abandoned animal rescue operations, medical health tracking, shelter inventory management, and prospective adopters. By coupling a high-performance JavaFX desktop frontend with a decoupled Node.js REST backend and a spatial PostgreSQL database, Pawsitive Connect ensures reliable and secure animal welfare coordination.

Pawsitive Connect successfully unifies animal rescue administration into a modular, decoupled architecture. By separating the frontend presentation from backend microservices — the system guarantees high maintainability, strict boundary rules, and robust audit capabilities.

## 2. Existing Problem

Traditional animal rescue organizations and shelters frequently struggle with fragmented operational workflows, leading to inefficiencies in animal care and placement. Key challenges include:

- **Decentralized Data Management:** Reliance on manual paper logs, unorganized spreadsheets, and isolated notes makes tracking animal health, vaccination schedules, and adoption statuses error-prone.
    
- **Coordination Delays in Emergencies:** Distress calls regarding stranded or injured animals lack real-time geographic tracking and structured dispatch protocols, delaying critical rescue response times.
    
- **Opaque Adoption & Welfare Workflows:** Prospective adopters experience prolonged waiting periods and unclear feedback loops due to disconnected identity verification, background checks, and welfare screening steps.
    
- **Financial & Data Inconsistency:** Lacking strict database boundaries and atomic transaction ledgers, shelters face risks of record corruption and untracked financial transactions.

## 3. Software Requirement Specification (SRS)

### Functional Requirements

1. **User Identity & Authentication:** Secure user registration, authentication via JSON Web Tokens (JWT), password hashing using `bcryptjs`, and role-based permissions.
    
2. **Pet Catalogue & Inventory:** Dynamic browsing, pagination, and multi-parameter filtering (by breed, age, status, and shelter branch) of rescue animals.
    
3. **Welfare & Adoptions Management:** Comprehensive logging of medical history, vaccinations, fundraising campaigns, and end-to-end processing of adoption applications.
    
4. **Emergency Rescue & GPS Dispatch:** Real-time logging of distress incidents, coordinate validation via PostGIS spatial extensions, and status transitions from `REPORTED` to `DISPATCHED`.
    
5. **Transactions & Financial Ledger:** Secure tracking and settlement of adoption fees, donations, and financial audit trails.

### Non-Functional Requirements

- **Performance & Responsiveness:** Instant UI feedback on JavaFX threads using asynchronous HTTP clients (`java.net.http.HttpClient`) to prevent interface freezing during network calls.
    
- **Security & Reliability:** Strict payload validation, standardized RFC 7807 Problem Details error responses, and exclusion of sensitive database internals from client-facing messages.
    
- **Portability:** Packaged as a standalone native Windows executable (`PawsitiveConnect_Setup.exe`) using JDK `jpackage` and the WiX Toolset.

---

## 4. UML Diagram

#### Class Architecture Diagram


![[Class Architecture Diagram.png|700]]

#### Sequence Diagrams for All Services

* **Identity Service**    (`/api/auth/login`)

![[Identity Service.png|700]]


* **Catalogue Service**    (`/api/pets`)

![[Catalogue Service.png|700]]


* **Welfare & Adoptions Service**    (`/api/adoptions` & `/api/medical`)

![[Welfare & Adoption Service.png|700]]


* **Rescue Service**    (`/api/rescue`)

![[Rescue Service.png|700]]


* **Transactions Service**    (`/api/transactions`)

![[Transaction Service.png|700]]


---

## 5. Data Flow Diagram (DFD)

#### Level 0 Data Flow Diagram (Context Diagram)


![[Level 0 Data Flow Diagram.png|700]]

#### Level 1 Data Flow Diagram (Service Breakdown Diagram)


![[Level 1 Data Flow Diagram.png|700]]


---

## 6. Technology Stack

- **Frontend Layer:** JavaFX (Java 21 LTS), FXML layouts via Gluon Scene Builder, and Maven build automation.
    
- **Backend Layer:** Node.js & Express.js REST API server running on **Port 8080**, utilizing JSON Web Tokens and `bcryptjs`.
    
- **Database Layer:** PostgreSQL relational database running on **Port 5432**, enhanced with the PostGIS spatial extension.
    
- **Packaging Toolset:** JDK `jpackage` and WiX Toolset for generating `PawsitiveConnect_Setup.exe`.

---

## 7. Conclusion

**Pawsitive Connect** designed as a unified ecosystem, the platform bridges the gap between stray/abandoned animal rescue operations, medical health tracking, shelter inventory management, and prospective adopters. It provides a complete, scalable technological solution to modern animal welfare and rescue challenges. Through rigorous domain modeling, clear service boundaries, standardized UML/DFD specifications, and a modern Java/Node.js tech stack, the platform establishes an enterprise-grade benchmark for digital animal rescue coordination and sustainable shelter management.

---

## 8. GitHub
 
 **Repository Link -** https://github.com/i4mGHOST/Pawsitive-Connect

---