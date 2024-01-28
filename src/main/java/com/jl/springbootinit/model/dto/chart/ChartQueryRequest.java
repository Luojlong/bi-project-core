package com.jl.springbootinit.model.dto.chart;

import com.jl.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {

    private Long id;

    /**
     * 分析目标
     */
    private String goal;

    private Long userId;
    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}