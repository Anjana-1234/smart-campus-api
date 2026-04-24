# Smart Campus Sensor & Room Management API

A RESTful API built with Java JAX-RS (Jersey) and Grizzly HTTP server.
No database - uses in-memory ConcurrentHashMap for data storage.

## How to Build and Run

### Requirements
- Java 11
- Apache Maven
- Apache NetBeans (or any IDE)

### Steps
1. Clone the repository:
   git clone https://github.com/Anjana-1234/smart-campus-api.git

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

### Q1 - JAX-RS Resource Lifecycle and In-Memory Data Management (Part 1.1)
By default, JAX-RS creates a brand-new instance of each resource class for every incoming HTTP request. This is called request-scoped lifecycle. Because each request gets its own object instance, any data stored as an instance variable inside a resource class is destroyed at the end of that request and is never shared with other requests. 
This design decision has a direct impact on how in-memory data must be managed. Since resource instances cannot hold shared state, all data that needs to persist between requests must be stored in a shared static structure. In this implementation, a dedicated DataStore class holds static ConcurrentHashMap fields for rooms, sensors, and readings. ConcurrentHashMap was chosen specifically because it is thread-safe by design multiple simultaneous requests can read and write to it without causing race conditions or data corruption, which would occur with a regular HashMap in a multi-threaded server environment.


### Q2 - HATEOAS and Hypermedia-Driven API Design (Part 1.2)
HATEOAS (Hypermedia as the Engine of Application State) is considered a hallmark of advanced RESTful design because it makes the API self-describing. Instead of requiring client developers to read separate static documentation to discover available endpoints, a hypermedia-driven API embeds navigation links directly inside its responses. The discovery endpoint at GET /api/v1 returns a JSON object containing the URLs for the rooms and sensors collections, so a client can immediately know where to go next without any prior knowledge of the API structure. 
This benefits client developers significantly. If a URL changes on the server side, the client automatically follows the updated link without requiring a code change. It also reduces the coupling between client and server, making the API more resilient to structural changes over time.


### Q3 - Returning IDs vs Full Objects in List Responses (Part 2.1)
Returning only IDs from a list endpoint minimises payload size and reduces bandwidth consumption, which is beneficial when the collection is large. However, it forces the client to make N additional HTTP requests to fetch the details for each item - a problem known as the N+1 request problem. This increases total latency and places more load on the server. Returning full objects in the list response increases the payload size but allows the client to render all data immediately with a single request. For this Smart Campus API, full room objects are returned in the list response because the dataset is relatively small, the client needs room details immediately, and a single round trip is more efficient than multiple follow-up requests.

### Q4 - Idempotency of the DELETE Operation (Part 2.2)
The DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it once. 
The first DELETE /api/v1/rooms/R1 request successfully removes the room and returns 204 No Content. If a client mistakenly sends the exact same DELETE request again, the server returns 404 Not Found because the room no longer exists. Although the HTTP status code differs between the first and subsequent calls, the server state is identical after each call -  no room with that ID exists in the system. This satisfies the definition of idempotency because the observable effect on server state does not change with repeated identical requests.


### Q5 - @Consumes mismatch and 415 (Part 3.1)
The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the POST endpoint only accepts requests with Content-Type: application/json. If a client sends data with a different Content-Type header, such as text/plain or application/xml, JAX-RS automatically intercepts the request before it reaches the method body and returns an HTTP 415 Unsupported Media Type response. This enforcement happens entirely at the framework level. No manual checking is required inside the method. This is a key benefit of JAX-RS, the framework handles contract validation automatically, ensuring that only correctly formatted JSON payloads ever reach the business logic, which improves reliability and reduces defensive coding

### Q6 - QueryParam vs PathParam for filtering (Part 3.2)
Query parameters and path parameters serve fundamentally different purposes in REST design. Path parameters such as /rooms/{id} are used to identify a specific unique resource by its identity. Query parameters such as /sensors?type=CO2 are used for optional filtering, searching, and refinement of a collection. 
Using GET /api/v1/sensors?type=CO2 is semantically correct because it means "give me the sensors collection, filtered by type". Using /api/v1/sensors/type/CO2 would be incorrect because it implies that CO2 is a distinct resource with its own identity, which it is not. Query parameters are also superior because they compose naturally multiple filters can be combined easily, for example ?type=CO2&status=ACTIVE whereas path-based filtering would require deeply nested URL structures that become difficult to maintain and document.


### Q7 - Sub-resource locator pattern (Part 4.1)
The Sub-Resource Locator pattern delegates the handling of nested paths to dedicated resource classes rather than defining every possible path in a single large controller. In this implementation, SensorResource handles sensor-level operations and returns a new instance of SensorReadingResource to handle all paths under /sensors/{id}/readings. 
This approach offers significant architectural benefits. Each class has a single, clear responsibility, which makes the codebase easier to understand and maintain. As the API grows with more nested resources, new classes can be added without modifying existing ones. It also makes unit testing simpler because each class can be tested in isolation. In contrast, placing all nested path logic inside one massive controller class would violate the Single Responsibility Principle and quickly become unmanageable in a real-world API with dozens of resource types.


### Q8 - 422 vs 404 for bad roomId (Part 5.2)
HTTP 404 Not Found means the requested URL or endpoint does not exist on the server. In the scenario where a client POSTs a new sensor with a roomId that does not exist, the URL /api/v1/sensors is perfectly valid and the server can find it without any problem. The issue is not with the URL  it is with the content of the request payload. The JSON body is syntactically correct but semantically invalid because it contains a reference to a roomId that does not exist in the system. HTTP 422 Unprocessable Entity is the correct status because it specifically means the server understands the content type and the syntax of the request, but cannot process it because of logical errors in the data. This gives the client far more precise and actionable information than a 404, which would incorrectly suggest the endpoint itself was not found.

### Q9 - Stack trace security risk (Part 5.4)
Exposing raw Java stack traces to external API consumers creates serious security vulnerabilities. A stack trace reveals the internal package structure and class names of the application, the specific third-party libraries and their exact version numbers being used, the file names and line numbers where errors occur, and the internal logic flow of the application. 
An attacker can use this information to identify known CVEs (Common Vulnerabilities and Exposures) in the specific library versions shown in the trace, to understand the internal architecture well enough to craft targeted exploits, and to locate error-prone code paths for further probing through deliberate malformed requests. The GlobalExceptionMapper in this implementation intercepts all Throwable types and returns only a clean, generic 500 JSON response to the client. The real error details are logged server-side only using java.util.logging.Logger, where they are visible to developers but never exposed to external consumers.

