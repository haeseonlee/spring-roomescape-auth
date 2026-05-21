package roomescape.domain.member;

public class Member {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
    private Long storeId;

    public Member(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(Long id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(Long id, String name, String email, String password, String role, Long storeId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.storeId = storeId;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isManagerOf(Long storeId) {
        return this.storeId != null && this.storeId.equals(storeId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }
}
