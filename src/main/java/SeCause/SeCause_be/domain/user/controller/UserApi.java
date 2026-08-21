package SeCause.SeCause_be.domain.user.controller;

import SeCause.SeCause_be.domain.user.dto.UserMeResponse;
import SeCause.SeCause_be.domain.user.dto.UserUpdateRequest;
import SeCause.SeCause_be.domain.user.dto.UserUpdateResponse;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자의 기본 정보를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 정보 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "내 정보 조회가 완료됐습니다.",
                                      "result": {
                                        "userId": 1,
                                        "githubLoginId": "chaeyoungwon",
                                        "email": "epopcy@naver.com",
                                        "name": "chaeyoungwon",
                                        "avatarUrl": "https://avatars.githubusercontent.com/u/1"
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "unauthorized",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON401",
                                      "message": "인증이 필요합니다."
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "userNotFound",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "USER404",
                                      "message": "요청한 유저 정보를 찾을 수 없습니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<UserMeResponse> getMe(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(
            summary = "내 정보 수정",
            description = """
                    로그인한 사용자의 이름과 프로필 이미지 URL을 수정합니다. \n
                    name 또는 avatarUrl을 빈 문자열로 보내면 기존 값을 유지합니다. \n
                    avatarUrl을 null로 보내면 프로필 이미지를 삭제하고, 필드를 보내지 않으면 기존 값을 유지합니다. \n
                    GitHub 연동 이메일은 수정 대상이 아닙니다.""",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 정보 수정 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "내 정보 수정이 완료됐습니다.",
                                      "result": {
                                        "userId": 1,
                                        "githubLoginId": "chaeyoungwon",
                                        "email": "epopcy@naver.com",
                                        "name": "chaeyoungwon",
                                        "avatarUrl": null
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "validationError",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON4001",
                                      "message": "요청 값 검증에 실패했습니다.",
                                      "error": {
                                        "name": "이름은 50자 이하여야 합니다."
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "unauthorized",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON401",
                                      "message": "인증이 필요합니다."
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "userNotFound",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "USER404",
                                      "message": "요청한 유저 정보를 찾을 수 없습니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<UserUpdateResponse> updateMe(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @RequestBody
            @Valid
            UserUpdateRequest request
    );
}
