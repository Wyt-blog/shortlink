package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkBrowserStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkBrowserStateMapper extends BaseMapper<ShortLinkBrowserStats> {

    @Insert("INSERT INTO t_link_browser_stats (full_short_url,gid, date, cnt, browser, create_time, update_time, del_flag) \n" +
            "VALUES (#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.cnt},#{v1.browser},NOW(),NOW(),0)" +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{v1.cnt};")
    void shortLinkState(@Param("v1") ShortLinkBrowserStats shortLinkBrowserStats);

}
