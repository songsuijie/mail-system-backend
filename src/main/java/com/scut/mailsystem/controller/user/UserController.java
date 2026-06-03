package com.scut.mailsystem.controller.user;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.service.user.UserService;
import com.scut.mailsystem.vo.user.CurrentUserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> getCurrentUser(@RequestHeader(value = "Authorization", required = false)
                                                     String authorizationHeader) {
        return ApiResponse.success(userService.getCurrentUser(authorizationHeader));
    }
}
