package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.MultiPartName;

import org.apache.commons.lang3.ObjectUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.*;

/**
 * Excel 工具类
 *
 */
@Slf4j
public class ExcelUtils {

    public static String excel2Csv(MultipartFile multipartFile) {
        // 读取数据
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处错误", e);
        }
        if (CollUtil.isEmpty(list))
            return "";
        // 转换为csv
        StringBuilder stringBuilder = new StringBuilder();
        // 读取表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        List<String> headerlist = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(StringUtils.join(headerlist,",")).append("\n");
        // 读取数据
        for (int i = 1; i < list.size(); i++){
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,",")).append("\n");
        }
        return stringBuilder.toString();
    }


}
