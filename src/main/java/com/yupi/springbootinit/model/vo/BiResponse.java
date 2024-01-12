package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * bi 返回结果
 */
@Data
public class BiResponse {
    /**
     * 生成的图表json代码
     */
    private String genChart;

    /**
     * 生成的分析结果
     */
    private String genResult;

    /**
     * 图标id
     */
    private Long chartId;

}
