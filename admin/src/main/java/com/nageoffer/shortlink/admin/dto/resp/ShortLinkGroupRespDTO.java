package com.nageoffer.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 短连接分组返回实体类
 */
@Data
public class ShortLinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;

    /**
     * 该分组下短连接数量
     */
    private Long shortLinkCount;
}
