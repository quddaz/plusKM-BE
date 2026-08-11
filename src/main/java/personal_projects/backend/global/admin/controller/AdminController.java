package personal_projects.backend.global.admin.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.global.admin.facade.CSVManagementFacade;

@Slf4j
@RestController
@RequestMapping("/admin/data")
@RequiredArgsConstructor
public class AdminController {
    private final CSVManagementFacade csvManagementService;


    // 관리자 요청: 공공데이터 갱신 작업
    @PostMapping("/update-places-from-csv")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePlaces(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        log.info("관리자 요청: RDB Place 데이터 갱신 작업 시작.");
        csvManagementService.csvManagement(customOAuth2User.getRoles().get(0));
    }
}
