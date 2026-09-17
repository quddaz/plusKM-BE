package personal_projects.backend.domain.emergency.cache;

import static personal_projects.backend.common.config.CacheConfig.EMERGENCY_BEDS_CACHE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.emergency.client.EmergencyBedData;

@Component
@RequiredArgsConstructor
public class EmergencyBedCache {

    private static final String ALL_BEDS_KEY = "all";
    private final CacheManager cacheManager;

    public void replaceAll(List<EmergencyBedData> beds) {
        Map<String, EmergencyBedData> bedsByHpid = beds.stream()
            .collect(Collectors.toUnmodifiableMap(
                EmergencyBedData::hpid,
                Function.identity(),
                (first, ignored) -> first
            ));
        cache().put(ALL_BEDS_KEY, bedsByHpid);
    }

    public Map<String, EmergencyBedData> findAll(List<String> hpids) {
        Map<String, EmergencyBedData> cachedBeds = cachedBeds();
        Map<String, EmergencyBedData> selectedBeds = new LinkedHashMap<>();
        hpids.forEach(hpid -> {
            EmergencyBedData bed = cachedBeds.get(hpid);
            if (bed != null) {
                selectedBeds.put(hpid, bed);
            }
        });
        return Map.copyOf(selectedBeds);
    }

    @SuppressWarnings("unchecked")
    private Map<String, EmergencyBedData> cachedBeds() {
        Map<String, EmergencyBedData> beds = cache().get(ALL_BEDS_KEY, Map.class);
        return beds == null ? Map.of() : beds;
    }

    private Cache cache() {
        Cache cache = cacheManager.getCache(EMERGENCY_BEDS_CACHE);
        if (cache == null) {
            throw new IllegalStateException("응급실 병상 캐시를 찾을 수 없습니다.");
        }
        return cache;
    }
}
