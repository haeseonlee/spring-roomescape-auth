package roomescape.domain.theme;

public class Theme {

    private Long id;
    private String name;
    private String description;
    private String url;
    private Long storeId;

    public Theme() {
    }

    public Theme(Long id, String name, String description, String url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
    }

    public Theme(Long id, String name, String description, String url, Long storeId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.storeId = storeId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public Long getStoreId() {
        return storeId;
    }
}
