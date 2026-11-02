package personal_projects.backend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.config.PlaceCsvProperties;
import personal_projects.backend.domain.place.dto.response.PlaceCsvImportResponse;
import personal_projects.backend.domain.place.importer.PlaceCsvFormat;
import personal_projects.backend.domain.place.importer.PlaceCsvReadResult;
import personal_projects.backend.domain.place.importer.PlaceCsvReader;
import personal_projects.backend.domain.place.repository.PlaceCsvStageRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceCsvImportService {

    private final PlaceCsvStageRepository stageRepository;
    private final PlaceCsvReader csvReader;
    private final PlaceCsvProperties properties;

    @Transactional
    public PlaceCsvImportResponse importAll() {
        stageRepository.ensureSpatialIndex();
        stageRepository.createStageTable();
        try {
            PlaceCsvReadResult hospitals = stage(properties.hospitalPath(), PlaceCsvFormat.HOSPITAL);
            PlaceCsvReadResult pharmacies = stage(properties.pharmacyPath(), PlaceCsvFormat.PHARMACY);
            validateStaging(hospitals, pharmacies);
            mergeStageIntoPlace();
            return new PlaceCsvImportResponse(
                hospitals.imported(),
                pharmacies.imported(),
                hospitals.skipped() + pharmacies.skipped(),
                stageRepository.countInactivePlaces()
            );
        } finally {
            stageRepository.dropStageTable();
        }
    }

    private PlaceCsvReadResult stage(String path, PlaceCsvFormat format) {
        PlaceCsvReadResult result = csvReader.read(
            path, format, properties.batchSize(), stageRepository::saveAllToStage
        );
        log.info("장소 CSV 스테이징 완료: category={}, imported={}, skipped={}",
            format.category(), result.imported(), result.skipped());
        return result;
    }

    private void validateStaging(PlaceCsvReadResult hospitals, PlaceCsvReadResult pharmacies) {
        int staged = stageRepository.countStaged();
        int active = stageRepository.countActivePlaces();
        boolean missingCategory = hospitals.imported() == 0 || pharmacies.imported() == 0;
        boolean tooFewRows = staged == 0 || (active > 0 && staged < active / 2);
        if (missingCategory || tooFewRows) {
            throw new IllegalStateException(
                "CSV 스테이징 행 수 검증 실패: hospitals=" + hospitals.imported()
                    + ", pharmacies=" + pharmacies.imported()
                    + ", staged=" + staged + ", active=" + active
            );
        }
    }

    private void mergeStageIntoPlace() {
        int updated = stageRepository.updateChangedFromStage();
        int inserted = stageRepository.insertNewFromStage();
        int deactivated = stageRepository.deactivateMissingFromStage();
        log.info("장소 CSV 병합 완료: updated={}, inserted={}, deactivated={}",
            updated, inserted, deactivated);
    }
}
