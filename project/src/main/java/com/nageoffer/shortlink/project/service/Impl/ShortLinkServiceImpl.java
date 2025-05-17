package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.constants.RedisKeyConstant;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreatRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.toolkit.ShortLinkUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    final ShortLinkGotoMapper shortLinkGotoMapper;

    final StringRedisTemplate stringRedisTemplate;

    final RedissonClient redissonClient;

    @Override
    public ShortLinkCreatRespDTO createShortLink(ShortLinkCreatReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = new StringBuilder(requestParam.getDomain())
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
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            shortLinkGotoMapper.insert(shortLinkGotoDO);
            baseMapper.insert(shortLinkDO);
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY,fullShortUrl),
                    requestParam.getOriginUrl(),
                    ShortLinkUtil.getLinkCacheValidDate(requestParam.getValidDate()),
                    TimeUnit.MILLISECONDS
            );
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

    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        UpdateWrapper<ShortLinkDO> wrapper = new UpdateWrapper<>();
        wrapper.lambda()
                .eq(ShortLinkDO::getGid,requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl,requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()),ShortLinkDO::getValidDate,null);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        baseMapper.update(shortLinkDO,wrapper);
    }

    @Override
    public void restoreUrl(String shortLink, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String fullShortUrl = request.getScheme() + "://" + serverName + "/" + shortLink;
        String originLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originLink)) {
            response.sendRedirect(originLink);
            return;
        }
        boolean contains = shortLinkCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            response.sendRedirect("/page/notfound");
            return;
        }
        String gotoNullShortLinkUrl = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoNullShortLinkUrl)) {
            response.sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originLink)) {
                response.sendRedirect(originLink);
                return;
            }
            QueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = new QueryWrapper<>();
            linkGotoQueryWrapper.lambda().eq(ShortLinkGotoDO::getFullShortUrl,fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null){
                stringRedisTemplate.opsForValue().set(
                        String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                        "-",30,TimeUnit.MINUTES);
                // 此处需要封控
                response.sendRedirect("/page/notfound");
            }
            QueryWrapper<ShortLinkDO> shortLinkQueryWrapper = new QueryWrapper<>();
            shortLinkQueryWrapper.lambda()
                    .eq(ShortLinkDO::getGid,shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getFullShortUrl,fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(shortLinkQueryWrapper);
            if (shortLinkDO != null){
                if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())){
                    stringRedisTemplate.opsForValue().set(
                            String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                            "-",30,TimeUnit.MINUTES);
                    response.sendRedirect("/page/notfound");
                }
                stringRedisTemplate.opsForValue().set(
                        String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl(),
                        ShortLinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),
                        TimeUnit.MILLISECONDS
                        );
                response.sendRedirect(shortLinkDO.getOriginUrl());
            }
        }finally {
            lock.unlock();
        }
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
