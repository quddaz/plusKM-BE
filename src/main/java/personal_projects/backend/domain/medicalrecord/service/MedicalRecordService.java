package personal_projects.backend.domain.medicalrecord.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;
import personal_projects.backend.domain.medicalrecord.dto.request.CreateMedicalRecordRequest;
import personal_projects.backend.domain.medicalrecord.dto.response.MedicalRecordSummaryResponse;
import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.domain.medicalrecord.exception.code.MedicalRecordErrorCode;
import personal_projects.backend.domain.medicalrecord.repository.MedicalRecordRepository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PlaceService placeService;
    private final UserService userService;

    @Transactional
    public void createMedicalRecord(CreateMedicalRecordRequest request, Long userId) {
        User user = userService.findById(userId);
        Place place = placeService.findById(request.placeId());

        medicalRecordRepository.save(request.toEntity(place, user));
    }

    @Transactional
    public void deleteMedicalRecord(Long recordId, Long userId) {
        MedicalRecord medicalRecord = findById(recordId);

        if (!medicalRecord.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 진료 정보에 대한 권한이 없습니다. id=" + recordId);
        }

        medicalRecordRepository.deleteById(recordId);
    }

    public List<MedicalRecordSummaryResponse> findMedicalRecords(String department, Long placeId, Long lastId, int size) {
        return medicalRecordRepository.findMedicalRecords(department, placeId, lastId, size);
    }
    public MedicalRecord findById(Long recordId) {
        return medicalRecordRepository.findById(recordId)
            .orElseThrow(() -> new BusinessException(MedicalRecordErrorCode.MEDICAL_RECORD_NOT_FOUND));
    }
}
