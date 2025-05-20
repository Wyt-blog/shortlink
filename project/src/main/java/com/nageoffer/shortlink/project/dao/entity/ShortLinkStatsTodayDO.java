package com.nageoffer.shortlink.project.dao.entity;

import com.nageoffer.shortlink.project.common.database.BaseDo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLinkStatsTodayDO extends BaseDo {
    /**
    * ID
    */
    private Long id;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 短链接
    */
    private String fullShortUrl;

    /**
    * 日期
    */
    private Date date;

    /**
    * 今日PV
    */
    private Integer todayPv;

    /**
    * 今日UV
    */
    private Integer todayUv;

    /**
    * 今日IP数
    */
    private Integer todayUip;
}