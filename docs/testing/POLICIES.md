# PlusKM 정책과 인수 테스트

기준은 현재 작업 트리의 API와 도메인 정책이다. 정책 ID를 HTTP 인수 테스트 이름에 붙여 변경 시 해당 시나리오를 찾을 수 있게 했다. 테스트는 운영 DB와 공공 API에 요청하지 않는다.

## 검색

| ID | 정책 | 확인하는 테스트 |
| --- | --- | --- |
| P01 | 병원·약국은 비로그인으로 조회한다. `active=true`인 장소만 반환하고 `ALL`, `HOSPITAL`, `PHARMACY`를 지원한다. | `PlaceAcceptanceTest.P01`, `PlaceRepositoryIntegrationTest` |
| P02 | 병원·약국 API 반경은 0.3, 0.5, 1, 3, 5, 10km다. 빈 결과는 `200`과 빈 `places` 배열이다. 프론트 선택지는 1, 3, 5, 10km다. | `PlaceAcceptanceTest.P02` |
| P03 | 지원하지 않는 반경은 `400`이다. 최대 반경은 10km다. | `PlaceAcceptanceTest.P03` |
| P04 | 좌표 범위를 벗어나거나 카테고리가 잘못됐으면 `400`이다. | `PlaceAcceptanceTest.P04` |
| P05 | 사각형은 후보 조회에만 사용한다. Haversine 거리 `<= 반경`인 장소를 거리, ID 순으로 반환한다. 200개로 자르지 않는다. | `PlaceRepositoryIntegrationTest`의 경계·정렬·205개 조회 테스트 |
| P06 | 숫자 위도·경도는 SRID 4326 `POINT`의 저장형 생성 열이다. 좌표를 갱신하면 함께 변경되고 초기화 재실행은 중복 열·인덱스를 만들지 않는다. | `PlaceRepositoryIntegrationTest.생성_열은_좌표_변경을_따르고_인덱스_초기화는_반복해도_된다` |
| E01 | 응급실은 비로그인 조회가 가능하다. API 반경은 0 초과 30km 이하이며 프론트 선택지는 1, 5, 10, 20, 30km다. | `EmergencyAcceptanceTest.E01`, `E02` |
| E02 | 없는 응급실 상세는 `404`, 잘못된 좌표와 반경은 `400`이다. | `EmergencyAcceptanceTest.E02` |
| E03 | 병상 조회는 HPID를 1~200개 받아 캐시에서 일치하는 항목만 반환한다. 중복은 한 번 반환하고 미등록 HPID는 생략한다. 조회마다 공공 API를 호출하지 않는다. | `EmergencyAcceptanceTest.E03` |
| E04 | 병상은 기본 5분 간격, 응급실 기본 정보는 기본 24시간 간격으로 갱신한다. 실패하거나 빈 목록을 받으면 이전 상태를 보존한다. | 기존 스케줄러 단위 테스트, `EmergencyIntegrationTest` |

`/emergencies/search`는 기존 사각형 조회이며 `/emergencies/search/optimized`가 구면 거리 판정과 거리순 정렬을 수행한다. 버퍼 등 비교 실험 엔드포인트를 정확 반경 조회와 같은 정책으로 설명하지 않는다.

## CSV 최신화와 삭제

| ID | 정책 | 확인하는 테스트 |
| --- | --- | --- |
| C01 | 관리자만 CSV 최신화를 실행한다. 비로그인은 `401`, 일반 사용자는 `403`이다. | `CsvImportAcceptanceTest.C01` |
| C02 | 전체 스테이징과 검증이 끝난 뒤 변경 행 갱신, 신규 삽입, 누락 비활성화 순으로 병합한다. 누락 행의 ID와 즐겨찾기는 보존한다. | `CsvImportAcceptanceTest.C02`, `PlaceCsvImportIntegrationTest` |
| C03 | `(name, address)`가 같으면 같은 기관으로 취급한다. 재등장 시 기존 ID로 활성화하고 중복 입력은 스테이징의 마지막 값으로 반영한다. | `PlaceCsvStageRepositoryIntegrationTest`, `PlaceCsvImportIntegrationTest` |
| C04 | 병원이나 약국 중 한 종류의 유효 행이 0이거나, 고유 스테이징 건수가 기존 활성 행의 절반 미만이면 반영하지 않는다. 9건 중 4건도 절반 미만이다. | `PlaceCsvImportServiceTest`, `PlaceCsvImportIntegrationTest` |
| C05 | 병합 중 DB 오류가 발생하면 갱신도 롤백한다. 임시 테이블은 정리돼 다음 최신화를 실행할 수 있다. | `PlaceCsvImportIntegrationTest.병합_도중_DB_오류가_나면_기존값까지_롤백하고_다음_갱신을_허용한다` |
| C06 | CSV를 배치로 읽고 형식 오류 행은 건너뛰어 집계한다. 반환 `hospitals`, `pharmacies`는 읽은 유효 행 수이고 `inactivePlaces`는 전체 비활성 행 수다. | `PlaceCsvReaderTest`, `PlaceCsvFormatTest`, `PlaceCsvImportIntegrationTest` |

이름 또는 주소가 바뀌면 현재 키로는 같은 기관임을 판단하지 않는다. CSV 최신화는 전체 삭제·재적재가 아니다.

## 즐겨찾기

| ID | 정책 | 확인하는 테스트 |
| --- | --- | --- |
| F01 | 응급실과 병원·약국을 로그인 사용자별로 저장한다. 같은 대상의 반복 PUT은 같은 즐겨찾기 ID를 반환한다. 목록은 최신 저장순이며 남의 목록을 섞지 않는다. 삭제를 반복해도 `204`다. | `FavoriteAcceptanceTest.F01`, `FavoriteRepositoryIntegrationTest`, `FavoriteIntegrationTest` |
| F02 | 없는 대상과 비활성 대상은 새로 저장할 수 없으며 `404`다. 이미 저장한 대상의 비활성화는 기록을 삭제하지 않는다. | `FavoriteAcceptanceTest.F02`, `CsvImportAcceptanceTest.C02` |

DB 유일 제약은 `(user_id, place_id)`와 `(user_id, emergency_id)`다. 순차 중복 저장과 DB 제약을 확인하며 동시 PUT 부하 테스트까지 수행한 것은 아니다.

## 인증과 보호자

| ID | 정책 | 확인하는 테스트 |
| --- | --- | --- |
| A01 | 즐겨찾기와 보호자는 유효한 JWT가 필요하다. 토큰이 없거나 잘못됐으면 `401`이다. | `AuthAcceptanceTest.A01` |
| A02 | 서명이 유효해도 사용자가 삭제됐으면 인증하지 않는다. 보호된 API는 `401`이다. | `AuthAcceptanceTest.A02` |
| A03 | 사용자 ID만 받는 테스트 토큰 발급 API는 `local`에서만 등록한다. 그 외 프로필에서는 `404`다. | `AuthAcceptanceTest.A03` |
| A04 | 리프레시 쿠키로 재발급하면 `204`, Authorization 헤더와 Secure·HttpOnly 쿠키를 반환한다. 쿠키가 없으면 `401`이다. 세션 쿠키를 만들지 않는다. | `AuthAcceptanceTest.A04` |
| G01 | 보호자 이름·관계·국내 휴대전화번호를 등록한다. 전화번호는 AES-GCM으로 암호화해 저장하고 조회 응답은 마스킹한다. 같은 번호도 IV가 달라 암호문이 다르다. | `GuardianAcceptanceTest.G01`, `GuardianIntegrationTest`, `GuardianPhoneCipherTest` |
| G02 | 다른 사용자의 보호자 수정·삭제·문자 링크 생성은 `404`다. 비활성 보호자의 문자 링크 생성은 `400`이다. | `GuardianAcceptanceTest.G02`, `GuardianIntegrationTest` |
| G03 | 빈 이름과 잘못된 휴대전화번호는 `400`이며 저장하지 않는다. | `GuardianAcceptanceTest.G03` |
| G04 | 안내에는 사용자 이름, 보호자 이름, 응급실 이름·전화번호와 현재 위치가 들어간다. 서버가 문자를 발송하는 대신 `sms:` 링크를 반환한다. | `GuardianAcceptanceTest.G01`, `GuardianIntegrationTest` |

## 통합 테스트 방식

고전파 방식을 사용한다. 서비스, 저장소, 트랜잭션과 캐시는 실제 객체를 함께 실행하고 결과 상태를 검사한다. 내부 협력 객체의 호출 횟수를 통합 테스트의 정답으로 삼지 않는다. 외부 API와 자동 스케줄러만 경계에서 대체한다.

## 테스트 실행

Java 21과 실행 중인 Docker가 필요하다. 별도 MySQL 설치나 운영 DB 접속 정보는 필요하지 않다.

```bash
./gradlew test                 # 기존 단위 테스트
./gradlew repositoryTest       # 실제 MySQL 저장소와 SQL
./gradlew integrationTest      # 서비스, 트랜잭션, CSV, 암호화, 캐시
./gradlew acceptanceTest       # 실제 HTTP와 JWT를 거치는 인수 테스트
./gradlew sshDeploymentTest    # SSH 스크립트 검사, 원격 접속 없음
./gradlew clean check bootJar  # 모든 검사와 배포 JAR 빌드
```

세 DB 테스트 작업은 MySQL 8.4.9 컨테이너를 각각 사용한다. 한 작업 안에서는 Spring 컨텍스트와 컨테이너를 공유하고 테스트마다 데이터를 정리한다. Docker를 사용할 수 없으면 건너뛰지 않고 실패한다. 종료 시 Testcontainers가 컨테이너를 정리한다.

저장소·서비스·HTTP 계층은 실제 빈이다. 외부 응급 API 클라이언트와 자동 스케줄러만 대체한다. 인수 테스트는 임의 포트에서 서버를 시작해 실제 HTTP 요청과 실제로 서명한 JWT를 사용한다. 서비스 갱신 테스트는 트랜잭션을 테스트 코드에서 감싸지 않아 실제 커밋과 롤백을 확인한다. 롤백 테스트의 실패 주입용 트리거는 컨테이너 안에서만 만든다.

보고서는 `build/reports/tests/{test,repositoryTest,integrationTest,acceptanceTest}/index.html`에, JUnit XML은 `build/test-results/`에 생성된다. PR 검사와 수동 SSH 배포에서도 `check`를 실행하고 보고서를 아티팩트로 남긴다.

공공 API 실제 가용성, Google 로그인 화면, SMS 앱 실행, 브라우저 지도 UI, 운영 성능과 실제 EC2 재시작은 이 테스트의 검증 범위에 포함되지 않는다. 별도 CSV 성능 실험은 `benchmark` 태그로 일반 `test`와 `check`에서 제외한다.

참고: [Testcontainers MySQL 모듈](https://java.testcontainers.org/modules/databases/mysql/)
