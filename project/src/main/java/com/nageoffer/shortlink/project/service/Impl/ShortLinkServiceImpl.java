package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 短连接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    final RBloomFilter<String> shortLinkCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreatRespDTO createShortLink(ShortLinkCreatReqDTO requestParm) {
        String shortLinkSuffix = generateSuffix(requestParm);
        String fullShortUrl = new StringBuilder(requestParm.getOriginUrl())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .domain(requestParm.getDomain())
                .originUrl(requestParm.getOriginUrl())
                .gid(requestParm.getGid())
                .createdType(requestParm.getCreatedType())
                .validDateType(requestParm.getValidDateType())
                .validDate(requestParm.getValidDate())
                .describe(requestParm.getDescribe())
                .shortUri(shortLinkSuffix)
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException exception) {
            log.warn("短连接：{} 重复入库",fullShortUrl);
            throw new ServiceException("短连接生成重复");
        }
        shortLinkCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreatRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParm.getOriginUrl())
                .gid(requestParm.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParm) {
         IPage<ShortLinkDO> resultPage = this.lambdaQuery()
                .eq(ShortLinkDO::getGid, requestParm.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .page(requestParm);
         return resultPage.convert(each -> BeanUtil.copyProperties(each, ShortLinkPageRespDTO.class));
    }

    public String generateSuffix(ShortLinkCreatReqDTO requestParm) {
        String shortUri;
        int count = 0;
        while (true) {
            if(count > 3) {
                throw new ServiceException("短连接生成频繁，请稍后再试");
            }
            String originUrl = requestParm.getOriginUrl() + UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originUrl);
            if(!shortLinkCachePenetrationBloomFilter.contains(requestParm.getDomain() + "/" + shortUri)) {
                break;
            }
            count++;
        }
        return shortUri;
    }
}
