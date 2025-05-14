package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 短连接控制层
 */
@RestController
@RequestMapping("/api/short-link/v1/")
@RequiredArgsConstructor
public class ShortLinkController {

    final ShortLinkService shortLinkService;

    /**
     * 新增短连接
     */
    @PostMapping("/create")
    public Result<ShortLinkCreatRespDTO> createShortLink(@RequestBody ShortLinkCreatReqDTO requestParm) {
        return Results.success(shortLinkService.createShortLink(requestParm));
    }

    /**
     * 分页查询短连接
     */
    @GetMapping("page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParm) {
        return Results.success(shortLinkService.pageShortLink(requestParm));
    }


}
