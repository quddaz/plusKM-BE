package personal_projects.backend.domain.medical.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.medical.repository.MedicalRepository;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MedicalService {
    private final MedicalRepository medicalRepository;
    
}
