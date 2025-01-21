package personal_projects.backend.domain.place.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlace is a Querydsl query type for Place
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlace extends EntityPathBase<Place> {

    private static final long serialVersionUID = 1236353599L;

    public static final QPlace place = new QPlace("place");

    public final StringPath address = createString("address");

    public final ListPath<personal_projects.backend.domain.bookmark.domain.BookMark, personal_projects.backend.domain.bookmark.domain.QBookMark> bookMarks = this.<personal_projects.backend.domain.bookmark.domain.BookMark, personal_projects.backend.domain.bookmark.domain.QBookMark>createList("bookMarks", personal_projects.backend.domain.bookmark.domain.BookMark.class, personal_projects.backend.domain.bookmark.domain.QBookMark.class, PathInits.DIRECT2);

    public final ComparablePath<org.locationtech.jts.geom.Point> coordinate = createComparable("coordinate", org.locationtech.jts.geom.Point.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<personal_projects.backend.domain.medical.domain.Medical, personal_projects.backend.domain.medical.domain.QMedical> medicals = this.<personal_projects.backend.domain.medical.domain.Medical, personal_projects.backend.domain.medical.domain.QMedical>createList("medicals", personal_projects.backend.domain.medical.domain.Medical.class, personal_projects.backend.domain.medical.domain.QMedical.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final EnumPath<Place_type> place_type = createEnum("place_type", Place_type.class);

    public final StringPath tel = createString("tel");

    public QPlace(String variable) {
        super(Place.class, forVariable(variable));
    }

    public QPlace(Path<? extends Place> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlace(PathMetadata metadata) {
        super(Place.class, metadata);
    }

}

