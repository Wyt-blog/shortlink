package com.nageoffer.shortlink.admin.remote.dto.resp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkBaseInfoRespDTO {

    /**
     * 描述信息
     */
    @ExcelProperty("标题")
    @ColumnWidth(40)
    private String describe;

    /**
     * 原始链接
     */
    @ExcelProperty("原始短连接")
    @ColumnWidth(80)
    private String originUrl;

    /**
     * 短链接
     */
    @ExcelProperty("短连接")
    @ColumnWidth(40)
    private String fullShortUrl;
}