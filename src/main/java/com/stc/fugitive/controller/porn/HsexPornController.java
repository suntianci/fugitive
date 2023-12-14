package com.stc.fugitive.controller.porn;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.stc.fugitive.entity.PornMovie;
import com.stc.fugitive.entity.PornMovieDto;
import com.stc.fugitive.entity.PornUser;
import com.stc.fugitive.mapper.PornMovieMapper;
import com.stc.fugitive.pojo.Result;
import com.stc.fugitive.service.PornMovieService;
import com.stc.fugitive.service.PornUserService;
import com.stc.fugitive.service.impl.HsexPornServiceImpl;
import com.stc.fugitive.util.HsexPornSpiderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author suntianci on 2022/6/10
 */
@Slf4j
@RestController
@RequestMapping("/hsex")
public class HsexPornController {


    @Autowired
    private PornUserService pornUserService;
    @Autowired
    private PornMovieService pornMovieService;
    @Autowired
    private HsexPornServiceImpl hsexPornService;

    public static String authors = "";

    static {
        authors += "zifei1210,";
        authors += "舌尖上的生活,";
        authors += "91porn大威天龙,";

        authors += "jhgggvv,diwan98,爱她-小二先生,大铁棍子捅主任,朵蜜LOKLUK,嘉嘉,EDC.,捞仔,YuzuKitty柚子猫,91王牌飞行员,Black_Snake,锅锅酱,天师的淫妻俱乐部,东征西战,利达LiD,Nectarina水蜜桃,下面有跟棒棒糖,拆二代Cc,stopdasekiro,你的白月光,肤絲即肉絲,不知名的KK,Jxxxxx,西门庆在91,烈Retsu_dao";
        authors += ",XBTCLB,youxirensheng369,Abyss22b,富二代Ee,玉树临风唐伯虎,LovELolita,MM陪玩-Mrtow,waldeinsamkeit2000,ENYQ,Au先生,91斯文禽兽x,李沫生的春天,Blessed,宿迁196,人間水蜜桃Bella,春花秋月何时了了,oip夫妻,一路向北D,茂总";
        authors += ",中出爱好家,中出专家,痞子术士,朝天一棍,唯美人妻258,★为非作歹★";
        authors += ",a1pro,哥哥好喜欢吃鸡吧嘿嘿,重生之我是楚留香,658990211,maobaobao821";
        authors += ",LOEWE_END,你的威猛先生,MRtu,swfbxn106,许木学长";
        authors += ",TAILOOK529,shadow94,宝藏收藏家,南京大大大,mrho1998,frankli,zjw12470,vviper,shayebushi12138,GG陪玩";
        authors += ",TeArTAki,算啊会,akutagawa,SexyChloe,zolo096,dadidige,心里老鸭汤,lookoo,91老渣男,gocci吻别昨天,海都冲锋炮,Dom窒息,王先生丶,LedigS.,Timepasserby,妇科圣手,韦小宝呀";
        authors += ",wloveruoqi1,佳多饱";
        authors += ",S1Mg2guanLi,ΝicoLove,ccy999,ttbxbm,eric_1111,nalaibani123,opopo78,w1556089057,lminsheng,下滑更精彩123,zz的主人,kiukiu2427938165,我爱大奶子412,paxlovegirl,xnb2022,91～张先生";
        authors += ",CFB-521,658990211,Bigfan13yo,Mr.HelvChen,Tongletongle,593854690,ZuUjsll,aa123www,ccy1871,gonganhuojing,ai美乳,wshinibaba2022";
        authors += ",玩物上志,xianfengww123,BroGuang,Vritra,风流不下流,rourou0,轩然Sky,猫先生,绅士Geltelent、,取杯天上水,涛哥爱你哈,keleping,91老渣男,Bigfan13yo,daweiwango,Tongletongle";
        authors += ",livepretty,1269777132,da123789,jzgbskl,qd阿丁,海都冲锋炮,江苏风清扬,papayax,江浙沪超人强,a26935266,c331516,gdjyxzl,xxoxxbb,chen1fei10,ZuUjsll,唱曲直入,VODKAS176,重生之我是大明星,91普人";
        authors += ",宁波情侣00,ladudu98,同城人妻Vx10677180,gai991,m375024214,Kyara,mengdi-s,9130410,sisisisihhhh1,Zyusn99_,xiaocaihh,sktt1faker,tv110tv110,a958149338,aA714873033,qp714873033_,z2326449,youxirensheng369,jiasatuo,pppooozxc,powercn206,ai美乳,蜜桃影像传媒,gonganhuojing";
        authors += ",mdiftff,lyfxiang,ltkkgotion,京城肉鸡,wshinibaba2022,杨sai,943162807,huang94,Zhangsanfeng88,91cc7777,kimoji911,wh106461,160CC,Dome疯子,Silky,ok嘎得,江户川,飞机,zzzann123,hotboys,日批哥,酒酿sama,大吃一精,43257871,lsjmatilda,laowanglaile2,Torrl69mor,314174044wodeqp,霸道总裁";
        authors += ",农夫黄泉,khcheung,想和你生一群baby";
    }

    @GetMapping("/batchStatus")//http://127.0.0.1:7085/hsex/batchStatus
    public Result batchStatus() {
        List<PornMovie> pornMovieList = new ArrayList<>();
        List<PornMovie> pornMovieList3 = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .in(PornMovie::getStatus, Arrays.stream("fail,unfinish".split(",")).collect(Collectors.toList()))
                .gt(PornMovie::getScore, 0)
        );
        pornMovieList.addAll(pornMovieList3);
        List<String> authorListGt0 = pornUserService.list(new QueryWrapper<PornUser>().lambda()
                .gt(PornUser::getScore, 0)
        ).stream().map(PornUser::getAuthor).collect(Collectors.toList());
        if (authorListGt0.size() > 0) {
            pornMovieList.addAll(pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                    .in(PornMovie::getAuthor, authorListGt0)
                    .in(PornMovie::getStatus, Arrays.stream("fail,unfinish".split(",")).collect(Collectors.toList()))
                    .orderByDesc(PornMovie::getVideoId)
            ));
        }
        pornMovieList = pornMovieList.stream().filter(e -> e.getVideoId() != null).filter(distinctByKey(PornMovie::getVideoId)).sorted(Comparator.comparing(PornMovie::getVideoId).reversed()).collect(Collectors.toList());
        pornMovieList.stream().map(PornMovie::getVideoId).forEach(System.out::println);
        pornMovieList.parallelStream().forEach(pornMovie -> {
            pornMovieService.update(new UpdateWrapper<PornMovie>().lambda().set(PornMovie::getStatus, "processing").eq(PornMovie::getId, pornMovie.getId()));
        });
        return Result.success(pornMovieList.size());
    }

    @GetMapping("/videoDownAll")//http://127.0.0.1:7085/hsex/videoDownAll
    public Result videoDownAll() {
        String authors = "ASSFUCKASS,乌梅子酱,无敌泡泡拳老刘,Don77,Sexbaby1,Timepasserby,AC2023,zoyex,萝莉爱大叔,91shishi,腰精武小姐,萝莉爱大叔,91欧巴桑,Don77,91小腰精,我叫山雞雞巴的雞,AslongAsyouLoveme,快枪手先生,lookoo,two22,UUS1980,ioopjkon897,Timepasserby,是性奴小雨喔,pprlv";
        authors += ",尼古丁不尼,91小宝,拆二代Cc,Tims";
        List<PornMovie> pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .in(PornMovie::getAuthor, Arrays.stream(authors.split(",")).collect(Collectors.toList()))
                .ne(PornMovie::getStatus, "success_PASS")
        );
        pornMovieList.addAll(pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .gt(PornMovie::getScore, 10)
                .in(PornMovie::getStatus, Arrays.stream("unfinish,fail,processing".split(",")).collect(Collectors.toList()))
        ));
        int parallelism = 10;
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
        List<PornMovie> finalPornMovieList = pornMovieList;
        forkJoinPool.submit(() ->
                finalPornMovieList.parallelStream().forEach(pornMovie -> {
                    hsexPornService.downPornMovie(pornMovie);
                })
        );
        return Result.success();
    }

    @GetMapping("/videoDownBatch")//http://127.0.0.1:7085/hsex/videoDownBatch
    public Result videoDownBatch() {
        videoDown(null);
        return Result.success();
    }

    @GetMapping("/videoDown")
    public Result videoDown(Integer videoId) {

        List<String> authorList = Stream.of(HsexPornController.authors.split(",")).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
        List<PornMovie> pornMovieList = new ArrayList<>();
        List<PornMovie> pornMovieList1 = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .in(PornMovie::getStatus, Arrays.stream("fail,processing".split(",")).collect(Collectors.toList()))
        );
        List<PornMovie> pornMovieList2 = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .in(PornMovie::getAuthor, authorList)
                .in(PornMovie::getStatus, Arrays.stream("fail,processing".split(",")).collect(Collectors.toList()))
                .orderByDesc(PornMovie::getVideoId)
        );
        List<PornMovie> pornMovieList3 = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .in(PornMovie::getStatus, Arrays.stream("fail,processing,unfinish".split(",")).collect(Collectors.toList()))
                .gt(PornMovie::getScore, 0)
        );

        pornMovieList.addAll(pornMovieList1);
        pornMovieList.addAll(pornMovieList2);
        pornMovieList.addAll(pornMovieList3);

        pornMovieList.clear();
        if (videoId == null) {
            pornMovieList.addAll(pornMovieList3);
            List<String> authorListGt0 = pornUserService.list(new QueryWrapper<PornUser>().lambda()
                    .gt(PornUser::getScore, 0)
            ).stream().map(PornUser::getAuthor).collect(Collectors.toList());
            if (authorListGt0.size() > 0) {
                pornMovieList.addAll(pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                        .in(PornMovie::getAuthor, authorListGt0)
                        .in(PornMovie::getStatus, Arrays.stream("fail,processing,unfinish".split(",")).collect(Collectors.toList()))
                        .orderByDesc(PornMovie::getVideoId)
                ));
            }
        } else {
            pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                    .eq(PornMovie::getVideoId, videoId)
                    .orderByDesc(PornMovie::getVideoId)
            ).stream().collect(Collectors.toList());
        }

        pornMovieList = pornMovieList.stream().filter(e -> e.getVideoId() != null).filter(distinctByKey(PornMovie::getVideoId)).sorted(Comparator.comparing(PornMovie::getVideoId).reversed()).collect(Collectors.toList());
        pornMovieList.stream().map(PornMovie::getVideoId).forEach(System.out::println);
        int parallelism = 5;
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
        List<PornMovie> finalPornMovieList = pornMovieList;
        forkJoinPool.submit(() ->
                finalPornMovieList.parallelStream().forEach(pornMovie -> {
                    hsexPornService.downPornMovie(pornMovie);
                })
        );
        return Result.success();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static boolean starting = false;

    @GetMapping("/initFile")//http://127.0.0.1:7085/hsex/initFile
    public Result initFile() {
        if (starting) {
            return Result.success();
        }
        starting = true;
        new Thread(() -> {
            while (true) {
                String sourceDirect = HsexPornSpiderUtils.ROOT_PATH;
                String targetDirect = HsexPornSpiderUtils.DISK_PORN_ROOT_PATH;

                File rootFile = new File(sourceDirect);
                if (rootFile.list() == null || Arrays.stream(rootFile.list()).filter(f -> f.endsWith(".mp4")).collect(Collectors.toList()).size() == 0) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("休眠结束，continue");
                    continue;
                }
                List<String> filelist = Arrays.stream(rootFile.list()).filter(f -> f.endsWith(".mp4")).collect(Collectors.toList());
                List<String> pornUserList = pornUserService.list(new QueryWrapper<PornUser>().lambda()).stream().map(PornUser::getAuthor).collect(Collectors.toList());

                List<Integer> videoIdList = new ArrayList<>();
                Map<Integer, String> videoIdInfoMap = new HashMap<>();

                filelist.stream().forEach(file -> {
                    boolean flag = pornUserList.stream().anyMatch(u -> file.startsWith(u));
                    if (flag) {
                        pornUserList.forEach(user -> {
                            if (file.startsWith(user)) {
                                String videoIdSrt = file.replace(user, "").split("_")[1];
                                Integer videoId = NumberUtils.toInt(videoIdSrt, 0);
                                if (videoId > 10000) {
                                    videoIdList.add(videoId);
                                    videoIdInfoMap.put(videoId, file);
                                }
                            }
                        });
                    } else {
                        String videoIdSrt = file.split("_")[1];
                        Integer videoId = NumberUtils.toInt(videoIdSrt, 0);
                        if (videoId > 10000) {
                            videoIdList.add(videoId);
                            videoIdInfoMap.put(videoId, file);
                        }
                    }
                });
                for (Integer videoId : videoIdList) {
                    PornMovie pornMovie = pornMovieService.getOne(new QueryWrapper<PornMovie>().lambda()
                            .eq(PornMovie::getVideoId, videoId)
                            .last("limit 1")
                    );
                    if (pornMovie == null) {
                        log.error("视频信息不存在{}", videoId);
                        continue;
                    }
                    if (StringUtils.isEmpty(videoIdInfoMap.get(videoId))) {
                        log.error("文件异常{}", videoId);
                        continue;
                    }
                    File sourceFile = new File(sourceDirect, videoIdInfoMap.get(videoId));
                    if (!sourceFile.exists()) {
                        continue;
                    }
                    File targetFileRoot = new File(targetDirect + File.separator + String.format("%03d", videoId / 1000));
                    if (!targetFileRoot.exists()) {
                        targetFileRoot.mkdirs();
                    }

                    String fileName = HsexPornSpiderUtils.getFileName(pornMovie.getAuthor(), videoId, pornMovie.getTitle());
//                    pornMovie.setDurationSecond(FfmpegUtils.convert(pornMovie.getDuration()));
//                    pornMovie.setFfmpegDurationSecond(FfmpegUtils.getVideoTimeSecond(HsexPornSpiderUtils.DISK_PORN_ROOT_PATH, fileName));
//                    pornMovie.setFfmpegDuration(FfmpegUtils.convert(FfmpegUtils.getVideoTimeSecond(HsexPornSpiderUtils.DISK_PORN_ROOT_PATH, fileName)));
//                    pornMovie.setDurationRate(FfmpegUtils.calculateDurationRate(pornMovie.getFfmpegDurationSecond(), pornMovie.getDurationSecond()));
                    pornMovie.setFileName(fileName);

                    try {
                        File targetFile = new File(targetDirect + File.separator + String.format("%03d", videoId / 1000), pornMovie.getFileName());
                        FileUtils.copyFile(sourceFile, targetFile);
                        if (targetFile.exists()) {
                            log.info("{}复制成功到{}", sourceFile.getName(), targetFile.getPath());
                            pornMovieService.updateById(pornMovie);
                            sourceFile.delete();
                        } else {
                            log.info("{}复制失败,文件不存在", targetFile.getName());
                        }
                    } catch (IOException e) {
                        log.info("{}复制失败:{}", sourceFile.getName(), e.getMessage());
                        log.error(e.getMessage(), e);
                    }
                }
                if (1 == 0) {
                    break;
                }
            }
        }).start();
        return Result.success();
    }

    @Autowired
    private PornMovieMapper pornMovieMapper;

    @GetMapping("/join")//http://127.0.0.1:7085/hsex/join
    public Result join() {
        List<PornMovieDto> list = pornMovieService.selectJoinList(PornMovieDto.class,
                new MPJLambdaWrapper<PornMovie>()
                        .selectAll(PornMovie.class)
                        .selectAll(PornUser.class)
                        .select(PornUser::getId)
                        .selectAs(PornUser::getCreateTime, PornMovieDto::getCreateTimeStart)
                        .selectAssociation(PornUser.class, PornMovieDto::getPornUser)
                        .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
                        .eq(PornMovie::getId, 3)
        );
        list.forEach(System.out::println);
        List<PornMovieDto> list2 = pornMovieService.selectJoinList(PornMovieDto.class,
                new MPJQueryWrapper<PornMovie>()
                        .selectAll(PornMovie.class)
                        .select("t1.*")
                        .leftJoin("porn_user t1 on t1.author = t.author")
                        .eq("t.id", 3)
        );
        list2.forEach(System.out::println);
//        List<PornUserDto> dtoList = pornUserService.selectJoinList(PornUserDto.class, new MPJLambdaWrapper<PornUser>()
//                .selectAll(PornUser.class)
//                //对多查询
//                .selectCollection(PornMovie.class, PornUserDto::getPornMovieList)
//                //对一查询
//                .selectAssociation(PornUser.class, PornUserDto::getPornUser)
//                .leftJoin(PornMovie.class, PornMovie::getAuthor, PornMovie::getAuthor)
//                .leftJoin(PornUser.class, PornUser::getAuthor, PornUser::getAuthor)
//                .last("limit 10")
//        );
//        dtoList.forEach(System.out::println);
        IPage<PornMovieDto> pornMovieDtoIPage = pornMovieService.selectJoinListPage(
                new Page<PornMovieDto>(2, 10),
                PornMovieDto.class,
                new MPJLambdaWrapper<PornMovie>()
                        .selectAll(PornMovie.class)
                        .selectAll(PornUser.class)
                        .select(PornUser::getId)
                        .selectAs(PornUser::getCreateTime, PornMovieDto::getCreateTimeStart)
                        .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
        );
        pornMovieDtoIPage.getRecords().forEach(System.out::println);

        Integer count = pornMovieService.selectJoinCount(new MPJLambdaWrapper<PornMovie>()
                .select(PornMovie::getId)
                .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
                .eq(PornMovie::getId, 3));
        System.out.println(count);
        PornMovieDto pornMovieDto = pornMovieService.selectJoinOne(PornMovieDto.class, new MPJLambdaWrapper<PornMovie>()
                .selectAll(PornMovie.class)
                .selectAs(PornUser::getCreateTime, PornMovieDto::getCreateTimeStart)
                .selectAssociation(PornUser.class, PornMovieDto::getPornUser)
                .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
                .eq(PornMovie::getId, 3));
        System.out.println(pornMovieDto);

        Map<String, Object> map = pornMovieService.selectJoinMap(
                new MPJLambdaWrapper<PornMovie>()
                        .selectAll(PornMovie.class)
                        .selectAll(PornUser.class)
                        .select(PornUser::getId)
                        .selectAs(PornUser::getCreateTime, PornMovieDto::getCreateTimeStart)
                        .selectAssociation(PornUser.class, PornMovieDto::getPornUser)
                        .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
                        .last("limit 1")
        );
        System.out.println(map);

        List<Map<String, Object>> mapList = pornMovieService.selectJoinMaps(
                new MPJLambdaWrapper<PornMovie>()
                        .selectAll(PornMovie.class)
                        .selectAll(PornUser.class)
                        .select(PornUser::getId)
                        .selectAs(PornUser::getCreateTime, PornMovieDto::getCreateTimeStart)
                        .selectAssociation(PornUser.class, PornMovieDto::getPornUser)
                        .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
                        .last("limit 10")
        );
        System.out.println(mapList);
        IPage<Map<String, Object>> mapIPage = pornMovieService.selectJoinMapsPage(
                new Page<Map<String, Object>>(2, 10),
                new MPJLambdaWrapper<PornMovie>()
                        .selectAll(PornMovie.class)
                        .selectAll(PornUser.class)
                        .select(PornUser::getId)
                        .selectAs(PornUser::getCreateTime, PornMovieDto::getCreateTimeStart)
                        .leftJoin(PornUser.class, PornUser::getAuthor, PornMovie::getAuthor)
        );
        mapIPage.getRecords().forEach(System.out::println);
        return Result.success(mapIPage);
    }


}
