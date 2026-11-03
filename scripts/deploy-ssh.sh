#!/usr/bin/env bash
set -euo pipefail

project_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
artifact=${1:?배포할 bootJar 경로가 필요합니다.}
: "${SSH_HOST:?SSH_HOST가 필요합니다.}"
: "${SSH_USER:?SSH_USER가 필요합니다.}"
: "${SSH_KEY_PATH:?SSH_KEY_PATH가 필요합니다.}"
: "${SSH_KNOWN_HOSTS_FILE:?SSH_KNOWN_HOSTS_FILE이 필요합니다.}"
ssh_port=${SSH_PORT:-22}

[[ "$SSH_HOST" =~ ^[a-zA-Z0-9.:-]+$ ]] || { echo '잘못된 SSH 호스트' >&2; exit 1; }
[[ "$SSH_USER" =~ ^[a-zA-Z_][a-zA-Z0-9_-]*$ ]] || { echo '잘못된 SSH 사용자' >&2; exit 1; }
[[ "$ssh_port" =~ ^[0-9]{1,5}$ ]] && ((10#$ssh_port > 0 && 10#$ssh_port <= 65535)) || {
    echo '잘못된 SSH 포트' >&2; exit 1;
}
[[ -f "$artifact" && -s "$artifact" && -f "$SSH_KEY_PATH" && -s "$SSH_KNOWN_HOSTS_FILE" ]] || {
    echo 'JAR, SSH 키, known_hosts 파일을 확인해 주세요.' >&2; exit 1;
}

target="$SSH_USER@$SSH_HOST"
options=(-i "$SSH_KEY_PATH" -o BatchMode=yes -o StrictHostKeyChecking=yes
    -o "UserKnownHostsFile=$SSH_KNOWN_HOSTS_FILE" -o ConnectTimeout=10
    -o ServerAliveInterval=15 -o ServerAliveCountMax=3)
if [[ -n "${SSH_CONTROL_PATH:-}" ]]; then
    # 임시 인증 키를 쓸 때는 이미 인증된 연결을 SCP와 원격 실행에서 공유한다.
    options+=(-o "ControlPath=$SSH_CONTROL_PATH")
fi
remote_dir=''
cleanup() {
    if [[ -n "$remote_dir" ]]; then
        # 이번 실행에서 만든 업로드 파일만 정리한다.
        ssh "${options[@]}" -p "$ssh_port" "$target" \
            "rm -f -- $remote_dir/backend.jar; rmdir -- $remote_dir" || true
    fi
}
trap cleanup EXIT

if command -v sha256sum >/dev/null; then
    checksum=$(sha256sum "$artifact" | awk '{print $1}')
else
    checksum=$(shasum -a 256 "$artifact" | awk '{print $1}')
fi
remote_dir=$(ssh "${options[@]}" -p "$ssh_port" "$target" 'mktemp -d /tmp/pluskm-upload.XXXXXXXX')
[[ "$remote_dir" =~ ^/tmp/pluskm-upload\.[a-zA-Z0-9]+$ ]] || {
    remote_dir=''; echo '업로드 디렉터리 생성 실패' >&2; exit 1;
}
scp "${options[@]}" -P "$ssh_port" "$artifact" "$target:$remote_dir/backend.jar"
ssh "${options[@]}" -p "$ssh_port" "$target" \
    "sudo -n bash -s -- $remote_dir/backend.jar $checksum" < "$project_dir/scripts/deploy-remote.sh"
