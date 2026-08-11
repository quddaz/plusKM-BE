package personal_projects.backend.domain.medical.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.medical.dto.request.MedicalDTO;
import personal_projects.backend.domain.medical.dto.response.MedicalPagesResponse;
import personal_projects.backend.domain.medical.service.MedicalService;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/medical")
public class MedicalController {
    private final MedicalService medicalService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveMedical(@RequestBody MedicalDTO medicalDTO,
                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        medicalService.saveMedical(medicalDTO, customOAuth2User.getUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedical(@PathVariable(name = "id") long id,
                              @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        medicalService.deleteMedical(id, customOAuth2User.getUserId());
    }

    @GetMapping("/medical")
    @ResponseStatus(HttpStatus.OK)
    public MedicalPagesResponse findMedicalByDepartment(
        @RequestParam(name = "department") String department,
        @RequestParam(name = "placeId") Long placeId,
        @RequestParam(name = "lastId") Long lastId,
        @RequestParam(name = "size") int size
    ) {
        return MedicalPagesResponse.from(
            medicalService.findMedicalByDepartment(department, placeId, lastId, size)
        );
    }
}
