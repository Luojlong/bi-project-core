package com.jl.springbootinit.service;

import com.jl.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 99367
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-12-25 16:07:42
*/
public interface ChartService extends IService<Chart> {

    String getChartTypeToCN(String type);

}
