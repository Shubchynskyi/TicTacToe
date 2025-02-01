# Tic-Tac-Toe Web Application

[![Live Application](https://img.shields.io/badge/Live-Application-brightgreen)](https://tic-tac-toe.shubchynskyi.pp.ua)

A web application for playing Tic-Tac-Toe with multiple game modes, advanced AI opponents, local two-player matches, and real-time online games via WebSocket. 
Built with adaptive layouts for desktops, tablets, and mobile devices.

---

## Overview

This application showcases professional Java web development using Spring Boot and WebSockets. It allows players to:

- **Single game** with four difficulty setting.
- **Play locally** with two players on the same device.
- **Compete online** in real-time matches.

An inactivity **timer** ensures stale games are cleaned up automatically, preventing resource bloat on the server. 
Comprehensive testing (unit, integration, and end-to-end with Selenium) ensures the application’s reliability.

---

## Technologies Used

- **Backend**
    - Java, Spring Boot
    - Maven for build & dependency management
    - _Chain of Responsibility_ pattern for difficulty
- **Frontend**
    - Thymeleaf templates, HTML5, CSS + Bulma
    - JavaScript for interactive UI elements
- **WebSocket**
    - Spring’s `SimpMessagingTemplate` for real-time messaging
- **Testing**
    - **JUnit** and **Mockito** for unit tests
    - **Selenium WebDriver** for browser-based end-to-end testing
    - **Integration Tests** for service layers
- **Deployment & CI/CD**
    - Docker
    - Jenkins for automated builds and tests

---

## Contact

For any questions or further information, please contact [d.shubchynskyi@gmail.com](mailto:d.shubchynskyi@gmail.com)
   ```
   d.shubchynskyi@gmail.com
   ```