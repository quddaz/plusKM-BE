package personal_projects.backend.domain.guardian.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.guardian.dto.request.EmergencyMessageRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactUpdateRequest;
import personal_projects.backend.domain.guardian.dto.response.EmergencyMessageResponse;
import personal_projects.backend.domain.guardian.dto.response.GuardianContactResponse;
import personal_projects.backend.domain.guardian.service.GuardianContactService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guardians")
public class GuardianContactController {

    private final GuardianContactService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuardianContactResponse create(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @Valid @RequestBody GuardianContactRequest request
    ) {
        return service.create(principal.getUserId(), request);
    }

    @GetMapping
    public List<GuardianContactResponse> findAll(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return service.findAll(principal.getUserId());
    }

    @PutMapping("/{guardianId}")
    public GuardianContactResponse update(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long guardianId,
        @Valid @RequestBody GuardianContactUpdateRequest request
    ) {
        return service.update(principal.getUserId(), guardianId, request);
    }

    @DeleteMapping("/{guardianId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long guardianId
    ) {
        service.delete(principal.getUserId(), guardianId);
    }

    @PostMapping("/{guardianId}/emergency-message")
    public EmergencyMessageResponse createEmergencyMessage(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long guardianId,
        @Valid @RequestBody EmergencyMessageRequest request
    ) {
        return service.createEmergencyMessage(principal.getUserId(), guardianId, request);
    }
}
