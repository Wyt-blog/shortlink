package com.nageoffer.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.constants.GotoDomainWhiteListConfiguration;
import com.nageoffer.shortlink.project.common.constants.RedisKeyConstant;
import com.nageoffer.shortlink.project.common.constants.ShortLinkConstant;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreatReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.*;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.GetMessageUtils;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.toolkit.RabbitMqHelper;
import com.nageoffer.shortlink.project.toolkit.ShortLinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    final RabbitMqHelper rabbitMqHelper;

    @Value("${shortlink.domain.default}")
    public String shortLinkDomainDefault;

    @Value("${shortlink.state.locale.key}")
    public String shortLinkStateKey;

    final ShortLinkAccessStatsMapper shortLinkAccessStateMapper;

    final ShortLinkLocaleStatsMapper shortLinkLocaleStateMapper;

    final ShortLinkBrowserStatsMapper shortLinkBrowserStateMapper;

    final ShortLinkOsStatsMapper shortLinkOsStatsMapper;

    final ShortLinkDeviceStatsMapper shortLinkDeviceStatsMapper;

    final ShortLinkAccessLogsMapper shortLinkAccessLogsMapper;

    final ShortLinkNetworkStatsMapper shortLinkNetworkStatsMapper;

    final ShortLinkStatsTodayMapper shortLinkStatsTodayMapper;

    final ShortLinkMapper shortLinkMapper;

    @Override
    @Transactional
    public ShortLinkCreatRespDTO createShortLink(ShortLinkCreatReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = new StringBuilder(shortLinkDomainDefault)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .domain(shortLinkDomainDefault)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .totalPv(0)
                .totalUip(0)
                .totalUv(0)
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
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
         IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
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
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each,80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortLink;
        String originLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originLink)) {
            ShortLinkState(buildLinkStatsRecordAndSetUser(null,fullShortUrl,request,response));
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
                ShortLinkState(buildLinkStatsRecordAndSetUser(null,fullShortUrl,request,response));
                response.sendRedirect(originLink);
                return;
            }
            QueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = new QueryWrapper<>();
            linkGotoQueryWrapper.lambda().eq(ShortLinkGotoDO::getFullShortUrl,fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null){
                stringRedisTemplate.opsForValue().set(
                        String.format(RedisKeyConstant.GO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
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
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))){
                stringRedisTemplate.opsForValue().set(
                        String.format(RedisKeyConstant.GO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                        "-",30,TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    ShortLinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS
            );
            ShortLinkState(buildLinkStatsRecordAndSetUser(shortLinkDO.getGid(),fullShortUrl,request,response));
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0;i<originUrls.size();i++) {
            ShortLinkCreatReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreatReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreatRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO shortLinkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .describe(describes.get(i))
                        .originUrl(shortLink.getOriginUrl())
                        .build();
                result.add(shortLinkBaseInfoRespDTO);
            }catch (Exception e){
                log.error("短连接创建失败\n原始短连接:{}\n描述信息:{}\n报错信息:{}",originUrls.get(i),describes.get(i),e.getMessage());
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .baseLinkInfos(result)
                .total(result.size())
                .build();
    }

    private void ShortLinkState(ShortLinkStatsRecordDTO shortLinkStatsRecordDTO){
        rabbitMqHelper.send("shortLink.exchange","shortLink.monitor",shortLinkStatsRecordDTO);
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

    @SneakyThrows
    public String getFavicon(String url){
        URL targetUrl = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        int responseCode = urlConnection.getResponseCode();
        System.out.println(responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = ShortLinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String gid,String fullShortUrl, HttpServletRequest request, HttpServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = request.getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            response.addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:state:uv:" + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:state:uv:" + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = GetMessageUtils.getClientIpAddress(request);
        String os = GetMessageUtils.getOs(request);
        String browser = GetMessageUtils.getBrowser(request);
        String device = GetMessageUtils.getDevice(request);
        String network = GetMessageUtils.getNetwork(request);
        Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:state:uip:" + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        String key = UUID.randomUUID().toString() + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().setIfAbsent("short-link:monitor:" + key , "",10,TimeUnit.SECONDS);
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .keys(key)
                .browser(browser)
                .device(device)
                .network(network)
                .currentDate(new Date())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMonitor(ShortLinkStatsRecordDTO uv){
        log.info("消费者收到了消息:{}", uv);
        Boolean b = stringRedisTemplate.hasKey("short-link:monitor:" + uv.getKeys());
        if(Boolean.FALSE.equals(b)){
            return;
        }
        Long isIpAdd = stringRedisTemplate.opsForSet().add("short-link:state:uip:" + uv.getFullShortUrl(), uv.getRemoteAddr());
        Boolean uipFirstFlag = isIpAdd != null && isIpAdd > 0;
        if (StrUtil.isBlank(uv.getGid())) {
            QueryWrapper<ShortLinkGotoDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ShortLinkGotoDO::getFullShortUrl, uv.getFullShortUrl());
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
            uv.setGid(shortLinkGotoDO.getGid());
        }
        int hour = DateUtil.hour(new Date(), true);
        int week = LocalDate.now().getDayOfWeek().getValue();
        ShortLinkAccessStatsDO shortLinkAccessStatsDO = ShortLinkAccessStatsDO.builder()
                .fullShortUrl(uv.getFullShortUrl())
                .gid(uv.getGid())
                .pv(1)
                .uip(uv.getUipFirstFlag() ? 1 : 0)
                .uv(uv.getUvFirstFlag() ? 1 : 0)
                .hour(hour)
                .weekday(week)
                .date(new Date())
                .build();
        shortLinkAccessStateMapper.shortLinkState(shortLinkAccessStatsDO);
        Map<String, Object> localeMap = new HashMap<>();
        localeMap.put("key", shortLinkStateKey);
        localeMap.put("ip", uv.getRemoteAddr());
        String resultMap = HttpUtil.get(ShortLinkConstant.GAODE_IP_SERVICE, localeMap);
        JSONObject jsonObject = JSON.parseObject(resultMap);
        String infocode = jsonObject.getString("infocode");
        if (StrUtil.isNotBlank(infocode) && "10000".equals(infocode)) {
            // 基本对象获取
            String fullShortUrl = uv.getFullShortUrl();
            String gid = uv.getGid();
            String device = uv.getDevice();
            String os = uv.getOs();
            String browser = uv.getBrowser();
            String network = uv.getNetwork();
            String remoteAddr = uv.getRemoteAddr();
            // 根据 ip 获取地区信息
            String province = jsonObject.getString("province");
            String city = jsonObject.getString("city");
            String adcode = jsonObject.getString("adcode");
            ShortLinkLocaleStatsDO shortLinkLocaleStats = ShortLinkLocaleStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .province(province = StrUtil.equals(province, "[]") ? "未知" : province)
                    .city(city = StrUtil.equals(city, "[]") ? "未知" : city)
                    .adcode(StrUtil.equals(adcode, "[]") ? "未知" : adcode)
                    .cnt(1)
                    .country("中国")
                    .gid(gid)
                    .date(new Date())
                    .build();
            shortLinkLocaleStateMapper.shortLinkState(shortLinkLocaleStats);
            // 获取操作系统
            ShortLinkOsStatsDO shortLinkOsStatsDO = ShortLinkOsStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .os(os)
                    .date(new Date())
                    .cnt(1)
                    .build();
            shortLinkOsStatsMapper.shortLinkState(shortLinkOsStatsDO);
            // 获取浏览器类型
            ShortLinkBrowserStatsDO shortLinkBrowserStats = ShortLinkBrowserStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .browser(browser)
                    .cnt(1)
                    .date(new Date())
                    .build();
            shortLinkBrowserStateMapper.shortLinkState(shortLinkBrowserStats);
            // 短连接访问设备统计
            ShortLinkDeviceStatsDO shortLinkDeviceStatsDO = ShortLinkDeviceStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .cnt(1)
                    .date(new Date())
                    .device(device)
                    .build();
            shortLinkDeviceStatsMapper.shortLinkState(shortLinkDeviceStatsDO);
            // 统计用户访问的网络类型
            ShortLinkNetworkStatsDO shortLinkNetworkStatsDO = ShortLinkNetworkStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .network(network)
                    .cnt(1)
                    .date(new Date())
                    .build();
            shortLinkNetworkStatsMapper.shortLinkState(shortLinkNetworkStatsDO);
            // 高频访问 ip
            ShortLinkAccessLogsDO shortLinkAccessLogsDO = ShortLinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .device(device)
                    .network(network)
                    .locale(String.join("-", "中国", province, city))
                    .gid(gid)
                    .ip(remoteAddr)
                    .os(os)
                    .browser(browser)
                    .user(uv.getUv())
                    .build();
            shortLinkAccessLogsMapper.insert(shortLinkAccessLogsDO);
            // 更新历史访问记录
            shortLinkMapper.incrementStats(gid, fullShortUrl, 1, uv.getUvFirstFlag()? 1 : 0, uv.getUipFirstFlag() ? 1 : 0);
            // 更新今日访问记录
            ShortLinkStatsTodayDO shortLinkStatsTodayDO = ShortLinkStatsTodayDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .todayUv(uv.getUvFirstFlag() ? 1 : 0)
                    .todayPv(1)
                    .todayUip(uipFirstFlag ? 1 : 0)
                    .build();
            shortLinkStatsTodayMapper.shortLinkState(shortLinkStatsTodayDO);
            stringRedisTemplate.delete("short-link:monitor:" + uv.getKeys());
        }
    }
}
