#!/usr/bin/env bash
set -euo pipefail

project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../.." && pwd)
test_dir=$(mktemp -d)
trap 'rm -rf -- "$test_dir"' EXIT
mkdir -p "$test_dir/bin"
export DEPLOY_TEST_LOG="$test_dir/transport.log"
cat > "$test_dir/bin/ssh" <<'SSH'
#!/usr/bin/env bash
printf 'ssh %s\n' "$*" >> "$DEPLOY_TEST_LOG"
case "${*: -1}" in
    'mktemp -d /tmp/pluskm-upload.XXXXXXXX') echo '/tmp/pluskm-upload.TEST1234' ;;
    'sudo -n /usr/local/sbin/pluskm-deploy '*) exit "${DEPLOY_TEST_REMOTE_EXIT:-0}" ;;
esac
SSH
cat > "$test_dir/bin/scp" <<'SCP'
#!/usr/bin/env bash
printf 'scp %s\n' "$*" >> "$DEPLOY_TEST_LOG"
SCP
chmod +x "$test_dir/bin/ssh" "$test_dir/bin/scp"
export PATH="$test_dir/bin:$PATH"
export SSH_HOST=server.test.invalid SSH_USER=ec2-user SSH_PORT=2222
export SSH_KEY_PATH="$test_dir/key" SSH_KNOWN_HOSTS_FILE="$test_dir/known_hosts"
export SSH_CONTROL_PATH="$test_dir/control.sock"
printf 'test-jar' > "$test_dir/backend.jar"
printf 'test-key' > "$SSH_KEY_PATH"
printf 'test-host-key' > "$SSH_KNOWN_HOSTS_FILE"

bash -n "$project_dir/scripts/deploy-ssh.sh"
bash -n "$project_dir/scripts/deploy-remote.sh"
bash "$project_dir/scripts/deploy-ssh.sh" "$test_dir/backend.jar"
grep -q 'StrictHostKeyChecking=yes' "$DEPLOY_TEST_LOG"
grep -q "ControlPath=$SSH_CONTROL_PATH" "$DEPLOY_TEST_LOG"
grep -q 'ec2-user@server.test.invalid' "$DEPLOY_TEST_LOG"
grep -q -- '-P 2222' "$DEPLOY_TEST_LOG"
grep -Eq 'sudo -n /usr/local/sbin/pluskm-deploy /tmp/pluskm-upload.TEST1234/backend.jar [a-f0-9]{64}' "$DEPLOY_TEST_LOG"
grep -q 'rmdir -- /tmp/pluskm-upload.TEST1234' "$DEPLOY_TEST_LOG"

if SSH_HOST='server;unexpected' bash "$project_dir/scripts/deploy-ssh.sh" "$test_dir/backend.jar" 2>/dev/null; then
    echo '유효하지 않은 호스트를 거부하지 않았습니다.' >&2; exit 1
fi
if SSH_KNOWN_HOSTS_FILE="$test_dir/missing" bash "$project_dir/scripts/deploy-ssh.sh" "$test_dir/backend.jar" 2>/dev/null; then
    echo 'known_hosts 없이 실행됐습니다.' >&2; exit 1
fi
if DEPLOY_TEST_REMOTE_EXIT=7 bash "$project_dir/scripts/deploy-ssh.sh" "$test_dir/backend.jar"; then
    echo '원격 배포 실패가 성공으로 처리됐습니다.' >&2; exit 1
fi
echo 'SSH 전송 옵션, 체크섬 전달, 임시 파일 정리, 실패 전파 검증 완료'
