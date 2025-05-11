package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/user")
public class UserController {

    final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        UserRespDTO result = userService.getUserByUsername(username);
        return Results.success(result);
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("has-username")
    public Result<Boolean> hasUsername(@RequestParam String username) {
        return Results.success(userService.hasUsername(username));
    }

    @PostMapping
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParm){
        userService.register(requestParm);
        return Results.success();
    }


}
