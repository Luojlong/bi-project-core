package com.jl.springbootinit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.jl.springbootinit.service.OpenaiService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.theokanning.openai.service.OpenAiService.*;

@Service
public class OpenaiServiceImpl implements OpenaiService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String model;

//    @Value("${proxy.host}")
//    private String proxyHost;
//
//    @Value("${proxy.port}")
//    private int proxyPort;


    // 超时时间
    private static final Duration TIMEOUT = Duration.ofSeconds(220L);

    // 理论最大处理数据条数，处理时间约为30s
    public static final Integer SYNCHRO_MAX_TOKEN = 340;

    // 设置重试，重试次数2次，重试间隔2s
    private final Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
            .retryIfResult(result->(!isValidResult(result)))
            .withStopStrategy(StopStrategies.stopAfterAttempt(2))
            .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
            .build();

    public String doChat(String userPrompt) {
        ObjectMapper mapper = defaultObjectMapper();
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        OkHttpClient client = defaultClient(openaiApiKey, TIMEOUT)
                .newBuilder()
//                .proxy(proxy)
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        OpenAiService service = new OpenAiService(api);
        List<ChatMessage> messages = new ArrayList<>();
        final String systemPrompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" + "{数据分析的需求或者目标}\n" +
                "原始数据：\n" + "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容,其中包括生成分隔符\"【【【【【\"，同时分析结论请直接给出（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象js代码(json格式)，代码需要包括title.text（需要该图的名称）部分、图例部分（即legend元素，文字部分应为黑色，图例线颜色与图例颜色相同），合理地将数据进行可视化，图表要求：1、若图表有轴线请将轴线画出，如y轴线，颜色为黑色2、坐标字体为黑色，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{请直接明确的数据分析结论、越详细越好（字数越多越好），不要生成多余的注释}";
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), systemPrompt);
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), userPrompt);
        messages.add(systemMessage);
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(model)
                .messages(messages)
                .build();
        ChatMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
        return responseMessage.getContent();
    }

    @Override
    public String doChatWithRetry(String userPrompt) throws Exception {
        try {
            Callable<String> callable = () -> {
                // 在这里调用你的doChat方法
                return doChat(userPrompt);
            };
            return retryer.call(callable);
        } catch (RetryException e) {
            throw new Exception("重试失败", e);
        }
    }

    /**
     * 分析结果是否存在错误
     * @param result
     * @return
     */
    private boolean isValidResult(String result) {
        String[] splits = result.split("【【【【【");
        if (splits.length < 3)
            return false;
        String genChart = splits[1].trim();
        try {
            JsonObject chartJson = JsonParser.parseString(genChart).getAsJsonObject();
            // 检查是否存在 "title" 字段
            if (!chartJson.has("title")) {
                return false;
            }
            // 检查 "title" 字段的内容是否为空或不含 "text" 字段
            JsonElement titleElement = chartJson.getAsJsonObject("title").get("text");
            if (titleElement == null || titleElement.isJsonNull()) {
                return false;
            }
            String titleText = titleElement.getAsString();
            if (titleText.isEmpty()) {
                return false;
            }
        } catch (JsonSyntaxException e) {
            // Json解析异常，直接返回 false
            return false;
        }
        return true;
    }

}
