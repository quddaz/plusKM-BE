const DEFAULT_LOCATION = { latitude: 37.4003268, longitude: 127.1048549 };
const state = { location: DEFAULT_LOCATION, radiusKilometers: 10,
  radiusByMode: { EMERGENCY: 10, MEDICAL: 1 }, searchMode: "EMERGENCY", hospitals: [], map: null,
  mapProvider: null, markers: [], userMarker: null, radiusOverlay: null, route: null, selectedHospital: null,
  usingDefaultLocation: true, visibleHospitals: [], sameLocationPlaces: [] };
const list = document.querySelector("#hospitalList");
const dataState = document.querySelector("#dataState");
const locationLabel = document.querySelector("#locationLabel");
const resultCount = document.querySelector("#resultCount");
const resultCountSuffix = document.querySelector("#resultCountSuffix");
const resultType = document.querySelector("#resultType");
const resultNotice = document.querySelector("#resultNotice");
const mapLegend = document.querySelector(".map-legend");
const emergencyCall = document.querySelector("#emergencyCall");
const radiusSelect = document.querySelector("#radiusSelect");
const RADIUS_OPTIONS_BY_MODE = { EMERGENCY: [1, 5, 10, 20, 30], MEDICAL: [1, 3, 5, 10] };
const MEDICAL_CLUSTER_REVEAL_ZOOM = 16;
const MAP_RADIUS_VIEW_MARGIN = 1.03;
const NAVER_MAP_CLIENT_ID = "cjtt3s316g";
const MAP_LOAD_DELAYS = [0, 1500, 4000];
const API_BASE_URL = (
  window.PLUSKM_API_BASE_URL
  || localStorage.getItem("pluskmApiBaseUrl")
  || "https://api.pluskm.live"
).replace(/\/$/, "");
let accessToken = localStorage.getItem("pluskmAccessToken") || "";
let guardians = [];
let favorites = [];
let bedRefreshInFlight = false;

function acceptLoginToken() {
  const loginError = new URLSearchParams(location.search).get("loginError");
  if (loginError) {
    history.replaceState(null, "", location.pathname);
    setTimeout(() => showToast("Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."), 0);
  }
  const token = new URLSearchParams(location.hash.slice(1)).get("accessToken");
  if (!token) return;
  accessToken = token;
  localStorage.setItem("pluskmAccessToken", token);
  history.replaceState(null, "", `${location.pathname}${location.search}`);
}

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

function normalizeHospitalCoordinate(hospital) {
  const longitude = Number(hospital.longitude);
  const latitude = Number(hospital.latitude);
  // MySQL SRID 4326은 환경에 따라 X/Y를 위도/경도 축 순서로 반환할 수 있다.
  // 위도로 올 수 없는 값이 들어오면 사용자에게 전달하기 전에 정상 순서로 복구한다.
  if (Math.abs(latitude) > 90 && Math.abs(longitude) <= 90) {
    return { ...hospital, longitude: latitude, latitude: longitude };
  }
  return { ...hospital, longitude, latitude };
}

async function loadHospitals() {
  dataState.textContent = "응급실 확인 중";
  try {
    const response = await fetch(`${API_BASE_URL}/emergencies/search`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...state.location, radiusKilometers: state.radiusKilometers })
    });
    if (!response.ok) throw new Error("API 연결 실패");
    state.hospitals = (await response.json()).emergencies.map(hospital => ({
      ...normalizeHospitalCoordinate(hospital), availability: null
    }));
    dataState.textContent = "병상 정보 갱신 중";
    render();
    void refreshBeds();
  } catch (error) {
    state.hospitals = [];
    dataState.textContent = "응급실 정보를 불러오지 못했어요";
    render();
  }
}

async function loadPlaces() {
  const label = modeLabel();
  dataState.textContent = `${label} 확인 중`;
  try {
    const response = await fetch(`${API_BASE_URL}/places/search`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ...state.location,
        radiusKilometers: state.radiusKilometers,
        category: "ALL"
      })
    });
    if (!response.ok) throw new Error("API 연결 실패");
    state.hospitals = (await response.json()).places.map(place => ({
      ...normalizeHospitalCoordinate(place),
      distance: Number(place.distanceMeters) / 1000,
      availability: null
    }));
    dataState.textContent = "방금 업데이트";
    render();
  } catch (error) {
    state.hospitals = [];
    dataState.textContent = `${label} 정보를 불러오지 못했어요`;
    render();
  }
}

function loadNearby() {
  closeSameLocationModal();
  closeHospitalDetail();
  return state.searchMode === "EMERGENCY" ? loadHospitals() : loadPlaces();
}

function modeLabel() {
  return { EMERGENCY: "응급실", MEDICAL: "병원·약국" }[state.searchMode];
}

function radiusLabel() {
  return state.radiusKilometers < 1
    ? `${Math.round(state.radiusKilometers * 1000)}m`
    : `${state.radiusKilometers}km`;
}

function distanceLabel(distanceKilometers) {
  return distanceKilometers < 1
    ? `${Math.round(distanceKilometers * 1000)}m`
    : `${distanceKilometers.toFixed(1)}km`;
}

async function refreshBeds() {
  if (state.searchMode !== "EMERGENCY" || bedRefreshInFlight || !state.hospitals.length) return;
  bedRefreshInFlight = true;
  try {
    const response = await fetch(`${API_BASE_URL}/emergencies/beds`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hpids: state.hospitals.map(hospital => hospital.hpid) })
    });
    if (!response.ok) throw new Error("병상 API 연결 실패");
    const availabilityByHpid = new Map(
      (await response.json()).availabilities.map(availability => [availability.hpid, availability])
    );
    state.hospitals = state.hospitals.map(hospital => ({
      ...hospital,
      availability: availabilityByHpid.get(hospital.hpid) ?? hospital.availability
    }));
    dataState.textContent = "방금 업데이트";
    render();
  } catch (error) {
    dataState.textContent = "병상 정보 갱신 대기 중";
  } finally {
    bedRefreshInFlight = false;
  }
}

function render() {
  resultType.textContent = modeLabel();
  mapLegend.hidden = state.searchMode !== "EMERGENCY";
  emergencyCall.hidden = state.searchMode !== "EMERGENCY";
  const hospitals = state.hospitals.map(hospital => ({
    ...hospital, distance: distanceInKilometers(state.location, hospital)
  })).filter(hospital => hospital.distance <= state.radiusKilometers)
    .sort((first, second) => first.distance - second.distance);
  resultCount.textContent = hospitals.length;
  resultCount.hidden = false;
  resultCountSuffix.hidden = false;
  resultNotice.textContent = state.searchMode === "EMERGENCY"
    ? "병상 정보는 방문 전 전화로 확인해 주세요."
    : "운영 여부와 진료·조제 시간은 방문 전 전화로 확인해 주세요.";
  list.innerHTML = hospitals.length ? hospitals.map(card).join("")
    : `<p class="empty">반경 안의 ${modeLabel()} 정보를 확인할 수 없습니다.</p>`;
  state.visibleHospitals = hospitals;
  renderMarkers(hospitals);
}

function card(hospital) {
  return `<article class="card" data-hospital-id="${hospital.id}"><div class="card-head"><h3>${escapeHtml(hospital.name)}</h3><span class="distance">${distanceLabel(hospital.distance)}</span>${favoriteButton(hospital)}</div><p class="address">${escapeHtml(hospital.address)}</p><div class="actions"><a class="call" href="tel:${hospital.phoneNumber}">전화하기</a><button class="route" data-route-id="${hospital.id}">경로 보기</button></div></article>`;
}

function favoriteType(item) { return item.hpid ? "EMERGENCY" : "PLACE"; }
function isFavorite(type, id) { return favorites.some(item => item.type === type && String(item.targetId) === String(id)); }
function favoriteButton(item) {
  const type = favoriteType(item);
  const saved = isFavorite(type, item.id);
  const label = saved ? "즐겨찾기 해제" : "즐겨찾기 추가";
  return `<button class="favorite-toggle${saved ? " saved" : ""}" type="button" data-favorite-type="${type}" data-favorite-id="${item.id}" aria-label="${label}" aria-pressed="${saved}">${saved ? "★" : "☆"}</button>`;
}

function refreshFavoriteButtons() {
  document.querySelectorAll("[data-favorite-type][data-favorite-id]").forEach(button => {
    const saved = isFavorite(button.dataset.favoriteType, button.dataset.favoriteId);
    button.classList.toggle("saved", saved);
    button.textContent = saved ? "★" : "☆";
    button.setAttribute("aria-pressed", String(saved));
    button.setAttribute("aria-label", saved ? "즐겨찾기 해제" : "즐겨찾기 추가");
  });
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
  state.map = new naver.maps.Map("map", { center, zoom: 13, zoomControl: false,
    mapTypeId: naver.maps.MapTypeId.NORMAL });
  state.userMarker = new naver.maps.Marker({ position: center, map: state.map, title: "검색 기준 위치", zIndex: 1000,
    icon: { content: '<div class="current-marker"><span></span></div>', anchor: new naver.maps.Point(13, 13) } });
  naver.maps.Event.addListener(state.map, "click", event => selectLocation(event.coord));
  naver.maps.Event.addListener(state.map, "zoom_changed", () => renderMarkers());
  hideMapFallback();
  fitMapToRadius();
  setTimeout(() => naver.maps.Event.trigger(state.map, "resize"), 100);
}

function initializeFallbackMap() {
  if (!window.L) {
    dataState.textContent = "지도를 불러오지 못했어요";
    return;
  }
  const center = [state.location.latitude, state.location.longitude];
  state.mapProvider = "leaflet";
  state.map = L.map("map", { zoomControl: false, zoomSnap: 0.01 }).setView(center, 13);
  state.map.createPane("currentLocationPane").style.zIndex = "625";
  L.tileLayer("/api/map-tile?z={z}&x={x}&y={y}", { maxZoom: 19, attribution: "&copy; OpenStreetMap" }).addTo(state.map);
  state.userMarker = L.circleMarker(center, { radius: 9, color: "#fff", weight: 3,
    fillColor: "#1769ff", fillOpacity: 1, pane: "currentLocationPane" }).addTo(state.map).bindTooltip("검색 기준 위치");
  state.map.on("click", event => {
    if (event.originalEvent?.target?.closest?.(".leaflet-marker-icon")) return;
    selectLocation(event.latlng);
  });
  state.map.on("zoomend", () => renderMarkers());
  hideMapFallback();
  fitMapToRadius();
}

function updateMapCenter() {
  if (state.mapProvider === "naver") {
    const center = new naver.maps.LatLng(state.location.latitude, state.location.longitude);
    state.map.setCenter(center); state.userMarker.setPosition(center);
    fitMapToRadius(); return;
  }
  const center = [state.location.latitude, state.location.longitude];
  state.map.setView(center, 13); state.userMarker.setLatLng(center);
  fitMapToRadius();
}

function fitMapToRadius() {
  if (!state.map) return;
  updateSearchRadiusOverlay();
  if (state.route || document.querySelector(".screen").classList.contains("routing")) return;
  const radius = state.radiusKilometers;
  const mapRect = document.querySelector("#map").getBoundingClientRect();
  const controlsBottom = document.querySelector(".map-controls").getBoundingClientRect().bottom;
  const topObstruction = state.searchMode === "EMERGENCY"
    ? Math.max(controlsBottom + 35, mapLegend.getBoundingClientRect().bottom)
    : controlsBottom;
  const topPadding = Math.ceil(topObstruction - mapRect.top + 10);
  const bottomPadding = Math.ceil(mapRect.bottom - document.querySelector("#bottomSheet").getBoundingClientRect().top + 10);
  const sidePadding = 18;
  if (state.mapProvider === "leaflet") {
    const bounds = state.radiusOverlay.getBounds();
    const northWest = state.map.project(bounds.getNorthWest(), 0);
    const southEast = state.map.project(bounds.getSouthEast(), 0);
    const availableWidth = Math.max(80, mapRect.width - sidePadding * 2);
    const availableHeight = Math.max(80, mapRect.height - topPadding - bottomPadding);
    const zoom = Math.log2(Math.min(
      availableWidth / (southEast.x - northWest.x),
      availableHeight / (southEast.y - northWest.y)
    ) / MAP_RADIUS_VIEW_MARGIN);
    const visibleCenter = L.point(sidePadding + availableWidth / 2, topPadding + availableHeight / 2);
    const mapCenter = L.point(mapRect.width / 2, mapRect.height / 2);
    const projectedCenter = state.map.project([state.location.latitude, state.location.longitude], zoom);
    state.map.setView(state.map.unproject(projectedCenter.subtract(visibleCenter.subtract(mapCenter)), zoom),
      zoom, { animate: false });
    return;
  }
  const latitudeDelta = radius * MAP_RADIUS_VIEW_MARGIN / 111;
  const longitudeDelta = radius * MAP_RADIUS_VIEW_MARGIN / (111 * Math.max(Math.cos(state.location.latitude * Math.PI / 180), 0.2));
  const southWest = new naver.maps.LatLng(
    state.location.latitude - latitudeDelta,
    state.location.longitude - longitudeDelta
  );
  const northEast = new naver.maps.LatLng(
    state.location.latitude + latitudeDelta,
    state.location.longitude + longitudeDelta
  );
  state.map.fitBounds(new naver.maps.LatLngBounds(southWest, northEast),
    { top: topPadding, right: sidePadding, bottom: bottomPadding, left: sidePadding });
}

function updateSearchRadiusOverlay() {
  const radiusMeters = state.radiusKilometers * 1000;
  if (state.mapProvider === "leaflet") {
    const center = [state.location.latitude, state.location.longitude];
    if (!state.radiusOverlay) {
      state.radiusOverlay = L.circle(center, { radius: radiusMeters, color: "#1769ff", weight: 2,
        opacity: 0.8, fillColor: "#1769ff", fillOpacity: 0.06, interactive: false }).addTo(state.map);
    } else {
      state.radiusOverlay.setLatLng(center).setRadius(radiusMeters);
    }
    return;
  }
  const center = new naver.maps.LatLng(state.location.latitude, state.location.longitude);
  if (!state.radiusOverlay) {
    state.radiusOverlay = new naver.maps.Circle({ map: state.map, center, radius: radiusMeters,
      strokeColor: "#1769ff", strokeWeight: 2, strokeOpacity: 0.8,
      fillColor: "#1769ff", fillOpacity: 0.06, clickable: false, zIndex: 1 });
  } else {
    state.radiusOverlay.setCenter(center);
    state.radiusOverlay.setRadius(radiusMeters);
  }
}

function hideMapFallback() { document.querySelector("#mapFallback").style.display = "none"; }

function selectLocation(coordinate) {
  state.location = coordinateValue(coordinate);
  state.usingDefaultLocation = false;
  if (state.mapProvider === "naver") {
    state.userMarker.setPosition(coordinate);
  } else {
    state.userMarker.setLatLng(coordinate);
  }
  if (document.querySelector(".screen").classList.contains("routing")) stopRouteGuide();
  else clearRoute();
  fitMapToRadius();
  locationLabel.textContent = `선택한 위치 기준 ${radiusLabel()}`;
  updateLocationName();
  loadNearby();
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
    locationLabel.textContent = `${(await response.json()).region} · ${radiusLabel()}`;
  } catch (error) {
    locationLabel.textContent = `선택한 위치 기준 ${radiusLabel()}`;
  }
}

function renderMarkers(hospitals = state.visibleHospitals) {
  if (!state.map) return;
  state.markers.forEach(marker => state.mapProvider === "naver" ? marker.setMap(null) : marker.remove());
  const clusters = markerClusters(hospitals);
  if (state.mapProvider === "naver") {
    state.markers = clusters.map(cluster => {
      const place = cluster.places[0];
      const marker = new naver.maps.Marker({ position: new naver.maps.LatLng(cluster.latitude, cluster.longitude), map: state.map,
        title: cluster.places.length === 1 ? place.name : clusterTooltip(cluster),
        icon: { content: markerContent(cluster, place), anchor: state.searchMode === "MEDICAL" ? new naver.maps.Point(13, 31) : new naver.maps.Point(19, 42) } });
      naver.maps.Event.addListener(marker, "click", () => cluster.places.length === 1 ? selectHospital(place) : openCluster(cluster));
      return marker;
    });
    return;
  }
  state.markers = clusters.map(cluster => {
    const place = cluster.places[0];
    const icon = L.divIcon({ className: "marker-shell", html: markerContent(cluster, place), iconSize: state.searchMode === "MEDICAL" ? [26, 34] : [38, 46], iconAnchor: state.searchMode === "MEDICAL" ? [13, 31] : [19, 43] });
    return L.marker([cluster.latitude, cluster.longitude], { icon, bubblingMouseEvents: false }).addTo(state.map)
      .bindTooltip(cluster.places.length === 1 ? place.name : clusterTooltip(cluster), { direction: "top", offset: [0, -28] })
      .on("click", event => {
        if (event.originalEvent) L.DomEvent.stopPropagation(event.originalEvent);
        cluster.places.length === 1 ? selectHospital(place) : openCluster(cluster);
      });
  });
}

function markerClusters(hospitals) {
  if (state.searchMode === "EMERGENCY") return hospitals.map(place => ({ places: [place], latitude: place.latitude, longitude: place.longitude }));
  const zoom = state.map.getZoom();
  if (zoom >= MEDICAL_CLUSTER_REVEAL_ZOOM) {
    const sameCoordinates = new Map();
    hospitals.forEach(place => {
      const key = `${place.latitude.toFixed(6)},${place.longitude.toFixed(6)}`;
      if (!sameCoordinates.has(key)) sameCoordinates.set(key, { places: [], latitude: place.latitude, longitude: place.longitude, key });
      sameCoordinates.get(key).places.push(place);
    });
    return [...sameCoordinates.values()];
  }
  const clusters = [];
  hospitals.forEach(place => {
    const point = markerPixel(place, zoom);
    let cluster = clusters.find(item => Math.hypot(item.x - point.x, item.y - point.y) < 38);
    if (!cluster) {
      cluster = { places: [], x: point.x, y: point.y, latitude: place.latitude, longitude: place.longitude };
      clusters.push(cluster);
    }
    const count = cluster.places.length;
    cluster.x = (cluster.x * count + point.x) / (count + 1);
    cluster.y = (cluster.y * count + point.y) / (count + 1);
    cluster.latitude = (cluster.latitude * count + place.latitude) / (count + 1);
    cluster.longitude = (cluster.longitude * count + place.longitude) / (count + 1);
    cluster.places.push(place);
  });
  return clusters;
}

function markerPixel(place, zoom) {
  const latitude = Math.max(-85, Math.min(85, place.latitude)) * Math.PI / 180;
  const size = 256 * 2 ** zoom;
  return { x: (place.longitude + 180) / 360 * size,
    y: (1 - Math.log(Math.tan(latitude) + 1 / Math.cos(latitude)) / Math.PI) / 2 * size };
}

function markerContent(cluster, place) {
  const multiple = cluster.places.length > 1;
  const status = !multiple && needsVerification(place) ? " needs-verification" : "";
  const mode = multiple ? "cluster" : markerModeClass(place);
  return `<div class="hospital-marker ${mode}${status}"><b>${multiple ? cluster.places.length : markerText(place)}</b></div>`;
}

function clusterTooltip(cluster) {
  return state.map.getZoom() >= MEDICAL_CLUSTER_REVEAL_ZOOM
    ? `같은 위치 ${cluster.places.length}곳 · 눌러서 목록 보기`
    : `${cluster.places.length}곳 · 눌러서 확대`;
}

function openCluster(cluster) {
  if (state.map.getZoom() >= MEDICAL_CLUSTER_REVEAL_ZOOM) {
    openSameLocationModal(cluster.places);
    return;
  }
  const center = state.mapProvider === "naver"
    ? new naver.maps.LatLng(cluster.latitude, cluster.longitude)
    : [cluster.latitude, cluster.longitude];
  const zoom = MEDICAL_CLUSTER_REVEAL_ZOOM;
  if (state.mapProvider === "naver") {
    state.map.setCenter(center);
    state.map.setZoom(zoom);
  } else {
    state.map.setView(center, zoom);
  }
}

function openSameLocationModal(places) {
  closeHospitalDetail();
  state.sameLocationPlaces = places;
  document.querySelector("#sameLocationTitle").textContent = `이 위치의 병원·약국 ${places.length}곳`;
  document.querySelector("#sameLocationAddress").textContent = places[0].address || "같은 위치에 등록된 기관을 선택해 주세요.";
  document.querySelector("#sameLocationList").innerHTML = places.map((place, index) => `
    <button class="same-location-item" type="button" data-same-location-index="${index}">
      <span class="same-location-kind ${place.category === "PHARMACY" ? "pharmacy" : "hospital"}">${place.category === "PHARMACY" ? "약국" : "병원"}</span>
      <span class="same-location-info"><strong>${escapeHtml(place.name)}</strong><small>${escapeHtml(place.facilityType || place.address || "기관 정보 보기")}</small></span>
      <span class="same-location-arrow" aria-hidden="true">›</span>
    </button>`).join("");
  const modal = document.querySelector("#sameLocationModal");
  modal.classList.add("visible");
  modal.setAttribute("aria-hidden", "false");
  document.querySelector("#sameLocationClose").focus();
}

function closeSameLocationModal() {
  const modal = document.querySelector("#sameLocationModal");
  modal.classList.remove("visible");
  modal.setAttribute("aria-hidden", "true");
  state.sameLocationPlaces = [];
}

function markerModeClass(place) {
  if (state.searchMode === "EMERGENCY") return "emergency";
  return place.category === "PHARMACY" ? "pharmacy" : "hospital";
}

function markerText(place) {
  if (state.searchMode === "EMERGENCY") return "+";
  return place.category === "PHARMACY" ? "약" : "병";
}

function needsVerification(hospital) {
  return state.searchMode === "EMERGENCY"
    && (!hospital.availability || hospital.availability.emergencyRoom === null);
}

function selectHospital(hospital) {
  state.selectedHospital = hospital;
  if (!hospital.hpid) return selectPlace(hospital);
  const availability = hospital.availability;
  const detail = document.querySelector("#hospitalDetail");
  detail.classList.remove("route-mode");
  detail.innerHTML = `<button class="detail-close" type="button" aria-label="닫기">×</button>
    <div class="detail-head"><div><small>선택한 응급실</small><h2>${escapeHtml(hospital.name)}</h2></div><span>${distanceInKilometers(state.location, hospital).toFixed(1)}km</span>${favoriteButton(hospital)}</div>
    <p>${escapeHtml(hospital.address)}</p>
    <div class="bed-grid">${bedItem("응급실", availability?.emergencyRoom)}${bedItem("수술실", availability?.operatingRoom)}${bedItem("중환자실", availability?.intensiveCareUnit)}${bedItem("입원실", availability?.inpatientRoom)}</div>
    <small class="bed-updated">${availability ? `${formatUpdatedAt(availability.updatedAt)} 기준` : "실시간 병상 정보 확인 필요"}</small>
    <div class="detail-actions with-guardian"><a href="tel:${hospital.phoneNumber}">전화하기</a><button type="button" data-detail-route>길찾기</button><button class="guardian-action" type="button" data-guardian-message>보호자 문자</button></div>`;
  detail.classList.add("visible");
  detail.querySelector(".detail-close").addEventListener("click", closeHospitalDetail);
  detail.querySelector("[data-detail-route]").addEventListener("click", () => showRoute(hospital));
  detail.querySelector("[data-guardian-message]").addEventListener("click", () => prepareGuardianMessage(hospital));
}

function selectPlace(place) {
  const detail = document.querySelector("#hospitalDetail");
  detail.classList.remove("route-mode");
  detail.innerHTML = `<button class="detail-close" type="button" aria-label="닫기">×</button>
    <div class="detail-head"><div><small>선택한 ${place.category === "PHARMACY" ? "약국" : "병원"}</small><h2>${escapeHtml(place.name)}</h2></div><span>${distanceLabel(distanceInKilometers(state.location, place))}</span>${favoriteButton(place)}</div>
    <p>${escapeHtml(place.address)}</p>
    <div class="place-information"><span>시설 구분</span><strong>${escapeHtml(place.facilityType || modeLabel())}</strong></div>
    <div class="detail-actions"><a href="tel:${place.phoneNumber}">전화하기</a><button type="button" data-detail-route>길찾기</button></div>`;
  detail.classList.add("visible");
  detail.querySelector(".detail-close").addEventListener("click", closeHospitalDetail);
  detail.querySelector("[data-detail-route]").addEventListener("click", () => showRoute(place));
}

function bedItem(label, count) {
  return `<div><span>${label}</span><strong class="${count === 0 ? "full" : ""}">${count === null || count === undefined ? "확인 필요" : `${count}개`}</strong></div>`;
}

function formatUpdatedAt(value) {
  if (!/^\d{14}$/.test(value ?? "")) return "최근 갱신";
  return `${value.slice(8, 10)}:${value.slice(10, 12)}`;
}

function closeHospitalDetail() {
  document.querySelector("#hospitalDetail").classList.remove("visible", "route-mode");
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
  document.querySelector(".screen").classList.add("routing");
  showRouteHospitalCard(hospital, route, minutes);
  dataState.textContent = `자동차 약 ${minutes}분`;
}

function showRouteHospitalCard(hospital, route, minutes) {
  state.selectedHospital = hospital;
  if (!hospital.hpid) return showRoutePlaceCard(hospital, route, minutes);
  const availability = hospital.availability;
  const detail = document.querySelector("#hospitalDetail");
  detail.innerHTML = `<div class="route-hospital-label"><span>이동 중인 응급실</span><strong>약 ${minutes}분 · ${(route.distance / 1000).toFixed(1)}km</strong></div>
    <div class="detail-head"><div><h2>${escapeHtml(hospital.name)}</h2></div>${favoriteButton(hospital)}</div>
    <p>${escapeHtml(hospital.address)}</p>
    <div class="bed-grid">${bedItem("응급실", availability?.emergencyRoom)}${bedItem("수술실", availability?.operatingRoom)}${bedItem("중환자실", availability?.intensiveCareUnit)}${bedItem("입원실", availability?.inpatientRoom)}</div>
    <div class="route-hospital-actions"><a href="tel:${hospital.phoneNumber}">전화하기</a><button class="guardian-action" type="button" data-route-guardian>보호자 문자</button><button class="route-stop" type="button" data-route-stop>길찾기 종료</button></div>`;
  detail.classList.add("visible", "route-mode");
  detail.querySelector("[data-route-guardian]").addEventListener("click", () => prepareGuardianMessage(hospital));
  detail.querySelector("[data-route-stop]").addEventListener("click", stopRouteGuide);
}

function showRoutePlaceCard(place, route, minutes) {
  const detail = document.querySelector("#hospitalDetail");
  detail.innerHTML = `<div class="route-hospital-label"><span>이동 중인 ${place.category === "PHARMACY" ? "약국" : "병원"}</span><strong>약 ${minutes}분 · ${(route.distance / 1000).toFixed(1)}km</strong></div>
    <div class="detail-head"><div><h2>${escapeHtml(place.name)}</h2></div>${favoriteButton(place)}</div>
    <p>${escapeHtml(place.address)}</p>
    <div class="place-information"><span>시설 구분</span><strong>${escapeHtml(place.facilityType || modeLabel())}</strong></div>
    <div class="route-hospital-actions place-route-actions"><a href="tel:${place.phoneNumber}">전화하기</a><button class="route-stop" type="button" data-route-stop>길찾기 종료</button></div>`;
  detail.classList.add("visible", "route-mode");
  detail.querySelector("[data-route-stop]").addEventListener("click", stopRouteGuide);
}

function stopRouteGuide() {
  clearRoute();
  document.querySelector("#routeGuide").classList.remove("visible");
  const detail = document.querySelector("#hospitalDetail");
  detail.classList.remove("visible", "route-mode");
  document.querySelector(".screen").classList.remove("routing");
  dataState.textContent = "방금 업데이트";
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
  if (!navigator.geolocation) return showToast("이 브라우저에서는 위치를 확인할 수 없어요.");
  const previousLabel = locationLabel.textContent;
  const previousDataState = dataState.textContent;
  locationLabel.textContent = "현재 위치를 확인하고 있어요";
  dataState.textContent = "위치 확인 중";
  let completed = false;
  const timeout = setTimeout(() => {
    if (!completed) locationUnavailable("위치 확인 시간이 지났어요. 지도나 주소로 찾아주세요.", previousLabel, previousDataState);
    completed = true;
  }, 6000);
  navigator.geolocation.getCurrentPosition(position => {
    if (completed) return;
    completed = true;
    clearTimeout(timeout);
    state.location = { latitude: position.coords.latitude, longitude: position.coords.longitude };
    state.usingDefaultLocation = false;
    initializeMap(); updateLocationName(); loadNearby();
  }, () => {
    if (completed) return;
    completed = true;
    clearTimeout(timeout);
    locationUnavailable("위치를 사용할 수 없어요. 지도나 주소로 찾아주세요.", previousLabel, previousDataState);
  }, { enableHighAccuracy: true, timeout: 5000 });
}

function locationUnavailable(message, previousLabel, previousDataState) {
  locationLabel.textContent = previousLabel;
  dataState.textContent = previousDataState;
  showToast(message);
}

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
radiusSelect.addEventListener("change", event => {
  const selectedRadius = Number(event.target.value);
  if (RADIUS_OPTIONS_BY_MODE[state.searchMode].includes(selectedRadius)) {
    state.radiusKilometers = selectedRadius;
    state.radiusByMode[state.searchMode] = selectedRadius;
  }
  radiusSelect.value = String(state.radiusKilometers);
  locationLabel.textContent = state.usingDefaultLocation
    ? `판교테크노밸리 기준 · ${radiusLabel()}`
    : `선택한 위치 기준 ${radiusLabel()}`;
  fitMapToRadius();
  loadNearby();
});
function updateRadiusOptions() {
  radiusSelect.querySelectorAll("option").forEach(option => {
    const unavailable = !RADIUS_OPTIONS_BY_MODE[state.searchMode].includes(Number(option.value));
    option.hidden = unavailable;
    option.disabled = unavailable;
  });
  radiusSelect.value = String(state.radiusKilometers);
}
function switchSearchMode(searchMode) {
  const sheet = document.querySelector("#bottomSheet");
  const wasExpanded = sheet.classList.contains("expanded");
  sheet.classList.remove("expanded");
  if (wasExpanded) setTimeout(resizeMap, 300);
  if (state.searchMode === searchMode) return;
  state.searchMode = searchMode;
  state.radiusKilometers = state.radiusByMode[searchMode];
  updateRadiusOptions();
  locationLabel.textContent = state.usingDefaultLocation
    ? `판교테크노밸리 기준 · ${radiusLabel()}`
    : `선택한 위치 기준 ${radiusLabel()}`;
  state.hospitals = [];
  stopRouteGuide();
  fitMapToRadius();
  closeGuardianModal();
  closeSameLocationModal();
  document.querySelectorAll("[data-search-mode]").forEach(button => {
    const active = button.dataset.searchMode === searchMode;
    button.classList.toggle("active", active);
    button.toggleAttribute("aria-current", active);
  });
  render();
  loadNearby();
}
document.querySelector("#addressSearch").addEventListener("submit", searchAddress);
document.querySelector("#closeRoute").addEventListener("click", () => {
  stopRouteGuide();
});
list.addEventListener("click", event => {
  if (event.target.closest("[data-favorite-type]")) return;
  const button = event.target.closest("[data-route-id]");
  if (!button) return;
  const hospital = state.hospitals.find(item => String(item.id) === button.dataset.routeId);
  if (hospital) showRoute(hospital);
});
document.querySelector("#hospitalDetail").addEventListener("click", event => {
  const button = event.target.closest("[data-favorite-type]");
  if (button) void toggleFavorite(button);
});
list.addEventListener("click", event => {
  const button = event.target.closest("[data-favorite-type]");
  if (button) void toggleFavorite(button);
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
    state.usingDefaultLocation = false;
    if (document.querySelector(".screen").classList.contains("routing")) stopRouteGuide();
    else clearRoute();
    closeHospitalDetail(); updateMapCenter();
    locationLabel.textContent = `${result.address} · ${radiusLabel()}`;
    loadNearby();
  } catch (error) {
    dataState.textContent = "주소를 찾지 못했어요";
  }
}
async function start() {
  acceptLoginToken();
  updateRadiusOptions();
  configureGuardianUi();
  configureFavoritesUi();
  configureSameLocationUi();
  if (accessToken) void loadFavorites();
  initializeMap();
  void loadNearby();
}

function configureSameLocationUi() {
  document.querySelector("#sameLocationClose").addEventListener("click", closeSameLocationModal);
  document.querySelector("#sameLocationModal").addEventListener("click", event => {
    if (event.target.id === "sameLocationModal") closeSameLocationModal();
  });
  document.querySelector("#sameLocationList").addEventListener("click", event => {
    const item = event.target.closest("[data-same-location-index]");
    if (!item) return;
    const place = state.sameLocationPlaces[Number(item.dataset.sameLocationIndex)];
    if (!place) return;
    closeSameLocationModal();
    selectHospital(place);
  });
  document.addEventListener("keydown", event => {
    if (event.key === "Escape" && document.querySelector("#sameLocationModal").classList.contains("visible")) closeSameLocationModal();
  });
}

function configureGuardianUi() {
  document.querySelector("#loginButton").href = `${API_BASE_URL}/oauth2/authorization/google`;
  document.querySelector("#guardianOpen").addEventListener("click", openGuardianModal);
  document.querySelectorAll("[data-search-mode]").forEach(button => {
    button.addEventListener("click", () => switchSearchMode(button.dataset.searchMode));
  });
  document.querySelector("#guardianClose").addEventListener("click", closeGuardianModal);
  document.querySelector("#guardianModal").addEventListener("click", event => {
    if (event.target.id === "guardianModal") closeGuardianModal();
  });
  document.querySelector("#guardianForm").addEventListener("submit", createGuardian);
  document.querySelector("#guardianList").addEventListener("click", deleteGuardian);
}

function configureFavoritesUi() {
  document.querySelector("#favoritesLoginButton").href = `${API_BASE_URL}/oauth2/authorization/google`;
  document.querySelector("#favoritesOpen").addEventListener("click", openFavoritesModal);
  document.querySelector("#favoritesClose").addEventListener("click", closeFavoritesModal);
  document.querySelector("#favoritesModal").addEventListener("click", event => {
    if (event.target.id === "favoritesModal") closeFavoritesModal();
  });
  document.querySelector("#favoritesList").addEventListener("click", async event => {
    const removeButton = event.target.closest("[data-remove-favorite]");
    if (removeButton) {
      await removeFavorite(removeButton.dataset.removeFavorite);
      return;
    }
    const routeButton = event.target.closest("[data-route-favorite]");
    if (!routeButton) return;
    const favorite = favorites.find(item => String(item.id) === routeButton.dataset.routeFavorite);
    if (!favorite || !favorite.active) return;
    closeFavoritesModal();
    await showRoute({ ...favorite, id: favorite.targetId });
  });
}

async function openFavoritesModal() {
  closeGuardianModal();
  const modal = document.querySelector("#favoritesModal");
  modal.classList.add("visible");
  modal.setAttribute("aria-hidden", "false");
  document.querySelector("#favoritesAuth").hidden = Boolean(accessToken);
  document.querySelector("#favoritesList").hidden = !accessToken;
  if (accessToken) await loadFavorites();
}

function closeFavoritesModal() {
  const modal = document.querySelector("#favoritesModal");
  modal.classList.remove("visible");
  modal.setAttribute("aria-hidden", "true");
}

async function loadFavorites() {
  try {
    favorites = (await authenticatedRequest("/favorites")).map(normalizeHospitalCoordinate);
    renderFavorites();
    refreshFavoriteButtons();
  } catch (error) {
    favorites = [];
    renderFavorites();
    refreshFavoriteButtons();
    document.querySelector("#favoritesAuth").hidden = Boolean(accessToken);
    document.querySelector("#favoritesList").hidden = !accessToken;
    showToast(error.message);
  }
}

function renderFavorites() {
  const container = document.querySelector("#favoritesList");
  container.innerHTML = favorites.length ? favorites.map(item => {
    const label = item.type === "EMERGENCY" ? "응급실" : item.category === "PHARMACY" ? "약국" : "병원";
    const phone = String(item.phoneNumber || "").replace(/[^+\d]/g, "");
    return `<article class="favorite-item">
      <div class="favorite-item-head"><span>${label}${item.active ? "" : " · 현재 검색 대상 아님"}</span><button type="button" data-remove-favorite="${item.id}" aria-label="${escapeHtml(item.name)} 즐겨찾기 해제">★</button></div>
      <strong>${escapeHtml(item.name)}</strong><p>${escapeHtml(item.address)}</p>
      <div class="favorite-item-actions">${item.active && phone ? `<a href="tel:${phone}">전화하기</a>` : ""}<button type="button" data-route-favorite="${item.id}" ${item.active ? "" : "disabled"}>길찾기</button></div>
    </article>`;
  }).join("") : '<p class="empty">저장한 응급실·병원·약국이 없습니다.</p>';
}

async function toggleFavorite(button) {
  if (!accessToken) {
    await openFavoritesModal();
    return showToast("즐겨찾기를 저장하려면 로그인해 주세요.");
  }
  const type = button.dataset.favoriteType;
  const id = button.dataset.favoriteId;
  const path = type === "EMERGENCY" ? `/favorites/emergencies/${id}` : `/favorites/places/${id}`;
  button.disabled = true;
  try {
    if (isFavorite(type, id)) {
      await authenticatedRequest(path, { method: "DELETE" });
      favorites = favorites.filter(item => !(item.type === type && String(item.targetId) === id));
      showToast("즐겨찾기를 해제했어요.");
    } else {
      const saved = await authenticatedRequest(path, { method: "PUT" });
      favorites = [normalizeHospitalCoordinate(saved), ...favorites.filter(item => item.id !== saved.id)];
      showToast("즐겨찾기에 저장했어요.");
    }
    renderFavorites();
    refreshFavoriteButtons();
  } catch (error) {
    showToast(error.message);
  } finally {
    button.disabled = false;
  }
}

async function removeFavorite(favoriteId) {
  const favorite = favorites.find(item => String(item.id) === favoriteId);
  if (!favorite) return;
  const path = favorite.type === "EMERGENCY"
    ? `/favorites/emergencies/${favorite.targetId}`
    : `/favorites/places/${favorite.targetId}`;
  try {
    await authenticatedRequest(path, { method: "DELETE" });
    favorites = favorites.filter(item => String(item.id) !== favoriteId);
    renderFavorites();
    refreshFavoriteButtons();
    showToast("즐겨찾기를 해제했어요.");
  } catch (error) { showToast(error.message); }
}

async function openGuardianModal() {
  closeFavoritesModal();
  document.querySelector("#guardianModal").classList.add("visible");
  document.querySelector("#guardianModal").setAttribute("aria-hidden", "false");
  document.querySelector("#guardianAuth").hidden = Boolean(accessToken);
  document.querySelector("#guardianForm").hidden = !accessToken;
  if (accessToken) await loadGuardians();
}

function closeGuardianModal() {
  document.querySelector("#guardianModal").classList.remove("visible");
  document.querySelector("#guardianModal").setAttribute("aria-hidden", "true");
}

async function authenticatedRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${accessToken}`, ...(options.headers || {}) }
  });
  if (response.status === 401 || response.status === 403) {
    accessToken = "";
    localStorage.removeItem("pluskmAccessToken");
    throw new Error("로그인이 만료됐어요. 다시 로그인해 주세요.");
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || "요청을 처리하지 못했어요.");
  }
  return response.status === 204 ? null : response.json();
}

async function loadGuardians() {
  try {
    guardians = await authenticatedRequest("/guardians");
    renderGuardians();
  } catch (error) {
    showToast(error.message);
  }
}

function renderGuardians() {
  const container = document.querySelector("#guardianList");
  container.innerHTML = guardians.length ? guardians.map(guardian => `<article class="guardian-item">
    <div><strong>${escapeHtml(guardian.name)} · ${escapeHtml(guardian.relationship)}</strong><span>${escapeHtml(guardian.maskedPhoneNumber)}</span></div>
    <button type="button" data-delete-guardian="${guardian.id}">삭제</button>
  </article>`).join("") : '<p class="empty">등록된 보호자가 없습니다.</p>';
}

async function createGuardian(event) {
  event.preventDefault();
  const phoneNumber = document.querySelector("#guardianPhone").value.trim();
  if (!/^01[016789]-?\d{3,4}-?\d{4}$/.test(phoneNumber)) return showToast("휴대전화 번호를 확인해 주세요.");
  try {
    await authenticatedRequest("/guardians", { method: "POST", body: JSON.stringify({
      name: document.querySelector("#guardianName").value.trim(),
      relationship: document.querySelector("#guardianRelationship").value.trim(), phoneNumber
    }) });
    event.target.reset();
    showToast("보호자를 등록했어요.");
    await loadGuardians();
  } catch (error) { showToast(error.message); }
}

async function deleteGuardian(event) {
  const button = event.target.closest("[data-delete-guardian]");
  if (!button) return;
  try {
    await authenticatedRequest(`/guardians/${button.dataset.deleteGuardian}`, { method: "DELETE" });
    showToast("보호자를 삭제했어요.");
    await loadGuardians();
  } catch (error) { showToast(error.message); }
}

async function prepareGuardianMessage(hospital) {
  if (!accessToken) {
    await openGuardianModal();
    return showToast("먼저 로그인하고 보호자를 등록해 주세요.");
  }
  if (!guardians.length) await loadGuardians();
  const guardian = guardians.find(item => item.active);
  if (!guardian) {
    await openGuardianModal();
    return showToast("활성 보호자를 먼저 등록해 주세요.");
  }
  try {
    const result = await authenticatedRequest(`/guardians/${guardian.id}/emergency-message`, {
      method: "POST", body: JSON.stringify({ emergencyHpid: hospital.hpid,
        longitude: state.location.longitude, latitude: state.location.latitude })
    });
    location.href = result.smsUri;
  } catch (error) { showToast(error.message); }
}

let toastTimer;
function showToast(message) {
  const toast = document.querySelector("#toast");
  toast.textContent = message;
  toast.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("visible"), 2800);
}

start();
setInterval(refreshBeds, 60000);
