# SeCause Backend

<div align="center">

### AI 기반 코드 보안 취약점 분석 서비스의 백엔드 API

<br />

<img
width="600"
alt="SeCause 서비스 화면"
src="https://github.com/user-attachments/assets/5c663f26-90cf-470e-899c-52d6ca8a8250"
/>

<br />
<br />

GitHub 인증부터 저장소 분석 요청, 취약점 결과 조회까지<br />
SeCause의 핵심 비즈니스 로직과 데이터 흐름을 담당합니다.

<br />

[**SeCause 시작하기 →**](https://www.secause.site) · [**API 문서 →**](http://localhost:8080/swagger-ui.html)

</div>

<br />

## 서비스 소개

[SeCause](https://www.secause.site)는 GitHub 저장소의 코드를 분석해 잠재적인 보안 취약점을 발견하고, 문제의 원인과 안전한 수정 방향을 제시하는 **AI 기반 코드 보안 분석 서비스**입니다.

SeCause Backend는 프론트엔드와 GitHub, 분석 서버 사이에서 요청을 조율합니다. 사용자의 GitHub 계정을 인증하고 접근 가능한 저장소와 브랜치를 조회하며, 분석 작업을 비동기로 전달합니다. 분석이 완료되면 취약점의 심각도와 유형, 파일별 탐지 결과 및 수정 가이드를 API로 제공합니다.

<br />

## 주요 기능

- **GitHub OAuth 인증** — GitHub 계정으로 로그인하고 JWT 기반 인증 정보를 발급·갱신합니다.
- **저장소 및 브랜치 조회** — 사용자가 접근할 수 있는 GitHub 계정, 저장소, 브랜치 목록을 제공합니다.
- **비동기 분석 요청** — 분석 요청을 저장한 뒤 이벤트 기반 큐를 통해 Analysis API로 전달합니다.
- **분석 상태 관리** — 요청부터 처리 완료까지 분석 진행 상태를 조회할 수 있습니다.
- **취약점 결과 조회** — 저장소의 취약점을 심각도별로 요약하고 파일 및 이슈 단위 상세 정보를 제공합니다.
- **사용자 정보 관리** — 로그인한 사용자의 프로필을 조회하고 수정합니다.
- **일관된 API 응답 및 예외 처리** — 공통 응답 형식과 도메인별 오류 코드를 제공합니다.
- **OpenAPI 문서화** — Swagger UI를 통해 API 명세를 확인하고 테스트할 수 있습니다.

<br />

## Backend 기술 스택

<div align="center">

| Category | Technologies |
| --- | --- |
| **Language & Framework** | Java 21, Spring Boot 4 |
| **Web & API** | Spring MVC, WebClient, Springdoc OpenAPI |
| **Security** | Spring Security, JWT, GitHub OAuth |
| **Database** | PostgreSQL, Spring Data JPA, QueryDSL |
| **Async Processing** | Spring Event, `@Async` |
| **Build & Test** | Gradle, JUnit 5 |
| **Infrastructure** | Docker, GitHub Actions |

</div>

<br />

## 아키텍처

<img width="820" height="620" alt="SeCause_architecture drawio" src="https://github.com/user-attachments/assets/50eb881f-3ea5-474c-97eb-7a1b9ece97c8" />


```text
GitHub API ───── SeCause Frontend
     │                  │
     └───────┬──────────┘
             ▼
      SeCause Backend
       ├─ Authentication
       ├─ Repository Management
       ├─ Analysis Orchestration
       └─ Result API
             │
      ┌──────┴──────┐
      ▼             ▼
 PostgreSQL    Analysis API
                    │
                    ▼
             Security Analysis
```

프론트엔드에서 저장소와 브랜치를 선택해 분석을 요청하면 백엔드는 요청 정보를 저장하고 Analysis API에 비동기로 작업을 전달합니다. 분석 결과는 PostgreSQL에 저장되며, 백엔드는 이를 저장소·파일·이슈 단위로 가공해 프론트엔드에 제공합니다.

<br />

## 파일 구조

```text
SeCause-BE
├── src
│   ├── main
│   │   ├── java/SeCause/SeCause_be
│   │   │   ├── domain
│   │   │   │   ├── analysis          # 분석 요청, 상태 관리 및 분석 서버 연동
│   │   │   │   ├── auth              # GitHub OAuth 및 토큰 발급
│   │   │   │   ├── projectRepository # 저장소별 취약점 결과 조회
│   │   │   │   ├── security          # 보안 문서 및 참고 자료
│   │   │   │   ├── user              # 사용자 정보 관리
│   │   │   │   └── vulnerability     # 취약점 엔티티 및 심각도 모델
│   │   │   ├── global
│   │   │   │   ├── apiPayload        # 공통 응답, 오류 코드 및 예외 처리
│   │   │   │   ├── config            # Security, Swagger, QueryDSL 등 설정
│   │   │   │   ├── entity            # 공통 엔티티
│   │   │   │   └── security          # JWT 인증 필터 및 토큰 처리
│   │   │   └── healthCheck            # 서버 상태 확인 API
│   │   └── resources
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-dev.yml
│   │       └── schema.sql
│   └── test                             # 테스트 코드
├── Dockerfile
├── build.gradle
└── settings.gradle
```

각 도메인은 `controller`, `service`, `repository`, `entity`, `dto`를 중심으로 구성하며, 공통 설정과 인증·예외 처리는 `global` 패키지에서 관리합니다.

<br />


## 관련 저장소

| Repository | Description |
| --- | --- |
| [**SeCause-FE**](https://github.com/SeCause/SeCause-FE) | 사용자 화면, GitHub 연동 및 분석 대시보드 |
| [**SeCause-BE**](https://github.com/SeCause/SeCause-BE) | 인증, 저장소 관리 및 서비스 API |
| [**SeCause-Analysis**](https://github.com/SeCause/SeCause-Analysis) | AI 기반 코드 분석 및 비동기 작업 처리 |

<br />

## 참여자

<div align="center">
<table>
  <tr>
    <td align="center" width="220">
      <a href="https://github.com/boogiewooki02">
        <img src="https://github.com/boogiewooki02.png" width="100" alt="김동욱" />
        <br />
        <strong>김동욱</strong>
      </a>
      <br />
      Backend · Analysis
    </td>
    <td align="center" width="220">
      <a href="https://github.com/dldusgh318">
        <img src="https://github.com/dldusgh318.png" width="100" alt="이연호" />
        <br />
        <strong>이연호</strong>
      </a>
      <br />
      Backend · Analysis
    </td>
  </tr>
</table>
</div>

<br />

---

<div align="center">

**더 안전한 코드를 위한 가장 명확한 원인과 해답, SeCause**

[Website](https://www.secause.site) · [Organization](https://github.com/SeCause) · [Repositories](https://github.com/orgs/SeCause/repositories)

</div>
