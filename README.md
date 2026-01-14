#  Spring Cloud Gateway (SCG)

Spring Cloud Gateway는 SentiStock 서비스에서
🌐 외부 트래픽이 처음으로 유입되는 단일 진입점(Single Entry Point) 역할을 담당합니다.
모든 클라이언트 요청은 ALB를 거쳐 Gateway로 전달되며,
이 단계에서 서비스 전반의 보안과 요청 흐름이 통제됩니다.

Gateway는 단순한 라우터가 아니라
🔐 JWT 기반 인증을 담당하는 보안 경계(Security Boundary) 로 동작합니다.
요청에 포함된 토큰을 검증한 뒤,
인증이 완료된 요청에 한해 👤 사용자 식별 정보를 헤더에 포함시켜
내부 서비스로 전달합니다.
인증되지 않은 요청은 Gateway 단계에서 즉시 차단됩니다.

이후 Gateway는 요청 경로에 따라
🔀 Backend 서비스와 Community 서비스로 요청을 라우팅합니다.
외부에서는 하나의 API 엔드포인트만 노출되지만,
내부적으로는 역할이 분리된 서비스 구조를 유지합니다.

Backend 및 Community 서비스는
Gateway를 신뢰하는 전제하에 설계되어 있으며,
Gateway를 통과한 요청은 이미 인증이 완료된 요청으로 간주합니다.
이를 통해 인증 로직은 Gateway에 집중되고,
각 서비스는 🎯 비즈니스 도메인 로직에만 집중할 수 있습니다.


## 🗂️ Project Structure (SCG)
```
📦src
 ┣ 📂main
 ┃ ┣ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂example
 ┃ ┃ ┃ ┃ ┗ 📂sentistock_scg
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CorsConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜GatewaySecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂filter
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜JwtAuthFilter.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JjwtTokenProvider.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜JwtTokenProvider.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📜SentistockScgApplication.java
 ┃ ┗ 📂resources
 ┃ ┃ ┣ 📜application-eks.yaml
 ┃ ┃ ┣ 📜application-local.yaml
 ┃ ┃ ┗ 📜application.yaml
 ┗ 📂test
 ┃ ┗ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂example
 ┃ ┃ ┃ ┃ ┗ 📂sentistock_scg
 ┃ ┃ ┃ ┃ ┃ ┗ 📜SentistockScgApplicationTests.java
```

---

## 구조 설명
config
- Gateway 보안 설정 및 CORS 설정
- 인증 필터 적용 및 라우팅 보안 정책 관리

filter
- Gateway 레벨에서 실행되는 JWT 인증 필터
- 요청 헤더의 토큰을 검증하고 사용자 정보 추출

jwt
- JWT 생성 및 검증을 담당
- 토큰 파싱 및 유효성 검사 로직 포함

resources
- 실행 환경별 설정 분리
- local / eks 환경에 따른 설정 관리
- 
---

## ⚙️ Tech Stack (SCG)

- Java 17
- Spring Boot 3.x
- Spring Cloud Gateway
- Spring Security
- JWT (Access / Refresh Token)
- Docker
- Kubernetes (EKS)
