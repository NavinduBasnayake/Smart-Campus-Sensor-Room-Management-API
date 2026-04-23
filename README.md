# Smart Campus API

A RESTful web service built with JAX-RS (Jersey) and Apache Tomcat for managing Rooms and Sensors across a university campus. All data is stored in-memory using `HashMap` and `ArrayList`. No database is used.

---

## API Overview

The API runs at the base path `/api/v1`. It has four main resource areas:

| Resource | Path | What it does |
|---|---|---|
| Discovery | `GET /api/v1/` | Returns API version, contact info, and links to all resources |
| Rooms | `/api/v1/rooms` | Create, list, view, and delete campus rooms |
| Sensors | `/api/v1/sensors` | Register and view sensors installed in rooms |
| Sensor Readings | `/api/v1/sensors/{sensorId}/readings` | Log and view historical readings for a sensor |

---

## How to Build and Run

**Requirements:**
- NetBeans 18+
- Apache Tomcat 9.x (added to NetBeans)
- Java 8+

**Steps:**

1. Clone the repository:
   ```bash
   git clone https://github.com/NavinduBasnayake/Smart-Campus-Sensor-Room-Management-API
   ```

2. Open the project in NetBeans:

   File → Open Project → select the SmartCampusAPI folder

3. Clean and build:

   Right-click the project → Clean and Build

   Maven will download all required dependencies automatically

4. Run the project:

   Right-click the project → Run

   NetBeans will deploy the app to Tomcat

5. The API is now live at:

   ```
   http://localhost:8080/SmartCampusAPI/api/v1/
   ```

---

## Sample curl Commands

**1. Create a new room (expect 201 Created):**

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CS-201\",\"name\":\"CS Lab\",\"capacity\":40}"
```

**2. Get the room by its ID (expect 200 OK):**

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/CS-201
```

**3. Create a sensor with a valid roomId (expect 201 Created):**

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIGHT-004\",\"type\":\"Light\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"CS-301\"}"
```

**4. Filter sensors by type (expect 200 OK with matching sensors only):**

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"
```

**5. Post a new reading to a sensor (expect 201 Created with UUID and timestamp):**

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":24.7}"
```

**6. Get all readings for a sensor (expect 200 OK with reading history):**

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```

**7. Get the sensor to verify currentValue was updated to 24.7 (expect 200 OK):**

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001
```

---

## Report — Answers to Coursework Questions

### Part 1 — Q1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance is instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and Synchronise your in-memory data structures (maps/lists) to prevent data loss or race conditions.

JAX-RS will create a fresh instance for every new request made therefore any data stored inside will no longer exist after end of the request. To keep track of data, create a static variable in a common class (such as DataStore.java) so that all requests will access and use the same static data. Because there could be multiple requests modifying the data, use ConcurrentHashMap instead of HashMap to make sure that everything is safe.

---

### Part 1 - Q2: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS results in an API informing its client of what actions to take next by returning hyperlinks within a response payload. Due to this mechanism, the client does not need to figure out the proper URL or through separate documentation how to perform an action. One advantage is that the links are always accurate; therefore, the client will still work correctly by following the links, even if their URL is changed. This is different from static documentation, which can become more of a liability due to potential changes in the API.

---

### Part 2 - Q3: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

In case the list contains only IDs, the client needs to make one additional request to obtain details on each item. Thus 50 rooms = 51 requests in total and is slow and inefficient (N+1 problem). When the API returns full objects, it is all in a single request. This can be done much quicker and efficiently than numerous small requests since each room contains very few fields.

---

### Part 2 - Q4: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, Idempotent means sending the same request multiple times gives the same final result on the server. For example, the first DELETE /rooms/CS-201 removes the room. If you send it again, the room is already gone, so the server says "not found." The response may differ, but the result is the same — the room doesn't exist. This makes retries safe, especially on slow or unstable networks.

---

### Part 3 - Q5: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

`@Consumes(MediaType.APPLICATION_JSON)` indicates that the method can only deal with the JSON data. In case the client transmits data in a new format such as XML or plain text, JAX-RS will not be able to run the method and will automatically send 415 Unsupported Media Type. This makes it clear to the client that it is the data format and not the URL that is the problem.

---

### Part 3 - Q6: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using `?type=CO2` is correct because query parameters are meant for optional filtering. The main URL `/api/v1/sensors` represents the full sensor list, and `?type=CO2` just narrows it down. Putting it in the path like `/sensors/type/CO2` is not ideal because it makes filters look like real resources and becomes messy when adding more filters (e.g., `?type=CO2&status=ACTIVE`). Query params stay clean, flexible, and optional.

---

### Part 4 - Q7: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

A sub-resource locator allows a root class to hand off part of the URL to a separate sub-resource class. In this project, SensorResource forwards `/{sensorId}/readings` to SensorReadingResource, and JAX-RS then routes the request to that class. This keeps things clean — one class handles sensors, another handles readings. Without this separation, one class would grow too large and become difficult to manage. Splitting them makes the code easier to read, maintain, test, and update.

---

### Part 5 - Q8: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

404 indicates a wrong or non-existent URL. However, in `POST /api/v1/sensors`, the endpoint is correct, but there is a problem with the request body, the roomId is invalid or absent. This is why 422 Unprocessable Entity is superior. It explicitly states: request has been sent to the server and is valid, however, the information within is incorrect. Using 404 the client would erroneously believe that the URL is wrong and debug the incorrect thing.

---

### Part 5 - Q9: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Class names, library versions, file paths and the line number at which the code failed can be gathered from this stack trace. Attackers use this information to create a mapping of the code base and identify known vulnerabilities. An attacker then can easily test various attack methods that exploit the weak points within the application as well. The GlobalExceptionMapper prevents this by trapping all errors and only returning a simple '500 Internal Server Error' message without any additional internal details about the error.

---

### Part 5 - Q10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Putting `Logger.info()` in each method implies that the same code has to be repeated across all methods. You can easily lose track of it, and your classes are cluttered with logging rather than their primary task. This is addressed by a JAX-RS filter which does all the logging centrally. In the case of the `@Provider`, all requests and responses automatically pass through it, meaning you do not need to access your resource classes. When you modify the logging format, it is applied everywhere leaving your code clean and structured.
