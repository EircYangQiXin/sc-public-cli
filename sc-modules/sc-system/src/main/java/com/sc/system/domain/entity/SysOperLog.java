package com.sc.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_oper_log")
@ApiModel(description = "操作日志")
public class SysOperLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "操作日志ID")
    private Long operId;

    @ApiModelProperty(value = "模块标题")
    private String title;

    @ApiModelProperty(value = "业务类型 (0其他 1新增 2修改 3删除 4授权 5导出 6导入 7清空)")
    private Integer businessType;

    @ApiModelProperty(value = "操作人类型 (0其他 1后台 2移动端)")
    private Integer operatorType;

    @ApiModelProperty(value = "请求方法")
    private String method;

    @ApiModelProperty(value = "请求URL")
    private String operUrl;

    @ApiModelProperty(value = "操作人")
    private String operName;

    @ApiModelProperty(value = "请求参数")
    private String operParam;

    @ApiModelProperty(value = "返回结果")
    private String jsonResult;

    @ApiModelProperty(value = "操作状态 (0正常 1异常)")
    private Integer status;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "操作时间")
    private LocalDateTime operTime;

    @ApiModelProperty(value = "耗时（毫秒）")
    private Long costTime;
}
