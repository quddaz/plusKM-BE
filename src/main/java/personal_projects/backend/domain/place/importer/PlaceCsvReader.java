package personal_projects.backend.domain.place.importer;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.place.dto.PlaceCsvRow;

/**
 * 클래스패스의 장소 CSV를 한 줄씩 읽어 batchSize 단위로 넘긴다.
 * 파일 전체를 메모리에 올리지 않고, 잘못된 행은 건너뛰고 개수만 센다.
 */
@Component
public class PlaceCsvReader {

    public PlaceCsvReadResult read(
        String path,
        PlaceCsvFormat format,
        int batchSize,
        Consumer<List<PlaceCsvRow>> batchConsumer
    ) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize는 1 이상이어야 합니다: " + batchSize);
        }
        int imported = 0;
        int skipped = 0;
        List<PlaceCsvRow> batch = new ArrayList<>(batchSize);

        try (CSVReader csvReader = open(path)) {
            csvReader.readNext(); // 헤더
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                PlaceCsvRow parsed;
                try {
                    parsed = format.parse(row);
                } catch (IllegalArgumentException exception) {
                    skipped++;
                    continue;
                }
                batch.add(parsed);
                imported++;
                if (batch.size() >= batchSize) {
                    flush(batch, batchConsumer);
                }
            }
        } catch (IOException | CsvValidationException exception) {
            // 저장(batchConsumer) 중 발생한 예외는 감싸지 않고 그대로 전파한다.
            throw new IllegalStateException("CSV 파일을 읽지 못했습니다: " + path, exception);
        }
        flush(batch, batchConsumer);
        return new PlaceCsvReadResult(imported, skipped);
    }

    private CSVReader open(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    }

    private void flush(List<PlaceCsvRow> batch, Consumer<List<PlaceCsvRow>> batchConsumer) {
        if (batch.isEmpty()) {
            return;
        }
        batchConsumer.accept(List.copyOf(batch));
        batch.clear();
    }
}
