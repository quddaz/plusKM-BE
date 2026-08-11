package personal_projects.backend.common.config;


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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import personal_projects.backend.domain.auth.filter.JwtAccessDeniedHandler;
import personal_projects.backend.domain.auth.filter.JwtAuthenticationEntryPoint;
import personal_projects.backend.domain.auth.filter.JwtAuthenticationFilter;
import personal_projects.backend.domain.auth.handler.OAuth2LoginSuccessHandler;
import personal_projects.backend.domain.auth.service.OAuth2LoginUserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final OAuth2LoginUserService oauth2LoginUserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String[] PUBLIC_ENDPOINTS = {
        "/error",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/*",
        "/webjars/**",
        "/global/**",
        "/actuator/**",
        "/auth/**"
    };
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .formLogin(AbstractHttpConfigurer::disable) // 기본 시큐리티 로그인 페이지 사용 안함
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP 기본 인증 사용 안함
            .cors(Customizer.withDefaults()) // CORS 설정 -> 기본 corsConfigurationSource 빈 사용
            .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 기능 비활성화
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터를 UsernamePasswordAuthenticationFilter 전에 추가
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증되지 않은 사용자가 보호된 리소스에 액세스 할 때 호출
                .accessDeniedHandler(jwtAccessDeniedHandler)) // 권한이 없는 사용자가 보호된 리소스에 액세스 할 때 호출
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                    .userService(oauth2LoginUserService))
                .successHandler(oauth2LoginSuccessHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll() // 화이트 리스트를 제외하고 모두 인증
                .anyRequest().authenticated());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
