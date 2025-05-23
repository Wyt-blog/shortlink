package com.nageoffer.shortlink.admin.common.blockHandler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreatRespDTO;

public class CustomBlockHandler {

    public static Result<ShortLinkCreatRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreatReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreatRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}