package com.jl.springbootinit.manager;
import com.jl.springbootinit.model.entity.Chart;
import com.jl.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 定时任务 4分钟自动设为失败
 */
@Service
@Slf4j
public class TaskManager {
    // 存储任务ID与开始时间的映射
    private Map<Long, Long> taskStartTimeMap = new HashMap<>();

    @Resource
    private ChartService chartService;

    // 开始计时某个任务
    public void startTaskTimer(long taskId) {
        taskStartTimeMap.put(taskId, System.currentTimeMillis());
    }

    // 清除计时某个任务
    public void clearTaskTimer(long taskId) {
        taskStartTimeMap.remove(taskId);
    }

    // 定时任务方法，当任务ID与开始时间的映射不为空时才运行
    @Scheduled(fixedRate = 1000) // 每隔1秒执行一次
    public void checkRunningTasks() {
        // 检查任务ID与开始时间的映射是否为空
        if (!taskStartTimeMap.isEmpty()) {
            // 遍历任务ID与开始时间的映射
            for (Map.Entry<Long, Long> entry : taskStartTimeMap.entrySet()) {
                long taskId = entry.getKey();
                long startTime = entry.getValue();
                long currentTime = System.currentTimeMillis();
                long duration = currentTime - startTime;

                if (duration > 4 * 60 * 1000) {
                    Chart chart = chartService.getById(taskId);
                    if (Objects.equals(chart.getStatus(), "succeed"))
                        clearTaskTimer(taskId);
                    else {
                        Chart update = new Chart();
                        update.setId(taskId);
                        update.setStatus("failed");
                        boolean saveResult = chartService.updateById(update);
                        if (!saveResult)
                            handleChartUpdateError(update.getId(), "图表状态更新失败");
                        clearTaskTimer(taskId);
                    }
                }
            }
        }
    }

    private void handleChartUpdateError(long chartId, String execMessage){
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChart);
        if (!b)
            log.error("更新图表失败状态错误" + chartId + ":" + execMessage);
    }

}