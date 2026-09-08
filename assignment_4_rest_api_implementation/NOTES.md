# CS543 Assignment 4: CampusEats Orders Service (REST)

**Team ID:** Team 09 
**Course:** CS543 Web Services  
**Professor:** Dr. Pramit Mazumdar  

### Team Members
- *Name:* **SUVOJIT NAG** | *Roll No:* **20251651094** (Team Leader)
- *Name:* **VANSH PANWAR** | *Roll No:* **20251651101**
- *Name:* **AJEET SHAKYA** | *Roll No:* **20251651010**
- *Name:* **ANKIT** | *Roll No:* **20251651021**
- *Name:* **ANJUL YADAV** | *Roll No:* **20251651020**
---

## Part A: Service Modeling

### A1: Selected Service
**CampusEats Orders Service** (`/orders`).

### A2: Corresponding Operations in SOAP WSDL Style
1. `createOrder(studentId, items, paymentMethodId) -> orderId, status`
2. `getOrderDetails(orderId) -> orderRecord`
3. `listOrders(studentId, status) -> orderList`
4. `cancelOrder(orderId, reason) -> cancellationConfirmation`

### A3 & A4: Resource Table
| Method | URL | What it does | Success Code | Failure Codes |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/orders` | Creates a new student order and charges payment | `201 Created` (`Location` header) | `400 Bad Request`, `422 Unprocessable Entity`, `502 Bad Gateway` |
| **GET** | `/orders/{orderId}` | Retrieves details of a single order | `200 OK` | `404 Not Found` |
| **GET** | `/orders` | Lists orders with optional `student_id` & `status` filters | `200 OK` | `400 Bad Request` |
| **POST** | `/orders/{orderId}/cancellation` | State-changing sub-resource to cancel an active order | `200 OK` | `400 Bad Request`, `404 Not Found`, `409 Conflict` |

### A5: Justification of Hard Resource Mapping
**Operation:** `cancelOrder(orderId, reason)`
* **Resolution:** Mapped to a state-changing sub-resource endpoint: `POST /orders/{orderId}/cancellation`.
* **Rejected Alternatives:** We rejected `POST /orders/cancel` because using a verb in the URL violates REST resource modeling principles. We also rejected `DELETE /orders/{orderId}` because deleting an order destroys audit trails and accounting history; cancellation is an event/state transition rather than physical resource removal.
* **Why Sub-resource Succeeded:** Creating a cancellation sub-resource (`/cancellation`) cleanly captures the intent and payload (cancellation reason) while transitioning the parent order's status to `CANCELLED` without violating idempotency or resource durability constraints.

---

## Part D: Network Hardening & Outbound Call

### D3: Outbound Dependency Fallback Reasoning
* **Dependency:** Payment Gateway Microservice (`http://localhost:5001/payments` resolved via `PAYMENTS_SERVICE_URL`).
* **Chosen Fallback Strategy:** **Fail-Fast with Circuit Interruption (`502 Bad Gateway`)**.
* **Justification:** When the external Payment Service is unreachable or timing out, the Orders Service immediately marks the order state as `FAILED` and returns an RFC 7807 `502 Bad Gateway` problem response. Degrading gracefully (e.g., accepting the order as `PENDING_PAYMENT` and processing food preparation asynchronously without payment authorization) was explicitly rejected. In a campus dining ecosystem, accepting orders without real-time payment guarantees creates severe financial discrepancies and food waste from uncollectible transactions.

---

## Part E: Questions & Answers

### Question 1
**Count the lines in your Assignment 3 WSDL and in your openapi.yaml. What is the difference actually made of? Name two things the WSDL declared that the OpenAPI file does not need to.**

* **Line Count Breakdown:**
  * Assignment 3 WSDL (`partner.wsdl`): 111 lines.
  * Assignment 4 OpenAPI (`openapi.yaml`): 215 lines.
* **Composition of Difference:** The line count increase in OpenAPI is primarily due to explicit HTTP status code mappings (`201`, `200`, `400`, `404`, `409`, `422`, `502`), inline parameter definitions (query params and `Idempotency-Key` headers), structured JSON schema validation constraints under `components.schemas`, and full RFC 7807 problem detail response bodies.
* **Two Things Declared in WSDL Not Needed in OpenAPI:**
  1. **Transport & Envelope Binding:** The `<wsdl:binding>` section defining SOAP-over-HTTP protocol bindings, literal body encodings (`use="literal"`), and document styles (`style="document"`).
  2. **Abstract Message Wrapper Declarations:** The `<wsdl:message>` constructs that explicitly wrap payload schemas before attaching them to abstract port type operations.

---

### Question 2
**Quote one soap: Fault from your Assignment 3 work and show the status code and problem body that replaced it. Why is returning that error inside a 200 OK a problem for the network in between?**

* **Assignment 3 SOAP Fault Quote:**
```xml
<soapenv:Fault>
  <faultcode>soapenv:Client</faultcode>
  <faultstring>Payment execution failed: Insufficient funds in user account.</faultstring>
  <detail>
    <pay:ChargeFault>
      <pay:errorCode>ERR_INSUFFICIENT_FUNDS</pay:errorCode>
      <pay:errorMessage>The account linked to the payment method does not have enough balance to complete the transaction.</pay:errorMessage>
    </pay:ChargeFault>
  </detail>
</soapenv:Fault>
```

* **Assignment 4 Status Code & Problem Body Replacement:**
  * **HTTP Status Code:** `422 Unprocessable Entity`
  * **Problem Details Body (RFC 7807):**
```json
{
  "type": "about:blank",
  "title": "Payment Processing Failed",
  "status": 422,
  "detail": "The account linked to the payment method does not have enough balance to complete the transaction."
}
```

* **Why Returning Errors Inside `200 OK` Harms Intermediate Networks:**
Returning application errors inside an HTTP `200 OK` response disguises operational failures as successful transactions to intermediate network infrastructure. Reverse proxies, API gateways, load balancers, caching layers, CDNs, and telemetry services (e.g., Prometheus, Datadog) rely exclusively on HTTP response status codes. When an error returns `200 OK`:
1. Automated circuit breakers fail to detect upstream service degradation and cannot trip.
2. Proxy retry rules fail to trigger on transient transport issues.
3. Metrics and Service Level Objectives (SLOs) record failed operations as successful, corrupting observability and alerting pipelines.

---

### Question 3
**Which of UDDI's three moves—publish, find, bind—still exist in your new setup, and which disappeared? Explain what took over the job.**

* **Publish (Transformed):** Explicitly publishing XML contract definitions into a centralized UDDI registry server disappeared. In the REST architecture, publishing is handled by checking `openapi.yaml` into version control (Git) or serving interactive OpenAPI/Swagger portals (`/docs`) directly from the service runtime.
* **Find (Transformed):** Querying a central UDDI registry for service endpoints disappeared. Service lookup is now handled by modern infrastructure components such as Kubernetes Service Discovery, internal DNS resolution, API Gateways, or Service Meshes (e.g., Consul, Envoy).
* **Bind (Still Exists):** Binding remains an active step. Clients bind directly to REST endpoints by issuing HTTP requests (`POST`, `GET`) to resource URLs defined in `openapi.yaml` or generated client SDKs.
* **Disappeared Component:** The centralized **UDDI XML Registry Server** completely disappeared. Infrastructure-level DNS, Kubernetes service routing, and standardized OpenAPI contracts eliminated the need for heavy, monolithic UDDI registries.

---

### Question 4
**Your XML Schema was enforced before your code ran; your OpenAPI schema is not. Name the specific function in your code that now carries that responsibility, and one failure that would get through if you had not written it.**

* **Specific Function Name:** `validate_order_request(data)` in `app.py`.
* **Failure That Would Get Through:** If `validate_order_request(data)` were omitted, an incoming JSON request containing missing key structures (such as omitting the `items` array entirely) or supplying invalid domain attributes (such as `"unitPriceCents": -500`) would pass OpenAPI parsing (since OpenAPI files in Flask act as documentation rather than automated request middleware). Without manual validation, Python execution would either crash with an unhandled `KeyError`/`TypeError` (producing an unhandled `500 Internal Server Error`) or process negative order totals directly into persistent storage.

---

### Question 5
**Name one part of your service where you would still choose the SOAP stack over REST, and state exactly what guarantee you would be buying. Answering "nowhere" needs a stronger argument than answering "somewhere".**

* **Chosen Service Edge:** The external financial payment gateway integration edge connecting CampusEats to external banking institutions (`PaymentGatewayService`).
* **Guarantees Bought:**
  1. **WS-AtomicTransaction (Strict Distributed ACID Guarantees):** Guarantees two-phase commit (2PC) transactions across heterogeneous institutional boundaries, ensuring that debiting user funds and committing order states either succeed atomically or roll back completely across both systems.
  2. **WS-Security (Message-Level Security):** Enables digital signatures and element-level encryption directly within the XML payload header. This guarantees end-to-end payload integrity and non-repudiation even across untrusted intermediate HTTP proxy nodes, providing stronger security than transport-layer TLS/HTTPS alone.
  ---