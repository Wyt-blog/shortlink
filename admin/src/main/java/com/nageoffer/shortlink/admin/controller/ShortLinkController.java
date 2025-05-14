package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/short-link/admin/v1/")
@RequiredArgsConstructor
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};

    /**
     * 分页查询短连接
     */
    @GetMapping("page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParm) {
        return shortLinkRemoteService.pageShortLink(requestParm);
    }

    /**
     * 新增短连接
     */
    @PostMapping("/create")
    public Result<ShortLinkCreatRespDTO> createShortLink(@RequestBody ShortLinkCreatReqDTO requestParm) {
        return shortLinkRemoteService.createShortLink(requestParm);
    }

}
