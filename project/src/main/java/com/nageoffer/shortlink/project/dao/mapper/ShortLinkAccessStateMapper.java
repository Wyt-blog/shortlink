package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkAccessStateMapper extends BaseMapper<ShortLinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)\n" +
            "VALUES(#{v1.fullShortUrl},#{v1.gid},#{v1.date},#{v1.pv},#{v1.uv},#{v1.uip},#{v1.hour},#{v1.weekday},NOW(),NOW(),0)\n" +
            "ON DUPLICATE KEY UPDATE pv = pv + #{v1.pv},uv = uv + ${v1.uv},uip = uip + #{v1.uip};")
    void shortLinkState(@Param("v1") ShortLinkAccessStatsDO shortLinkAccessStatsDO);

}
