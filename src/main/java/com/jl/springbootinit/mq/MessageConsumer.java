package com.jl.springbootinit.mq;

import com.google.gson.*;
import com.jl.springbootinit.manager.TaskManager;
import com.jl.springbootinit.service.ScoreService;
import com.rabbitmq.client.Channel;
import com.jl.springbootinit.common.ErrorCode;
import com.jl.springbootinit.exception.BusinessException;
import com.jl.springbootinit.model.entity.Chart;
import com.jl.springbootinit.service.ChartService;
import com.jl.springbootinit.service.OpenaiService;
import com.jl.springbootinit.ws.WebSocketServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.jl.springbootinit.mq.MqConstant.QUEUE_NAME;

@Component
@Slf4j
public class MessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private ScoreService scoreService;

    @Resource
    private OpenaiService openaiService;

    @Resource
    private WebSocketServer webSocketService;

    @Resource
    private TaskManager taskManager;
    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 如果失败，消息拒绝
            channel.basicNack(deliveryTag, false, false);
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        taskManager.startTaskTimer(chartId);
        // 将运行状态更新
        boolean bool = chartService.updateById(updateChart);
        if (!bool) {
            setChartErrorMessage(updateChart.getId(), "图表执行状态保存失败");
            channel.basicNack(deliveryTag, false, false);
        }
        String result = openaiService.doChat(handleUserInput(chart));
        scoreService.deductPoints(chart.getUserId(),1L);
        String[] splits = result.split("【【【【【");
        if (splits.length < 3){
            try {
                retryMessage(chartId, channel, deliveryTag, "AI生成格式错误");
            } catch (BusinessException e) {
                // 如果 retryMessage 抛出 BusinessException，记录错误并确认消息
                log.error("retryMessage 抛出 BusinessException: {}", e.getMessage(), e);
                channel.basicAck(deliveryTag, false);
            }
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // Echarts代码过滤 "var option ="
        if (genChart.startsWith("var option =")) {
            genChart = genChart.replaceFirst("var\\s+option\\s*=\\s*", "");
        }
        Chart updateResult = new Chart();
        updateResult.setId(chartId);
        updateResult.setGenResult(genResult);
        JsonObject chartJson;
        String updatedGenChart = "";
        try {
            chartJson = JsonParser.parseString(genChart).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            retryMessage(chartId, channel, deliveryTag, "图表json代码生成错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "json代码解析异常，将重试");
        }
        // 自动添加图表类型
        if (StringUtils.isEmpty(chart.getChartType())) {
            if (chartJson.has("series") && chartJson.get("series").isJsonArray()) {
                JsonArray seriesArray = chartJson.getAsJsonArray("series");
                if (seriesArray.size() > 0) {
                    JsonObject firstSeries = seriesArray.get(0).getAsJsonObject();
                    if (firstSeries.has("type")) {
                        String typeChart = firstSeries.get("type").getAsString();
                        String CnChartType = chartService.getChartTypeToCN(typeChart);
                        updateResult.setChartType(CnChartType);
                    }
                }
            }
        }
        // 自动加入图表名称结尾并设置图表名称
        if (StringUtils.isEmpty(chart.getName())) {
            JsonElement titleElement = chartJson.getAsJsonObject("title").get("text");
            if (titleElement == null || titleElement.isJsonNull()) {
                try {
                    retryMessage(chartId, channel, deliveryTag, "生成的json代码不存在title元素或无title.text元素");
                } catch (BusinessException e) {
                    // 如果 retryMessage 抛出 BusinessException，记录错误并确认消息
                    log.error("retryMessage 抛出 BusinessException: {}", e.getMessage(), e);
                    channel.basicAck(deliveryTag, false);
                }
            }
            String titleText = titleElement.getAsString();
            if (titleText.isEmpty()) {
                try {
                    retryMessage(chartId, channel, deliveryTag, "生成的json代码不存在text字段");
                } catch (BusinessException e) {
                    // 如果 retryMessage 抛出 BusinessException，记录错误并确认消息
                    log.error("retryMessage 抛出 BusinessException: {}", e.getMessage(), e);
                    channel.basicAck(deliveryTag, false);
                }
            }
            String genChartName = String.valueOf(chartJson.getAsJsonObject("title").get("text"));
            genChartName = genChartName.replace("\"", "");
            if (! genChartName.endsWith("图") && ! genChartName.endsWith("表") && ! genChartName.endsWith("图表"))
                genChartName = genChartName + "图";
            updateResult.setName(genChartName);
        }
        // 加入下载按钮
        JsonObject toolbox = new JsonObject();
        toolbox.addProperty("show", true);
        JsonObject saveAsImage = new JsonObject();
        saveAsImage.addProperty("show", true);
        saveAsImage.addProperty("excludeComponents", "['toolbox']");
        saveAsImage.addProperty("pixelRatio", 2);
        JsonObject feature = new JsonObject();
        feature.add("saveAsImage", saveAsImage);
        toolbox.add("feature", feature);
        chartJson.add("toolbox", toolbox);
        chartJson.remove("title");
        updatedGenChart = chartJson.toString();
        updateResult.setGenChart(updatedGenChart);
        // TODO:枚举值实现
        updateResult.setStatus("succeed");
        boolean code = chartService.updateById(updateResult);
        if (!code){
            setChartErrorMessage(updateResult.getId(), "图表代码保存失败");
            channel.basicNack(deliveryTag, false, false);
        }
        taskManager.clearTaskTimer(chartId);
        webSocketService.sendToAllClient("图表生成好啦，快去看看吧！");
        channel.basicAck(deliveryTag, false);
    }

    private String handleUserInput(Chart chart){
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：\n");
        // 拼接分析目标
        String userGoal = chart.getGoal();
        String chartType = chart.getChartType();

        // 分析输入加入图表类型
        if (StringUtils.isNotBlank(chartType))
            userGoal += ",请使用" + chartType;
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：\n");
        // 压缩数据
        String userData = chart.getChartData();
        userInput.append(userData).append("\n");
        return userInput.toString();
    }

    /**
     * 设置图表错误信息
     * @param chartId
     * @param execMessage
     */
    private void setChartErrorMessage(long chartId, String execMessage){
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            log.error("更新图表失败状态错误" + chartId + ":" + execMessage);
        }
    }

    /**
     *
     * 消息重试
     * @param chartId
     * @param channel
     * @param deliveryTag
     * @param execMessage
     */
    @SneakyThrows
    private void retryMessage(long chartId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, String execMessage){
        Chart chart = chartService.getById(chartId);
        // 超过重试次数
        if (chart.getRetry() > 0) {
            setChartErrorMessage(chartId, execMessage);
            channel.basicNack(deliveryTag, false, false);
        }else {
            Chart updateRetryChart = new Chart();
            updateRetryChart.setId(chartId);
            updateRetryChart.setRetry(chart.getRetry() + 1);
            boolean updateBool = chartService.updateById(updateRetryChart);
            if (!updateBool) {
                setChartErrorMessage(chartId, "图表重试次数保存失败");
                channel.basicNack(deliveryTag, false, false);
            }
            log.info(execMessage + "，将重试");
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
