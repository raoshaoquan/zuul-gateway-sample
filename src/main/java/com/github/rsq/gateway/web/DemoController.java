package com.github.rsq.gateway.web;

import com.github.rsq.gateway.event.RefreshRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by xujingfeng on 2017/4/1.
 */
@RestController
public class DemoController {

    @Autowired
    RefreshRouteService refreshRouteService;

    @Autowired
    ZuulHandlerMapping zuulHandlerMapping;

    @RequestMapping("/refreshRoute")
    public String refreshRoute(){
        refreshRouteService.refreshRoute();
        return "refreshRoute";
    }

    @RequestMapping("/watchNowRoute")
    public String watchNowRoute(){
        //可以用debug模式看里面具体是什么
        Map<String, Object> handlerMap = zuulHandlerMapping.getHandlerMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : handlerMap.entrySet()) {
            if (entry.getValue() instanceof ZuulController) {
                ZuulController route = (ZuulController) entry.getValue();
                sb.append(entry.getKey()).append("=").append(route.toString()).append("\n");
            }
        }
        return sb.toString();
    }



}
