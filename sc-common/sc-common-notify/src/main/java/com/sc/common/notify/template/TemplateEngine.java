package com.sc.common.notify.template;

import java.util.Map;

/**
 * 简易消息模板引擎
 * <p>
 * 支持 ${variableName} 格式的变量替换。
 * 如需更复杂的模板引擎（如 Freemarker / Thymeleaf），可替换此实现。
 * </p>
 */
public final class TemplateEngine {

    private TemplateEngine() {
    }

    /**
     * 解析模板内容，将 ${key} 替换为 params 中对应的值
     *
     * @param template 模板内容，如 "你好，${name}，你的验证码是 ${code}"
     * @param params   变量映射
     * @return 替换后的内容
     */
    public static String render(String template, Map<String, String> params) {
        if (template == null || params == null || params.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}
