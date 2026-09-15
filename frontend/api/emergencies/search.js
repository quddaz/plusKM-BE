const FACILITY_URL = "https://apis.data.go.kr/B552657/ErmctInfoInqireService/getEgytListInfoInqire";

export default async function handler(request, response) {
  if (request.method !== "POST") return response.status(405).end();
  const { longitude, latitude, radiusKilometers = 10 } = request.body ?? {};
  if (![longitude, latitude, radiusKilometers].every(Number.isFinite)) {
    return response.status(400).json({ message: "위치와 검색 반경이 필요합니다." });
  }
  const key = decodeURIComponent(process.env.EMERGENCY_API_SERVICE_KEY.replace(/\+/g, "%2B"));
  const query = new URLSearchParams({ serviceKey: key, pageNo: "1", numOfRows: "1000" });
  const apiResponse = await fetch(`${FACILITY_URL}?${query}`);
  if (!apiResponse.ok) return response.status(502).json({ message: "응급실 정보를 가져올 수 없습니다." });
  const emergencies = items(await apiResponse.text()).map(toEmergency).filter(Boolean)
    .map(emergency => ({ ...emergency, distance: distance({ longitude, latitude }, emergency) }))
    .filter(emergency => emergency.distance <= radiusKilometers)
    .sort((first, second) => first.distance - second.distance).slice(0, 30)
    .map(({ distance: ignored, ...emergency }) => emergency);
  response.setHeader("Cache-Control", "s-maxage=300, stale-while-revalidate=600");
  return response.status(200).json({ emergencies });
}

function items(xml) { return xml.match(/<item>[\s\S]*?<\/item>/g) ?? []; }
function toEmergency(item, index) {
  const longitude = Number(value(item, "wgs84Lon"));
  const latitude = Number(value(item, "wgs84Lat"));
  const name = value(item, "dutyName");
  if (!name || !Number.isFinite(longitude) || !Number.isFinite(latitude)) return null;
  return { id: value(item, "hpid") || index, name, address: value(item, "dutyAddr"),
    phoneNumber: value(item, "dutyTel3") || value(item, "dutyTel1"), longitude, latitude };
}
function value(xml, tag) {
  const match = xml.match(new RegExp(`<${tag}>([\\s\\S]*?)<\\/${tag}>`));
  return match ? match[1].trim().replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">") : "";
}
function distance(origin, destination) {
  const radians = value => value * Math.PI / 180;
  const latitude = radians(destination.latitude - origin.latitude);
  const longitude = radians(destination.longitude - origin.longitude);
  const calculation = Math.sin(latitude / 2) ** 2 + Math.cos(radians(origin.latitude))
    * Math.cos(radians(destination.latitude)) * Math.sin(longitude / 2) ** 2;
  return 6371 * 2 * Math.atan2(Math.sqrt(calculation), Math.sqrt(1 - calculation));
}
