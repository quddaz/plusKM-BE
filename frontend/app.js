const DEFAULT_LOCATION = { latitude: 37.5665, longitude: 126.9780 };
const state = { location: DEFAULT_LOCATION, radiusKilometers: 10, hospitals: [], map: null,
  mapProvider: null, markers: [], userMarker: null, route: null, selectedHospital: null };
const list = document.querySelector("#hospitalList");
const dataState = document.querySelector("#dataState");
const locationLabel = document.querySelector("#locationLabel");
const resultCount = document.querySelector("#resultCount");
const NAVER_MAP_CLIENT_ID = "cjtt3s316g";
const MAP_LOAD_DELAYS = [0, 1500, 4000];

async function loadNaverMap() {
  for (let attempt = 0; attempt < MAP_LOAD_DELAYS.length; attempt++) {
    await wait(MAP_LOAD_DELAYS[attempt]);
    dataState.textContent = `네이버 지도 연결 중 ${attempt + 1}/${MAP_LOAD_DELAYS.length}`;
    if (await loadNaverMapScript(attempt)) return true;
  }
  return false;
}

function loadNaverMapScript(attempt) {
  return new Promise(resolve => {
    const script = document.createElement("script");
    let completed = false;
    const finish = loaded => {
      if (completed) return;
      completed = true;
      if (!loaded) script.remove();
      resolve(loaded);
    };
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${NAVER_MAP_CLIENT_ID}&retry=${attempt}`;
    script.onload = () => finish(Boolean(window.naver?.maps));
    script.onerror = () => finish(false);
    document.head.appendChild(script);
    setTimeout(() => finish(Boolean(window.naver?.maps)), 5000);
  });
}

function wait(milliseconds) {
  return new Promise(resolve => setTimeout(resolve, milliseconds));
}

function distanceInKilometers(origin, destination) {
  const toRadians = value => value * Math.PI / 180;
  const latitude = toRadians(destination.latitude - origin.latitude);
  const longitude = toRadians(destination.longitude - origin.longitude);
  const value = Math.sin(latitude / 2) ** 2 + Math.cos(toRadians(origin.latitude))
    * Math.cos(toRadians(destination.latitude)) * Math.sin(longitude / 2) ** 2;
  return 6371 * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
}

async function loadHospitals() {
  dataState.textContent = "응급실 확인 중";
  try {
    const response = await fetch("/api/emergencies/search", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...state.location, radiusKilometers: state.radiusKilometers })
    });
    if (!response.ok) throw new Error("API 연결 실패");
    state.hospitals = (await response.json()).emergencies;
    dataState.textContent = "방금 업데이트";
  } catch (error) {
    state.hospitals = [];
    dataState.textContent = "응급실 정보를 불러오지 못했어요";
  }
  render();
}

function render() {
  const hospitals = state.hospitals.map(hospital => ({
    ...hospital, distance: distanceInKilometers(state.location, hospital)
  })).sort((first, second) => first.distance - second.distance);
  resultCount.textContent = hospitals.length;
  list.innerHTML = hospitals.length ? hospitals.map(card).join("") : '<p class="empty">반경 안의 응급실 정보를 확인할 수 없습니다.</p>';
  renderMarkers(hospitals);
}

function card(hospital) {
  return `<article class="card" data-hospital-id="${hospital.id}"><div class="card-head"><h3>${escapeHtml(hospital.name)}</h3><span class="distance">${hospital.distance.toFixed(1)}km</span></div><p class="address">${escapeHtml(hospital.address)}</p><div class="actions"><a class="call" href="tel:${hospital.phoneNumber}">전화하기</a><button class="route" data-route-id="${hospital.id}">경로 보기</button></div></article>`;
}

function escapeHtml(value) {
  const element = document.createElement("div"); element.textContent = value ?? ""; return element.innerHTML;
}

function initializeMap() {
  if (state.map) return updateMapCenter();
  if (window.L) return initializeFallbackMap();
  if (window.naver?.maps) return initializeNaverMap();
  dataState.textContent = "지도를 불러오지 못했어요";
}

function initializeNaverMap() {
  const center = new naver.maps.LatLng(state.location.latitude, state.location.longitude);
  state.mapProvider = "naver";
  state.map = new naver.maps.Map("map", { center, zoom: 13, zoomControl: false });
  state.userMarker = new naver.maps.Marker({ position: center, map: state.map, title: "현재 위치",
    icon: { content: '<div class="current-marker"><span></span></div>', anchor: new naver.maps.Point(13, 13) } });
  naver.maps.Event.addListener(state.map, "click", event => selectLocation(event.coord));
  hideMapFallback();
  setTimeout(() => naver.maps.Event.trigger(state.map, "resize"), 100);
}

function initializeFallbackMap() {
  if (!window.L) {
    dataState.textContent = "지도를 불러오지 못했어요";
    return;
  }
  const center = [state.location.latitude, state.location.longitude];
  state.mapProvider = "leaflet";
  state.map = L.map("map", { zoomControl: false }).setView(center, 13);
  L.tileLayer("/api/map-tile?z={z}&x={x}&y={y}", { maxZoom: 19, attribution: "&copy; OpenStreetMap" }).addTo(state.map);
  state.userMarker = L.circleMarker(center, { radius: 9, color: "#fff", weight: 3,
    fillColor: "#1769ff", fillOpacity: 1 }).addTo(state.map).bindTooltip("현재 위치");
  state.map.on("click", event => selectLocation(event.latlng));
  hideMapFallback();
}

function updateMapCenter() {
  if (state.mapProvider === "naver") {
    const center = new naver.maps.LatLng(state.location.latitude, state.location.longitude);
    state.map.setCenter(center); state.userMarker.setPosition(center); return;
  }
  const center = [state.location.latitude, state.location.longitude];
  state.map.setView(center, 13); state.userMarker.setLatLng(center);
}

function hideMapFallback() { document.querySelector("#mapFallback").style.display = "none"; }

function selectLocation(coordinate) {
  state.location = coordinateValue(coordinate);
  if (state.mapProvider === "naver") state.userMarker.setPosition(coordinate);
  else state.userMarker.setLatLng(coordinate);
  clearRoute();
  locationLabel.textContent = `선택한 위치 기준 ${state.radiusKilometers}km`;
  updateLocationName();
  loadHospitals();
}
function coordinateValue(coordinate) {
  const latitude = typeof coordinate.lat === "function" ? coordinate.lat() : coordinate.lat;
  const longitude = typeof coordinate.lng === "function" ? coordinate.lng() : coordinate.lng;
  return { latitude, longitude };
}

async function updateLocationName() {
  try {
    const query = new URLSearchParams({ longitude: state.location.longitude, latitude: state.location.latitude });
    const response = await fetch(`/api/reverse-geocode?${query}`);
    if (!response.ok) throw new Error("지역 조회 실패");
    locationLabel.textContent = `${(await response.json()).region} · ${state.radiusKilometers}km`;
  } catch (error) {
    locationLabel.textContent = `선택한 위치 기준 ${state.radiusKilometers}km`;
  }
}

function renderMarkers(hospitals) {
  if (!state.map) return;
  state.markers.forEach(marker => state.mapProvider === "naver" ? marker.setMap(null) : marker.remove());
  if (state.mapProvider === "naver") {
    state.markers = hospitals.map(hospital => {
      const marker = new naver.maps.Marker({ position: new naver.maps.LatLng(hospital.latitude, hospital.longitude), map: state.map,
        title: hospital.name, icon: { content: '<div class="hospital-marker"><b>+</b></div>', anchor: new naver.maps.Point(19, 42) } });
      naver.maps.Event.addListener(marker, "click", () => selectHospital(hospital));
      return marker;
    });
    return;
  }
  state.markers = hospitals.map(hospital => {
    const icon = L.divIcon({ className: "marker-shell", html: '<div class="hospital-marker"><b>+</b></div>', iconSize: [38, 46], iconAnchor: [19, 43] });
    return L.marker([hospital.latitude, hospital.longitude], { icon }).addTo(state.map)
      .bindTooltip(hospital.name, { direction: "top", offset: [0, -36] })
      .on("click", () => selectHospital(hospital));
  });
}

function selectHospital(hospital) {
  state.selectedHospital = hospital;
  const availability = hospital.availability;
  const detail = document.querySelector("#hospitalDetail");
  detail.innerHTML = `<button class="detail-close" type="button" aria-label="닫기">×</button>
    <div class="detail-head"><div><small>선택한 응급실</small><h2>${escapeHtml(hospital.name)}</h2></div><span>${distanceInKilometers(state.location, hospital).toFixed(1)}km</span></div>
    <p>${escapeHtml(hospital.address)}</p>
    <div class="bed-grid">${bedItem("응급실", availability?.emergencyRoom)}${bedItem("수술실", availability?.operatingRoom)}${bedItem("중환자실", availability?.intensiveCareUnit)}${bedItem("입원실", availability?.inpatientRoom)}</div>
    <small class="bed-updated">${availability ? `${formatUpdatedAt(availability.updatedAt)} 기준` : "실시간 병상 정보 확인 필요"}</small>
    <div class="detail-actions"><a href="tel:${hospital.phoneNumber}">전화하기</a><button type="button" data-detail-route>자동차 길찾기</button></div>`;
  detail.classList.add("visible");
  detail.querySelector(".detail-close").addEventListener("click", closeHospitalDetail);
  detail.querySelector("[data-detail-route]").addEventListener("click", () => showRoute(hospital));
}

function bedItem(label, count) {
  return `<div><span>${label}</span><strong class="${count === 0 ? "full" : ""}">${count === null || count === undefined ? "확인 필요" : `${count}개`}</strong></div>`;
}

function formatUpdatedAt(value) {
  if (!/^\d{14}$/.test(value ?? "")) return "최근 갱신";
  return `${value.slice(8, 10)}:${value.slice(10, 12)}`;
}

function closeHospitalDetail() {
  document.querySelector("#hospitalDetail").classList.remove("visible");
}

async function showRoute(hospital) {
  dataState.textContent = "자동차 경로 확인 중";
  const parameters = new URLSearchParams({
    start: `${state.location.longitude},${state.location.latitude}`,
    goal: `${hospital.longitude},${hospital.latitude}`
  });
  try {
    const response = await fetch(`/api/directions?${parameters}`);
    if (!response.ok) throw new Error("경로 조회 실패");
    const route = await response.json();
    drawRoute(route.path);
    showRouteGuide(hospital, route);
  } catch (error) {
    dataState.textContent = "자동차 경로를 불러오지 못했어요";
  }
}

function showRouteGuide(hospital, route) {
  const minutes = Math.max(1, Math.ceil(route.duration / 60000));
  document.querySelector("#routeDuration").textContent = `약 ${minutes}분`;
  document.querySelector("#routeDistance").textContent = `${(route.distance / 1000).toFixed(1)}km`;
  document.querySelector("#routeDestination").textContent = hospital.name;
  document.querySelector("#routeGuide").classList.add("visible");
  document.querySelector("#bottomSheet").classList.remove("expanded");
  closeHospitalDetail();
  dataState.textContent = `자동차 약 ${minutes}분`;
}

function drawRoute(path) {
  clearRoute();
  if (state.mapProvider === "leaflet") {
    const coordinates = path.map(([longitude, latitude]) => [latitude, longitude]);
    const outline = L.polyline(coordinates, { color: "#fff", weight: 11, opacity: 0.95 });
    const route = L.polyline(coordinates, { color: "#1677ff", weight: 7, opacity: 1 });
    state.route = L.layerGroup([outline, route]).addTo(state.map);
    state.map.fitBounds(route.getBounds(), { paddingTopLeft: [30, 145], paddingBottomRight: [30, 260] });
    return;
  }
  const coordinates = path.map(([longitude, latitude]) => new naver.maps.LatLng(latitude, longitude));
  state.route = new naver.maps.Polyline({ map: state.map, path: coordinates,
    strokeColor: "#ef3325", strokeWeight: 7, strokeOpacity: 0.9 });
  state.map.fitBounds(state.route.getBounds(), { top: 45, right: 35, bottom: 45, left: 35 });
}

function clearRoute() {
  if (state.route && state.mapProvider === "naver") state.route.setMap(null);
  if (state.route && state.mapProvider === "leaflet") state.route.remove();
  state.route = null;
}

function locate() {
  initializeMap();
  if (!navigator.geolocation) return useDefaultLocation("지도를 눌러 출발 위치를 선택하세요");
  locationLabel.textContent = "현재 위치를 확인하고 있어요";
  let completed = false;
  const timeout = setTimeout(() => {
    if (!completed) useDefaultLocation("지도를 눌러 출발 위치를 선택하세요");
    completed = true;
  }, 6000);
  navigator.geolocation.getCurrentPosition(position => {
    if (completed) return;
    completed = true;
    clearTimeout(timeout);
    state.location = { latitude: position.coords.latitude, longitude: position.coords.longitude };
    initializeMap(); updateLocationName(); loadHospitals();
  }, () => {
    if (completed) return;
    completed = true;
    clearTimeout(timeout);
    useDefaultLocation("지도를 눌러 출발 위치를 선택하세요");
  }, { enableHighAccuracy: true, timeout: 5000 });
}

function useDefaultLocation(message) {
  state.location = DEFAULT_LOCATION;
  locationLabel.textContent = message;
  initializeMap(); loadHospitals();
}

document.querySelector("#refreshButton").addEventListener("click", loadHospitals);
document.querySelector("#myLocationButton").addEventListener("click", locate);
document.querySelector("#sheetHandle").addEventListener("click", () => {
  const sheet = document.querySelector("#bottomSheet");
  sheet.classList.toggle("expanded");
  setTimeout(resizeMap, 300);
});

function resizeMap() {
  if (state.mapProvider === "naver") naver.maps.Event.trigger(state.map, "resize");
  if (state.mapProvider === "leaflet") state.map.invalidateSize();
}
document.querySelector("#radiusSelect").addEventListener("change", event => {
  state.radiusKilometers = Number(event.target.value);
  locationLabel.textContent = `선택한 위치 기준 ${state.radiusKilometers}km`;
  loadHospitals();
});
document.querySelector("#addressSearch").addEventListener("submit", searchAddress);
document.querySelector("#closeRoute").addEventListener("click", () => {
  clearRoute();
  document.querySelector("#routeGuide").classList.remove("visible");
  dataState.textContent = "방금 업데이트";
});
list.addEventListener("click", event => {
  const button = event.target.closest("[data-route-id]");
  if (!button) return;
  const hospital = state.hospitals.find(item => String(item.id) === button.dataset.routeId);
  if (hospital) showRoute(hospital);
});

async function searchAddress(event) {
  event.preventDefault();
  const input = document.querySelector("#addressInput");
  const query = input.value.trim();
  if (!query) return;
  dataState.textContent = "주소 검색 중";
  try {
    const response = await fetch(`/api/geocode?${new URLSearchParams({ query })}`);
    if (!response.ok) throw new Error("주소 검색 실패");
    const result = await response.json();
    state.location = { latitude: result.latitude, longitude: result.longitude };
    clearRoute(); closeHospitalDetail(); updateMapCenter();
    locationLabel.textContent = `${result.address} · ${state.radiusKilometers}km`;
    loadHospitals();
  } catch (error) {
    dataState.textContent = "주소를 찾지 못했어요";
  }
}
async function start() {
  locate();
}

start();
setInterval(loadHospitals, 5000);
