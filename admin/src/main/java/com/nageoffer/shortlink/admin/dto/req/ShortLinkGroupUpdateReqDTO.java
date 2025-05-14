package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 修改分组名参数
 */
@Data
public class ShortLinkGroupUpdateReqDTO {

    private String gid;

    private String name;

}
