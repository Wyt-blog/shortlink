package com.nageoffer.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkGroupCountQueryRespDTO {

    /**
     * 分组 id
     */
    private String gid;

    /**
     * 分组下短连接数量
     */
    private Long shortLinkCount;

}
