package personal_projects.backend.global.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final String[] WHITE_LIST = {
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/global/**",
        "/actuator/**",
        "/auth/**"

    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .formLogin(AbstractHttpConfigurer::disable) // 기본 시큐리티 로그인 페이지 사용 안함
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP 기본 인증 사용 안함
            .cors(Customizer.withDefaults()) // CORS 설정 -> 기본 corsConfigurationSource 빈 사용
            .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 기능 비활성화
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST).permitAll() // 화이트 리스트를 제외하고 모두 인증
                .anyRequest().authenticated());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}