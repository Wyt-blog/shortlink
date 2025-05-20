package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShortLinkAccessStatsMapper extends BaseMapper<ShortLinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)\n" +
            "VALUES(#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.pv},#{v1.uv},#{v1.uip},#{v1.hour},#{v1.weekday},NOW(),NOW(),0)\n" +
            "ON DUPLICATE KEY UPDATE pv = pv + #{v1.pv},uv = uv + ${v1.uv},uip = uip + #{v1.uip};")
    void shortLinkState(@Param("v1") ShortLinkAccessStatsDO shortLinkAccessStatsDO);

    @Select("SELECT date,sum(pv) as pv,sum(uv) as uv,sum(uip) as uip FROM  t_link_access_stats\n" +
            "WHERE gid = #{v1.gid} AND full_short_url = #{v1.fullShortUrl} AND create_time BETWEEN #{v1.startDate} AND #{v1.endDate}\n" +
            "GROUP BY gid,full_short_url,date")
    List<ShortLinkAccessStatsDO> listStatsByShortLink(@Param("v1") ShortLinkStatsReqDTO requestParam);

    @Select("SELECT hour, SUM(pv) AS pv FROM t_link_access_stats " +
            "WHERE full_short_url = #{v1.fullShortUrl} AND gid = #{v1.gid} AND date BETWEEN #{v1.startDate} and #{v1.endDate} " +
            "GROUP BY full_short_url, gid, hour;")
    List<ShortLinkAccessStatsDO> listHourStatsByShortLink(@Param("v1") ShortLinkStatsReqDTO requestParam);

    @Select("select weekday,sum(pv) as pv from t_link_access_stats where gid = #{v1.gid} and full_short_url = #{v1.fullShortUrl}\n" +
            "and create_time between #{v1.startDate} and #{v1.endDate} group by weekday,full_short_url,gid;")
    List<ShortLinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("v1") ShortLinkStatsReqDTO requestParam);

    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, date;")
    List<ShortLinkAccessStatsDO> listStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, hour;")
    List<ShortLinkAccessStatsDO> listHourStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, weekday;")
    List<ShortLinkAccessStatsDO> listWeekdayStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
