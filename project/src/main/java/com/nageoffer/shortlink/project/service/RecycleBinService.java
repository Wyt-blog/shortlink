package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.*;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站接口
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将短连接移动至回收站
     *
     * @param requestParam 需要移动的短连接信息
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 根据分组 gid 分页查询回收站短连接
     *
     * @param requestParam 分页查询参数
     * @return 返回分页查询结果
     */
    IPage<ShortLinkPageRespDTO> pageRecycleBin(RecycleBinPageReqDTO requestParam);

    /**
     * 从回收站恢复短连接
     *
     * @param requestParam 恢复的短连接参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 从回收站删除短连接（软删除）
     *
     * @param requestParam 需要删除的短连接的信息
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
