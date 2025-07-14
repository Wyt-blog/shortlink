package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.ShortLinkFeginRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 获取短连接标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {

    final ShortLinkFeginRemoteService shortLinkFeginRemoteService;

    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) throws IOException {
        return shortLinkFeginRemoteService.getTitleByUrl(url);
    }

}
