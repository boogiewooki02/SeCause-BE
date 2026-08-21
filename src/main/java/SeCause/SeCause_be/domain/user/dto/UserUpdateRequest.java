package SeCause.SeCause_be.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @Size(max = 1024, message = "프로필 이미지 URL은 1024자 이하여야 합니다.")
    private String avatarUrl;

    @JsonIgnore
    private boolean avatarUrlPresent;

    public String name() {
        return name;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    @JsonIgnore
    public boolean hasAvatarUrl() {
        return avatarUrlPresent;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrlPresent = true;
        this.avatarUrl = avatarUrl;
    }
}
