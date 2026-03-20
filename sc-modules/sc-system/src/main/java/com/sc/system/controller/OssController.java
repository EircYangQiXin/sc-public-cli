package com.sc.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.sc.common.core.domain.R;
import com.sc.common.log.annotation.OperationLog;
import com.sc.common.log.enums.BusinessType;
import com.sc.common.oss.domain.StsTokenVO;
import com.sc.common.oss.template.OssTemplate;
import com.sc.common.redis.annotation.RepeatSubmit;
import com.sc.system.domain.vo.OssVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Api(tags = "文件管理")
@RestController
@RequestMapping("/system/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssTemplate ossTemplate;

    @ApiOperation("上传单个文件")
    @SaCheckPermission("system:oss:upload")
    @OperationLog(title = "文件上传", businessType = BusinessType.OTHER)
    @RepeatSubmit(interval = 3)
    @PostMapping("/upload")
    public R<OssVO> upload(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file) throws IOException {
        OssVO vo = doUpload(file);
        return R.ok(vo);
    }

    @ApiOperation("批量上传文件")
    @SaCheckPermission("system:oss:upload")
    @OperationLog(title = "批量文件上传", businessType = BusinessType.OTHER)
    @RepeatSubmit(interval = 3)
    @PostMapping("/upload/batch")
    public R<List<OssVO>> uploadBatch(
            @ApiParam(value = "文件列表", required = true) @RequestParam("files") MultipartFile[] files) throws IOException {
        List<OssVO> list = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            list.add(doUpload(file));
        }
        return R.ok(list);
    }

    @ApiOperation("获取 STS 临时上传凭证（前端直传）")
    @SaCheckLogin
    @GetMapping("/sts")
    public R<StsTokenVO> getStsToken(
            @ApiParam(value = "上传路径前缀（可选，限制上传目录）", example = "upload/avatar/")
            @RequestParam(required = false) String uploadPath) {
        if (uploadPath == null || uploadPath.isEmpty()) {
            // 默认按日期目录
            uploadPath = "upload/" + java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/";
        }
        StsTokenVO vo = ossTemplate.getStsToken(uploadPath);
        return R.ok(vo);
    }

    @ApiOperation("删除文件")
    @SaCheckPermission("system:oss:remove")
    @OperationLog(title = "文件删除", businessType = BusinessType.DELETE)
    @DeleteMapping
    public R<Void> delete(@ApiParam(value = "对象存储Key", required = true) @RequestParam String objectKey) {
        ossTemplate.delete(objectKey);
        return R.ok();
    }

    /**
     * 执行上传逻辑
     */
    private OssVO doUpload(MultipartFile file) throws IOException {
        // 生成存储路径：年/月/日/UUID.扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectKey = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 上传到 OSS
        String url = ossTemplate.upload(objectKey, file.getInputStream(), file.getContentType());

        // 构建响应
        OssVO vo = new OssVO();
        vo.setFileName(originalName);
        vo.setUrl(url);
        vo.setObjectKey(objectKey);
        vo.setSize(file.getSize());
        vo.setContentType(file.getContentType());
        return vo;
    }
}
