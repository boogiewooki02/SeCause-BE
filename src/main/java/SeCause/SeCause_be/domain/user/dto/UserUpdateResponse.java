package SeCause.SeCause_be.domain.user.dto;

import SeCause.SeCause_be.domain.user.entity.User;

public record UserUpdateResponse(
        Long userId,
        String githubLoginId,
        String email,
        String name,
        String avatarUrl
) {

    public static UserUpdateResponse from(User user) {
        return new UserUpdateResponse(
                user.getUserId(),
                user.getGithubLoginId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl()
        );
    }
}
