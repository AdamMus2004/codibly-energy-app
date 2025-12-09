# Energy Mix UK - Backend API

---

## Project Overview

Backend service for the Energy Mix UK application.
Calculates optimal EV charging windows based on Carbon Intensity API data.

---

## Tech Stack
* **Java 21** (Dockerized) / **Java 23** (Local)
* **Spring Boot 3.2.5**
* **Maven**
* **JUnit 5 & Mockito**
* **Docker**

---

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/energy-mix/daily` | Returns energy generation data for Today, Tomorrow, and Day After Tomorrow. |
| `GET` | `/api/energy-mix/optimal-window` | Calculates the best time to charge an EV (highest clean energy share). |

---

## Running the Project

### Local (Maven)
```bash
mvn clean install
mvn spring-boot:run
```

---

## Testing
Run unit tests with:
```bash
mvn test
```
