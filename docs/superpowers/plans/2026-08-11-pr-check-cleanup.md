# PR Check Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 외부 API에 의존하는 AI 리뷰를 제거하고 `develop`에서 `main`으로 보내는 통합 PR이 기능 브랜치 규칙 때문에 실패하지 않도록 한다.

**Architecture:** 일반 Gradle 검증 워크플로는 유지한다. 브랜치 정책 검사만 장기 브랜치 통합 여부에 따라 조건부 실행하고, AI 리뷰에만 사용되는 워크플로·스크립트·정책은 함께 삭제한다.

**Tech Stack:** GitHub Actions, Bash, Gradle, Git

## Global Constraints

- `develop`에서 `main`으로 보내는 PR만 브랜치 이름과 이슈 연결 검사에서 제외한다.
- 일반 기능 및 수정 브랜치는 기존 이름과 이슈 연결 규칙을 유지한다.
- Gradle 테스트는 모든 PR에서 실행한다.
- `docs/변경점.md`는 수정하거나 커밋하지 않는다.
- 기존 커밋은 코드 내용과 순서를 유지하고 Conventional Commits 제목만 적용한다.

---

### Task 1: AI 리뷰 제거

**Files:**
- Delete: `.github/workflows/ai-pr-review.yml`
- Delete: `.github/review-policy.md`
- Delete: `scripts/ai_pr_review.mjs`

**Interfaces:**
- Consumes: GitHub `pull_request` 이벤트
- Produces: 외부 OpenAI API 호출이 없는 PR 검사 구성

- [ ] **Step 1: 전용 파일과 참조 확인**

Run: `rg --hidden -n "ai-pr-review|ai_pr_review|review-policy|codex-pre-review" .github scripts`

Expected: 삭제 대상 세 파일에만 AI 리뷰 참조가 존재한다.

- [ ] **Step 2: AI 리뷰 전용 파일 삭제**

`.github/workflows/ai-pr-review.yml`, `.github/review-policy.md`, `scripts/ai_pr_review.mjs`를 삭제한다.

- [ ] **Step 3: 잔여 참조 검증**

Run: `rg --hidden -n "ai-pr-review|ai_pr_review|review-policy|codex-pre-review" .github scripts`

Expected: 검색 결과가 없다.

### Task 2: 통합 브랜치 정책 예외

**Files:**
- Modify: `.github/workflows/pr-check.yml`

**Interfaces:**
- Consumes: `github.head_ref`, `github.event.pull_request.base.ref`, PR 본문
- Produces: `develop -> main`은 정책 검사 생략, 나머지는 기존 정책 검사

- [ ] **Step 1: 통합 브랜치 여부를 환경 변수로 전달**

`BASE_BRANCH: ${{ github.event.pull_request.base.ref }}`를 검사 단계 환경 변수에 추가한다.

- [ ] **Step 2: 장기 브랜치 통합 예외 추가**

검사 스크립트 시작에 아래 조건을 추가한다.

```bash
if [[ "$BRANCH_NAME" == "develop" && "$BASE_BRANCH" == "main" ]]; then
  echo "Skipping feature branch policy for develop -> main integration PR."
  exit 0
fi
```

- [ ] **Step 3: 정책 조건 로컬 검증**

Run: `bash -n <(sed -n '/run: |/,$p' .github/workflows/pr-check.yml | sed '1d; s/^          //')`

Expected: 셸 구문 오류가 없다.

### Task 3: 전체 검증과 커밋 정리

**Files:**
- Verify: `.github/workflows/pr-check.yml`
- Verify: `src/main/**`, `src/test/**`

**Interfaces:**
- Consumes: Tasks 1-2 결과
- Produces: 검증된 `develop` 브랜치와 갱신된 PR #33

- [ ] **Step 1: 전체 테스트 실행**

Run: `bash gradlew test --rerun-tasks`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: 기존 커밋 제목 재작성**

기존 커밋을 `refactor`, `test`, `docs` 접두사로 재작성하고 CI 변경은 `ci: AI 리뷰 제거 및 통합 브랜치 검사 예외 처리`로 커밋한다.

- [ ] **Step 3: 원격 브랜치 갱신**

Run: `git push --force-with-lease origin develop`

Expected: PR #33의 head가 갱신되고 일반 테스트 체크가 다시 실행된다.
