package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author 99367
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-12-25 16:07:42
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    @Override
    public String getChartTypeToCN(String type) {
        switch (type){
            case "line":
                return "折线图";
            case "bar":
                return "柱状图";
            case "pie":
                return "饼图";
            case "scatter":
                return "散点图";
            case "radar":
                return "雷达图";
            case "map":
                return "地图";
            case "candlestick":
                return "K线图";
            case "heatmap":
                return "热力图";
            case "tree":
                return "树图";
            case "lines":
                return "路线图";
            case "graph":
                return "关系图";
            case "sunburst":
                return "旭日图";
            default:
                return "特殊图表";
        }
    }

}




