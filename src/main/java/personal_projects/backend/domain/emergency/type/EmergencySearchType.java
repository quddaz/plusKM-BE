package personal_projects.backend.domain.emergency.type;

public enum EmergencySearchType {
    ALL("전체"),
    HOSPITAL("병원"),
    PHARMACY("약국");

    private final String displayName;

    EmergencySearchType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
