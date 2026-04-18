# Code Review: GitLab baseUrl 2차 연동
날짜: 2026-04-18

---

## 1. [BUG] `resolvePlatformBaseUrl` — `/api/v4` 미자동 부착
**파일**: `AuthService.java:185-193`

```java
return requestedBaseUrl.endsWith("/")
    ? requestedBaseUrl.substring(0, requestedBaseUrl.length() - 1)
    : requestedBaseUrl;
```

**문제**: trailing slash만 제거하고, `/api/v4` 포함 여부를 검증하지 않는다.

사용자가 `https://mygitlab.com`을 입력하면 그대로 저장된다.  
이후 `DefaultGitLabApiClient`가 `https://mygitlab.com/projects`, `https://mygitlab.com/user`를 호출해 런타임 404가 발생한다.

기본값 `https://gitlab.com/api/v4`와 정규화 로직 간 형식 불일치가 있다.

**재현 경로**: `POST /api/platforms/GITLAB/token` body에 `baseUrl: "https://mygitlab.com"` 입력 시.

---

## 2. [BUG] `PlatformConnection.baseUrl` — DB 마이그레이션 누락
**파일**: `PlatformConnection.java:51-52`

```java
@Column(name = "base_url")
private String baseUrl;
```

**문제**: JPA 엔티티에 `base_url` 컬럼이 추가되었으나 DDL 마이그레이션 스크립트가 보이지 않는다.

`spring.jpa.hibernate.ddl-auto`가 `validate` 또는 `none`인 운영 환경에서 서비스 기동 실패.  
`create`/`update` 환경이더라도 기존 연결 데이터의 `base_url`은 모두 `NULL`로 남아, 이후 `connection.getBaseUrl()`이 `null`을 반환하게 된다.  
`resolveApiBaseUrl(null)` → `properties.apiBaseUrl()` 폴백은 동작하지만, self-managed 인스턴스로 등록한 기존 연결은 `gitlab.com`으로 요청이 돌아간다.

---

## 3. [SECURITY] `resolvePlatformBaseUrl` — HTTP scheme 미검증
**파일**: `AuthService.java:186-193`

```java
if (platform == PlatformType.GITLAB) {
    if (requestedBaseUrl == null || requestedBaseUrl.isBlank()) {
        return "https://gitlab.com/api/v4";
    }
    return requestedBaseUrl.endsWith("/") ? ... : requestedBaseUrl;
}
```

**문제**: `http://mygitlab.com/api/v4`를 입력해도 검증 없이 저장된다.

이후 `DefaultGitLabApiClient`가 `PRIVATE-TOKEN` 헤더를 포함한 요청을 평문 HTTP로 전송하게 된다.  
암호화된 PAT가 복호화된 후(`patCryptoService.decrypt`) 헤더에 실어 전송되므로, 토큰이 네트워크에 평문 노출된다.

---

## 총평

치명적 버그 2건, 보안 위험 1건.

| # | 심각도 | 내용 | 파일 |
|---|---|---|---|
| 1 | 🔴 BUG | `/api/v4` 미자동 부착 → self-managed GitLab 전체 기능 불동작 | `AuthService.java:185` |
| 2 | 🔴 BUG | `base_url` 컬럼 마이그레이션 누락 → 운영 기동 실패 또는 기존 연결 URL 초기화 | `PlatformConnection.java:51` |
| 3 | 🟠 SECURITY | HTTP scheme 미검증 → PAT 평문 전송 가능 | `AuthService.java:186` |

---

## 검증 결과

### 1. `/api/v4` 미자동 부착
- 검증 결과: 타당
- 근거: `AuthService.resolvePlatformBaseUrl`는 빈 값일 때만 `https://gitlab.com/api/v4`를 넣고, 사용자가 입력한 값은 trailing slash 제거 외 추가 정규화를 하지 않는다.
- 영향 판단: `https://mygitlab.com` 같은 값이 저장되면 이후 `DefaultGitLabApiClient`가 `/user`, `/projects`를 API 루트가 아닌 일반 루트 기준으로 호출할 수 있다.

### 2. `base_url` 컬럼 마이그레이션 누락
- 검증 결과: 취지는 타당하지만 표현은 운영 환경 전제
- 근거: 코드상 `PlatformConnection`에 `baseUrl` 컬럼이 추가된 것은 맞다.
- 보정 사항: 저장소만 기준으로는 운영 DB 마이그레이션 방식까지 단정할 수 없다. 따라서 “운영 환경이 Hibernate 자동 스키마 업데이트를 사용하지 않으면 별도 DB 반영이 필요하다” 수준으로 해석하는 것이 더 정확하다.

### 3. HTTP scheme 미검증
- 검증 결과: 타당
- 근거: `AuthService.resolvePlatformBaseUrl`는 `http://...` 입력을 차단하지 않는다.
- 영향 판단: 이후 `DefaultGitLabApiClient`가 `PRIVATE-TOKEN` 헤더를 포함한 요청을 해당 URL로 보내므로, HTTP 사용 시 토큰이 평문 전송될 위험이 있다.
