#!/usr/bin/env bash
set -euo pipefail

RESULT_DIR="${RESULT_DIR:-/opt/pluskm-indexed-bounds-benchmark-20260929}"
SCRIPT_PATH="${SCRIPT_PATH:-/opt/k6-spatial-search.js}"
BASE_URL="${BASE_URL:-http://172.31.27.77:8080}"
BUCKET="${BUCKET:-pluskm-deploy-524320817955-ap-northeast-2}"
RUN_ID="${RUN_ID:-20260929-mysql-indexed-bounds}"

mkdir -p "$RESULT_DIR"
date -u +%FT%TZ > "$RESULT_DIR/started-at.txt"
sar -o "$RESULT_DIR/load-generator.sar" 1 700 >/dev/null 2>&1 &
SAR_PID=$!
trap 'kill "$SAR_PID" 2>/dev/null || true' EXIT

run_case() {
  local label="$1"
  curl -fsS "$BASE_URL/actuator/prometheus" > "$RESULT_DIR/${label}-before-warmup-prometheus.txt"
  BASE_URL="$BASE_URL" ENDPOINT=/emergencies/search/indexed-bounds QUERY_NAME="$label" \
    VUS=10 DURATION=60s SUMMARY_FILE="$RESULT_DIR/${label}-warmup-summary.json" \
    k6 run --quiet "$SCRIPT_PATH" > "$RESULT_DIR/${label}-warmup-console.json"
  curl -fsS "$BASE_URL/actuator/prometheus" > "$RESULT_DIR/${label}-after-warmup-prometheus.txt"
  BASE_URL="$BASE_URL" ENDPOINT=/emergencies/search/indexed-bounds QUERY_NAME="$label" \
    VUS=25 DURATION=180s SUMMARY_FILE="$RESULT_DIR/${label}-measure-summary.json" \
    k6 run --quiet "$SCRIPT_PATH" > "$RESULT_DIR/${label}-measure-console.json"
  curl -fsS "$BASE_URL/actuator/prometheus" > "$RESULT_DIR/${label}-after-measure-prometheus.txt"
  sleep 30
}

run_case indexed-c
run_case indexed-c2

kill "$SAR_PID" 2>/dev/null || true
wait "$SAR_PID" 2>/dev/null || true
sar -A -f "$RESULT_DIR/load-generator.sar" > "$RESULT_DIR/load-generator-sar.txt"
date -u +%FT%TZ > "$RESULT_DIR/finished-at.txt"
tar -C "$(dirname "$RESULT_DIR")" -czf "/tmp/${RUN_ID}-k6.tar.gz" "$(basename "$RESULT_DIR")"
aws s3 cp "/tmp/${RUN_ID}-k6.tar.gz" "s3://${BUCKET}/benchmarks/${RUN_ID}-k6.tar.gz"
