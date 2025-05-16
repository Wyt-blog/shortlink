package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    void listGroupShortLinkCount(List<String> requestParam);
}
