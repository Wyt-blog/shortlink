package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 短连接访问
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkAccessController {

    final ShortLinkService shortLinkService;

    /**
     * 短连接跳转
     */
    @GetMapping("{short-link}")
    public void restoreUrl(@PathVariable("short-link") String shortLink, HttpServletRequest request, HttpServletResponse response) throws IOException {
        shortLinkService.restoreUrl(shortLink,request,response);
    }

}
