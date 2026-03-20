package com.sc.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传响应
 */
@Data
@ApiModel(description = "文件上传结果")
public class OssVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原始文件名", example = "photo.jpg")
    private String fileName;

    @ApiModelProperty(value = "文件访问URL", example = "https://oss.example.com/bucket/2026/03/xxx.jpg")
    private String url;

    @ApiModelProperty(value = "对象存储Key", example = "2026/03/19/abc123.jpg")
    private String objectKey;

    @ApiModelProperty(value = "文件大小（字节）", example = "102400")
    private Long size;

    @ApiModelProperty(value = "内容类型", example = "image/jpeg")
    private String contentType;
}
