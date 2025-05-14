package com.nageoffer.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * 短连接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短连接远程调用
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParm) {
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("gid",requestParm.getGid());
        requestMap.put("current",requestParm.getCurrent());
        requestMap.put("size",requestParm.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
    }

    /**
     * 分页查询短连接远程调用
     */
    default Result<ShortLinkCreatRespDTO> createShortLink(ShortLinkCreatReqDTO requestParm){
        String result = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParm));
        return JSON.parseObject(result, new TypeReference<>() {});
    }
}
