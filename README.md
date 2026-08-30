<div align="center">

# 🚆 RailYatra — Railway Reservation & Live Tracking System

**A modern, full-stack Railway Reservation Platform with real-time RailRadar GPS train tracking, high-concurrency seat locking, and interactive travel UI.**

[![Author](https://img.shields.io/badge/Author-Sumit%20Kumar-blue.svg?style=flat-square&logo=github)](https://github.com/SumitKumar-990)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg?style=flat-square&logo=react&logoColor=black)](https://reactjs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38B2AC.svg?style=flat-square&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

</div>

---

## 🏛️ System Architecture

```mermaid
graph TD
    Client["💻 React 18 + Vite + Tailwind CSS<br/>(Single-Page App + Autocomplete + Live Radar Modal)"]
    API["⚙️ Spring Boot 3.x REST API<br/>(Security + JWT + WebSockets STOMP)"]
    RailRadar["📡 RailRadar Live API<br/>(api.railradar.in/v1)"]
    Lock["🔒 Pessimistic Write Lock<br/>(SeatInventory Concurrency Guard)"]
    DB[("💾 H2 (Dev) / Oracle SQL (Prod) Database<br/>(Inventory, Bookings, Users, Runs)")]
    ExpJob["⏰ 60s Hold Expiry Scheduled Worker"]
    QR["🔲 ZXing QR Code Generator<br/>(Base64 PNG Data URI)"]

    Client -->|REST & WebSockets| API
    API -->|Live Radar Sync| RailRadar
    API -->|Acquire Lock| Lock
    Lock --> DB
    ExpJob -->|Release Unpaid Holds| Lock
    API -->|Generate QR Code| QR
    QR --> Client
```

---

## 📂 Project File Structure

```
Railway-Reservation-System/
│
├── src/main/java/com/railway/
│   ├── config/               # Security, JWT filters, CORS, WebSockets
│   │   ├── CorsConfig.java
│   │   ├── JwtAuthFilter.java
│   │   ├── JwtService.java
│   │   ├── SecurityConfig.java
│   │   └── WebSocketConfig.java
│   │
│   ├── controller/           # REST API endpoints
│   │   ├── AdminController.java
│   │   ├── AuthController.java
│   │   ├── BookingController.java
│   │   ├── PaymentController.java
│   │   ├── StationController.java
│   │   └── TrainController.java
│   │
│   ├── dto/                  # Request & Response Data Transfer Objects
│   │   ├── admin/
│   │   ├── auth/
│   │   ├── booking/
│   │   ├── payment/
│   │   ├── railradar/        # RailRadar live status & schedule DTOs
│   │   ├── station/
│   │   └── train/
│   │
│   ├── model/                # JPA Database Entities & Enums
│   │   ├── Booking.java
│   │   ├── Passenger.java
│   │   ├── Payment.java
│   │   ├── SeatInventory.java   # Concurrency locked table
│   │   ├── Station.java
│   │   ├── Train.java
│   │   └── User.java
│   │
│   ├── repository/           # Spring Data JPA Repositories
│   │   ├── BookingRepository.java
│   │   ├── SeatInventoryRepository.java  # @Lock(PESSIMISTIC_WRITE)
│   │   ├── StationRepository.java
│   │   └── TrainRepository.java
│   │
│   ├── seed/                 # Auto-seeding for nationwide fleet & test users
│   │   └── DataSeeder.java
│   │
│   ├── service/              # Core business logic
│   │   ├── AuthService.java
│   │   ├── BookingExpiryService.java   # 60s hold cleanup worker
│   │   ├── BookingService.java         # Waitlist waterfall cascade
│   │   ├── FareCalculator.java
│   │   ├── PaymentService.java
│   │   ├── QrCodeService.java          # ZXing base64 ticket generator
│   │   ├── RailRadarService.java       # Real-time GPS & timetable sync
│   │   └── TrainSearchService.java
│   │
│   └── RailwayApplication.java
│
├── frontend/                 # React 18 + Vite + Tailwind CSS Single-Page App
│   ├── src/
│   │   ├── api/client.js            # Axios client with auth interceptors
│   │   ├── components/              # UI components
│   │   │   ├── ConfirmDialog.jsx
│   │   │   ├── LiveTrackingModal.jsx # RailRadar live tracking popup
│   │   │   ├── Navbar.jsx
│   │   │   ├── StationAutocomplete.jsx
│   │   │   ├── TicketCard.jsx        # Boarding pass with QR display
│   │   │   └── TrainCard.jsx
│   │   ├── context/AuthContext.jsx   # Global user auth state
│   │   ├── pages/                   # Application views
│   │   │   ├── AdminDashboardPage.jsx
│   │   │   ├── BookingDetailPage.jsx
│   │   │   ├── BookingPage.jsx
│   │   │   ├── LoginPage.jsx
│   │   │   ├── MyBookingsPage.jsx
│   │   │   ├── PaymentPage.jsx
│   │   │   └── SearchPage.jsx
│   │   └── App.jsx
│   ├── tailwind.config.js
│   └── vite.config.js
│
├── pom.xml                   # Maven dependencies & build configuration
└── README.md
```

---

## ✨ Key Features

- 🛰️ **Live RailRadar GPS Tracking:** Real-time running delay, next halts, platform numbers, and coach composition (`ENG-SL1-3A1-2A1...`).
- 🔒 **Concurrency-Proof Seat Engine:** Database-level `PESSIMISTIC_WRITE` locks prevent double bookings during high-traffic surges.
- 🎟️ **Deterministic Seat Allocation:** Instant classification into `CONFIRMED`, `RAC` (Reservation Against Cancellation), or `WAITLISTED`.
- 🌊 **Automatic Waitlist Cascade:** Instant waterfall promotion on ticket cancellation (`RAC` $\rightarrow$ `CONFIRMED`, `WAITLISTED` $\rightarrow$ `RAC`/`CONFIRMED`).
- ⏱️ **15-Minute Unpaid Hold Expiry:** Scheduled background worker automatically releases unconfirmed seat holds.
- 🎫 **Digital QR Boarding Pass:** Generates scannable Base64 QR-code tickets using Google ZXing.
- 🔍 **Fuzzy Station Search & Autocomplete:** Search by station code (`NDLS`), name (`New Delhi`), or city (`Delhi`) with 1-click popular routes.
- 🛡️ **JWT Security & Roles:** Role-based access control with BCrypt password hashing for `CUSTOMER` and `ADMIN`.

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17+, Spring Boot 3.3.4, Spring Security (JWT), Spring Data JPA (Hibernate), Spring WebSocket (STOMP), H2 / Oracle SQL, Google ZXing |
| **Frontend** | React 18, Vite 5, Tailwind CSS, Lucide Icons, Axios, React Router 6 |
| **Integration** | RailRadar Live REST API (`api.railradar.in/v1`) |

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (JDK 17, 21, or 25)
- **Node.js 18+** & **npm**
- **Git**

### 1. Clone the Repository
```bash
git clone https://github.com/SumitKumar-990/Railway-Reservation-System.git
cd Railway-Reservation-System
```

### 2. Run Backend (Spring Boot)
```bash
# On Windows PowerShell:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# On Linux / macOS:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
- REST API: `http://localhost:8080`
- H2 Database Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:railwaydb`, User: `sa`, Password: *(blank)*)

### 3. Run Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
- Frontend Web App: **`http://localhost:5173`**

---

## 🔑 Demo Accounts

Both test accounts are pre-seeded and accessible via **1-Click Login buttons** on the sign-in page:

| Role | Email | Password | Access |
|---|---|---|---|
| **Customer** | `user@railway.com` | `user1234` | Search trains, book seats, mock payments, track live status, download QR ticket |
| **Admin** | `admin@railway.com` | `admin123` | Fleet management, live class-wise seat occupancy inspector, run cancellation |

---

## 🧪 Automated Testing

```powershell
# API Lifecycle Integration Test (15/15 checks)
powershell -ExecutionPolicy Bypass -File scratch/api_test.ps1

# 10-Thread Concurrency Stress Test
powershell -ExecutionPolicy Bypass -File scratch/concurrency_test.ps1

# Admin Security Test
powershell -ExecutionPolicy Bypass -File scratch/admin_test.ps1
```

---

## 👨‍💻 Author

**Sumit Kumar**  
- GitHub: [@SumitKumar-990](https://github.com/SumitKumar-990)
- Repository: [Railway-Reservation-System](https://github.com/SumitKumar-990/Railway-Reservation-System.git)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).