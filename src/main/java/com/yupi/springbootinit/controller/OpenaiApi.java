package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.model.dto.chat.OpenaiRequest;
import com.yupi.springbootinit.model.dto.chat.OpenaiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenaiApi {

    @Autowired
    private static RestTemplate openaiRestTemplate;

    @Value("${openai.api.url}")
    private static String openaiUrl;

    @Value("${openai.model}")
    private static String model;
    /**
     * AI 对话（需要自己创建请求响应对象）
     *
     * @param prompt
     * @return
     */
//    public static OpenaiResponse doChat(OpenaiRequest request, String openaiApiKey){
//        if (StringUtils.isBlank(openaiApiKey)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未传入openaiKey");
//        }
//        String url = "https://api.openai.com/v1/chat/completions";
//        String json = JSONUtil.toJsonStr(request);
//        String result = HttpRequest.post(url)
//                .header("Authorization", "Bearer" + openaiApiKey)
//                .body(json)
//                .execute().body();
//            return JSONUtil.toBean(result, creatChatCompletionResponse.class);
//    }
    public static String doChat(@RequestParam("prompt") String prompt){
        OpenaiRequest request = new OpenaiRequest(model, prompt);
        OpenaiResponse response = openaiRestTemplate.postForObject(openaiUrl, request, OpenaiResponse.class);
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty())
            return "分析失败";
        return response.getChoices().get(0).getMessage().getPrompt();
    }

    public static void main(String[] args) {

    }


}
