package com.yupi.springbootinit.mq;

import com.google.gson.*;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.OpenaiService;
import com.yupi.springbootinit.ws.WebSocketServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.yupi.springbootinit.mq.MqConstant.QUEUE_NAME;

@Component
@Slf4j
public class MessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private OpenaiService openaiService;

    @Resource
    private WebSocketServer webSocketService;
    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            // 如果失败，消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean bool = chartService.updateById(updateChart);
        if (!bool) {
            handleChartUpdateError(updateChart.getId(), "图表执行状态保存失败");
            return;
        }
        String result = openaiService.doChat(handleUserInput(chart));
        String[] splits = result.split("【【【【【");
        if (splits.length < 3){
            // 重新再次生成一次
            if (deliveryTag > 1) {
                channel.basicNack(deliveryTag, false, false);
            }
            channel.basicNack(deliveryTag, false, true);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // Echarts代码过滤 "var option ="
        if (genChart.startsWith("var option =")) {
            // 去除 "var option ="
            genChart = genChart.replaceFirst("var\\s+option\\s*=\\s*", "");
        }
        Chart updateResult = new Chart();
        updateResult.setId(chart.getId());
        updateResult.setGenResult(genResult);
        JsonObject chartJson;
        String genChartName;
        String updatedGenChart = "";
        try {
            chartJson = JsonParser.parseString(genChart).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            // 重新再次生成一次
            if (deliveryTag > 1) {
                channel.basicNack(deliveryTag, false, false);
            }
            channel.basicNack(deliveryTag, false, true);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "json代码解析异常");
        }
        // 自动添加图表类型
        if (StringUtils.isEmpty(chart.getName())) {
            JsonArray seriesArray = chartJson.getAsJsonArray("series");
            for (JsonElement i : seriesArray) {
                String typeChart = i.getAsJsonObject().get("type").getAsString();
                String CnChartType = chartService.getChartTypeToCN(typeChart);
                updateResult.setChartType(CnChartType);
                System.out.println(CnChartType);
            }
        }
        // 自动加入图表名称结尾并设置图表名称
        if (StringUtils.isEmpty(chart.getName())) {
            try {
                genChartName = String.valueOf(chartJson.getAsJsonObject("title").get("text"));
            } catch (JsonSyntaxException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "json代码不存在title字段");
            }
            genChartName = genChartName.replace("\"", "");
            if (!genChartName.endsWith("图") || !genChartName.endsWith("表"))
                genChartName = genChartName + "图";
            System.out.println(genChartName);
            updateResult.setName(genChartName);
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
        }
        updateResult.setGenChart(updatedGenChart);
        // TODO:枚举值实现
        updateResult.setStatus("succeed");
        boolean code = chartService.updateById(updateResult);
        if (!code){
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(updateResult.getId(), "图表代码保存失败");
        }
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
     * 图表错误状态处理
     * @param chartId
     * @param execMessage
     */
    private void handleChartUpdateError(long chartId, String execMessage){
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        webSocketService.sendToAllClient("坏了，分析好像出了点问题 ~~");
        boolean b = chartService.updateById(updateChart);
        if (!b)
            log.error("更新图表失败状态错误" + chartId + ":" + execMessage);
    }
}
