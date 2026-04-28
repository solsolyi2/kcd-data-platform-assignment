---
name: commit-push
description: KCD 저장소 커밋/푸시 가드. Conventional Commits 형식, 단위 커밋(서비스/기능 1개), .env·시크릿 차단, hooks 우회 금지를 보장하며 push 까지 수행. "커밋해", "푸시해", "변경 커밋" 같은 요청 시 사용.
---

# commit-push — Conventional Commits + Git 안전 가드

본 저장소의 git 운용 규칙(CLAUDE.md §7 / §10 / §11) 을 자동 적용한다.
**위반 시 사용자에게 확인 받기 전까지 commit/push 진행 금지.**

## 사전 점검 (모두 통과해야 진행)

- [ ] `git status` / `git diff --staged` 로 변경 파일 식별.
- [ ] `.env`, `*.pem`, `*.key`, `secrets/` 같은 시크릿 후보가 staged 에 없음.
      있다면 즉시 STOP 하고 사용자에게 알림.
- [ ] 한 커밋 = **하나의 기능/서비스** 단위. 무관한 변경이 섞였으면 분리 제안.
- [ ] 빌드 / 테스트 / ktlint 가 통과한 상태인지 확인 (필요 시 `./gradlew build` 권장).
- [ ] hook 우회 옵션 사용 금지: `--no-verify`, `--no-gpg-sign`, `-c commit.gpgsign=false`.

## 커밋 메시지 — Conventional Commits

형식: `<type>(<scope>): <subject>`

- type: `feat`, `fix`, `chore`, `build`, `docs`, `refactor`, `test`, `perf`
- scope (이 저장소 전용): `pipeline`, `outbox`, `report`, `partner`, `auth`,
  `webhook`, `querylog`, `common`, `db`, `infra`, `docs`
- subject: 50자 이내, 명령형 한국어 또는 영어. 끝에 마침표 X.

본문에는 다음을 포함:
- **WHY** — 왜 변경했는가 (CLAUDE.md 어느 절 / 어떤 NFR 충족)
- **검증** — 어떤 테스트/명령으로 통과 확인했는가
- 트레이드오프 / 향후 개선이 있다면 한 줄

**Co-Authored-By 트레일러 추가 금지** (사용자가 명시 요청한 경우 제외).

### 예시

```
feat(pipeline): add outbox-driven REQUESTED→COLLECTING transition

- ReportRequestService 가 도메인 변경 + OutboxAppender.append 를
  같은 @Transactional 에 묶어 §1.2 (1)(2)(3) 충족.
- Producer key = registrationNumber → §4.2 순서 보장.
- 검증: ReportRequestServiceIntegrationTest, ./gradlew test 통과.
```

## 실행 절차

```bash
# 1. 상태 확인
git status
git diff --staged

# 2. 메시지 작성 (HEREDOC)
git commit -m "$(cat <<'EOF'
<type>(<scope>): <subject>

<body>
EOF
)"

# 3. 푸시 (현재 브랜치 → 동일 이름 원격 추적)
git push
```

## 금지 / 주의

- `git add -A` / `git add .` 지양. 의도한 파일만 명시.
- `git push --force` 는 사용자 명시 승인 필요 (특히 main 절대 금지).
- pre-commit hook 실패 시: **--amend 금지**, 원인 수정 후 새 커밋.
- 충돌 발생 시 `git checkout -- .` / `git reset --hard` 같은 파괴적 명령 금지,
  rebase 또는 merge 로 해결.

## 푸시 후

- `git log -1 --stat` 로 푸시 결과 확인.
- 의도와 다른 변경이 들어갔으면 즉시 사용자에게 알림 + revert 제안.
