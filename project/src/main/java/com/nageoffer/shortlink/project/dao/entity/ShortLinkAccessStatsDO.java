package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.project.common.database.BaseDo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("t_link_access_stats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkAccessStatsDO extends BaseDo {

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 完整短链接
    */
    private String fullShortUrl;

    /**
    * 日期
    */
    private Date date;

    /**
    * 访问量
    */
    private Integer pv;

    /**
    * 独立访问数
    */
    private Integer uv;

    /**
    * 独立IP数
    */
    private Integer uip;

    /**
    * 小时
    */
    private Integer hour;

    /**
    * 星期
    */
    private Integer weekday;
}