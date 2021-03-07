package com.jacky.imagecloud.controller;

import com.jacky.imagecloud.data.Info;
import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import com.jacky.imagecloud.err.item.ItemNotFoundException;
import com.jacky.imagecloud.models.items.Item;
import com.jacky.imagecloud.models.items.Share;
import com.jacky.imagecloud.models.items.ShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sharePort")
public class FileShareController {
    LoggerHandle logger =LoggerHandle.newLogger(FileShareController.class);

    @Autowired
    public ShareRepository shareRepository;

    @GetMapping("/s/{shareCode:.+}")
    public Result<List<Item>> getShare(
            @PathVariable(name = "shareCode") String shareUUID
    ) {
        try {
            Share share = shareRepository.findByUuid(shareUUID);
            share.constructUser();

            logger.shareOperateSuccess("Get Share Items",shareUUID);
            return Result.okResult(share.getItems());
        } catch (ItemNotFoundException e) {
            logger.operateFailure("Get Share Items", Info.of(shareUUID,"Share Code"));
            return Result.failureResult(e);
        }
    }

}
