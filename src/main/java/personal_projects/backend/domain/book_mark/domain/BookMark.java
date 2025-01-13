package personal_projects.backend.domain.book_mark.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookMark {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
