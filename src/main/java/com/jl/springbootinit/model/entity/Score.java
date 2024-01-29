package com.jl.springbootinit.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * 积分表
 * @TableName score
 */
@TableName(value ="score")
@Data
public class Score implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 0表示未签到，1表示已签到
     */
    private Integer isSign;

    /**
     * 总积分
     */
    private Long scoreTotal;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
