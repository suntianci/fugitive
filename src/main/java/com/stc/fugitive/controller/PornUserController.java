package com.stc.fugitive.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.CaseFormat;
import com.stc.fugitive.config.exception.BusinessException;
import com.stc.fugitive.entity.PornMovie;
import com.stc.fugitive.entity.PornUser;
import com.stc.fugitive.pojo.Result;
import com.stc.fugitive.service.PornMovieService;
import com.stc.fugitive.service.PornUserService;
import com.stc.fugitive.service.impl.HsexPornServiceImpl;
import com.stc.fugitive.util.HsexPornSpiderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequestMapping("/pornUser")
public class PornUserController {


    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private PornMovieService pornMovieService;

    @Autowired
    private PornUserService pornUserService;
    @Autowired
    private HsexPornServiceImpl hsexPornService;

    @GetMapping("/page")
    public Result<IPage<PornUser>> page(HttpServletRequest request, HttpServletResponse response,
                                        PornUser pornUser,
                                        @RequestParam(value = "orderColumn", required = false) String orderColumn,
                                        @RequestParam(value = "order", required = false) String order,
                                        @RequestParam("movieStatus") String movieStatus,
                                        @RequestParam("scoreStart") Integer scoreStart,
                                        @RequestParam("scoreEnd") Integer scoreEnd,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDatetime,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDatetime,
                                        @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        Page<PornUser> page = new Page<>(pageNo, pageSize);
        orderColumn = orderColumn == null ? "" : orderColumn;
        orderColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, orderColumn);
        LambdaQueryWrapper<PornUser> lambdaQueryWrapper = new QueryWrapper<PornUser>().orderBy(StringUtils.isNotBlank(orderColumn), !StringUtils.equals("descending", order), orderColumn).lambda();
        if ("origin".equals(pornUser.getCreateMan())) {
            lambdaQueryWrapper.isNull(PornUser::getCreateMan);
        } else if ("system".equals(pornUser.getCreateMan())) {
            lambdaQueryWrapper.eq(PornUser::getCreateMan, pornUser.getCreateMan());
        }
        lambdaQueryWrapper
//                .in(PornUser::getAuthor, Stream.of(HsexPornController.authors.split(",")).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList()))
                .ge(scoreStart != null, PornUser::getScore, scoreStart)
                .le(scoreEnd != null, PornUser::getScore, scoreEnd)
                .ge(pornUser.getCreateTimeStart() != null, PornUser::getCreateTime, pornUser.getCreateTimeStart())
                .le(pornUser.getCreateTimeEnd() != null, PornUser::getCreateTime, pornUser.getCreateTimeEnd())
                .ge(pornUser.getUpdateTimeStart() != null, PornUser::getUpdateTime, pornUser.getUpdateTimeStart())
                .le(pornUser.getUpdateTimeEnd() != null, PornUser::getUpdateTime, pornUser.getUpdateTimeEnd())
                .like(StringUtils.isNotEmpty(pornUser.getAuthor()), PornUser::getAuthor, pornUser.getAuthor())
                .like(StringUtils.isNotEmpty(pornUser.getStatus()), PornUser::getStatus, pornUser.getStatus());
        IPage<PornUser> iPage = pornUserService.getBaseMapper().selectPage(page, lambdaQueryWrapper);
        iPage.getRecords().parallelStream().forEach(user -> {
            user.setPornMovieList(
                    pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                            .eq(PornMovie::getAuthor, user.getAuthor())
                            .eq(StringUtils.isNotBlank(movieStatus), PornMovie::getStatus, movieStatus)
                            .orderByDesc(PornMovie::getVideoId)
                            .last("limit 10")
                    ));
        });
        return Result.success(iPage);
    }

    public static void main(String[] args) {
        System.out.println(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, "test-data"));//testData
        System.out.println(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "test_data"));//testData
        System.out.println(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, "test_data"));//TestData

        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "testdata"));//testdata
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "TestData"));//test_data
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "testData"));//test-data


        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "createTime"));//test_data
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, null));//test_data


    }

    @GetMapping("/syncMovie")
    public Result syncMovie() {
        hsexPornService.syncMovie();
        return Result.success();
    }

    @GetMapping("/syncPageMovie")
    public Result syncPageMovie(Integer pageNo) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(5);
        forkJoinPool.submit(() -> IntStream.range(1, pageNo != null && pageNo > 0 ? pageNo : 100).forEach(i -> {
            List<PornMovie> pornMovieList = hsexPornService.getMovieByPage(i, HsexPornServiceImpl.VIDEO_PAGE_URL);
            pornMovieService.saveOrUpdateMultiple(pornMovieList);
            pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumb(e.getVideoId()));//处理缩略图

            pornMovieList.parallelStream().forEach(pornMovie -> HsexPornSpiderUtils.thumbmp4(pornMovie.getVideoId()));
            pornMovieList.parallelStream().map(PornMovie::getAuthor).collect(Collectors.toSet()).parallelStream().forEach(author -> {
                int movieCount = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, author));
                int movieCountUnfinish = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, author).eq(PornMovie::getStatus, "unfinish"));
                pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                        .set(PornUser::getMovieCount, movieCount)
                        .set(PornUser::getMovieCountUnfinish, movieCountUnfinish)
                        .eq(PornUser::getAuthor, author));
            });

        }));
        return Result.success();
    }

    @GetMapping("/readHtml")
    public Result readHtml() {
        List<PornMovie> pornMovieList = hsexPornService.getMovieByAuthorUid();
        pornMovieService.saveOrUpdateMultiple(pornMovieList);
        pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumb(e.getVideoId()));//处理缩略图
        pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumbmp4(e.getVideoId()));//处理缩略图
        return Result.success();
    }

    @GetMapping("/syncUserMovie")
    public Result syncUserMovie(Integer id, String author) {
        List<PornUser> pornUserList = pornUserService.list(new QueryWrapper<PornUser>().lambda()
                .eq(id != null, PornUser::getId, id)
                .eq(StringUtils.isNotBlank(author), PornUser::getAuthor, author)
                .eq(PornUser::getStatus, "unfinish")
                .orderByDesc(PornUser::getId));
        ForkJoinPool forkJoinPool = new ForkJoinPool(5);
        forkJoinPool.submit(() ->
                pornUserList.stream().parallel().forEach(pornUser -> {
                    pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                            .set(PornUser::getStatus, "processing")
                            .eq(PornUser::getId, pornUser.getId()));
                    try {
                        List<PornMovie> pornMovieList = hsexPornService.getMovieByAuthorUid(pornUser.getAuthor());
                        pornMovieService.saveOrUpdateMultiple(pornMovieList);
                        pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                                .set(PornUser::getStatus, "success:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                                .set(PornUser::getMovieCount, pornMovieService.count(new UpdateWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, pornUser.getAuthor())))
                                .eq(PornUser::getId, pornUser.getId()));
//                        pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumb(e.getVideoId()));//处理缩略图
//                        pornMovieList.parallelStream().forEach(e -> HsexPornSpiderUtils.thumbmp4(e.getVideoId()));//处理缩略图
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                                .set(PornUser::getStatus, "fail:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                                .eq(PornUser::getId, pornUser.getId()));
                    }
                })
        );
        return Result.success();
    }

    @GetMapping("/initUnfinish")
    public Result initUnfinish(Integer id, String author) {
        pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                .set(PornUser::getStatus, "unfinish")
                .eq(id != null, PornUser::getId, id)
                .eq(StringUtils.isNotBlank(author), PornUser::getAuthor, author)
        );
        return Result.success();
    }

    @PostMapping("/score")
    public Result<Boolean> score(@RequestBody PornUser pornUser) {
        return Result.success(pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                .set(PornUser::getScore, pornUser.getScore())
                .eq(PornUser::getId, pornUser.getId())
        ));
    }

    @PostMapping("/operate")
    public Result<Boolean> insert(@RequestBody PornUser pornUser) {
        if (pornUserService.list(new QueryWrapper<PornUser>().lambda().eq(PornUser::getAuthor, pornUser.getAuthor())).size() > 0) {
            throw new BusinessException(pornUser.getAuthor() + ":已经存在");
        }
        boolean flag = pornUserService.save(pornUser);
        if (flag) {
//            new Thread(() -> syncUserMovie(null, pornUser.getAuthor())).start();
        }
        return Result.success();
    }

    @PutMapping("/operate")//update
    public Result<Boolean> update(@RequestBody PornUser pornUser) {
        return Result.success(pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                .set(PornUser::getScore, pornUser.getScore())
                .eq(PornUser::getId, pornUser.getId())
        ));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.success(pornUserService.removeById(id));
    }

    @GetMapping("/copy")
    public Result copy(String author) {
        String sourceDirect = HsexPornSpiderUtils.DISK_PORN_ROOT_PATH;
        String targetDirect = "/Volumes/P10/porn/91porn_vidoeos";
        List<PornMovie> pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .eq(PornMovie::getAuthor, author)
                .in(PornMovie::getStatus, Arrays.stream("success_PASS,success_DOWNLOAD_UNCOMPELETE".split(",")).collect(Collectors.toList()))
        );
        pornMovieList.stream().forEach(pornMovie -> {
            File sourceFile = new File(sourceDirect + File.separator + String.format("%03d", pornMovie.getVideoId() / 1000) + File.separator + pornMovie.getFileName());
            if (!sourceFile.exists()) {
                return;
            }
            String authorDirect = pornMovie.getAuthor();
            authorDirect = StringUtils.isBlank(authorDirect) ? "" : authorDirect.replaceAll("/", "-");
            File targetFile = new File(targetDirect + File.separator + authorDirect + File.separator + pornMovie.getFileName());
            if (targetFile.exists()) {
                return;
            }
            try {
                FileUtils.copyFile(sourceFile, targetFile);
                if (targetFile.exists()) {
                    log.info("{}复制成功到{}", sourceFile.getName(), targetFile.getPath());
                } else {
                    log.info("{}复制失败,文件不存在", targetFile.getName());
                }
            } catch (IOException e) {
                log.info("{}复制失败:{}", sourceFile.getName(), e.getMessage());
                log.error(e.getMessage(), e);
            }
        });
        return Result.success();
    }

    @GetMapping("/get/{author}")
    public Result<PornUser> get(@PathVariable String author) {
        return Result.success(pornUserService.getOne(new QueryWrapper<PornUser>().lambda().eq(PornUser::getAuthor, author).last(" limit 1 ")));
    }

    @GetMapping("/add/{author}")
    public Result<Boolean> add(@PathVariable String author) {
        PornUser one = pornUserService.getOne(new QueryWrapper<PornUser>().lambda().eq(PornUser::getAuthor, author));
        if (one == null) {
            PornUser pornUser = PornUser.builder().author(author).build();
            insert(pornUser);
        } else {
            pornUserService.update(new UpdateWrapper<PornUser>().lambda().set(PornUser::getCreateMan, null).eq(PornUser::getId, one.getId()));
        }
        return Result.success();
    }

    @GetMapping("/calcuCount")//http://localhost:7085/pornUser/calcuCount
    public Result calcuCount() {
        List<PornUser> authorList = pornUserService.list(new QueryWrapper<PornUser>().lambda().select(PornUser::getId, PornUser::getAuthor));
        authorList.parallelStream().forEach(pornUser -> {
            int movieCount = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, pornUser.getAuthor()));
            int movieCountUnfinish = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, pornUser.getAuthor()).eq(PornMovie::getStatus, "unfinish"));
            pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                    .set(PornUser::getMovieCount, movieCount)
                    .set(PornUser::getMovieCountUnfinish, movieCountUnfinish)
                    .eq(PornUser::getId, pornUser.getId()));
        });
        return Result.success();
    }


}
