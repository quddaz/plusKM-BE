# Issue·PR 브랜치 자동화 설계

## 목표

이슈에서 작업 브랜치를 만드는 반복 작업을 줄이고, 이슈와 PR의 연결 관계를 일관되게 관리한다.

## 브랜치 규칙

```text
feat/{issue-number}/{slug}
fix/{issue-number}/{slug}
```

예시:

```text
feat/14/emergency-status
fix/15/cache-stale
```

`#` 문자는 브랜치명에 포함하지 않는다. GitHub 이슈 번호는 PR 본문의 `Closes #14`와 같은 방식으로 연결한다.

## 이슈에서 브랜치 생성

- 트리거: 이슈에 `ready` 라벨이 추가될 때
- 타입: `feat` 또는 `fix` 라벨 중 하나를 사용하며, 라벨이 없으면 템플릿 제목의 `[feat]` 또는 `[fix]` 접두사를 사용한다.
- 타입 라벨이 없거나 둘 다 있으면 브랜치를 만들지 않고 이슈에 안내 댓글을 남긴다.
- 이슈 제목을 소문자 kebab-case slug로 변환한다.
- 같은 브랜치가 이미 있으면 새로 만들지 않고 기존 브랜치를 안내한다.
- 브랜치 생성 후 이슈에 브랜치명과 PR 연결 형식을 댓글로 남긴다.
- 기본 브랜치의 최신 커밋을 기준으로 브랜치를 생성한다.

## PR 자동화

- 대상: `main`, `develop`으로 향하는 PR
- PR 생성·재오픈·동기화 시 `./gradlew clean test` 실행
- PR 본문에 `Closes #숫자` 또는 `Fixes #숫자`가 없으면 검증 실패
- 브랜치 접두사에 따라 `feat` 또는 `fix` 라벨을 자동 부여한다.
- PR에는 기존 이슈를 중복해서 생성하지 않는다.

## Codex 사전 리뷰

- 트리거: `main`, `develop` 대상 PR의 생성·재오픈·동기화·리뷰 준비
- 입력: PR 제목·본문, 변경 파일 목록, diff, `.github/review-policy.md`
- 범위: PR에 적은 의사결정과 diff의 일치 여부, 파일별 테스트 후보, 저장소 컨벤션 점검
- 범위 밖: 일반적인 버그·보안·성능·아키텍처 리뷰 및 자동 merge 차단
- 결과: PR의 고정 댓글을 갱신하며 사람이 최종 판단한다.
- 인증: `OPENAI_API_KEY` 저장소 Secret과 선택적인 `OPENAI_MODEL` 저장소 Variable을 사용한다.
- 보안: PR 코드를 실행하지 않고 diff만 OpenAI Responses API에 전달하며 `store: false`를 사용한다.
- 외부 fork PR은 Secret 노출을 막기 위해 해당 workflow의 자동 댓글 대상에서 제외한다.

## 권한과 안전성

- 이슈 브랜치 workflow는 `contents: write`, `issues: write` 권한만 사용한다.
- PR 검증 workflow는 `contents: read`, `pull-requests: write` 권한만 사용한다.
- 외부 pull request의 코드를 실행하는 workflow에서는 쓰기 권한을 사용하지 않는다.
- 브랜치 자동 생성은 `ready` 라벨을 승인 신호로 사용해 이슈 생성만으로 브랜치가 생기지 않도록 한다.

## 실패 처리

- `feat`와 `fix`가 모두 지정된 경우 이슈에 올바른 라벨 하나만 남기라는 댓글을 남긴다.
- 제목으로 만든 slug가 비어 있으면 이슈 번호를 사용한다.
- 브랜치 생성 또는 라벨 부여 실패 시 workflow를 실패시키고 로그에 원인을 남긴다.

## 검증

- 이슈에 `ready`와 `feat`를 붙였을 때 `feat/{number}/{slug}`가 생성되는지 확인한다.
- 동일 이슈에 `ready`를 다시 붙여도 중복 브랜치가 생성되지 않는지 확인한다.
- `fix` 라벨로 `fix/{number}/{slug}`가 생성되는지 확인한다.
- 연결 이슈가 없는 PR이 실패하는지 확인한다.
- 연결 이슈가 있는 PR에서 Gradle 테스트가 실행되는지 확인한다.
