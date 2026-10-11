#!/usr/bin/env bash
set -euo pipefail

RESULT_DIR="${RESULT_DIR:-/opt/pluskm-pangyo-exact-circle-20260930}"
SCRIPT_PATH="${SCRIPT_PATH:-/opt/k6-spatial-search.js}"
BASE_URL="${BASE_URL:-http://172.31.27.77:8080}"
RUN_ID="${RUN_ID:-20260930-pangyo-exact-circle}"
PANGYO_LONGITUDE="${PANGYO_LONGITUDE:-127.1112}"
PANGYO_LATITUDE="${PANGYO_LATITUDE:-37.3948}"
RADII="${RADII:-1 5 10 30}"
WARMUP_DURATION="${WARMUP_DURATION:-60s}"
MEASURE_DURATION="${MEASURE_DURATION:-180s}"
WARMUP_VUS="${WARMUP_VUS:-25}"
MEASURE_VUS="${MEASURE_VUS:-100}"

mkdir -p "$RESULT_DIR"
date -u +%FT%TZ > "$RESULT_DIR/started-at.txt"
printf 'longitude=%s\nlatitude=%s\nradii_km=%s\n' \
  "$PANGYO_LONGITUDE" "$PANGYO_LATITUDE" "$RADII" > "$RESULT_DIR/query-point.txt"
uname -a > "$RESULT_DIR/load-generator-uname.txt"
k6 version > "$RESULT_DIR/k6-version.txt"

# 부하 생성기 자체의 CPU, 메모리, 네트워크 사용량도 함께 남긴다.
sar -o "$RESULT_DIR/load-generator.sar" 1 8000 >/dev/null 2>&1 &
SAR_PID=$!
trap 'kill "$SAR_PID" 2>/dev/null || true' EXIT

snapshot() {
  local name="$1"
  curl -fsS "$BASE_URL/actuator/prometheus" > "$RESULT_DIR/${name}-prometheus.txt"
  curl -fsS "$BASE_URL/actuator/health" > "$RESULT_DIR/${name}-health.json"
}

run_case() {
  local radius="$1"
  local label="$2"
  local endpoint="$3"
  local prefix="radius-${radius}km-${label}"

  if [[ -f "$RESULT_DIR/${prefix}-measure-summary.json" ]]; then
    echo "[$(date -u +%FT%TZ)] SKIP ${prefix} (existing result)"
    return
  fi

  echo "[$(date -u +%FT%TZ)] START ${prefix} warmup (${WARMUP_VUS} VU, ${WARMUP_DURATION})"
  snapshot "${prefix}-before-warmup"
  BASE_URL="$BASE_URL" ENDPOINT="$endpoint" QUERY_NAME="$prefix" \
    SEARCH_LONGITUDE="$PANGYO_LONGITUDE" SEARCH_LATITUDE="$PANGYO_LATITUDE" \
    SEARCH_RADIUS_KM="$radius" VUS="$WARMUP_VUS" DURATION="$WARMUP_DURATION" \
    SUMMARY_FILE="$RESULT_DIR/${prefix}-warmup-summary.json" \
    k6 run --quiet "$SCRIPT_PATH" > "$RESULT_DIR/${prefix}-warmup-console.json"
  snapshot "${prefix}-after-warmup"

  echo "[$(date -u +%FT%TZ)] START ${prefix} measure (${MEASURE_VUS} VU, ${MEASURE_DURATION})"
  BASE_URL="$BASE_URL" ENDPOINT="$endpoint" QUERY_NAME="$prefix" \
    SEARCH_LONGITUDE="$PANGYO_LONGITUDE" SEARCH_LATITUDE="$PANGYO_LATITUDE" \
    SEARCH_RADIUS_KM="$radius" VUS="$MEASURE_VUS" DURATION="$MEASURE_DURATION" \
    SUMMARY_FILE="$RESULT_DIR/${prefix}-measure-summary.json" \
    k6 run --quiet "$SCRIPT_PATH" > "$RESULT_DIR/${prefix}-measure-console.json"
  snapshot "${prefix}-after-measure"
  echo "[$(date -u +%FT%TZ)] DONE ${prefix}"
  sleep 30
}

for radius in $RADII; do
  # 두 경로를 교차해 순서 효과를 줄이는 ABBA 배치다.
  run_case "$radius" single-distance-a /emergencies/search/optimized
  run_case "$radius" circle-within-b /emergencies/search/circle-within
  run_case "$radius" circle-within-b2 /emergencies/search/circle-within
  run_case "$radius" single-distance-a2 /emergencies/search/optimized
done

kill "$SAR_PID" 2>/dev/null || true
wait "$SAR_PID" 2>/dev/null || true
sar -A -f "$RESULT_DIR/load-generator.sar" > "$RESULT_DIR/load-generator-sar.txt"
date -u +%FT%TZ > "$RESULT_DIR/finished-at.txt"

tar -C "$(dirname "$RESULT_DIR")" -czf "/tmp/${RUN_ID}-k6.tar.gz" "$(basename "$RESULT_DIR")"
