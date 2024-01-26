package com.yupi.springbootinit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.yupi.springbootinit.service.OpenaiService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.theokanning.openai.service.OpenAiService.*;

@Service
public class OpenaiServiceImpl implements OpenaiService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private int proxyPort;

    // 根据需要配置超时时间
    private static final Duration TIMEOUT = Duration.ofSeconds(120L);

    public static final Integer SYNCHRO_MAX_TOKEN = 340;

    public String doChat(String userPrompt) {
        ObjectMapper mapper = defaultObjectMapper();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        OkHttpClient client = defaultClient(openaiApiKey,TIMEOUT)
                .newBuilder()
                .proxy(proxy)
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
                "{前端 Echarts V5 的 option 配置对象js代码(json格式)，代码需要包括title.text（需要该图的名称）部分，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
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
        System.out.println(responseMessage);
        return responseMessage.getContent();
    }
}
