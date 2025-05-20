package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessLogsDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsAccessReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkAccessLogsMapper extends BaseMapper<ShortLinkAccessLogsDO> {


    @Select("select ip,count(id) as count from t_link_access_logs where gid = #{v1.gid} and full_short_url = #{v1.fullShortUrl} and create_time \n" +
            "between #{v1.startDate} and #{v1.endDate} group by gid,full_short_url,ip order by count DESC LIMIT 5")
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("v1") ShortLinkStatsReqDTO requestParam);

    @Select("SELECT SUM(old_user) AS oldUserCnt, SUM(new_user) AS newUserCnt " +
            "FROM ( SELECT CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{param.startDate} AND MAX(create_time) <= #{param.endDate} THEN 1 ELSE 0 END AS new_user " +
            "FROM t_link_access_logs WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} " +
            "GROUP BY user ) AS user_counts;")
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    @Select("<script> " +
            "SELECT user, CASE WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' ELSE '老访客' END AS uvType " +
            "FROM t_link_access_logs " +
            "WHERE full_short_url = #{fullShortUrl} AND gid = #{gid} AND user IN " +
            "<foreach item='item' index='index' collection='userList' open='(' separator=',' close=')'> " +
            "    #{item} " +
            "</foreach> " +
            "GROUP BY user;" +
            "</script>"
    )
    List<Map<String, Object>> selectUvTypeByusers(@Param("gid")String gid,
                                                  @Param("fullShortUrl")String fullShortUrl,
                                                  @Param("startDate")String startDate,
                                                  @Param("endDate")String endDate,
                                                  @Param("userList") List<String> userList);

    @Select("SELECT " +
            "    ip, " +
            "    COUNT(ip) AS count " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND create_time BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, ip " +
            "ORDER BY " +
            "    count DESC " +
            "LIMIT 5;")
    List<HashMap<String, Object>> listTopIpByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("SELECT " +
            "    COUNT(user) AS pv, " +
            "    COUNT(DISTINCT user) AS uv, " +
            "    COUNT(DISTINCT ip) AS uip " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND create_time BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid;")
    ShortLinkAccessStatsDO findPvUvUidStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>"
    )
    List<Map<String, Object>> selectGroupUvTypeByUsers(
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userAccessLogsList") List<String> userAccessLogsList
    );
}
