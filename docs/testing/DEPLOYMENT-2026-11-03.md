# 2026-11-03 SSH 운영 배포

사용자 요청으로 시드니 `pluskm-api-sydney-final` EC2의 `pluskm.service`에 배포했다. 실제 명령과 JAR 전송은 SSH/SCP를 사용했다.

## 산출물

- 테스트: 단위 48개, 저장소 15개, 서비스 통합 11개, HTTP 인수 34개 통과
- 배포 직전 `./gradlew check bootJar --no-daemon`과 SSH 스크립트 검사 통과
- 파일: `build/libs/backend-0.0.1-SNAPSHOT.jar`
- 로컬과 서버 SHA-256 일치: `fad945ae5de2a2374544392159e5adea89d4f39183283a32740444535ff890a2`
- 서버 파일: `/opt/pluskm/backend.jar`
- 직전 JAR: `/opt/pluskm/backend.jar.previous`

## 접속과 실행

AWS 인증 갱신 후 EC2 Instance Connect로 임시 공개키를 전송했다. 현재 컴퓨터 IP의 `/32`에만 임시로 SSH를 허용했고, AWS 콘솔 기록의 호스트 공개키를 고정해 확인했다. 인증된 SSH 연결을 SCP와 원격 실행에서 공유했다.

서버의 기존 `start.sh`와 설정을 사용해 새 JAR로 교체하고 `pluskm.service`를 재시작했다. 내부 `/actuator/health`가 `UP`이고 systemd 상태가 `active/running`임을 확인했다. 전송용 임시 파일, 로컬 임시 키와 SSH 연결을 정리했으며, AWS 조회로 22번 포트의 인바운드 규칙이 남지 않았음을 확인했다.

## 외부 API 확인

| 항목 | 결과 |
| --- | --- |
| 판교테크노밸리 1km 병원·약국 조회 | `200`, 109건, 배포 전후 ID 집합 일치 |
| `https://pluskm.live` CORS | 허용 주소 일치 |
| 5km 응급실 정확 반경 조회 | `200`, 5건, 경도·위도 순서 정상 |
| `/auth/test-tokens/1` | `404`, 운영 테스트 토큰 발급 차단 |
| 없는 응급실 상세 | `404` |
| 31km 응급실 조회 | `400` |
| 비로그인 즐겨찾기 조회 | `401` |

프론트 파일과 DNS는 이번 배포에서 변경하지 않았다. 사이트는 [pluskm.live](https://pluskm.live/), API는 [api.pluskm.live](https://api.pluskm.live)다.
