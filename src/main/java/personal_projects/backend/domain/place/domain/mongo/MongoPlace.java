package personal_projects.backend.domain.place.domain.mongo;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "place")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MongoPlace {

    @Id
    private String id;
    private Long placeId;
    private String name;
    private String placeType;
    private String address;
    private String tel;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)  // 좌표에 대한 인덱스를 생성
    private GeoJsonPoint coordinate;
}