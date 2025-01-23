package personal_projects.backend.domain.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.bookmark.domain.BookMark;
@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

}
