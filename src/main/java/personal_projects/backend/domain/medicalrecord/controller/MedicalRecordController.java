package personal_projects.backend.domain.medicalrecord.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.medicalrecord.dto.request.CreateMedicalRecordRequest;
import personal_projects.backend.domain.medicalrecord.dto.response.MedicalRecordsResponse;
import personal_projects.backend.domain.medicalrecord.service.MedicalRecordService;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createMedicalRecord(@Valid @RequestBody CreateMedicalRecordRequest request,
                                    @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        medicalRecordService.createMedicalRecord(request, principal.getUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicalRecord(@PathVariable(name = "id") long recordId,
                                    @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        medicalRecordService.deleteMedicalRecord(recordId, principal.getUserId());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public MedicalRecordsResponse findMedicalRecords(
        @RequestParam(name = "department") String department,
        @RequestParam(name = "placeId") Long placeId,
        @RequestParam(name = "lastId") Long lastId,
        @RequestParam(name = "size") int size
    ) {
        return MedicalRecordsResponse.from(
            medicalRecordService.findMedicalRecords(department, placeId, lastId, size)
        );
    }
}
