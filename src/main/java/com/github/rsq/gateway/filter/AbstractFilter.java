package com.github.rsq.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;

/**
 * Created by raoshaoquan on 2017/11/13.
 */
public abstract class AbstractFilter extends ZuulFilter {

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

}
