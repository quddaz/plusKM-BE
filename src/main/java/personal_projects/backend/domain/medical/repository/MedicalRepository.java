package personal_projects.backend.domain.medical.repository;

import personal_projects.backend.domain.medical.domain.Medical;
import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;

import java.util.List;
import java.util.Optional;

public interface MedicalRepository {

    Medical save(Medical medical);

    Optional<Medical> findById(Long id);

    void deleteById(Long id);

    List<MedicalPageResponse> findMedicalByDepartment(
        String department,
        Long placeId,
        Long lastId,
        int size
    );
}
