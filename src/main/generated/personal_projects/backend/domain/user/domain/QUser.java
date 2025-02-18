package personal_projects.backend.domain.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1531489939L;

    public static final QUser user = new QUser("user");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final EnumPath<personal_projects.backend.domain.user.domain.enumType.Oauth_type> oauth_type = createEnum("oauth_type", personal_projects.backend.domain.user.domain.enumType.Oauth_type.class);

    public final EnumPath<personal_projects.backend.domain.user.domain.enumType.Role> role = createEnum("role", personal_projects.backend.domain.user.domain.enumType.Role.class);

    public final StringPath socialId = createString("socialId");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

