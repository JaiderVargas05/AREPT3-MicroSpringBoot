# AREPT3 — MicroSpringBoot

A minimal “Apache-like” **HTTP server in Java** plus a tiny **IoC micro-framework** (annotations-based) to expose endpoints from **POJOs**.  
It serves **HTML/CSS/JS/PNG/JPG** from a static folder, publishes services via `@RestController`, `@GetMapping`, `@RequestParam`, and —as required— handles **multiple non-concurrent requests**.

---

## Overview

- **HTTP/1.1 server** that serves static assets from `src/main/resources/static`.
- **IoC framework**: reflective registration of classes annotated with `@RestController` and methods with `@GetMapping`.
- **Parameter injection** via `@RequestParam(value, defaultValue)`.
- **Example controller**: `GreetingController` under `escuelaing.edu.co.microspringboot.examples`.

---

## Lab Objectives

1. Build a Java **web server** that serves **HTML** and **PNG/JPG**.  
2. Provide a simple **IoC framework** to build web apps from **POJOs** using reflection.  
3. **First version**: load the **POJO from the command line** (similar to test frameworks).  
4. **Final version**: explore the **classpath** and auto-load all `@RestController` components.  
5. Support **`@GetMapping`** and **`@RequestParam`** with **String** return type.  
6. Support the following component:

```java
package escuelaing.edu.co.microspringboot.examples;

import annotations.GetMapping;
import annotations.RequestParam;
import annotations.RestController;

@RestController
public class GreetingController {
    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}
```

---

## Architecture

### Main components

- **`HttpServer`** — Listens on **port 35000**, parses the request line & headers, and routes:
  - **`/app/...`** → invokes the annotated controller method (IoC / reflection).
  - All other paths → **static files** from `src/main/resources/static`.
  - Writes proper HTTP/1.1 responses with `Content-Type`, `Content-Length`, `Connection: close`.

- **Annotations & IoC**
  - `@RestController` on a class (POJO).
  - `@GetMapping("/path")` on a method (return type **String**).
  - `@RequestParam(value, defaultValue)` on parameters.
  - A route registry: `Map<String, Method>` mapping `"/path"` → controller method.

### Request flow

```
Client
  |
  v
[HttpServer] --accept--> [Socket]
  | parse "METHOD /path HTTP/1.1"
  +--> if path starts with /app -----> [IoC Router] -> [invoke @GetMapping method]
  |                                      | resolve @RequestParam from query/default
  |                                      '-- returns String (response body)
  '--> else ----------------------------> [Static Handler] -> read file -> MIME + bytes

[HttpServer] writes:
- Status line + headers (Content-Type, Content-Length, Connection: close)
- CRLF (\r\n)
- Body
then closes the socket
```

---

## Prerequisites

- **Java SDK 21**  
- **Apache Maven 3.9+**  
- **Git**

---

## Getting Started

Clone and build:

```bash
git clone https://github.com/JaiderVargas05/AREPT3-MicroSpringBoot.git
cd AREPT3-MicroSpringBoot/Microspringboot
mvn clean install
```

### Option A — First version (POJO passed via command line)

Pass the controller’s **FQN** to the main class:

```bash
# Linux/macOS
java -cp target/classes   escuelaing.edu.co.microspringboot.Microspringboot   escuelaing.edu.co.microspringboot.examples.GreetingController

# Windows
java -cp target/classes escuelaing.edu.co.microspringboot.Microspringboot escuelaing.edu.co.microspringboot.examples.GreetingController
```

Server URL: **http://localhost:35000**

### Option B — Classpath scanning

```bash
java -cp target/classes escuelaing.edu.co.microspringboot.Microspringboot
```

---

## Static Files

Place assets under `src/main/resources/static`:

- `GET /` → `index.html`  
- `GET /index.html` → HTML  
- `GET /styles.css` → CSS  
- `GET /code.js` → JS  
- `GET /james.jpg` → JPG 

---

## IoC Endpoints

By default, annotated endpoints are exposed under **`/app`**:

- **Default parameter (uses `defaultValue`)**
  ```
  GET http://localhost:35000/app/greeting
  → Hola World
  ```

- **With `name`**
  ```
  GET http://localhost:35000/app/greeting?name=Jaider
  → Hola Jaider
  ```

---

## Tests

Run all tests:

```bash
mvn test
```

Coverage includes:
- **Server up & index** — `GET /` returns `200` and `text/html`.  
- **Static files** — `index.html`, `styles.css`, `james.jpg` with correct MIME types and non-empty bodies.  
- **404** — `GET /eci.com` → `404 Not Found`.  
- **IoC** — `GET /app/greeting` and `GET /app/greeting?name=...` → `200`, `text/plain; charset=utf-8`, expected body.
---

## Design Notes

- **HTTP 1.1 by hand**: status line + headers + CRLF + body via `OutputStream`.
- **Reflection-based IoC**: map `path → Method` from `@RestController` classes.
- **String-only return** (per lab scope).

---

## Known Limitations

- Only **GET** for IoC endpoints; return type **String**.  
- Parameters injected as **String** only.
- Basic MIME map.

---

## Author

**Jaider Vargas**  
Repo: https://github.com/JaiderVargas05/AREPT3-MicroSpringBoot
