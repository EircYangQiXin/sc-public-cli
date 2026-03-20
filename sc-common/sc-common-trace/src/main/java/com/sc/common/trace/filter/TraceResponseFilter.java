package com.sc.common.trace.filter;

import brave.Tracer;
import brave.propagation.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 将 Sleuth traceId 写入 HTTP 响应头 {@code X-Trace-Id}，
 * 便于前端和运维通过 traceId 快速定位问题。
 * <p>
 * 仅在 Servlet (非 WebFlux) 环境下生效，Gateway 通过 GlobalFilter 单独处理。
 */
@RequiredArgsConstructor
public class TraceResponseFilter implements Filter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            if (tracer.currentSpan() != null && response instanceof HttpServletResponse) {
                TraceContext context = tracer.currentSpan().context();
                ((HttpServletResponse) response).setHeader(TRACE_ID_HEADER, context.traceIdString());
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
