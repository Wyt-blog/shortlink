package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.project.common.database.BaseDo;
import lombok.*;

import java.util.Date;

@Data
@TableName("t_link_locale_stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLinkLocaleStatsDO extends BaseDo {
    /**
    * 完整短链接
    */
    private String fullShortUrl;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 日期
    */
    private Date date;

    /**
    * 访问量
    */
    private Integer cnt;

    /**
    * 省份名称
    */
    private String province;

    /**
    * 市名称
    */
    private String city;

    /**
    * 城市编码
    */
    private String adcode;

    /**
    * 国家标识
    */
    private String country;
}