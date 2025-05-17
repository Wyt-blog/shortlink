package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 短连接接口
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短连接
     *
     * @param requestParam 短连接创建信息
     * @return 返回信息
     */
    ShortLinkCreatRespDTO createShortLink(ShortLinkCreatReqDTO requestParam);

    /**
     * 根据分组 gid 分页查询短连接
     *
     * @param requestParam 分页查询参数
     * @return 返回分页查询结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 根据分组 id 集合查询分组下短连接数量
     *
     * @param requestParam 分组 id 集合
     * @return 查询结果实体类
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短连接信息
     *
     * @param requestParam 修改参数实体类
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短连接跳转
     *
     * @param shortLink 短连接
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    void restoreUrl(String shortLink, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
