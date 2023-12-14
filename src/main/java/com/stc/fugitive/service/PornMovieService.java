package com.stc.fugitive.service;

import com.github.yulichang.base.service.MPJJoinService;
import com.stc.fugitive.entity.PornMovie;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * movie 服务类
 * </p>
 *
 * @author suntianci
 * @since 2022-06-11
 */
public interface PornMovieService extends MPJJoinService<PornMovie> {

    void saveOrUpdateMultiple(List<PornMovie> pornMovieList);

    boolean saveOrUpdateSingle(PornMovie pornMovie);
}
