const DEFAULT_LOCATION = { latitude: 37.5665, longitude: 126.9780 };
const SAMPLE_HOSPITALS = [
  { id: 1, name: "서울대학교병원 응급의료센터", address: "서울 종로구 대학로 101", phoneNumber: "02-2072-2475", latitude: 37.5796, longitude: 126.9990 },
  { id: 2, name: "강북삼성병원 응급의료센터", address: "서울 종로구 새문안로 29", phoneNumber: "02-2001-1000", latitude: 37.5684, longitude: 126.9679 },
  { id: 3, name: "세브란스병원 응급진료센터", address: "서울 서대문구 연세로 50-1", phoneNumber: "02-2228-8888", latitude: 37.5623, longitude: 126.9408 }
];

const state = { location: DEFAULT_LOCATION, hospitals: [], map: null, markers: [], userMarker: null, route: null };
const list = document.querySelector("#hospitalList");
const dataState = document.querySelector("#dataState");
const locationLabel = document.querySelector("#locationLabel");
const resultCount = document.querySelector("#resultCount");

function distanceInKilometers(origin, destination) {
  const radius = 6371;
  const latitude = (destination.latitude - origin.latitude) * Math.PI / 180;
  const longitude = (destination.longitude - origin.longitude) * Math.PI / 180;
  const value = Math.sin(latitude / 2) ** 2 + Math.cos(origin.latitude * Math.PI / 180)
    * Math.cos(destination.latitude * Math.PI / 180) * Math.sin(longitude / 2) ** 2;
  return radius * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
}

async function loadHospitals() {
  dataState.textContent = "업데이트 중";
  try {
    if (!window.APP_CONFIG.apiBaseUrl) throw new Error("API 주소 없음");
    const response = await fetch(`${window.APP_CONFIG.apiBaseUrl}/emergencies/search`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...state.location, radiusKilometers: 10 })
    });
    if (!response.ok) throw new Error("API 연결 실패");
    const data = await response.json();
    state.hospitals = data.emergencies;
    dataState.textContent = "방금 업데이트";
  } catch (error) {
    state.hospitals = SAMPLE_HOSPITALS;
    dataState.textContent = "화면 확인용 데이터";
  }
  render();
}

function render() {
  const hospitals = state.hospitals.map(hospital => ({
    ...hospital, distance: distanceInKilometers(state.location, hospital)
  })).sort((first, second) => first.distance - second.distance);
  resultCount.textContent = hospitals.length;
  list.innerHTML = hospitals.length ? hospitals.map(card).join("") : '<p class="empty">반경 안에 응급실이 없습니다.</p>';
  renderMarkers(hospitals);
}

function card(hospital) {
  return `<article class="card"><div class="card-head"><h3>${escapeHtml(hospital.name)}</h3><span class="distance">${hospital.distance.toFixed(1)}km</span></div><p class="address">${escapeHtml(hospital.address)}</p><div class="actions"><a class="call" href="tel:${hospital.phoneNumber}">전화하기</a><button class="route" data-route-id="${hospital.id}">지도에서 길찾기</button></div></article>`;
}

function escapeHtml(value) {
  const element = document.createElement("div"); element.textContent = value ?? ""; return element.innerHTML;
}

function initializeMap() {
  if (!window.naver?.maps) return;
  const center = new naver.maps.LatLng(state.location.latitude, state.location.longitude);
  state.map = new naver.maps.Map("map", { center, zoom: 13, zoomControl: false });
  state.userMarker = new naver.maps.Marker({ position: center, map: state.map, title: "출발 위치" });
  naver.maps.Event.addListener(state.map, "click", event => selectLocation(event.coord));
}

function selectLocation(coordinate) {
  state.location = { latitude: coordinate.lat(), longitude: coordinate.lng() };
  state.userMarker.setPosition(coordinate);
  clearRoute();
  locationLabel.textContent = "선택한 위치 기준 10km";
  updateLocationName();
  loadHospitals();
}

function updateLocationName() {
  if (!window.naver?.maps?.Service) {
    locationLabel.textContent = "현재 위치 기준 10km";
    return;
  }
  const coordinate = new naver.maps.LatLng(state.location.latitude, state.location.longitude);
  naver.maps.Service.reverseGeocode({ coords: coordinate }, (status, response) => {
    if (status !== naver.maps.Service.Status.OK) {
      locationLabel.textContent = "현재 위치 기준 10km";
      return;
    }
    const region = response.v2.results[0]?.region;
    const names = [region?.area1?.name, region?.area2?.name, region?.area3?.name].filter(Boolean);
    locationLabel.textContent = names.length ? `${names.join(" ")} · 10km` : "현재 위치 기준 10km";
  });
}

function renderMarkers(hospitals) {
  if (!state.map || !window.naver?.maps) return;
  state.markers.forEach(marker => marker.setMap(null));
  state.markers = hospitals.map(hospital => new naver.maps.Marker({
    position: new naver.maps.LatLng(hospital.latitude, hospital.longitude), map: state.map,
    title: hospital.name
  }));
}

async function showRoute(hospital) {
  if (!state.map) return;
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
    dataState.textContent = `${Math.round(route.distance / 100) / 10}km · 약 ${Math.ceil(route.duration / 60000)}분`;
  } catch (error) {
    dataState.textContent = "자동차 경로를 불러오지 못했어요";
  }
}

function drawRoute(path) {
  clearRoute();
  const coordinates = path.map(([longitude, latitude]) => new naver.maps.LatLng(latitude, longitude));
  state.route = new naver.maps.Polyline({
    map: state.map, path: coordinates, strokeColor: "#ef3325", strokeWeight: 6, strokeOpacity: 0.9
  });
  state.map.fitBounds(state.route.getBounds(), { top: 45, right: 30, bottom: 45, left: 30 });
}

function clearRoute() {
  if (state.route) state.route.setMap(null);
  state.route = null;
}

function locate() {
  if (!navigator.geolocation) {
    return useDefaultLocation("위치 기능 미지원 · 서울시청 기준 10km");
  }
  locationLabel.textContent = "현재 위치를 확인하고 있어요";
  let completed = false;
  const timeout = setTimeout(() => {
    if (!completed) useDefaultLocation("위치 확인 지연 · 서울시청 기준 10km");
    completed = true;
  }, 6000);
  navigator.geolocation.getCurrentPosition(position => {
    if (completed) return;
    completed = true;
    clearTimeout(timeout);
    state.location = { latitude: position.coords.latitude, longitude: position.coords.longitude };
    locationLabel.textContent = "현재 위치 기준 10km";
    initializeMap();
    updateLocationName();
    loadHospitals();
  }, () => {
    if (completed) return;
    completed = true;
    clearTimeout(timeout);
    useDefaultLocation("위치 권한 없음 · 서울시청 기준 10km");
  }, { enableHighAccuracy: true, timeout: 5000 });
}

function useDefaultLocation(message) {
  state.location = DEFAULT_LOCATION;
  locationLabel.textContent = message;
  initializeMap();
  loadHospitals();
}

document.querySelector("#locateButton").addEventListener("click", locate);
document.querySelector("#refreshButton").addEventListener("click", loadHospitals);
list.addEventListener("click", event => {
  const button = event.target.closest("[data-route-id]");
  if (!button) return;
  const hospital = state.hospitals.find(item => String(item.id) === button.dataset.routeId);
  if (hospital) showRoute(hospital);
});
locate();
setInterval(loadHospitals, 5000);
