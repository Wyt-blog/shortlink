package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkLocaleStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShortLinkLocaleStatsMapper extends BaseMapper<ShortLinkLocaleStatsDO>{

    @Insert("INSERT INTO t_link_locale_stats (full_short_url, gid, date, cnt, province, city, adcode, country,create_time,update_time,del_flag)\n" +
            "VALUES (#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.cnt},#{v1.province},#{v1.city},#{v1.adcode},#{v1.country},NOW(),NOW(),0)\n" +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{v1.cnt};")
    void shortLinkState(@Param("v1") ShortLinkLocaleStatsDO shortLinkLocaleStats);

    @Select("select province,sum(cnt) as cnt from t_link_locale_stats where gid = #{v1.gid} and full_short_url = #{v1.fullShortUrl} and create_time\n" +
            "    between #{v1.startDate} and #{v1.endDate} group by gid,full_short_url,province")
    List<ShortLinkLocaleStatsDO> listLocaleByShortLink(@Param("v1") ShortLinkStatsReqDTO requestParam);

    @Select("SELECT " +
            "    province, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_locale_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, province;")
    List<ShortLinkLocaleStatsDO> listLocaleByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
