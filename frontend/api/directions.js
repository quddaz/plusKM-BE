const DIRECTIONS_URL = "https://maps.apigw.ntruss.com/map-direction/v1/driving";

export default async function handler(request, response) {
  const { start, goal } = request.query;
  if (!isCoordinate(start) || !isCoordinate(goal)) {
    return response.status(400).json({ message: "출발지와 목적지 좌표가 필요합니다." });
  }

  const routeResponse = await fetch(`${DIRECTIONS_URL}?start=${start}&goal=${goal}`, {
    headers: {
      "x-ncp-apigw-api-key-id": process.env.NAVER_MAPS_CLIENT_ID,
      "x-ncp-apigw-api-key": process.env.NAVER_MAPS_CLIENT_SECRET
    }
  });
  if (!routeResponse.ok) {
    return response.status(502).json({ message: "자동차 경로를 조회할 수 없습니다." });
  }

  const data = await routeResponse.json();
  const route = data.route?.traoptimal?.[0];
  if (!route) {
    return response.status(404).json({ message: "자동차 경로가 없습니다." });
  }
  return response.status(200).json({
    path: route.path,
    distance: route.summary.distance,
    duration: route.summary.duration
  });
}

function isCoordinate(value) {
  return typeof value === "string" && /^-?\d+(\.\d+)?,-?\d+(\.\d+)?$/.test(value);
}
