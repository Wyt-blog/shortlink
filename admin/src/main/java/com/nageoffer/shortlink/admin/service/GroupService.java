package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短连接分组接口
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增分组接口
     *
     * @param groupName 新增分组名
     */
    void saveGroup(String groupName);

    /**
     * 新增分组接口
     * @param username 用户名
     * @param groupName 新增分组名
     */
    void saveGroup(String username, String groupName);

    /**
     * 查询短连接分组
     *
     * @return 短连接分组结合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改分组名接口
     *
     * @param requestParam 修改分组名参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除分组接口
     *
     * @param gid 删除的分组的 gid
     */
    void deleteGroup(String gid);

    /**
     * 短连接分组排序
     *
     * @param requestParam 排序字段参数实体
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
