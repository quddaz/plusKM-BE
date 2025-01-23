package personal_projects.backend.domain.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.bookmark.domain.BookMark;
import personal_projects.backend.domain.bookmark.exception.BookMarkNotFoundException;
import personal_projects.backend.domain.bookmark.exception.errorCode.BookMarkErrorCode;
import personal_projects.backend.domain.bookmark.repository.BookMarkRepository;

@Service
@RequiredArgsConstructor
public class BookMarkService {

    private final BookMarkRepository bookMarkRepository;

    public BookMark findById(Long id) {
        return bookMarkRepository.findById(id).orElseThrow(() -> new BookMarkNotFoundException(BookMarkErrorCode.BOOKMARK_NOT_FOUND));
    }
}
