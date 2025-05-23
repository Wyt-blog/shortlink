package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.constants.RedisCacheConstants;
import com.nageoffer.shortlink.admin.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 短连接分组接口实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper,GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};

    final RedissonClient redisson;

    @Value("${shortlink.group.create.max}")
    private int groupCreateMax;

    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redisson.getLock(RedisCacheConstants.LOCK_GROUP_CREATE_KEY + username);
        lock.lock();
        try {
            String gid;
            LambdaQueryWrapper<GroupDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag,0);
            long count = this.count(queryWrapper);
            if (count > groupCreateMax) {
                throw new ServiceException(String.format("分组已超过最大分组数:%S",groupCreateMax));
            }
            while (true) {
                gid = RandomCodeGenerator.generateRandomCode();
                if (hasGroup(username,gid)) break;
            }
            GroupDO groupDO = GroupDO.builder()
                    .gid(gid)
                    .name(groupName)
                    .username(username)
                    .sortOrder(0)
                    .build();
            baseMapper.insert(groupDO);
        }finally {

        }
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(wrapper);
        List<String> gids = groupDOS.stream().map(GroupDO::getGid).toList();
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOS = BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);
        Result<List<ShortLinkGroupCountQueryRespDTO>> respDTOResult = shortLinkRemoteService.listGroupShortLinkCount(gids);
        Map<String, Long> map = respDTOResult.getData().stream().collect(Collectors.toMap(ShortLinkGroupCountQueryRespDTO::getGid, ShortLinkGroupCountQueryRespDTO::getShortLinkCount));
        shortLinkGroupRespDTOS.forEach(shortLinkGroupRespDTO -> {
            shortLinkGroupRespDTO.setShortLinkCount(map.get(shortLinkGroupRespDTO.getGid()));
        });
        return shortLinkGroupRespDTOS;
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        this.lambdaUpdate()
                .eq(GroupDO::getGid,requestParam.getGid())
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getDelFlag,0)
                .set(GroupDO::getName,requestParam.getName())
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
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            baseMapper.update(groupDO,new LambdaUpdateWrapper<GroupDO>()
                            .eq(GroupDO::getUsername,UserContext.getUsername())
                            .eq(GroupDO::getDelFlag,0)
                            .eq(GroupDO::getGid,each.getGid()));
        });
    }

    public boolean hasGroup(String username,String gid) {
        GroupDO hasGroup = baseMapper.selectOne(
                new QueryWrapper<GroupDO>().lambda()
                        .eq(GroupDO::getGid, gid)
                        .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()))
        );
        return hasGroup == null;
    }
}
