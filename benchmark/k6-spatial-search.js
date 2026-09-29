import http from 'k6/http';
import { check } from 'k6';

const endpoint = __ENV.ENDPOINT || '/emergencies/search';
const baseUrl = __ENV.BASE_URL || 'http://172.31.27.77:8080';
const vus = Number(__ENV.VUS || 25);
const duration = __ENV.DURATION || '3m';

export const options = {
  vus,
  duration,
  discardResponseBodies: true,
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

const searches = [
  [126.9780, 37.5665, 3],
  [127.0276, 37.4979, 5],
  [126.7052, 37.4563, 10],
  [129.0756, 35.1796, 15],
  [128.6014, 35.8714, 20],
  [126.8514, 35.1595, 30],
  [127.3845, 36.3504, 50],
];

export default function () {
  const search = searches[Math.floor(Math.random() * searches.length)];
  const payload = JSON.stringify({
    longitude: search[0],
    latitude: search[1],
    radiusKilometers: search[2],
  });

  const response = http.post(`${baseUrl}${endpoint}`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { query: __ENV.QUERY_NAME || endpoint },
    timeout: '10s',
  });

  check(response, {
    'HTTP 200 응답': (res) => res.status === 200,
  });
}

export function handleSummary(data) {
  const output = __ENV.SUMMARY_FILE || 'summary.json';
  return {
    [output]: JSON.stringify(data, null, 2),
    stdout: JSON.stringify(data.metrics, null, 2),
  };
}
