const NAVER_URL = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode";
const NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

export default async function handler(request, response) {
  const query = String(request.query.query ?? "").trim();
  if (query.length < 2) return response.status(400).json({ message: "검색할 주소를 입력해 주세요." });
  const naverResult = await searchNaver(query);
  const location = naverResult ?? await searchNominatim(query);
  if (!location) return response.status(404).json({ message: "주소를 찾을 수 없습니다." });
  response.setHeader("Cache-Control", "public, s-maxage=86400, stale-while-revalidate=604800");
  return response.status(200).json(location);
}

async function searchNaver(query) {
  const response = await fetch(`${NAVER_URL}?${new URLSearchParams({ query })}`, { headers: {
    "x-ncp-apigw-api-key-id": process.env.NAVER_MAPS_CLIENT_ID,
    "x-ncp-apigw-api-key": process.env.NAVER_MAPS_CLIENT_SECRET
  }});
  if (!response.ok) return null;
  const address = (await response.json()).addresses?.[0];
  return address ? { latitude: Number(address.y), longitude: Number(address.x), address: address.roadAddress || address.jibunAddress } : null;
}

async function searchNominatim(query) {
  const parameters = new URLSearchParams({ q: query, format: "jsonv2", limit: "1", countrycodes: "kr", "accept-language": "ko" });
  const response = await fetch(`${NOMINATIM_URL}?${parameters}`, { headers: { "User-Agent": "plusKM-emergency-finder/1.0" } });
  if (!response.ok) return null;
  const address = (await response.json())[0];
  return address ? { latitude: Number(address.lat), longitude: Number(address.lon), address: address.display_name } : null;
}
