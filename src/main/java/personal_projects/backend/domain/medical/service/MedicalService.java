package personal_projects.backend.domain.medical.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.medical.domain.Medical;
import personal_projects.backend.domain.medical.dto.request.MedicalDTO;
import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;
import personal_projects.backend.domain.medical.exception.MedicalNotFoundException;
import personal_projects.backend.domain.medical.exception.errorCode.MedicalErrorCode;
import personal_projects.backend.domain.medical.repository.MedicalRepository;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MedicalService {
    private final MedicalRepository medicalRepository;
    private final PlaceService placeService;
    private final UserService userService;

    @Transactional
    public void saveMedical(MedicalDTO medicalDTO, Long userId) {
        User user = userService.findById(userId);
        Place place = placeService.findById(medicalDTO.place_id());

        medicalRepository.save(MedicalDTO.toEntity(medicalDTO, place, user));
    }

    @Transactional
    public void deleteMedical(Long medicalId, Long userId) {
        Medical medical = findById(medicalId);

        if (!medical.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 진료 정보에 대한 권한이 없습니다. id=" + medicalId);
        }

        medicalRepository.deleteById(medicalId);
    }

    public List<MedicalPageResponse> findMedicalByDepartment(String department, Long placeId, Long lastId, int size) {
        return medicalRepository.findMedicalByDepartment(department, placeId, lastId, size);
    }
    public Medical findById(Long medicalId) {
        return medicalRepository.findById(medicalId)
            .orElseThrow(() -> new MedicalNotFoundException(MedicalErrorCode.MEDICAL_NOT_FOUND));
    }
}
