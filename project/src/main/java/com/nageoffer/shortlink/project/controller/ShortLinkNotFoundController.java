package com.nageoffer.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ShortLinkNotFoundController {

    /**
     * 跳转到 404 页面
     */
    @RequestMapping("/page/notfound")
    public String notfound() {
        return "notfound";
    }

}
