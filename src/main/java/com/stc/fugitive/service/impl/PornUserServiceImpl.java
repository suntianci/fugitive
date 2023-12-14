package com.stc.fugitive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.stc.fugitive.entity.PornUser;
import com.stc.fugitive.mapper.PornUserMapper;
import com.stc.fugitive.service.PornUserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * porn用户表 服务实现类
 * </p>
 *
 * @author suntianci
 * @since 2022-06-11
 */
@Service
public class PornUserServiceImpl extends MPJBaseServiceImpl<PornUserMapper, PornUser> implements PornUserService {

    @Autowired
    private PornUserService pornUserService;

    @Override
    public void saveOrUpdateMultiple(List<PornUser> pornUserList) {
        if (CollectionUtils.isEmpty(pornUserList)) {
            return;
        }
        for (PornUser pornUser : pornUserList) {
            this.pornUserService.saveOrUpdateSingle(pornUser);
        }
    }

    @Override
    public boolean saveOrUpdateSingle(PornUser pornUser) {
        List<PornUser> db = this.pornUserService.list(new QueryWrapper<PornUser>().lambda()
                .eq(PornUser::getAuthor, pornUser.getAuthor())
        );
        if (CollectionUtils.isEmpty(db)) {
            return pornUserService.save(pornUser);
        } else {
            pornUser.setId(db.get(0).getId());
            return this.pornUserService.updateById(pornUser);
        }
    }

    @Override
    public boolean saveOrUpdateByAuthor(String authorUid, String authorName) {
        List<PornUser> db = this.pornUserService.list(new QueryWrapper<PornUser>().lambda()
                .eq(PornUser::getAuthor, authorName)
        );
        PornUser pornUser = PornUser.builder().author(authorName).authorUid(authorUid).build();
        if (CollectionUtils.isEmpty(db)) {
            return pornUserService.save(pornUser);
        } else {
            pornUser.setId(db.get(0).getId());
            return this.pornUserService.updateById(pornUser);
        }
    }

}
