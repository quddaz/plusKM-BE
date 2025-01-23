package personal_projects.backend.domain.medical.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.medical.dto.request.MedicalDTO;
import personal_projects.backend.domain.medical.service.MedicalService;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.global.dto.ResponseTemplate;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/medical")
public class MedicalController {
    private final MedicalService medicalService;

    @PostMapping("/save")
    public ResponseEntity<?> saveMedical(@RequestBody MedicalDTO medicalDTO,
                                         @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        medicalService.saveMedical(medicalDTO, customOAuth2User.getUserId());
        return ResponseEntity
            .ok(ResponseTemplate.EMPTY_RESPONSE);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedical(@PathVariable(name = "id") long id,
                                             @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        medicalService.deleteMedical(id, customOAuth2User.getUserId());
        return ResponseEntity
            .ok(ResponseTemplate.EMPTY_RESPONSE);
    }

    @GetMapping("/medical")
    public ResponseEntity<?> findMedicalByDepartment(@RequestParam(name = "department") String department,
                                                        @RequestParam(name = "placeId") Long placeId,
                                                        @RequestParam(name = "lastId") Long lastId,
                                                        @RequestParam(name = "size") int size) {
        return ResponseEntity
            .ok(medicalService.findMedicalByDepartment(department, placeId, lastId, size));
    }
}
