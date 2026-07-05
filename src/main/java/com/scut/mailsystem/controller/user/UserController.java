package com.scut.mailsystem.controller.user;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.dto.settings.UserSettingsUpdateRequest;
import com.scut.mailsystem.dto.user.ChangePasswordRequest;
import com.scut.mailsystem.service.settings.UserSettingsService;
import com.scut.mailsystem.service.user.UserService;
import com.scut.mailsystem.vo.settings.UserSettingsVO;
import com.scut.mailsystem.vo.user.CurrentUserVO;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserSettingsService userSettingsService;

    public UserController(UserService userService, UserSettingsService userSettingsService) {
        this.userService = userService;
        this.userSettingsService = userSettingsService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> getCurrentUser(@RequestHeader(value = "Authorization", required = false)
                                                     String authorizationHeader) {
        return ApiResponse.success(userService.getCurrentUser(authorizationHeader));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestHeader(value = "Authorization", required = false)
                                            String authorizationHeader,
                                            @RequestBody(required = false)
                                            ChangePasswordRequest request) {
        userService.changePassword(authorizationHeader, request);
        return ApiResponse.success();
    }

    @GetMapping("/settings")
    public ApiResponse<UserSettingsVO> getSettings(@RequestHeader(value = "Authorization", required = false)
                                                   String authorizationHeader) {
        return ApiResponse.success(userSettingsService.getSettings(authorizationHeader));
    }

    @PutMapping("/settings")
    public ApiResponse<UserSettingsVO> updateSettings(@RequestHeader(value = "Authorization", required = false)
                                                      String authorizationHeader,
                                                      @RequestBody(required = false)
                                                      UserSettingsUpdateRequest request) {
        return ApiResponse.success(userSettingsService.updateSettings(authorizationHeader, request));
    }
}
