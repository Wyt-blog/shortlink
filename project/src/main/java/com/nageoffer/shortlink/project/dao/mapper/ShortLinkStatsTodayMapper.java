package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkStatsTodayMapper extends BaseMapper<ShortLinkStatsTodayDO> {

    @Insert("INSERT INTO t_link_stats_today (full_short_url, gid, date, today_pv,today_uip,today_uv,create_time ,update_time, del_flag) \n" +
            "VALUES (#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.todayPv},#{v1.todayUip},#{v1.todayUv},NOW(),NOW(),0)\n" +
            "ON DUPLICATE KEY UPDATE today_pv = today_pv +  #{v1.todayPv},today_uv = today_uv + #{v1.todayUv},today_uip = today_uip + #{v1.todayUip}")
    void shortLinkState(@Param("v1") ShortLinkStatsTodayDO shortLinkStatsTodayDO);

}
