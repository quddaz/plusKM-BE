package personal_projects.backend.domain.medical.repository;

import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;

import java.util.List;

public interface MedicalRepositoryCustom {


    List<MedicalPageResponse> findMedicalByDepartment(String department, Long placeId, Long lastId, int size);
}
