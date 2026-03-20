package com.sc.system.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.sc.common.core.domain.PageResult;
import com.sc.common.core.domain.R;
import com.sc.common.core.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sc.system.domain.dto.XxlJobSaveDTO;
import com.sc.system.domain.vo.XxlJobInfoVO;
import com.sc.system.domain.vo.XxlJobLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XXL-JOB Admin REST API 客户端
 * <p>
 * 封装对 xxl-job-admin 的 HTTP 调用，提供任务管理和日志查询能力。
 * 先通过 /login 接口获取 Cookie，后续请求携带 Cookie 进行认证。
 * </p>
 */
@Slf4j
@Component
public class XxlJobAdminClient {

    @Value("${xxl.job.admin.addresses:}")
    private String adminAddresses;

    @Value("${xxl.job.admin.username:admin}")
    private String username;

    @Value("${xxl.job.admin.password:123456}")
    private String password;

    /**
     * 登录 Cookie 缓存
     */
    private volatile String loginCookie;

    // ========================= 任务管理 =========================

    /**
     * 分页查询任务列表
     */
    public PageResult<XxlJobInfoVO> pageList(int jobGroup, int triggerStatus,
                                             String jobDesc, String executorHandler,
                                             String author, int start, int length) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobGroup", jobGroup);
        params.put("triggerStatus", triggerStatus);
        params.put("jobDesc", jobDesc != null ? jobDesc : "");
        params.put("executorHandler", executorHandler != null ? executorHandler : "");
        params.put("author", author != null ? author : "");
        params.put("start", start);
        params.put("length", length);

        String responseBody = doPost("/jobinfo/pageList", params);
        JsonNode root = validatePageResponse(responseBody, "/jobinfo/pageList");

        int recordsTotal = root.path("recordsTotal").asInt(0);
        List<XxlJobInfoVO> data = JsonUtils.parseObject(
                root.path("data").toString(),
                new TypeReference<List<XxlJobInfoVO>>() {
                });

        int pageNo = length > 0 ? (start / length + 1) : 1;
        return new PageResult<>(recordsTotal,
                data != null ? data : Collections.<XxlJobInfoVO>emptyList(),
                pageNo, length);
    }

    /**
     * 新增任务
     *
     * @return 任务 ID
     */
    public R<Integer> addJob(XxlJobSaveDTO dto) {
        Map<String, Object> params = buildJobParams(dto);
        String responseBody = doPost("/jobinfo/add", params);
        return parseAdminResponse(responseBody);
    }

    /**
     * 更新任务
     */
    public R<Void> updateJob(XxlJobSaveDTO dto) {
        Map<String, Object> params = buildJobParams(dto);
        params.put("id", dto.getId());
        String responseBody = doPost("/jobinfo/update", params);
        return parseAdminVoidResponse(responseBody);
    }

    /**
     * 删除任务
     */
    public R<Void> removeJob(int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        String responseBody = doPost("/jobinfo/remove", params);
        return parseAdminVoidResponse(responseBody);
    }

    /**
     * 启动任务
     */
    public R<Void> startJob(int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        String responseBody = doPost("/jobinfo/start", params);
        return parseAdminVoidResponse(responseBody);
    }

    /**
     * 停止任务
     */
    public R<Void> stopJob(int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        String responseBody = doPost("/jobinfo/stop", params);
        return parseAdminVoidResponse(responseBody);
    }

    /**
     * 手动触发一次任务
     */
    public R<Void> triggerJob(int id, String executorParam) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("executorParam", executorParam != null ? executorParam : "");
        String responseBody = doPost("/jobinfo/trigger", params);
        return parseAdminVoidResponse(responseBody);
    }

    // ========================= 执行日志 =========================

    /**
     * 分页查询执行日志
     */
    public PageResult<XxlJobLogVO> logPageList(int jobGroup, int jobId,
                                               int logStatus, int start, int length) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobGroup", jobGroup);
        params.put("jobId", jobId);
        params.put("logStatus", logStatus);
        params.put("start", start);
        params.put("length", length);

        String responseBody = doPost("/joblog/pageList", params);
        JsonNode root = validatePageResponse(responseBody, "/joblog/pageList");

        int recordsTotal = root.path("recordsTotal").asInt(0);
        List<XxlJobLogVO> data = JsonUtils.parseObject(
                root.path("data").toString(),
                new TypeReference<List<XxlJobLogVO>>() {
                });

        int pageNo = length > 0 ? (start / length + 1) : 1;
        return new PageResult<>(recordsTotal,
                data != null ? data : Collections.<XxlJobLogVO>emptyList(),
                pageNo, length);
    }

    /**
     * 查看执行日志详情
     */
    public R<String> logDetailCat(long logId, int fromLineNum) {
        Map<String, Object> params = new HashMap<>();
        params.put("logId", logId);
        params.put("fromLineNum", fromLineNum);
        String responseBody = doPost("/joblog/logDetailCat", params);
        JsonNode root = JsonUtils.parseTree(responseBody);
        int code = root.path("code").asInt(-1);
        if (code == 200) {
            JsonNode content = root.path("content");
            return R.ok(content.path("logContent").asText(""));
        }
        return R.fail(root.path("msg").asText("查询日志失败"));
    }

    // ========================= 内部方法 =========================

    private Map<String, Object> buildJobParams(XxlJobSaveDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("jobGroup", dto.getJobGroup());
        params.put("jobDesc", dto.getJobDesc());
        params.put("author", dto.getAuthor());
        params.put("alarmEmail", dto.getAlarmEmail() != null ? dto.getAlarmEmail() : "");
        params.put("scheduleType", dto.getScheduleType());
        params.put("scheduleConf", dto.getScheduleConf());
        params.put("glueType", dto.getGlueType() != null ? dto.getGlueType() : "BEAN");
        params.put("executorHandler", dto.getExecutorHandler());
        params.put("executorParam", dto.getExecutorParam() != null ? dto.getExecutorParam() : "");
        params.put("executorRouteStrategy", dto.getExecutorRouteStrategy() != null ? dto.getExecutorRouteStrategy() : "FIRST");
        params.put("executorBlockStrategy", dto.getExecutorBlockStrategy() != null ? dto.getExecutorBlockStrategy() : "SERIAL_EXECUTION");
        params.put("executorTimeout", dto.getExecutorTimeout() != null ? dto.getExecutorTimeout() : 0);
        params.put("executorFailRetryCount", dto.getExecutorFailRetryCount() != null ? dto.getExecutorFailRetryCount() : 0);
        params.put("misfireStrategy", "DO_NOTHING");
        params.put("glueRemark", "GLUE代码初始化");
        return params;
    }

    /**
     * 发送 POST 请求到 XXL-JOB Admin
     */
    private String doPost(String path, Map<String, Object> params) {
        String url = getAdminUrl() + path;
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .cookie(getCookie())
                    .timeout(10000)
                    .execute();

            // Cookie 过期，重新登录并重试
            if (response.getStatus() == 302 || response.body().contains("login")) {
                loginCookie = null;
                response = HttpRequest.post(url)
                        .form(params)
                        .cookie(getCookie())
                        .timeout(10000)
                        .execute();
            }

            return response.body();
        } catch (Exception e) {
            log.error("调用 XXL-JOB Admin 失败, url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("调用 XXL-JOB Admin 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取登录 Cookie（懒加载 + 简单缓存）
     */
    private String getCookie() {
        if (StrUtil.isNotBlank(loginCookie)) {
            return loginCookie;
        }
        synchronized (this) {
            if (StrUtil.isNotBlank(loginCookie)) {
                return loginCookie;
            }
            String url = getAdminUrl() + "/login";
            Map<String, Object> params = new HashMap<>();
            params.put("userName", username);
            params.put("password", password);

            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .timeout(10000)
                    .execute();

            List<String> cookies = response.headerList("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                // 取第一个 Cookie（XXL_JOB_LOGIN_IDENTITY）
                loginCookie = cookies.get(0);
                log.info("XXL-JOB Admin 登录成功");
            } else {
                log.warn("XXL-JOB Admin 登录未获取到 Cookie, response={}", response.body());
                loginCookie = "";
            }
            return loginCookie;
        }
    }

    private String getAdminUrl() {
        if (StrUtil.isBlank(adminAddresses)) {
            throw new RuntimeException("XXL-JOB Admin 地址未配置（xxl.job.admin.addresses）");
        }
        // 支持多地址，取第一个
        String address = adminAddresses.split(",")[0].trim();
        // 移除末尾斜杠
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }
        return address;
    }

    /**
     * 校验分页接口的返回体合法性
     * <p>
     * XXL-JOB Admin 的分页接口返回格式为 {recordsTotal, recordsFiltered, data:[...]}，
     * 没有标准的 code 字段。当登录失效或 Admin 异常时，可能返回 HTML 登录页或非 JSON 内容。
     * 此方法检查：
     * <ol>
     *   <li>返回体是否为空或 null</li>
     *   <li>返回体是否可以解析为 JSON</li>
     *   <li>JSON 中是否包含 data 字段（分页结构标志）</li>
     * </ol>
     * </p>
     *
     * @param responseBody 原始响应体
     * @param path         请求路径（用于错误日志）
     * @return 解析后的 JsonNode
     * @throws RuntimeException 校验失败时抛出
     */
    private JsonNode validatePageResponse(String responseBody, String path) {
        if (StrUtil.isBlank(responseBody)) {
            throw new RuntimeException("XXL-JOB Admin 返回为空, path=" + path);
        }

        JsonNode root;
        try {
            root = JsonUtils.parseTree(responseBody);
        } catch (Exception e) {
            log.error("XXL-JOB Admin 返回非 JSON, path={}, body={}", path,
                    StrUtil.sub(responseBody, 0, 200));
            throw new RuntimeException("XXL-JOB Admin 返回异常（可能登录失效或服务不可用）, path=" + path, e);
        }

        if (root == null || root.isMissingNode()) {
            throw new RuntimeException("XXL-JOB Admin 返回解析为空, path=" + path);
        }

        // 分页接口正常时必定包含 data 数组字段
        if (!root.has("data")) {
            log.error("XXL-JOB Admin 返回缺少 data 字段, path={}, body={}", path,
                    StrUtil.sub(responseBody, 0, 200));
            // 检查是否包含 code 字段（某些版本/接口可能返回 {code:500, msg:...}）
            if (root.has("code")) {
                int code = root.path("code").asInt(-1);
                String msg = root.path("msg").asText("未知错误");
                throw new RuntimeException("XXL-JOB Admin 请求失败, path=" + path + ", code=" + code + ", msg=" + msg);
            }
            throw new RuntimeException("XXL-JOB Admin 返回格式异常, path=" + path);
        }

        return root;
    }

    private R<Integer> parseAdminResponse(String responseBody) {
        JsonNode root = JsonUtils.parseTree(responseBody);
        int code = root.path("code").asInt(-1);
        if (code == 200) {
            String content = root.path("content").asText("0");
            return R.ok(Integer.parseInt(content));
        }
        return R.fail(root.path("msg").asText("操作失败"));
    }

    private R<Void> parseAdminVoidResponse(String responseBody) {
        JsonNode root = JsonUtils.parseTree(responseBody);
        int code = root.path("code").asInt(-1);
        if (code == 200) {
            return R.ok();
        }
        return R.fail(root.path("msg").asText("操作失败"));
    }
}
