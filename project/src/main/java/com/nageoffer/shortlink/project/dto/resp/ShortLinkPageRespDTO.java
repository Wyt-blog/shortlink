package com.nageoffer.shortlink.project.dto.resp;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 分页查询返回对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkPageRespDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 点击量
     */
    private Integer clickNum;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    private Integer validDateType;

    /**
     * 历史 pv
     */
    private Integer totalPv;

    /**
     * 历史 Uip
     */
    private Integer totalUip;

    /**
     * 历史 Uv
     */
    private Integer totalUv;

    /**
     * 当天 pv
     */
    private Integer todayPv;

    /**
     * 当天 Uip
     */
    private Integer todayUip;

    /**
     * 当天 Uv
     */
    private Integer todayUv;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date validDate;

    /**
     * 网站图标
     */
    private String favicon;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;
}
