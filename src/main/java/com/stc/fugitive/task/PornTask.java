package com.stc.fugitive.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.stc.fugitive.controller.PornUserController;
import com.stc.fugitive.controller.porn.HsexPornController;
import com.stc.fugitive.entity.PornUser;
import com.stc.fugitive.service.PornUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author suntianci on 2022/3/2
 */
@Component
@Slf4j
public class PornTask {

    @Autowired
    private HsexPornController hsexPornController;
    @Autowired
    private PornUserController pornUserController;
    @Autowired
    private PornUserService pornUserService;

    @Scheduled(cron = "0 0 3/6 * * ?")
    @Async("stcThreadPoolTaskExecutor")
    public void videoDown() {
//        hsexPornController.videoDown();
    }

    @Scheduled(cron = "0 0 3/6 * * ?")
    @Async("stcThreadPoolTaskExecutor")
    public void syncUserMovie() {
//        pornUserService.update(new UpdateWrapper<PornUser>().lambda()
//                .set(PornUser::getStatus, "unfinish")
//        );
//        pornUserController.syncUserMovie(null, null);
    }

}
