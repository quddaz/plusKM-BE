# PlusKM Backend

현재 위치 주변의 응급실, 병원, 약국을 찾는 PlusKM의 Spring Boot API입니다. 응급실의 기본 정보와 병상 정보를 분리해 갱신하고, 병원·약국 CSV는 MySQL 공간 검색에 사용할 수 있도록 적재합니다.

> 이 문서는 현재 `develop` 작업 트리의 코드 기준입니다. 배포된 서비스의 버전과 다를 수 있습니다.

## 주요 기능

| 기능 | 현재 구현 |
| --- | --- |
| 주변 응급실 | 위치·반경으로 검색하고 상세 정보를 조회합니다. 기본 정보는 공공데이터 API에서 주기적으로 동기화합니다. |
| 병상 정보 | 공공데이터 API에서 5분 간격으로 갱신해 서버 메모리에 보관하고, 요청한 응급실 기관 코드(HPID)에 해당하는 정보만 반환합니다. |
| 주변 병원·약국 | 위도·경도의 `BETWEEN` 조건으로 후보를 줄이고 Haversine 거리로 반경을 판정해 거리·ID 순으로 반환합니다. 프론트에서는 1, 3, 5, 10km를 선택합니다. |
| CSV 최신화 | Java에서 CSV를 배치로 읽어 임시 스테이징 테이블에 적재·검증하고, DB에서 변경 행만 병합합니다. 파일에서 빠진 장소는 삭제하지 않고 `active=false`로 남깁니다. |
| 즐겨찾기 | 로그인 사용자별로 응급실·병원·약국을 저장하고 조회·해제합니다. CSV에서 빠져 비활성화된 장소도 저장 기록은 유지하되 현재 검색 대상이 아님을 표시합니다. |
| 로그인·보호자 | Google OAuth2/JWT 인증, 보호자 전화번호 암호화 저장, 응급 안내 문구와 문자 앱을 여는 `sms:` 링크 생성을 지원합니다. 현재 보호자 API가 문자를 자동 발송하지는 않습니다. |

## 기술 스택

- Java 21, Spring Boot 3.3.5, Spring Security, Spring Data JPA, Spring JDBC
- MySQL 8, Hibernate Spatial, JTS, OpenCSV
- Spring Cache + Caffeine, Spring Boot Actuator + Micrometer Prometheus
- Gradle, JUnit 5, Testcontainers MySQL 8.4.9

MongoDB·PostgreSQL 관련 주석은 과거 비교 실험의 기록이며, 현재 실행 경로의 데이터베이스는 MySQL입니다.

## 데이터 처리 방식

- **응급실:** 기본 정보는 기본 설정상 24시간 간격으로 DB에 동기화합니다. 병상 정보는 5분 간격으로 별도 갱신하며, 조회 API는 캐시에 들어 있는 병상 정보에서 요청 대상만 선택합니다.
- **병원·약국 검색:** 활성 장소만 대상으로 B-tree 후보 조회 → Haversine 반경 판정 → 거리·ID 순 정렬을 수행합니다. 위도·경도는 `POINT`에서 자동 계산되는 저장형 생성 열입니다.
- **병원·약국 최신화:** CSV를 스테이징에 모두 읽은 뒤 건수를 확인합니다. 이후 같은 `(name, address)`의 변경 행은 UPDATE, 새 행은 INSERT, 누락 행은 비활성화합니다. 같은 이름·주소로 다시 등장하면 기존 `place.id`가 재활성화됩니다. 이름이나 주소가 바뀐 기관의 동일성은 현재 키만으로 보장할 수 없습니다.

## API 빠른 보기

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/emergencies/search` | 주변 응급실 검색 |
| `GET` | `/emergencies/{id}` | 응급실 상세 정보 |
| `POST` | `/emergencies/beds` | 지정한 응급실의 캐시된 병상 정보 조회 |
| `POST` | `/places/search` | 주변 병원·약국 검색 |
| `GET` | `/favorites` | 로그인 사용자의 응급실·병원·약국 즐겨찾기 목록 |
| `PUT/DELETE` | `/favorites/emergencies/{id}` | 응급실 즐겨찾기 저장·해제 |
| `PUT/DELETE` | `/favorites/places/{id}` | 병원·약국 즐겨찾기 저장·해제 |
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

`category`는 `ALL`, `HOSPITAL`, `PHARMACY` 중 하나이며, API의 `radiusKilometers`는 `0.3`, `0.5`, `1`, `3`, `5`, `10`을 허용합니다. 응답은 `places` 배열과 각 장소의 거리(`distanceMeters`)를 포함하며 200개 제한은 없습니다. 응급실 API 반경은 0 초과 30km 이하입니다.

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
./gradlew test                 # 단위 테스트
./gradlew clean check bootJar  # MySQL 저장소·서비스 통합·HTTP 인수 테스트와 JAR 빌드
```

전체 검사는 Java 21과 실행 중인 Docker가 필요합니다. 테스트용 MySQL 컨테이너가 자동 생성되며 운영 DB는 사용하지 않습니다. 정책별 시나리오와 개별 실행 방법은 [정책과 인수 테스트](docs/testing/POLICIES.md)에 있습니다. 테스트용 토큰 API는 `local` 프로필에서만 사용할 수 있습니다.

상태 확인은 `/actuator/health`, Prometheus 지표는 `/actuator/prometheus`에서 할 수 있습니다. 외부에 공개할 때는 이 엔드포인트의 접근 범위를 별도로 제한해야 합니다.

## SSH 배포

`main`에 푸시하면 GitHub Actions의 `Deploy backend over SSH`가 전체 테스트를 실행하고, 통과한 JAR을 SCP로 전송해 SSH로 서비스를 재시작합니다. 수동 실행도 main에서만 배포합니다. SHA-256 검증과 내부 상태 확인을 수행하며, 상태 확인에 실패하면 이전 JAR로 복원합니다. 필요한 서버 설정과 GitHub Secrets는 [SSH 배포 문서](docs/DEPLOY-SSH.md)를 참고하세요.

## 실험과 설계 기록

- [CSV 갱신 중 메모리 문제](https://quddnd.tistory.com/302)
- [공간 검색 DB 비교](https://quddnd.tistory.com/316)
- [MySQL 공간 검색 쿼리 최적화](https://quddnd.tistory.com/317)
- [10만 건 CSV 최신화 방식 비교](https://quddnd.tistory.com/318)

블로그의 성능 수치는 당시 실험 코드·인프라에서 얻은 값입니다. 현재 코드의 배포 성능이나 소프트 삭제 적용 후의 측정값으로 해석하지 않습니다.
