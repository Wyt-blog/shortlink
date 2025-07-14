package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.remote.ShortLinkFeginRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    final ShortLinkFeginRemoteService shortLinkFeginRemoteService;

    final GroupMapper groupMapper;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBin(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag,0);
        List<GroupDO> groupDOS = groupMapper.selectList(wrapper);
        if(CollectionUtil.isEmpty(groupDOS)){
            throw new ServiceException("用户无分组信息");
        }
        List<String> gids = groupDOS.stream().map(GroupDO::getGid).toList();
        return shortLinkFeginRemoteService.pageRecycleBinShortLink(
                gids,requestParam.getCurrent(),requestParam.getSize()
        );
    }
}
