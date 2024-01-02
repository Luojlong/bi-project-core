package com.yupi.springbootinit.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.chat.CreatChatCompletionResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

// key: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
@Service
public class OpenaiApi {
    /**
     * AI 对话（需要自己创建请求响应对象）
     *
     * @param request
     * @param openaiApiKey
     * @return
     */
    public CreatChatCompletionResponse creatChatCompletionResponse(CreatChatCompletionResponse request, String openaiApiKey){
        if (StringUtils.isBlank(openaiApiKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未传入openaiKey");
        }
        String url = "https://api.openai.com/v1/chat/completions";
        String json = JSONUtil.toJsonStr(request);
        String result = HttpRequest.post(url)
                .header("Authorization", "Bearer" + openaiApiKey)
                .body(json)
                .execute().body();
            return JSONUtil.toBean(result, creatChatCompletionResponse.class);
    }

    public static void main(String[] args) {

    }

}
