package com.stc.fugitive.service;

import com.github.yulichang.base.service.MPJJoinService;
import com.stc.fugitive.entity.PornUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * porn用户表 服务类
 * </p>
 *
 * @author suntianci
 * @since 2022-06-11
 */
public interface PornUserService extends MPJJoinService<PornUser> {

    void saveOrUpdateMultiple(List<PornUser> pornUserList);

    boolean saveOrUpdateSingle(PornUser pornUser);

    boolean saveOrUpdateByAuthor(String authorUid, String authorName);
}
