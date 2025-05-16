package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 短连接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    final RBloomFilter<String> shortLinkCachePenetrationBloomFilter;

    final ShortLinkMapper shortLinkMapper;

    @Override
    public ShortLinkCreatRespDTO createShortLink(ShortLinkCreatReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = new StringBuilder(requestParam.getOriginUrl())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
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
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
         IPage<ShortLinkDO> resultPage = this.lambdaQuery()
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .page(requestParam);
         return resultPage.convert(each -> BeanUtil.copyProperties(each, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShortLinkDO::getEnableStatus, 0)
                .groupBy(ShortLinkDO::getGid)
                .in(ShortLinkDO::getGid, requestParam);
        queryWrapper.select("gid,count(id) as shortLinkCount");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        return maps.stream().map(map -> {
            Long count = (Long) map.get("shortLinkCount");
            String gid = (String) map.get("gid");
            return new ShortLinkGroupCountQueryRespDTO(gid, count);
        }).toList();
    }

    public String generateSuffix(ShortLinkCreatReqDTO requestParam) {
        String shortUri;
        int count = 0;
        while (true) {
            if(count > 3) {
                throw new ServiceException("短连接生成频繁，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl() + UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originUrl);
            if(!shortLinkCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            count++;
        }
        return shortUri;
    }
}
