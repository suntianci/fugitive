package com.stc.fugitive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.stc.fugitive.entity.PornMovie;
import com.stc.fugitive.mapper.PornMovieMapper;
import com.stc.fugitive.service.PornMovieService;
import com.stc.fugitive.util.FfmpegUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * movie 服务实现类
 * </p>
 *
 * @author suntianci
 * @since 2022-06-10
 */
@Service
public class PornMovieServiceImpl extends MPJBaseServiceImpl<PornMovieMapper, PornMovie> implements PornMovieService {

    @Autowired
    private PornMovieService pornMovieService;

    @Override
    public void saveOrUpdateMultiple(List<PornMovie> pornMovieList) {
        if (CollectionUtils.isEmpty(pornMovieList)) {
            return;
        }
        for (PornMovie pornMovie : pornMovieList) {
            this.pornMovieService.saveOrUpdateSingle(pornMovie);
        }
    }

    @Override
    public boolean saveOrUpdateSingle(PornMovie pornMovie) {
        List<PornMovie> db = this.pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .eq(PornMovie::getVideoId, pornMovie.getVideoId())
        );
        if (CollectionUtils.isEmpty(db)) {
            return pornMovieService.save(pornMovie);
        } else {
            PornMovie pornMovieDB = db.get(0);
            return this.pornMovieService.update(new UpdateWrapper<PornMovie>().lambda()
                    .set(StringUtils.isNotEmpty(pornMovie.getTitle()), PornMovie::getTitle, pornMovie.getTitle())
                    .set(StringUtils.isNotEmpty(pornMovie.getDuration()), PornMovie::getDuration, pornMovie.getDuration())
                    .set(StringUtils.isNotEmpty(pornMovie.getDuration()), PornMovie::getDurationSecond, FfmpegUtils.convert(pornMovie.getDuration()))
                    .set(StringUtils.isNotEmpty(pornMovie.getAddTime()), PornMovie::getAddTime, pornMovie.getAddTime())
                    .set(StringUtils.isNotEmpty(pornMovie.getAuthor()), PornMovie::getAuthor, pornMovie.getAuthor())
                    .set(StringUtils.isNotEmpty(pornMovie.getAuthorUid()), PornMovie::getAuthorUid, pornMovie.getAuthorUid())
                    .set(pornMovie.getViewCount() != null, PornMovie::getViewCount, pornMovie.getViewCount())
                    .set(pornMovie.getCollect() != null, PornMovie::getCollect, pornMovie.getCollect())
                    .set(pornMovie.getMessageNumber() != null, PornMovie::getMessageNumber, pornMovie.getMessageNumber())
                    .set(pornMovie.getIntegration() != null, PornMovie::getIntegration, pornMovie.getIntegration())
                    .set(StringUtils.isNotEmpty(pornMovie.getDownloadUrl()), PornMovie::getDownloadUrl, pornMovie.getDownloadUrl())
                    .set(StringUtils.isNotEmpty(pornMovie.getStatus()), PornMovie::getStatus, pornMovie.getStatus())
                    .eq(PornMovie::getId, pornMovieDB.getId())
            );
        }
    }

}
