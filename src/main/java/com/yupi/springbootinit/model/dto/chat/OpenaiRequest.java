package com.yupi.springbootinit.model.dto.chat;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class OpenaiRequest {

    private String model;
    private List<Message> messages;

    public OpenaiRequest(String model,String prompt){
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("你是一个专业的数据分析师", prompt));
    }

}
