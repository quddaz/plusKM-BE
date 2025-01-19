package personal_projects.backend.domain.place.dto;

public enum Search_Type {
    ALL("전체"),
    HOSPITAL("병원"),
    PHARMACY("약국");

    private final String type;

    Search_Type(String type) {
        this.type = type;
    }
}
