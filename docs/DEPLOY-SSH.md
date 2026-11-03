# SSH 배포

`main` 푸시 시 GitHub Actions의 `Deploy backend over SSH`를 실행한다. 수동 실행도 main에서만 배포한다. `verify` 작업에서 모든 테스트와 `bootJar`를 통과한 뒤 그 JAR만 내려받아 SSH로 보낸다. EC2 명령 전송에 SSM을 사용하지 않는다.

## 서버 전제

현재 시드니 서버의 실행 구조를 사용한다.

- Amazon Linux의 `ec2-user`로 SSH 접속 가능
- Java 21, curl, sha256sum, flock, systemd 설치
- `/opt/pluskm/start.sh`와 `pluskm.service` 존재
- 시작 스크립트는 `/opt/pluskm/backend.jar`를 실행
- 내부 API 포트 8080, 상태 확인 경로 `/actuator/health`
- 배포 계정에서 `sudo -n` 실행 가능

DB와 OAuth, 암호화 키 등 런타임 설정은 서버의 기존 시작 스크립트에서 읽는다. CI가 이를 JAR에 넣거나 새로 전송하지 않는다. 프론트와 DNS를 변경하는 작업은 포함하지 않는다.

배포 접속 설정 후 저장소 변수 `SSH_DEPLOY_ENABLED=true`로 자동 배포를 활성화한다. 설정 전에는 검증 작업만 실행한다. 현재 AWS 계정의 조직 정책은 IAM/OIDC 설정을 차단하므로 배포 키와 네트워크 접근 설정이 별도로 필요하다.

## GitHub 설정

`production` Environment에 다음 값을 등록한다.

| 구분 | 이름 | 값 |
| --- | --- | --- |
| Secret | `SSH_HOST` | EC2의 접속 주소 |
| Secret | `SSH_USER` | 예: `ec2-user` |
| Secret | `SSH_PRIVATE_KEY` | 해당 EC2에 등록한 공개키의 개인키 전체 |
| Secret | `SSH_KNOWN_HOSTS` | 서버 지문을 확인한 known_hosts 항목 |
| Variable | `SSH_PORT` | 생략하면 `22` |

SSH는 `StrictHostKeyChecking=yes`로 실행한다. 비표준 포트의 known_hosts 항목은 `[호스트]:포트` 형식이어야 한다. 실행 시 호스트 키를 자동 수락하지 않는다. GitHub runner의 출발지에서 EC2 SSH 포트에 도달할 수 있어야 한다. 필요하면 `production` Environment의 허용 브랜치를 현재 운영 브랜치로 제한한다.

## 배포 과정

```text
clean check bootJar
  → 통과한 JAR 아티팩트 저장
  → SSH에서 이번 배포용 임시 디렉터리 생성
  → SCP로 JAR 업로드
  → 서버에서 SHA-256 대조
  → 현재 JAR를 backend.jar.previous로 보관
  → 새 JAR로 교체하고 pluskm.service 재시작
  → 최대 120초 동안 내부 health가 UP인지 확인
  → 실패하면 이전 JAR를 복원하고 서비스 재시작
```

동시에 두 배포가 교체 작업을 수행하지 않도록 Actions concurrency와 서버의 flock을 함께 사용한다. 백업은 직전 JAR 하나만 유지한다. 정상 종료와 실패 시 이번 업로드 파일을 정리하며 벤치마크 파일과 로그는 정리하지 않는다. 재시작 중 API 요청은 잠시 실패할 수 있다.

## 로컬에서 같은 스크립트로 배포

먼저 전체 검사를 통과시킨 뒤 실행한다.

```bash
./gradlew clean check bootJar
export SSH_HOST='서버 주소'
export SSH_USER='ec2-user'
export SSH_KEY_PATH="$HOME/.ssh/배포키"
export SSH_KNOWN_HOSTS_FILE="$HOME/.ssh/known_hosts"
export SSH_PORT=22
bash scripts/deploy-ssh.sh build/libs/backend-0.0.1-SNAPSHOT.jar
```

`sshDeploymentTest`는 대역 SSH/SCP로 호스트 검증, 포트, 체크섬 전달, 업로드 정리와 실패 전파를 검사한다. 실제 EC2에서 서비스 교체와 복원까지 실행한 검증과는 구분한다. 상태 확인 실패 시 이전 JAR 복원을 시도하고 배포 작업을 실패로 끝낸다. 이전 프로세스의 정상 복구 여부는 서비스 상태와 로그로 확인한다.

EC2 Instance Connect 임시 키로 접속할 때는 먼저 SSH master 연결을 만들고 `SSH_CONTROL_PATH`에 해당 소켓 경로를 설정하면 SCP와 원격 실행이 인증된 연결을 공유한다. 2026-11-03 운영 배포도 이 방식으로 완료했다. [배포 기록](testing/DEPLOYMENT-2026-11-03.md)
