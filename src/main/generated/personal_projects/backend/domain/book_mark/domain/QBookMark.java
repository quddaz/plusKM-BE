package personal_projects.backend.domain.book_mark.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookMark is a Querydsl query type for BookMark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookMark extends EntityPathBase<BookMark> {

    private static final long serialVersionUID = 201230330L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookMark bookMark = new QBookMark("bookMark");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final personal_projects.backend.domain.place.domain.QPlace place;

    public final personal_projects.backend.domain.user.domain.QUser user;

    public QBookMark(String variable) {
        this(BookMark.class, forVariable(variable), INITS);
    }

    public QBookMark(Path<? extends BookMark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookMark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookMark(PathMetadata metadata, PathInits inits) {
        this(BookMark.class, metadata, inits);
    }

    public QBookMark(Class<? extends BookMark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.place = inits.isInitialized("place") ? new personal_projects.backend.domain.place.domain.QPlace(forProperty("place")) : null;
        this.user = inits.isInitialized("user") ? new personal_projects.backend.domain.user.domain.QUser(forProperty("user")) : null;
    }

}

