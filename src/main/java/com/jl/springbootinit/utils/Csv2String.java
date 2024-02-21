package com.jl.springbootinit.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Csv2String {

    public static String MultipartFileToString(MultipartFile multipartFile) {
        InputStreamReader isr;
        BufferedReader br;
        StringBuilder txtResult = new StringBuilder();
        try {
            isr = new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8);
            br = new BufferedReader(isr);
            String lineTxt;
            while ((lineTxt = br.readLine()) != null) {
                txtResult.append(lineTxt);
            }
            isr.close();
            br.close();
            return txtResult.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
