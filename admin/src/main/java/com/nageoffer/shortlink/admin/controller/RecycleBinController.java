package com.nageoffer.shortlink.admin.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.remote.ShortLinkFeginRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/short-link/admin/v1/recycle-bin")
@RequiredArgsConstructor
public class RecycleBinController {

    final ShortLinkFeginRemoteService shortLinkFeginRemoteService;

    final RecycleBinService recycleBinService;

    /**
     * 将短连接移动至回收站当中
     */
    @PostMapping("save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        return shortLinkFeginRemoteService.saveRecycleBin(requestParam);
    }

    /**
     * 分页查询回收站短连接
     */
    @GetMapping("page")
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBin(ShortLinkPageReqDTO requestParam) {
        return recycleBinService.pageRecycleBin(requestParam);
    }

    /**
     * 恢复短连接
     */
    @PostMapping("recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam){
        return shortLinkFeginRemoteService.recoverRecycleBin(requestParam);
    }

    /**
     * 从回收站删除短连接（软删除）
     */
    @PostMapping("remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam){
        return shortLinkFeginRemoteService.removeRecycleBin(requestParam);
    }


}
