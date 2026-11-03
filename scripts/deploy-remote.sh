#!/usr/bin/env bash
set -euo pipefail

incoming=${1:?업로드된 JAR 경로가 필요합니다.}
expected_sha=${2:?SHA-256 값이 필요합니다.}
app_dir=/opt/pluskm
service=pluskm
health_url=http://127.0.0.1:8080/actuator/health

[[ $EUID -eq 0 ]] || { echo 'sudo 권한이 필요합니다.' >&2; exit 1; }
[[ "$incoming" =~ ^/tmp/pluskm-upload\.[a-zA-Z0-9]+/backend\.jar$ ]] || exit 1
[[ "$expected_sha" =~ ^[a-f0-9]{64}$ ]] || exit 1
[[ -f "$incoming" && ! -L "$incoming" && -x "$app_dir/start.sh" ]] || exit 1
for executable in java curl sha256sum systemctl flock install; do
    command -v "$executable" >/dev/null || { echo "$executable 명령이 필요합니다." >&2; exit 1; }
done
systemctl cat "$service" >/dev/null
actual_sha=$(sha256sum "$incoming" | awk '{print $1}')
[[ "$actual_sha" == "$expected_sha" ]] || { echo '업로드된 JAR의 SHA-256이 다릅니다.' >&2; exit 1; }

# 현재 서비스의 시작 스크립트와 서버에 보관된 환경 설정을 그대로 사용한다.
exec 9>"$app_dir/.deploy.lock"
flock -n 9 || { echo '다른 배포가 진행 중입니다.' >&2; exit 1; }
has_previous=false
activated=false
finish() {
    result=$?
    trap - EXIT
    if (( result != 0 )) && [[ "$activated" == true && "$has_previous" == true ]]; then
        echo '배포 실패: 이전 JAR로 복원합니다.' >&2
        install -m 644 "$app_dir/backend.jar.previous" "$app_dir/backend.jar"
        systemctl restart "$service" || true
    fi
    rm -f -- "$app_dir/backend.jar.next"
    exit "$result"
}
trap finish EXIT

if [[ -f "$app_dir/backend.jar" ]]; then
    install -m 644 "$app_dir/backend.jar" "$app_dir/backend.jar.previous"
    has_previous=true
fi
install -m 644 "$incoming" "$app_dir/backend.jar.next"
mv -f -- "$app_dir/backend.jar.next" "$app_dir/backend.jar"
activated=true
systemctl restart "$service"

deadline=$((SECONDS + 120))
while (( SECONDS < deadline )); do
    if systemctl is-active --quiet "$service" && \
        curl --fail --silent --max-time 3 "$health_url" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"'; then
        printf '%s\n' "$expected_sha" > "$app_dir/.deployed-sha256"
        echo 'SSH 배포 및 상태 확인 완료'
        exit 0
    fi
    sleep 2
done
echo '120초 안에 서비스가 정상 상태가 되지 않았습니다.' >&2
exit 1
