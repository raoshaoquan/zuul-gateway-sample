package com.github.rsq.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by raoshaoquan on 2017/11/9.
 */
public class GatewayFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 6;
    }

    protected boolean isIgnoreUrl(RequestContext ctx) {
        String[] urls = {"ping", "health"};
        String uri = ctx.getRequest().getRequestURI();
        for (String s : urls) {
            if (StringUtils.isNotBlank(s) && uri.contains(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        if (isIgnoreUrl(ctx)) {
            return true;
        }

        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }

        try {
            MediaType mediaType = MediaType.valueOf(contentType);
            return MediaType.APPLICATION_JSON.includes(mediaType)
                    || MediaType.APPLICATION_JSON_UTF8.includes(mediaType) ||
                    ctx.getRequest().getContentType().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE) ||
                    ctx.getRequest().getContentType().startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        } catch (InvalidMediaTypeException ex) {
            return false;
        }
    }

    protected String getBody(RequestContext ctx) throws IOException {
        InputStream in = (InputStream) ctx.get(FilterConstants.REQUEST_ENTITY_KEY);
        if (in == null) {
            in = ctx.getRequest().getInputStream();
        }
        String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
        return body;
    }

    /**
     * 重新包装Request对象
     *
     * @param ctx
     * @param body
     * @throws UnsupportedEncodingException
     */
    public void wrapperRequest(RequestContext ctx, String body) throws UnsupportedEncodingException {
        if (StringUtils.isNotBlank(body)) {
            byte[] bytes = body.getBytes("UTF-8");
            ctx.setRequest(new HttpServletRequestWrapper(ctx.getRequest()) {
                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new ServletInputStreamWrapper(bytes);
                }

                @Override
                public int getContentLength() {
                    return bytes.length;
                }

                @Override
                public long getContentLengthLong() {
                    return bytes.length;
                }
            });
        }
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = getCurrentContext();
            if (isIgnoreUrl(ctx)) {
                return null;
            }
            String body = getBody(ctx);
            if (StringUtils.isNotBlank(body)) {
                JSONObject jsonObject = JSON.parseObject(body);
                String token = jsonObject.getString("token");
                String content = jsonObject.getString("content");

                //重新包装Request对象
                wrapperRequest(ctx, content);
            }
            return null;

        } catch (IOException e) {
            rethrowRuntimeException(e);
        }
        return null;
    }
}
