package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短连接排序传参实体类
 */
@Data
public class ShortLinkGroupSortReqDTO {

    /**
     * 排序短连接组 gid
     */
    private String gid;

    /**
     * 排序字段
     */
    private Integer sortOrder;

}
