package personal_projects.backend.domain.place.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.domain.mongo.MongoPlace;
@Repository
public interface PlaceMongoRepository extends MongoRepository<MongoPlace, String>, PlaceMongoRepositoryCustom {
}
