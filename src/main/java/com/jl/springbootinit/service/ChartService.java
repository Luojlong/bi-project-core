package com.jl.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jl.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 99367
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-12-25 16:07:42
*/
public interface ChartService extends IService<Chart> {

    /**
     * 图表类型转换为中文
     * @param type
     * @return
     */
    String getChartTypeToCN(String type);

    /**
     * 查询图表分页是否相同
     * @param page1
     * @param page2
     * @return
     */
    boolean isSamePage(Page<Chart> page1, Page<Chart> page2);

    /**
     * 查询图表是否相同
     * @param chart1
     * @param chart2
     * @return
     */
    boolean isSameChart(Chart chart1, Chart chart2);
}
