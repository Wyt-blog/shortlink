package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkOsStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

public interface ShortLinkOsStatsMapper extends BaseMapper<ShortLinkOsStatsDO> {

    @Insert("INSERT INTO t_link_os_stats (full_short_url, gid, date, cnt, os, create_time, update_time, del_flag)\n" +
            "VALUES (#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.cnt},#{v1.os},NOW(),NOW(),0)\n" +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{v1.cnt}")
    void shortLinkState (@Param("v1") ShortLinkOsStatsDO shortLinkOsStatsDO);

    @Select("select os,sum(cnt) as count from t_link_os_stats where gid = #{v1.gid} and full_short_url = #{v1.fullShortUrl}\n" +
            "and create_time between #{v1.startDate} and #{v1.endDate} group by os,full_short_url,gid;")
    List<HashMap<String, Object>> listOsStatsByShortLink(@Param("v1") ShortLinkStatsReqDTO requestParam);

    @Select("SELECT " +
            "    os, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_os_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, os;")
    List<HashMap<String, Object>> listOsStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
