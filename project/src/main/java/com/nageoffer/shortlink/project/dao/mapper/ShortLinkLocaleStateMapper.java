package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkLocaleStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkLocaleStateMapper extends BaseMapper<ShortLinkLocaleStats>{

    @Insert("INSERT INTO t_link_locale_stats (full_short_url, gid, date, cnt, province, city, adcode, country,create_time,update_time,del_flag)\n" +
            "VALUES (#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.cnt},#{v1.province},#{v1.city},#{v1.adcode},#{v1.country},NOW(),NOW(),0)\n" +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{v1.cnt};")
    void shortLinkState(@Param("v1") ShortLinkLocaleStats shortLinkLocaleStats);

}
