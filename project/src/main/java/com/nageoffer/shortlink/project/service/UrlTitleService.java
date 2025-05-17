package com.nageoffer.shortlink.project.service;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * 获取网站标题接口
 */
public interface UrlTitleService {

    /**
     * 通过网站链接获取网站标题
     *
     * @param url 目标网站url
     */
    String getTitleByUrl (String url) throws IOException;

}
