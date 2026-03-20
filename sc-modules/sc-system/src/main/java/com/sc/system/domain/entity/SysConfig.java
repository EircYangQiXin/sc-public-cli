package com.sc.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sc.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
@ApiModel(description = "系统配置参数")
public class SysConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "参数ID")
    private Long configId;

    @ApiModelProperty(value = "参数名称", example = "验证码开关")
    private String configName;

    @ApiModelProperty(value = "参数键名", example = "sys.captcha.enabled")
    private String configKey;

    @ApiModelProperty(value = "参数键值", example = "true")
    private String configValue;

    @ApiModelProperty(value = "系统内置（Y是 N否）", example = "Y")
    private String configType;
}
