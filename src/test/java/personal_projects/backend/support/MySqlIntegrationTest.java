package personal_projects.backend.support;

import static org.mockito.Mockito.reset;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import personal_projects.backend.common.scheduler.EmergencyBedSyncScheduler;
import personal_projects.backend.common.scheduler.EmergencySyncScheduler;
import personal_projects.backend.domain.auth.token.JwtTokenProvider;
import personal_projects.backend.domain.emergency.client.EmergencyApiClient;
import personal_projects.backend.domain.emergency.client.EmergencyBedApiClient;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.repository.jpa.EmergencyJpaRepository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.place.repository.PlaceJpaRepository;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.repository.jpa.UserJpaRepository;
import personal_projects.backend.domain.user.type.OAuthProvider;
import personal_projects.backend.domain.user.type.Role;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.profiles.active=test")
public abstract class MySqlIntegrationTest {

    // JVM마다 하나만 띄우고 Ryuk가 종료 시 정리한다. Docker가 없으면 테스트를 실패시킨다.
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.9")
        .withDatabaseName("pluskm_test")
        .withUsername("pluskm_test")
        .withPassword("test-only-password")
        .withCommand("--log-bin-trust-function-creators=1")
        .withUrlParam("rewriteBatchedStatements", "true");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected PlatformTransactionManager transactionManager;
    @Autowired protected PlaceJpaRepository places;
    @Autowired protected EmergencyJpaRepository emergencies;
    @Autowired protected UserJpaRepository users;
    @Autowired protected JwtTokenProvider tokens;
    @Autowired private CacheManager cacheManager;

    // 외부 API와 시간에 따라 실행되는 작업만 대체하고 서비스와 저장소는 실제 빈을 쓴다.
    @MockBean protected EmergencyApiClient emergencyApi;
    @MockBean protected EmergencyBedApiClient bedApi;
    @MockBean private EmergencySyncScheduler emergencyScheduler;
    @MockBean private EmergencyBedSyncScheduler bedScheduler;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM favorite");
        jdbc.update("DELETE FROM guardian_contact");
        jdbc.update("DELETE FROM place");
        jdbc.update("DELETE FROM emergency");
        jdbc.update("DELETE FROM users");
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        reset(emergencyApi, bedApi);
    }

    protected void inTransaction(Runnable work) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> work.run());
    }

    protected Place place(String name, PlaceCategory category, boolean active, double longitude, double latitude) {
        return places.saveAndFlush(Place.builder().name(name).address(name + " 주소")
            .category(category).facilityType(category == PlaceCategory.PHARMACY ? "약국" : "의원")
            .phoneNumber("031-000-0000").active(active).coordinate(point(longitude, latitude)).build());
    }

    protected Emergency emergency(String hpid, boolean active, double longitude, double latitude) {
        return emergencies.saveAndFlush(Emergency.builder().hpid(hpid).name(hpid + " 응급실")
            .address(hpid + " 주소").phoneNumber("031-119-0000").active(active)
            .coordinate(point(longitude, latitude)).build());
    }

    protected User user(Role role) {
        String key = UUID.randomUUID().toString();
        return users.saveAndFlush(User.builder().name("테스트 사용자").email(key.substring(0, 8) + "@test.invalid")
            .socialId(key).oauthProvider(OAuthProvider.GOOGLE).role(role).build());
    }

    protected String bearer(User user) {
        return "Bearer " + tokens.createAccessToken(user.getId(), user.roleAuthorities());
    }

    protected static Point point(double longitude, double latitude) {
        Point point = new GeometryFactory().createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    // 구면에서 중심, 방위각, 거리가 주어졌을 때 도착점을 구해 경계 테스트 데이터를 만든다.
    protected static double[] destination(double lon, double lat, double bearing, double meters) {
        double angularDistance = meters / 6_370_986.0;
        double phi = Math.toRadians(lat);
        double theta = Math.toRadians(bearing);
        double targetLat = Math.asin(Math.sin(phi) * Math.cos(angularDistance)
            + Math.cos(phi) * Math.sin(angularDistance) * Math.cos(theta));
        double targetLon = Math.toRadians(lon) + Math.atan2(
            Math.sin(theta) * Math.sin(angularDistance) * Math.cos(phi),
            Math.cos(angularDistance) - Math.sin(phi) * Math.sin(targetLat));
        return new double[]{Math.toDegrees(targetLon), Math.toDegrees(targetLat)};
    }
}
