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
import org.springframework.data.mongodb.core.mapping.MongoId;


@Document(collection = "place")  // MongoDB의 Collection에 해당
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MongoPlace {

    @Id  // MongoDB의 _id 필드에 해당
    private String id;

    private String name;
    private String placeType;
    private String address;
    private String tel;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)  // 좌표에 대한 인덱스를 생성
    private GeoJsonPoint coordinate;
}