package personal_projects.backend.domain.user.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import personal_projects.backend.domain.user.domain.enumType.Oauth_type;
import personal_projects.backend.domain.user.domain.enumType.Role;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.repository.UserRepository;
import personal_projects.backend.global.util.DummyDataInit;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Order(2)
@DummyDataInit
public class UserInitializer implements ApplicationRunner {
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(userRepository.count() > 0) {
            log.info("[User] 더미 데이터 존재");
        } else {
            List<User> users = new ArrayList<>();

            for(int i = 1; i <= 3; i++){
                users.add(User.builder()
                    .email("user"+ i + "@gmail.com")
                    .name("김철수" + i)
                    .oauth_type(Oauth_type.GOOGLE)
                    .socialId("123456789" + i)
                    .role(Role.USER)
                    .build());
            }

            userRepository.saveAll(users);
        }

    }
}
