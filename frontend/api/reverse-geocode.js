const URL = "https://nominatim.openstreetmap.org/reverse";

export default async function handler(request, response) {
  const { longitude, latitude } = request.query;
  const query = new URLSearchParams({
    lon: longitude,
    lat: latitude,
    format: "jsonv2",
    zoom: "16",
    "accept-language": "ko"
  });
  const apiResponse = await fetch(`${URL}?${query}`, {
    headers: { "User-Agent": "plusKM-emergency-finder/1.0" }
  });
  if (!apiResponse.ok) return response.status(502).json({ message: "지역을 확인할 수 없습니다." });
  const data = await apiResponse.json();
  const address = data.address ?? {};
  const names = [address.city || address.province, address.borough || address.county, address.suburb || address.quarter]
    .filter(Boolean);
  response.setHeader("Cache-Control", "s-maxage=86400, stale-while-revalidate=604800");
  return response.status(200).json({ region: [...new Set(names)].join(" ") || "선택한 위치" });
}
