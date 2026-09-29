#!/usr/bin/env bash
set -euo pipefail

RESULT_DIR="${RESULT_DIR:-/opt/pluskm-spatial-benchmark-20260929}"
SCRIPT_PATH="${SCRIPT_PATH:-/opt/k6-spatial-search.js}"
BASE_URL="${BASE_URL:-http://172.31.27.77:8080}"
BUCKET="${BUCKET:-pluskm-deploy-524320817955-ap-northeast-2}"
RUN_ID="${RUN_ID:-20260929-mysql-spatial}"
WARMUP_DURATION="${WARMUP_DURATION:-60s}"
MEASURE_DURATION="${MEASURE_DURATION:-180s}"
WARMUP_VUS="${WARMUP_VUS:-10}"
MEASURE_VUS="${MEASURE_VUS:-25}"

mkdir -p "$RESULT_DIR"
date -u +%FT%TZ > "$RESULT_DIR/started-at.txt"
uname -a > "$RESULT_DIR/load-generator-uname.txt"
k6 version > "$RESULT_DIR/k6-version.txt"

sar -o "$RESULT_DIR/load-generator.sar" 1 1400 >/dev/null 2>&1 &
SAR_PID=$!
trap 'kill "$SAR_PID" 2>/dev/null || true' EXIT

snapshot() {
  local name="$1"
  curl -fsS "$BASE_URL/actuator/prometheus" > "$RESULT_DIR/${name}-prometheus.txt"
  curl -fsS "$BASE_URL/actuator/health" > "$RESULT_DIR/${name}-health.json"
}

run_case() {
  local label="$1"
  local endpoint="$2"

  snapshot "${label}-before-warmup"
  BASE_URL="$BASE_URL" ENDPOINT="$endpoint" QUERY_NAME="$label" \
    VUS="$WARMUP_VUS" DURATION="$WARMUP_DURATION" \
    SUMMARY_FILE="$RESULT_DIR/${label}-warmup-summary.json" \
    k6 run --quiet "$SCRIPT_PATH" > "$RESULT_DIR/${label}-warmup-console.json"
  snapshot "${label}-after-warmup"

  BASE_URL="$BASE_URL" ENDPOINT="$endpoint" QUERY_NAME="$label" \
    VUS="$MEASURE_VUS" DURATION="$MEASURE_DURATION" \
    SUMMARY_FILE="$RESULT_DIR/${label}-measure-summary.json" \
    k6 run --quiet "$SCRIPT_PATH" > "$RESULT_DIR/${label}-measure-console.json"
  snapshot "${label}-after-measure"
  sleep 30
}

# 순서 편향을 줄이기 위한 ABBA 실행. 각 본 측정 앞에서 워밍업을 새로 수행한다.
run_case legacy-a /emergencies/search
run_case optimized-b /emergencies/search/optimized
run_case optimized-b2 /emergencies/search/optimized
run_case legacy-a2 /emergencies/search

kill "$SAR_PID" 2>/dev/null || true
wait "$SAR_PID" 2>/dev/null || true
sar -A -f "$RESULT_DIR/load-generator.sar" > "$RESULT_DIR/load-generator-sar.txt"
date -u +%FT%TZ > "$RESULT_DIR/finished-at.txt"

tar -C "$(dirname "$RESULT_DIR")" -czf "/tmp/${RUN_ID}-k6.tar.gz" "$(basename "$RESULT_DIR")"
aws s3 cp "/tmp/${RUN_ID}-k6.tar.gz" "s3://${BUCKET}/benchmarks/${RUN_ID}-k6.tar.gz"
