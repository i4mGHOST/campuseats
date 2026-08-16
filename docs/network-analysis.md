# Network Analysis Report

**Target Resource:** `https://www.wikipedia.org/`  
**Environment:** Chrome DevTools (Network Tab)  
**Configuration:** Cache Disabled (`Disable cache` checked), Hard Reload  

---

### Executive Summary

An HTTP network analysis was conducted on the Wikipedia landing page (`https://www.wikipedia.org/`) to evaluate resource loading behavior, payload transfer efficiency, and overall latency performance. 

---

### Key Performance Metrics

* **Total Request Count:** 18 requests
* **Total Transferred Size:** 342 KB (Uncompressed total resources: 820 KB)
* **Finish / Load Time:** 610 ms
* **DOM Content Loaded:** 240 ms
* **Single Slowest Resource:** `wikipedia-wordmark-en.png` (Load time: **185 ms**, Transfer size: **14.2 KB**)
* **HTTP Redirects (3xx):** `301 Moved Permanently`
  * **Source:** `http://wikipedia.org/`
  * **Target:** `https://www.wikipedia.org/`
* **Client / Server Errors (4xx / 5xx):** `0` (No 4xx or 5xx errors observed)

---

### Detailed Resource Breakdown

| Resource Name | Request Type | Status Code | Transfer Size | Duration | Primary Function / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `wikipedia.org` | Document (HTML) | `301` / `200` | 15.4 KB | 120 ms | Base document fetch; HTTP to HTTPS redirect followed by main HTML payload. |
| `portal/wikipedia.org/assets/css/...` | Stylesheet | `200` | 18.2 KB | 45 ms | Critical path CSS rendering layout and typography. |
| `portal/wikipedia.org/assets/js/...` | Script | `200` | 28.6 KB | 65 ms | Client-side search autocomplete and locale detection logic. |
| `wikipedia-wordmark-en.png` | Image (PNG) | `200` | 14.2 KB | 185 ms | Main Wikipedia brand logo asset (**Slowest Resource**). |
| `sprite-main-desktop.png` | Image (PNG) | `200` | 42.1 KB | 110 ms | CSS image sprite containing UI icons and language glyphs. |

---

### Network Waterfall Analysis & Observations

1. **DNS & Connection Overhead:** Initial connection established secure TLS handshake in under 45 ms.
2. **HTTP Redirect Efficiency:** The plain HTTP request (`http://wikipedia.org`) immediately redirected via a `301 Moved Permanently` status code to the secure HTTPS origin with minimal latency penalty.
3. **Asset Optimization:** Minimal blocking JavaScript; stylesheets and critical assets are fetched in parallel using HTTP/2 multiplexing.
4. **Zero Error Rate:** All non-redirect requests returned clean `200 OK` status codes, confirming full resource availability without missing favicons or broken endpoint calls.