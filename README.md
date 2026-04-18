# Smart Campus Sensor & Room Management API

A RESTful API built with Java JAX-RS (Jersey) and Grizzly HTTP server.
No database — uses in-memory ConcurrentHashMap for data storage.

## How to Build and Run

### Requirements
- Java 11
- Apache Maven
- Apache NetBeans (or any IDE)

### Steps
1. Clone the repository:
   git clone https://github.com/YOUR_USERNAME/smart-campus-api.git

2. Open in NetBeans → right-click project → Clean and Build

3. Right-click Main.java → Run File

4. Server starts at: http://localhost:8080/api/v1/

---

## API Endpoints

### Discovery
GET /api/v1/

### Rooms
GET    /api/v1/rooms
POST   /api/v1/rooms
GET    /api/v1/rooms/{id}
DELETE /api/v1/rooms/{id}

### Sensors
GET  /api/v1/sensors
GET  /api/v1/sensors?type=CO2
POST /api/v1/sensors
GET  /api/v1/sensors/{id}

### Readings
GET  /api/v1/sensors/{id}/readings
POST /api/v1/sensors/{id}/readings

---

## Example curl Commands

### Create a room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"R1\",\"name\":\"Lab A\",\"capacity\":30}"

### Create a sensor
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"S1\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"R1\"}"

### Get sensors filtered by type
curl http://localhost:8080/api/v1/sensors?type=CO2

### Add a reading
curl -X POST http://localhost:8080/api/v1/sensors/S1/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":450.5,\"unit\":\"ppm\"}"

### Delete a room (fails with 409 if sensors exist)
curl -X DELETE http://localhost:8080/api/v1/rooms/R1

---

## HTTP Status Codes Used

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Missing required field |
| 403 | Forbidden | Reading on MAINTENANCE sensor |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Delete room with active sensors |
| 422 | Unprocessable Entity | Invalid roomId reference |
| 500 | Internal Server Error | Unexpected server error |

---

## Report Q&A Answers

### Q1 — JAX-RS lifecycle and thread safety (Part 1.1)
JAX-RS creates a new resource class instance for every HTTP request
by default (request-scoped). This means instance variables are not
shared between requests, but static fields are. To safely share
in-memory data across all requests, I used ConcurrentHashMap which
provides thread-safe read and write operations without explicit
synchronization. This prevents race conditions when multiple clients
hit the API simultaneously.

### Q2 — HATEOAS justification (Part 1.2)
The discovery endpoint implements HATEOAS (Hypermedia as the Engine
of Application State) by embedding resource links directly in the
response. This makes the API self-documenting — clients can discover
available endpoints without reading external documentation. It also
reduces coupling between client and server because if a URL changes,
the client follows the updated link rather than hardcoding paths.

### Q3 — ID-only vs full object in list responses (Part 2.1)
Returning only IDs minimises payload size and bandwidth consumption,
but forces clients to make N additional requests to fetch details —
known as the N+1 problem. Returning full objects increases payload
but allows immediate rendering. For this API, full objects are
returned in list responses since the dataset is small and the
client benefits from having all data in one request.

### Q4 — Idempotency of DELETE (Part 2.2)
DELETE is idempotent. The first call removes the room and returns
204 No Content. Subsequent identical calls return 404 Not Found
because the resource no longer exists. The server state is
identical after both calls — no room with that ID exists — so the
outcome is idempotent even though the HTTP status code differs.

### Q5 — @Consumes mismatch and 415 (Part 3.1)
If a client sends Content-Type: text/plain to an endpoint annotated
with @Consumes(MediaType.APPLICATION_JSON), JAX-RS automatically
returns 415 Unsupported Media Type before the method code executes.
The framework enforces the media type contract, so no manual
checking is needed inside the method.

### Q6 — QueryParam vs PathParam for filtering (Part 3.2)
Query parameters are semantically correct for filtering, searching,
and optional refinement of a collection. Path parameters identify
a specific unique resource. /sensors?type=CO2 correctly means
"give me the sensors collection filtered by type". Using
/sensors/type/CO2 would wrongly imply CO2 is a distinct resource
with its own identity. Query parameters also compose easily:
?type=CO2&status=ACTIVE filters by multiple criteria simultaneously.

### Q7 — Sub-resource locator pattern (Part 4.1)
Delegating nested paths to a separate SensorReadingResource class
keeps each class focused on a single responsibility. The main
SensorResource handles sensor-level concerns only, while
SensorReadingResource handles reading-level concerns only. In large
APIs with many nested routes, this prevents a single monolithic
controller that becomes unmaintainable. It also makes unit testing
easier since each class can be tested independently.

### Q8 — 422 vs 404 for bad roomId (Part 5.1)
404 Not Found means the requested URL does not exist. Here the URL
/api/v1/sensors is perfectly valid. The problem is that the roomId
value inside the JSON body references a non-existent entity. 422
Unprocessable Entity precisely signals that the request is
syntactically correct JSON but semantically invalid due to a broken
reference. This gives the client far more actionable information
than a generic 404.

### Q9 — Stack trace security risk (Part 5.2)
Exposing Java stack traces reveals internal package names, class
names, library versions, and line numbers. An attacker can use
this to identify known CVEs in specific library versions, understand
the internal architecture to craft targeted exploits, and locate
error-prone code paths for further probing. The GlobalExceptionMapper
intercepts all Throwable types and returns only a clean 500 JSON
response, logging the real error server-side only where it is safe.
