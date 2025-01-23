package personal_projects.backend.domain.bookmark.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;

import java.awt.print.Book;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookMark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static BookMark of(Place place, User user) {
        return BookMark.builder()
            .place(place)
            .user(user)
            .build();
    }
}
