package personal_projects.backend.domain.place.type;

public enum PlaceSearchType {
    ALL("전체"),
    HOSPITAL("병원"),
    PHARMACY("약국");

    private final String displayName;

    PlaceSearchType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
