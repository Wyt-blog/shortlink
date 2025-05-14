package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短连接分组接口实现
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper,GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        while (true) {
            gid = RandomCodeGenerator.generateRandomCode();
            if (hasGroup(gid)) break;
        }
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .name(groupName)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(wrapper);
        return BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParm) {
        this.lambdaUpdate()
                .eq(GroupDO::getGid,requestParm.getGid())
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getDelFlag,0)
                .set(GroupDO::getName,requestParm.getName())
                .update();
    }

    @Override
    public void deleteGroup(String gid) {
        this.lambdaUpdate()
                .eq(GroupDO::getGid,gid)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getDelFlag,0)
                .set(GroupDO::getDelFlag,1)
                .update();
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParm) {
        requestParm.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            baseMapper.update(groupDO,new LambdaUpdateWrapper<GroupDO>()
                            .eq(GroupDO::getUsername,UserContext.getUsername())
                            .eq(GroupDO::getDelFlag,0)
                            .eq(GroupDO::getGid,each.getGid()));
        });
    }

    public boolean hasGroup(String gid) {
        GroupDO hasGroup = baseMapper.selectOne(
                new QueryWrapper<GroupDO>().lambda()
                        .eq(GroupDO::getGid, gid)
                        .eq(GroupDO::getUsername, null)
        );
        return hasGroup == null;
    }
}
