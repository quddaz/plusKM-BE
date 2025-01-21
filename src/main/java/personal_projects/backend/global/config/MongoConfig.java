package personal_projects.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "personal_projects.backend.domain.place.domain.mongo")
public class MongoConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory mongoDatabaseFactory,
                                                       MongoMappingContext mongoMappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);

        // MongoDB에 저장된 데이터에서 _class 필드를 제거하도록 설정 (매핑된 클래스만 사용)
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return converter;
    }
}