# 판교역 공간 검색 100 VU 상세 측정 기록

> 상태: 측정 진행 중. 모든 원본 파일 확보와 집계가 끝난 뒤 수치를 채운다.

## 1. 재현 정보

| 항목 | 값 |
| --- | --- |
| 기준 시각 | UTC 및 KST를 함께 기록 |
| 기준 좌표 | 판교역, 경도 `127.1112`, 위도 `37.3948` |
| 반경 | 1km, 5km, 10km, 30km |
| 워밍업 | 25 VU, 60초 |
| 본 측정 | 100 VU, 180초 |
| 반복 | 반경·경로별 2회, ABBA 순서 |
| 부하 생성기 | k6 전용 EC2 |
| 백엔드 | Spring Boot EC2 컨테이너 |
| DB | MySQL RDS |

## 2. 비교 경로와 SQL 의미

| 경로 | API | 공간 조건 | 거리 계산 | 기대 인덱스 사용 |
| --- | --- | --- | --- | --- |
| 정확 반경 | `/emergencies/search/optimized` | MBR 후보 선별 + 반경 필터 | `ST_Distance_Sphere` 1회 | `MBRContains` 후보 선별 |
| 원형 Within | `/emergencies/search/circle-within` | `ST_Buffer` + `ST_Within` | 없음 | 공간 인덱스 직접 활용 보장 안 됨 |

`ST_Buffer`는 원형처럼 보이는 다각형 버퍼를 만든다. 따라서 거리 기반 정확 반경과 경계 근처 결과가 다를 수 있다. 각 반경에서 두 경로의 결과 건수도 기록한다.

## 3. 원본 아티팩트 목록

측정 완료 후 아래 원본을 로컬 `benchmark/results/2026-09-30-pangyo-exact-circle/`에 보존한다.

| 분류 | 파일/수집 원천 | 포함 내용 |
| --- | --- | --- |
| k6 | `*-warmup-summary.json`, `*-measure-summary.json` | 요청 수, RPS, 지연시간 분포, 실패·체크 |
| k6 | `*-console.json` | k6가 콘솔로 출력한 전체 메트릭 |
| 부하 생성 EC2 | `load-generator-sar.txt` | CPU, 메모리, 네트워크, 디스크 |
| 백엔드 Prometheus | `*-prometheus.txt` | JVM, Spring, HTTP, Hikari, 프로세스 메트릭 |
| 백엔드 | `*-health.json`, 컨테이너·`sar`·`pidstat` 기록 | 헬스, 호스트·프로세스 사용량 |
| RDS CloudWatch | 추출 JSON/CSV | CPU, 메모리, 연결, IOPS, 지연, 네트워크 |
| SQL | `EXPLAIN` 결과 | 실행 계획·인덱스·예상 행 수 |

## 4. k6 HTTP 상세 지표

각 `반경 × 경로 × 반복`에 아래를 모두 표로 기록한다.

| 반경 | 경로 | 반복 | 요청 수 | RPS | 평균 | 최소 | 중앙값 | p90 | p95 | p99 | 최대 | 실패율 | HTTP 200 체크율 |
| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 측정 후 기입 |  |  |  |  |  |  |  |  |  |  |  |  |  |

단위: 지연시간은 ms, 실패율·체크율은 %.

## 5. API 응답 의미 검증

| 반경 | 정확 반경 결과 건수 | 원형 Within 결과 건수 | 차이 | 판정 |
| --- | ---: | ---: | ---: | --- |
| 1km | 측정 후 기입 | 측정 후 기입 |  |  |
| 5km | 측정 후 기입 | 측정 후 기입 |  |  |
| 10km | 측정 후 기입 | 측정 후 기입 |  |  |
| 30km | 118 | 115 | -3 | 경계 근처 버퍼 근사 차이 확인 |

## 6. Spring Boot · JVM · HTTP 지표

각 케이스의 시작 전·워밍업 후·본 측정 후 Prometheus 원본에서 아래 항목을 추출해 최솟값·평균·최댓값·증감량을 기록한다.

| 분류 | 메트릭 예시 | 기록 값 |
| --- | --- | --- |
| JVM 힙 | `jvm_memory_used_bytes`, `jvm_memory_max_bytes` | used/max, 사용률, GC 전후 변화 |
| JVM GC | `jvm_gc_pause_seconds_*` | 횟수, 누적 정지 시간, 최대 정지 시간 |
| JVM 스레드 | `jvm_threads_live_threads`, `jvm_threads_peak_threads` | 평균/최대 |
| 프로세스 | `process_cpu_usage`, `process_uptime_seconds` | 평균/최대 CPU, uptime |
| HTTP 서버 | `http_server_requests_seconds_*` | URI·상태별 count/sum/max, p95/p99 가능 여부 |
| Hikari | `hikaricp_connections_active`, `idle`, `pending`, `max`, `timeout_total` | 평균/최대, 대기·타임아웃 여부 |
| DB 풀 사용률 | active/max | 최대 사용률, 포화 여부 |

## 7. 백엔드 EC2 상세 지표

| 지표 | 수집 원천 | 기록 값 |
| --- | --- | --- |
| 인스턴스 CPU | `sar -u` / CloudWatch | 평균·최대·user/system/iowait |
| 메모리·스왑 | `sar -r`, `sar -S` | 평균·최저 가용 메모리·스왑 사용 |
| 네트워크 | `sar -n DEV` | RX/TX 평균·최대 |
| 디스크 I/O | `sar -d` | tps, 읽기/쓰기, await 가능 시 기록 |
| Java 프로세스 | `pidstat` | CPU·RSS·스레드별 변화 |
| 컨테이너 | `docker stats` | CPU·메모리·네트워크 |

## 8. RDS MySQL 상세 지표

| 지표 | CloudWatch 이름 | 기록 값 |
| --- | --- | --- |
| CPU | `CPUUtilization` | 평균·최대 |
| 가용 메모리 | `FreeableMemory` | 평균·최소 |
| 연결 수 | `DatabaseConnections` | 평균·최대 |
| 읽기 IOPS/처리량 | `ReadIOPS`, `ReadThroughput` | 평균·최대 |
| 쓰기 IOPS/처리량 | `WriteIOPS`, `WriteThroughput` | 평균·최대 |
| 읽기/쓰기 지연 | `ReadLatency`, `WriteLatency` | 평균·최대 |
| 네트워크 | `NetworkReceiveThroughput`, `NetworkTransmitThroughput` | 평균·최대 |
| 스토리지·버스트 | `FreeStorageSpace`, 해당 시 burst 관련 지표 | 최소·변화량 |

## 9. 실행 계획과 해석

각 경로의 반경별 `EXPLAIN`을 첨부하고 다음을 비교한다.

- 접근 방식(`type`), 선택한 키(`key`), 예상 행 수(`rows`), 필터 비율
- `MBRContains`를 통한 후보 범위 축소 여부
- `ST_Within(ST_Buffer(...))`가 공간 인덱스를 직접 활용하지 못하는지 여부
- 반환 건수, DB 부하, p95/p99 간의 관계

## 10. 결론 및 한계

완료 후 다음을 명확히 적는다.

- 거리 계산 1회 변경의 실제 효과와 반경별 차이
- 원형 Within이 제공하는 공간 의미상의 장점과 성능 비용
- 100 VU 조건에서 병목이 애플리케이션·DB·네트워크 중 어디였는지
- RDS 사양, 데이터 규모, 캐시 워밍업, 반복 횟수에 따른 재현 한계
