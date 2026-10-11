const TILE_URL = "https://tile.openstreetmap.org";

export default async function handler(request, response) {
  const zoom = integer(request.query.z, 0, 19);
  const x = integer(request.query.x, 0, 600000);
  const y = integer(request.query.y, 0, 600000);
  if (zoom === null || x === null || y === null) {
    return response.status(400).json({ message: "올바른 지도 타일 좌표가 필요합니다." });
  }

  const tileResponse = await fetch(`${TILE_URL}/${zoom}/${x}/${y}.png`, {
    headers: { "User-Agent": "plusKM emergency-map/1.0" }
  });
  if (!tileResponse.ok) {
    return response.status(502).json({ message: "지도 타일을 가져올 수 없습니다." });
  }
  response.setHeader("Content-Type", "image/png");
  response.setHeader("Cache-Control", "public, s-maxage=86400, stale-while-revalidate=604800");
  return response.status(200).send(Buffer.from(await tileResponse.arrayBuffer()));
}

function integer(value, minimum, maximum) {
  const number = Number(value);
  if (!Number.isInteger(number) || number < minimum || number > maximum) return null;
  return number;
}
