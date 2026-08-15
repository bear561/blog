package com.blog.controller;

import com.blog.service.RssService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RssController {

    private final RssService rssService;

    @GetMapping(value = "/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss(HttpServletRequest request) {
        // 从请求构建真实站点地址（nginx 反代时走 X-Forwarded-Proto），避免 RSS 链接写死 localhost
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.isEmpty()) {
            proto = request.getScheme();
        }
        String host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            host = request.getServerName();
        }
        return rssService.generateRssFeed(proto + "://" + host);
    }
}
