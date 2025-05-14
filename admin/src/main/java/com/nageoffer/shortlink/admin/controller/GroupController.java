package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短连接管理控制层
 */
@RestController
@RequestMapping("/api/short-link/admin/v1/group")
@RequiredArgsConstructor
public class GroupController {

    final GroupService groupService;

    /**
     * 新增短连接分组
     */
    @PostMapping
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParm) {
        groupService.saveGroup(requestParm.getName());
        return Results.success();
    }

    /**
     * 查询分组集合
     */
    @GetMapping
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改分组名
     */
    @PutMapping
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParm) {
        groupService.updateGroup(requestParm);
        return Results.success();
    }

    /**
     * 删除分组
     */
    @DeleteMapping
    public Result<Void> deleteGroup(String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 短连接分组排序
     */
    @PostMapping("sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParm) {
        groupService.sortGroup(requestParm);
        return Results.success();
    }

}
