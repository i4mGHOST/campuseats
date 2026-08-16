# CampusEats System Brief

### What the System Does
CampusEats is an online dining platform designed to streamline food ordering across university campus cafeterias, food trucks, and local vendors. It enables students and faculty to browse real-time menus, place pre-orders, make digital payments, and track meal fulfillment to eliminate long queue times during peak lunch hours.

### Who Uses It
* **Students & University Staff (Customers):** Browse vendor options, place orders, make payments, and collect orders.
* **Campus Food Vendors & Kitchen Managers:** Manage active menu inventory, accept incoming orders, update fulfillment statuses, and view daily sales analytics.
* **Campus System Administrators:** Onboard new dining vendors, manage user accounts, resolve order disputes, and oversee platform system settings.

---

### Nouns (Entities & Data Structures)

**Customer Interactive Services**
* **User Account:** Profile entity storing user credentials, role permissions, and contact details to identify the user.
* **Vendor Directory:** Searchable listing of active campus eateries and food stalls available for customer ordering.
* **Cart:** Temporary storage collection mapping selected menu items and quantities before final checkout.
* **Order Receipt:** Digital transactional record confirming meal purchase details, timestamps, and pickup codes.

**Vendor Interactive Services**
* **Vendor Profile:** Business record storing eatery operating hours, campus location, and manager contact details.
* **Menu Catalog:** Dynamic inventory structure containing dish names, pricing, descriptions, and real-time availability toggles.
* **Kitchen Order Queue:** Operational dashboard displaying incoming, active, and completed customer meal requests.

**Administrative & Platform Services**
* **Payment Gateway Record:** Financial log capturing transaction IDs, payment verification statuses, and balance audit trails.
* **Dispute Ticket:** Logged issue record generated when a customer or vendor requests resolution for order errors or refunds.
* **System Analytics Report:** Aggregated data summary tracking campus dining volume, peak order hours, and revenue metrics.

---

### Verbs (System Actions & Contracts)

**Customer Interactive Actions**
* **Authenticate User:** Validates user credentials via campus Single Sign-On (SSO) to grant access to the ordering portal.
* **Browse Vendors:** Queries and filter available campus dining outlets based on location, cuisine, or operational status.
* **Add to Cart:** Appends selected menu items and custom options to the student's active shopping session.
* **Checkout Order:** Submits cart items to create a pending order and requests payment processing.
* **Track Fulfillment:** Fetches live status updates showing whether an order is pending, preparing, or ready for pickup.

**Vendor Interactive Actions**
* **Update Inventory:** Toggles menu item availability in real time when ingredients run out during peak meal hours.
* **Accept Order:** Acknowledges incoming customer orders from the kitchen queue to initiate meal preparation.
* **Mark Ready:** Triggers an automated pickup notification to the customer once the kitchen completes meal prep.
* **Generate Sales Summary:** Compiles daily transaction totals and item sales counts for vendor financial tracking.

**Administrative & Platform Actions**
* **Onboard Vendor:** Registers and configures new campus food outlets and assigns administrative manager accounts.
* **Process Refund:** Reverses financial transactions through the payment gateway for canceled or disputed orders.
* **Audit System Logs:** Reviews system operational logs to ensure secure transactions and maintain platform stability.

---
## Summary
CampusEats unifies campus dining by establishing structured entities (nouns) like User Accounts, Menu Catalogs, and Order Receipts, alongside defined actions (verbs) such as Authenticating Users, Browsing Vendors, and Marking Orders Ready. By categorizing these entities and workflows into student, vendor, and administrative domain layers, the system ensures scalable food ordering, automated kitchen operations, and seamless payment processing across the institution.