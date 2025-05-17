package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@TableName("t_link_goto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLinkGotoDO {

    /**
     * id
     */
    private Long id;

    /**
     * 短连接所在分组gid
     */
    private String gid;

    /**
     * 完整短连接
     */
    private String fullShortUrl;

}
