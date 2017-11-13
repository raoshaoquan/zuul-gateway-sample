package com.github.rsq.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * Created by raoshaoquan on 2017/11/9.
 */
@Component
public class GatewayPostFilter extends AbstractFilter {

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 99;
    }

    /**
     * 重新包装Response对象
     *
     * @param ctx
     * @throws UnsupportedEncodingException
     */
    public void wrapperResponse(RequestContext ctx) throws IOException {
        JSONObject response = new JSONObject();
        String body = "";
        InputStream in = ctx.getResponseDataStream();
        if (in != null) {
            body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
        }
        String uri = ctx.getRequest().getRequestURI();
        if (uri.contains("search") && StringUtils.isNotBlank(body)) {
            JSONObject jsonObject = JSON.parseObject(body);
            JSONObject content = jsonObject.getJSONObject("data");
            if (content != null) {
                response.put("code", 0);
                response.put("msg", "请求响应成功");
                response.put("content", content);
                ctx.setResponseBody(JSON.toJSONString(response));
            }
        }
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = getCurrentContext();
            if (isIgnoreUrl(ctx)) {
                return null;
            }
            //重新包装Request对象
            wrapperResponse(ctx);
            return null;

        } catch (IOException e) {
            rethrowRuntimeException(e);
        }
        return null;
    }
}
