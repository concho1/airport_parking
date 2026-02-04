# airport_parking (주차장 실시간 조회) — 베이스 프로젝트 안내서

이 프로젝트는 **주차장 실시간(현재 상태) 데이터**를 서버에 저장/갱신하고, **REST API로 빠르게 조회**할 수 있는 기본 골격입니다.  
현재는 공공데이터 API 연동 전 단계이므로 **더미 데이터(seed)** 로 동작을 확인할 수 있게 구성합니다.

## 테스트 페이지  : http://localhost:5178

---

## 1) 개발 환경

### 1-1. 필수 설치
- **JDK 17**
- **IntelliJ IDEA** (Community/Ultimate 상관 없음)
- **MariaDB** (원격 DB application.properties에 정의)
- **application.properties 파일 필요** (운영자에게 문의)

---

## 2) 사용 의존성(Gradle) 설명

`build.gradle` 주요 의존성은 아래 역할을 합니다.

### ✅ Web / MVC
- `spring-boot-starter-webmvc`  
  Spring MVC 기반 웹 애플리케이션을 구성합니다.
    - `@Controller`, `@RestController`, `@GetMapping` 같은 기능 제공
    - REST API 엔드포인트를 만들기 위해 필수입니다.

### ✅ DB / JPA
- `spring-boot-starter-data-jpa`  
  JPA(Hibernate)로 DB를 쉽게 다루게 해줍니다.
    - `@Entity`, `JpaRepository`, 트랜잭션 처리 등 제공
- `mariadb-java-client`  
  MariaDB에 접속하기 위한 JDBC 드라이버입니다.

### ✅ Validation
- `spring-boot-starter-validation`  
  입력값 검증(Bean Validation)을 지원합니다.
    - 예: `@Valid`, `@NotNull`, `@Pattern` 등

### ✅ Cache
- `spring-boot-starter-cache`  
  조회 성능 향상을 위한 캐시 추상화 기능을 제공합니다.
    - 예: `@Cacheable`, `@CacheEvict`
    - 실시간 조회 트래픽이 많아질 때 DB 부담을 줄이는 데 유용합니다.

### ✅ 운영/모니터링
- `spring-boot-starter-actuator`  
  서버 상태 점검 엔드포인트를 제공합니다.
    - 예: `/actuator/health`

### ✅ Lombok
- `lombok`  
  DTO/Entity의 반복 코드(getter/setter/constructor/builder)를 줄여줍니다.

### ✅ DevTools
- `spring-boot-devtools`  
  개발 중 코드 변경 시 자동 재시작 등 개발 편의 기능을 제공합니다.

### ✅ Swagger(OpenAPI)
- `springdoc-openapi-starter-webmvc-ui:3.0.1`  
  API 문서를 자동으로 생성하고 Swagger UI를 제공합니다.
    - Swagger UI: `http://localhost:5178/swagger-ui/index.html`
    - OpenAPI JSON: `http://localhost:5178/v3/api-docs`

---

## 3) 프로젝트 레이어 구조(패키지 설명)

이 프로젝트는 전형적인 Spring 레이어 구조를 사용합니다.

### 3-1. Controller (API 입구)
- 역할: **HTTP 요청을 받아서** 서비스로 전달하고, 응답을 반환합니다.
- 특징:
    - 비즈니스 로직(계산/DB 처리)을 직접 넣지 않습니다.
    - Swagger 문서용 애노테이션(`@Operation`, `@Tag`)을 붙여 API 설명을 제공합니다.

예: `ParkingRealtimeController`

### 3-2. Service (비즈니스 로직)
- 역할: **업무 로직 처리**
    - 예: 업서트(upsert), 상태 계산, 조회 조합 등
- 특징:
    - 트랜잭션 처리가 필요하면 `@Transactional`을 사용합니다.
    - Controller가 요청한 일을 실제로 수행하는 핵심 레이어입니다.

예: `ParkingLotStatusService`

### 3-3. Repository (DB 접근)
- 역할: DB CRUD를 담당합니다.
- 특징:
    - `JpaRepository`를 상속받아 `save()`, `findAll()` 등을 자동으로 사용합니다.
    - 메서드 이름 규칙으로 조회 쿼리를 자동 생성할 수 있습니다.

예: `ParkingLotStatusRepository`

### 3-4. Entity (DB 테이블과 매핑)
- 역할: 테이블(row)을 자바 객체로 표현합니다.
- 특징:
    - `@Entity`, `@Table`, `@Column`으로 DB 컬럼과 매핑합니다.
    - “현재 상태” 테이블은 폴링 결과로 계속 갱신되므로 `updatedAt` 같은 필드를 둡니다.

예: `ParkingLotStatusEntity`

### 3-5. DTO (응답/요청 전용 객체)
- 역할: API 응답/요청 형태를 정의합니다.
- 특징:
    - Entity를 그대로 외부로 노출하는 대신 DTO로 변환해 반환하는 것이 보안/유지보수에 유리합니다.
    - Swagger 문서에 보기 좋게 나오도록 `@Schema`를 붙입니다.

예: `ParkingRealtimeResponse`

---

## 4) DB 테이블(현재 상태 테이블)

이 프로젝트는 “실시간”을 위해 **현재 상태만 저장/갱신**하는 테이블을 사용합니다.

- 테이블명: `parking_lot_status`
- 특징:
    - `lot_code + terminal + parking_type + area_side` 조합을 **유니크 키**로 두어 업서트가 쉽습니다.
    - 공공데이터를 폴링할 때마다 최신값으로 UPDATE/INSERT 됩니다.

> 이력/통계를 저장하려면 `parking_lot_status_history` 같은 별도 테이블을 추가하는 것을 권장합니다.

---

## 5) 베이스 API 동작 방식

### 5-1. 더미 데이터 삽입 (개발용)
API 연동 전이므로 더미 데이터를 DB에 넣어 화면/API 테스트를 합니다.

- `POST /api/parking/admin/seed-dummy`

> ⚠️ 운영에서는 반드시 비활성화하거나 관리자 인증을 적용하세요.

### 5-2. 실시간 조회 (현재 상태)
- `GET /api/parking/realtime?terminal=T1&type=long`

응답은 동편/서편 목록으로 나눠서 반환합니다.

---

## 6) application.properties 설정 (중요)

이 프로젝트는 **환경(PC/서버/원격 DB)에 따라 접속 정보가 달라지므로**,  
`src/main/resources/application.properties` 파일을 **직접 생성/수정해서 넣어야** 정상 동작합니다.

> ✅ 이유
> - MariaDB 접속 정보(호스트/IP, 포트, DB명, 계정, 비밀번호)는 사람/환경마다 다릅니다.
> - 특히 **원격 DB 정보(주소/계정/비밀번호 등)** 는 보안상 저장소에 올리면 안 됩니다.
> - 따라서 이 프로젝트는 `application.properties`를 **사용자가 직접 채우는 방식**으로 운영합니다.


---

## 7) 프론트(UI) — Bootstrap 로컬 사용 안내

이 프로젝트의 UI(HTML)는 **CDN이 아닌 로컬 Bootstrap 파일**을 사용합니다.  
즉, 인터넷 연결이 없어도(또는 사내망 환경에서도) 동일하게 동작하도록 구성했습니다.

### 7-1. Bootstrap 파일 위치
Bootstrap 5.0.2 파일이 아래 경로에 포함되어 있어야 합니다.

- CSS: `/assets/bootstrap-5.0.2-dist/css/bootstrap.min.css`
- JS:  `/assets/bootstrap-5.0.2-dist/js/bootstrap.bundle.min.js`

> 일반적으로 Spring Boot에서는 정적 파일을 `src/main/resources/static` 아래에 둡니다.  
> 예시)  
> `src/main/resources/static/assets/bootstrap-5.0.2-dist/...`

### 7-2. HTML에서 로컬 Bootstrap 불러오는 예시
`index.html` 또는 `home.html`에서 아래처럼 로컬 경로로 참조합니다.

```html
<link rel="stylesheet" href="/assets/bootstrap-5.0.2-dist/css/bootstrap.min.css" />
<script src="/assets/bootstrap-5.0.2-dist/js/bootstrap.bundle.min.js"></script>