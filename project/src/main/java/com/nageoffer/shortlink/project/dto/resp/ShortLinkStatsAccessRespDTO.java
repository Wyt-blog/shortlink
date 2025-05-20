package com.nageoffer.shortlink.project.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 短链接监控响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsAccessRespDTO {
    /**
     * 访客类型
     */
    private String uvType;
    /**
     * 用户信息
     */
    private String user;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * IP
     */
    private String ip;

    /**
     * 访问设备
     */
    private String device;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 访问地区
     */
    private String locale;

    /**
     * 访问时间
     */
    @JsonFormat(pattern = "yyyy-MM--dd HH::mm::ss",timezone = "GMT+8")
    private Date createTime;
}