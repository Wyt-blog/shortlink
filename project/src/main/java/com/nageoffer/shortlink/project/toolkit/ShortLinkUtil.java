package com.nageoffer.shortlink.project.toolkit;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.nageoffer.shortlink.project.common.constants.ShortLinkConstant;

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

}
