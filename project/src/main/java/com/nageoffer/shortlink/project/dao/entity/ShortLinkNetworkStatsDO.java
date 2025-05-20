package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.project.common.database.BaseDo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @description t_link_network_stats
 * @author BEJSON.com
 * @date 2025-05-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_link_network_stats")
public class ShortLinkNetworkStatsDO extends BaseDo {
    private Long id;
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
    * 访问网络
    */
    private String network;
}