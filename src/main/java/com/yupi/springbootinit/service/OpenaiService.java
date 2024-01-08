package com.yupi.springbootinit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
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
public class OpenaiService {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private int proxyPort;

    private String systemPrompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容: \n" +
            "分析需求: \n" + "{数据分析的需求或目标}\n" +
            "原始数据: \n" + "{csv格式的原始数据，用,作为分隔符}" +
            "请根据这以上两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释，直接生成代码）" +
            "【【【【【\n" +
            "{前端Echarts V5的option配置对象json代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
            "【【【【【\n" +
            "{明确的数据分析结论，越详细越好，不要生成任何多余的内容}";
    // 根据需要配置超时时间
    private static final Duration TIMEOUT = Duration.ofSeconds(30L);

    /**
     * AI 对话（需要自己创建请求响应对象）
     *
     * @param userPrompt
     * @return
     */
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
