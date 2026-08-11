package personal_projects.backend.domain.user.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import personal_projects.backend.domain.user.type.OAuthProvider;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.repository.UserRepository;
import personal_projects.backend.common.annotation.DevelopmentDataInitializer;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Order(2)
@DevelopmentDataInitializer
public class DevelopmentUserInitializer implements ApplicationRunner {
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments arguments) {
        if (userRepository.count() > 0) {
            log.info("[User] 더미 데이터 존재");
            return;
        }

        List<User> users = new ArrayList<>();
        users.add(User.builder()
            .email("playgrounm@gmail.com")
            .name("김병웅")
            .oauthProvider(OAuthProvider.GOOGLE)
            .socialId("0")
            .role(Role.ADMIN)
            .build());

        for (int index = 1; index <= 3; index++) {
            users.add(User.builder()
                .email("user" + index + "@gmail.com")
                .name("김철수" + index)
                .oauthProvider(OAuthProvider.GOOGLE)
                .socialId("123456789" + index)
                .role(Role.USER)
                .build());
        }

        userRepository.saveAll(users);
    }
}
