package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("t_link_os_stats")
public class ShortLinkOsStatsDO extends BaseDo {
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
    * 操作系统
    */
    private String os;
}