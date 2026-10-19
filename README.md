# PlusKM Backend

현재 위치 주변의 응급실, 병원, 약국을 찾는 PlusKM의 Spring Boot API입니다. 응급실의 기본 정보와 병상 정보를 분리해 갱신하고, 병원·약국 CSV는 MySQL 공간 검색에 사용할 수 있도록 적재합니다.

> 이 문서는 현재 `develop` 작업 트리의 코드 기준입니다. 배포된 서비스의 버전과 다를 수 있습니다.

## 주요 기능

| 기능 | 현재 구현 |
| --- | --- |
| 주변 응급실 | 위치·반경으로 검색하고 상세 정보를 조회합니다. 기본 정보는 공공데이터 API에서 주기적으로 동기화합니다. |
| 병상 정보 | 공공데이터 API에서 5분 간격으로 갱신해 서버 메모리에 보관하고, 요청한 응급실 기관 코드(HPID)에 해당하는 정보만 반환합니다. |
| 주변 병원·약국 | MySQL의 MBR 조건으로 후보를 줄인 뒤 구면 거리를 계산해 가까운 순으로 반환합니다. 검색 반경은 1km 또는 3km입니다. |
| CSV 최신화 | Java에서 CSV를 배치로 읽어 임시 스테이징 테이블에 적재·검증하고, DB에서 변경 행만 병합합니다. 파일에서 빠진 장소는 삭제하지 않고 `active=false`로 남깁니다. |
| 로그인·보호자 | Google OAuth2/JWT 인증, 보호자 전화번호 암호화 저장, 응급 안내 문구와 문자 앱을 여는 `sms:` 링크 생성을 지원합니다. 현재 보호자 API가 문자를 자동 발송하지는 않습니다. |

## 기술 스택

- Java 21, Spring Boot 3.3.5, Spring Security, Spring Data JPA, Spring JDBC
- MySQL 8, Hibernate Spatial, JTS, OpenCSV
- Spring Cache + Caffeine, Spring Boot Actuator + Micrometer Prometheus
- Gradle, JUnit 5

MongoDB·PostgreSQL 관련 주석은 과거 비교 실험의 기록이며, 현재 실행 경로의 데이터베이스는 MySQL입니다.

## 데이터 처리 방식

- **응급실:** 기본 정보는 기본 설정상 24시간 간격으로 DB에 동기화합니다. 병상 정보는 5분 간격으로 별도 갱신하며, 조회 API는 캐시에 들어 있는 병상 정보에서 요청 대상만 선택합니다.
- **병원·약국 검색:** 활성 장소만 대상으로 MBR 후보 조회 → `ST_Distance_Sphere` 반경 판정 → 거리순 정렬을 수행합니다.
- **병원·약국 최신화:** CSV를 스테이징에 모두 읽은 뒤 건수를 확인합니다. 이후 같은 `(name, address)`의 변경 행은 UPDATE, 새 행은 INSERT, 누락 행은 비활성화합니다. 같은 이름·주소로 다시 등장하면 기존 `place.id`가 재활성화됩니다. 이름이나 주소가 바뀐 기관의 동일성은 현재 키만으로 보장할 수 없습니다.

## API 빠른 보기

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/emergencies/search` | 주변 응급실 검색 |
| `GET` | `/emergencies/{id}` | 응급실 상세 정보 |
| `POST` | `/emergencies/beds` | 지정한 응급실의 캐시된 병상 정보 조회 |
| `POST` | `/places/search` | 주변 병원·약국 검색 |
| `POST` | `/admin/places/import-csv` | CSV 최신화 (`ADMIN` 권한 필요) |
| `GET/POST/PUT/DELETE` | `/guardians` | 로그인 사용자의 보호자 관리 |
| `POST` | `/guardians/{guardianId}/emergency-message` | 안내 문구 및 `sms:` 링크 생성 |

공간 검색 실험용 응급실 엔드포인트(`/emergencies/search/optimized`, `/indexed-bounds`, `/circle-within`, `/circle-within-sorted`)도 코드에 남아 있습니다. 전체 요청·응답 형식은 실행 후 `/swagger-ui/index.html`에서 확인할 수 있습니다.

병원·약국 검색 예시:

```bash
curl -X POST http://localhost:8080/places/search \
  -H 'Content-Type: application/json' \
  -d '{"longitude":127.1112,"latitude":37.3948,"radiusKilometers":3,"category":"ALL"}'
```

`category`는 `ALL`, `HOSPITAL`, `PHARMACY` 중 하나이며, `radiusKilometers`는 `1` 또는 `3`만 허용합니다. 응답은 `places` 배열과 각 장소의 거리(`distanceMeters`)를 포함합니다.

## 로컬 실행

1. Java 21과 MySQL을 준비합니다.
2. `src/main/resources/application-local.yml`의 로컬 DB(`myapp_local`, `local_user`)를 생성하거나 연결 정보를 자신의 환경에 맞게 바꿉니다. 이 파일의 OAuth/JWT 값은 로컬 예시 값이므로 실제 서비스에 사용하지 마세요.
3. 아래 명령으로 실행합니다.

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

병원·약국 CSV는 `src/main/resources/data/`에 포함되어 있습니다. 기본값은 시작 시 자동 적재하지 않으며, 처음 적재가 필요하면 `CSV_IMPORT_ON_STARTUP=true`를 지정하거나 관리자 API를 사용합니다. 스테이징 적재에는 DB 계정의 임시 테이블 생성 권한이 필요합니다. 응급실 데이터 동기화에는 `EMERGENCY_API_SERVICE_KEY`가 필요합니다.

프로덕션 설정은 `src/main/resources/application.yml`의 환경 변수로 주입합니다. 특히 DB 접속 정보, Google OAuth2, JWT 비밀키, `GUARDIAN_PHONE_ENCRYPTION_KEY`, CORS 허용 주소를 배포 환경에서 설정해야 합니다. 비밀값을 저장소에 커밋하지 마세요.

```bash
./gradlew test
./gradlew bootJar
```

상태 확인은 `/actuator/health`, Prometheus 지표는 `/actuator/prometheus`에서 할 수 있습니다. 외부에 공개할 때는 이 엔드포인트의 접근 범위를 별도로 제한해야 합니다.

## 실험과 설계 기록

- [CSV 갱신 중 메모리 문제](https://quddnd.tistory.com/302)
- [공간 검색 DB 비교](https://quddnd.tistory.com/316)
- [MySQL 공간 검색 쿼리 최적화](https://quddnd.tistory.com/317)
- [10만 건 CSV 최신화 방식 비교](https://quddnd.tistory.com/318)

블로그의 성능 수치는 당시 실험 코드·인프라에서 얻은 값입니다. 현재 코드의 배포 성능이나 소프트 삭제 적용 후의 측정값으로 해석하지 않습니다.
