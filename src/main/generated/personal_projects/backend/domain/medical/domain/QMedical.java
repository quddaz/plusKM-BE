package personal_projects.backend.domain.medical.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;
import personal_projects.backend.domain.medical.domain.enumType.Medical_department;


/**
 * QMedical is a Querydsl query type for Medical
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMedical extends EntityPathBase<Medical> {

    private static final long serialVersionUID = 535399487L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMedical medical = new QMedical("medical");

    public final personal_projects.backend.global.domain.QBaseTimeEntity _super = new personal_projects.backend.global.domain.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final EnumPath<Medical_department> department = createEnum("department", Medical_department.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> medical_fee = createNumber("medical_fee", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final personal_projects.backend.domain.place.domain.QPlace place;

    public final personal_projects.backend.domain.user.domain.QUser user;

    public QMedical(String variable) {
        this(Medical.class, forVariable(variable), INITS);
    }

    public QMedical(Path<? extends Medical> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMedical(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMedical(PathMetadata metadata, PathInits inits) {
        this(Medical.class, metadata, inits);
    }

    public QMedical(Class<? extends Medical> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.place = inits.isInitialized("place") ? new personal_projects.backend.domain.place.domain.QPlace(forProperty("place")) : null;
        this.user = inits.isInitialized("user") ? new personal_projects.backend.domain.user.domain.QUser(forProperty("user")) : null;
    }

}

