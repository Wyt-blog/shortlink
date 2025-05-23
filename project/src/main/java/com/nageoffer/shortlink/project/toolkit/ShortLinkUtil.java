package com.nageoffer.shortlink.project.toolkit;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.nageoffer.shortlink.project.common.constants.ShortLinkConstant;

import java.net.URI;
import java.util.Date;
import java.util.Optional;

public class ShortLinkUtil {

    public static long getLinkCacheValidDate(Date date) {
        return Optional.ofNullable(date)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(ShortLinkConstant.DEFAULT_CACHE_DATE);
    }

    public static void main(String[] args) {
        Date date = DateUtil.parse("2025-06-12 11:11:11");
        System.out.println(getLinkCacheValidDate(date));
    }

    /**
     * 获取原始链接中的域名
     * 如果原始链接包含 www 开头的话需要去掉
     *
     * @param url 创建或者修改短链接的原始链接
     * @return 原始链接中的域名
     */
    public static String extractDomain(String url) {
        String domain = null;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (StrUtil.isNotBlank(host)) {
                domain = host;
                if (domain.startsWith("www.")) {
                    domain = host.substring(4);
                }
            }
        } catch (Exception ignored) {
        }
        return domain;
    }

}
