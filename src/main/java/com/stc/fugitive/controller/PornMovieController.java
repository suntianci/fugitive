package com.stc.fugitive.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.CaseFormat;
import com.stc.fugitive.entity.PornMovie;
import com.stc.fugitive.entity.PornUser;
import com.stc.fugitive.pojo.Result;
import com.stc.fugitive.service.PornMovieService;
import com.stc.fugitive.service.PornUserService;
import com.stc.fugitive.service.impl.HsexPornServiceImpl;
import com.stc.fugitive.util.HsexPornSpiderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * movie 前端控制器
 * </p>
 *
 * @author suntianci
 * @since 2022-06-11
 */
@Slf4j
@RestController
@RequestMapping("/pornMovie")
public class PornMovieController {

    @Value("${server.port}")
    private String serverPort;

    public static final String THUMB_URL = "http://localhost:%s/pornMovie/thumb/preview/%s.jpg";
    public static final String THUMB_MP4_URL = "http://localhost:%s/pornMovie/thumb/preview/%s.mp4";


    @Autowired
    private PornMovieService pornMovieService;
    @Autowired
    private PornUserService pornUserService;
    @Autowired
    private HsexPornServiceImpl hsexPornService;
    @Autowired
    private HsexPornSpiderUtils hsexPornSpiderUtils;

    @GetMapping(value = "/thumb/preview/{thumb}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> preview(@PathVariable("thumb") String thumb) throws IOException {
        File file = new File(HsexPornSpiderUtils.ROOT_THUMB_PATH + thumb);
        if (thumb.endsWith(".mp4")) {
            file = new File(HsexPornSpiderUtils.ROOT_THUMB_MP4_PATH + thumb);
        }
        InputStream inputStream = new FileInputStream(file);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.OK);
    }


    @GetMapping("/page")
    public Result<IPage<PornMovie>> page(HttpServletRequest request, HttpServletResponse response,
                                         PornMovie pornMovie,
                                         @RequestParam(value = "inStatus", required = false) String inStatus,
                                         @RequestParam(value = "orderColumn", required = false) String orderColumn,
                                         @RequestParam(value = "order", required = false) String order,
                                         @RequestParam("scoreStart") Integer scoreStart,
                                         @RequestParam("scoreEnd") Integer scoreEnd,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDatetime,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDatetime,
                                         @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) throws UnsupportedEncodingException {
        Page<PornMovie> page = new Page<>(pageNo, pageSize);
        orderColumn = orderColumn == null ? "" : orderColumn;
        orderColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, orderColumn);

        LambdaQueryWrapper<PornMovie> lambdaQueryWrapper = new QueryWrapper<PornMovie>().orderBy(StringUtils.isNotBlank(orderColumn), !StringUtils.equals("descending", order), orderColumn).lambda();
        if ("in".equals(inStatus)) {
            List<String> authorList = pornUserService.list(new QueryWrapper<PornUser>().lambda().select(PornUser::getAuthor).ne(PornUser::getCreateMan, "system").or().isNull(PornUser::getCreateMan)).stream().map(PornUser::getAuthor).collect(Collectors.toList());
            lambdaQueryWrapper.in(PornMovie::getAuthor, authorList);
        } else if ("notIn".equals(inStatus)) {
            List<String> authorList = pornUserService.list(new QueryWrapper<PornUser>().lambda().select(PornUser::getAuthor).eq(PornUser::getCreateMan, "system")).stream().map(PornUser::getAuthor).collect(Collectors.toList());
            lambdaQueryWrapper.in(PornMovie::getAuthor, authorList);
        }
        lambdaQueryWrapper
                .like(StringUtils.isNotEmpty(pornMovie.getTitle()), PornMovie::getTitle, pornMovie.getTitle())
                .like(StringUtils.isNotEmpty(pornMovie.getAuthor()) && !pornMovie.getAuthor().contains(","), PornMovie::getAuthor, pornMovie.getAuthor())
                .in(StringUtils.isNotEmpty(pornMovie.getAuthor()) && pornMovie.getAuthor().contains(","), PornMovie::getAuthor, Arrays.stream(pornMovie.getAuthor().split(",")).collect(Collectors.toList()))
                .ge(scoreStart != null, PornMovie::getScore, scoreStart)
                .le(scoreEnd != null, PornMovie::getScore, scoreEnd)
                .ge(pornMovie.getCreateTimeStart() != null, PornMovie::getCreateTime, pornMovie.getCreateTimeStart())
                .le(pornMovie.getCreateTimeEnd() != null, PornMovie::getCreateTime, pornMovie.getCreateTimeEnd())
                .ge(pornMovie.getUpdateTimeStart() != null, PornMovie::getUpdateTime, pornMovie.getUpdateTimeStart())
                .le(pornMovie.getUpdateTimeEnd() != null, PornMovie::getUpdateTime, pornMovie.getUpdateTimeEnd())
                .eq(StringUtils.isNotEmpty(pornMovie.getStatus()), PornMovie::getStatus, pornMovie.getStatus())
                .like(pornMovie.getVideoId() != null, PornMovie::getVideoId, pornMovie.getVideoId())
                .orderByDesc(PornMovie::getCreateTime)
        ;

        IPage<PornMovie> iPage = pornMovieService.getBaseMapper().selectPage(page, lambdaQueryWrapper);
        iPage.getRecords().parallelStream().forEach(movie -> {
            if (new File(HsexPornSpiderUtils.DISK_PORN_ROOT_PATH + File.separator + String.format("%03d", movie.getVideoId() / 1000) + File.separator + movie.getFileName()).exists()) {
                movie.setFilePath(HsexPornSpiderUtils.DISK_PORN_ROOT_PATH + File.separator + String.format("%03d", movie.getVideoId() / 1000) + File.separator + movie.getFileName());
            } else if (new File(HsexPornSpiderUtils.ROOT_PATH + File.separator + movie.getFileName()).exists()) {
                movie.setFilePath(HsexPornSpiderUtils.ROOT_PATH + File.separator + movie.getFileName());
            }
            movie.setThumb(String.format(PornMovieController.THUMB_URL, serverPort, movie.getVideoId()));
        });
        return Result.success(iPage);
    }

    @GetMapping("/thumb")
    public Result thumb(String auth) {
        List<PornMovie> pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .eq(StringUtils.isNotEmpty(auth), PornMovie::getAuthor, auth)
                .orderByDesc(PornMovie::getVideoId)
        );
        pornMovieList.stream().forEach(movie -> {
            HsexPornSpiderUtils.thumb(movie.getVideoId());
        });
        return Result.success();
    }

    @PostMapping("/score")
    public Result<Boolean> score(@RequestBody PornMovie pornMovie) {
        return Result.success(pornMovieService.update(new UpdateWrapper<PornMovie>().lambda()
                .set(PornMovie::getScore, pornMovie.getScore())
                .eq(PornMovie::getId, pornMovie.getId())
        ));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.success(pornMovieService.removeById(id));
    }

    @GetMapping("/download")
    public Result download(String author, Integer videoId) {
        if (true) {
            List<Integer> list = new ArrayList<Integer>(Arrays.asList(
                    115203, 338895, 389686, 402899, 428759, 431385, 437005, 463577, 466186, 474468, 497449, 507703, 517569, 522973, 544529, 575610,
                    578634, 596958, 597587, 602619, 603438, 607258, 613744, 620609, 625486, 626225, 632487, 638582, 644639, 648216, 648688, 652399,
                    652703, 653184, 656436, 660577, 660585, 661956, 679351, 709827, 715026, 719364, 719367, 719848, 720381, 720387, 721777, 722278,
                    737735, 749636, 763888, 764330, 765695, 769116, 769275, 769880, 770464, 770496, 771061, 772688, 772721, 773239, 773286, 773311,
                    773351, 773813, 773873, 774370, 774441, 774881, 774963, 775449, 775487, 775511, 775843, 775981, 776014, 776486, 776547, 776911,
                    777127, 779534, 779788, 780006, 781215, 781541, 782128, 782441, 782921, 782955, 784033, 784108, 784262, 784789, 785064, 785240,
                    785260, 785509, 785873, 785891, 786138, 787065, 787390, 787980, 790235, 792074, 793467, 794093, 794735, 795314, 796420, 797019,
                    797169, 797377, 798758, 800132, 800314, 800367, 800752, 801874, 802831, 803165, 803504, 803610, 805287, 806255, 807480, 808210,
                    808712, 811678, 815789, 815915, 819766, 824124, 825222, 825838, 826026, 826136, 826285, 826861, 827067, 827711, 828166, 828632,
                    829302, 829474, 829618, 830958, 831454, 831875, 833996, 834534, 834712, 835246, 835619, 835856, 837224, 837362, 838167, 838343,
                    838447, 838519, 839684, 840503, 840988, 843332, 844208, 844790, 845157, 845242, 845273, 845732, 847208, 847640, 847684, 848296,
                    848547, 848678, 849117, 849699, 850228, 850614, 850676, 851210, 851260, 851772, 852275, 853087, 853236, 853551, 853571, 853763,
                    855820, 856851, 856933, 857762, 857791, 860486, 860525, 862274, 863437, 864565, 868848, 869543, 870436, 870619, 871055, 871531,
                    871802, 872009, 872143, 875497, 875668, 876265, 876268, 876269, 876343, 876973, 877398, 877778, 877827, 878377, 878417, 878709,
                    879145, 879400, 879543, 879549, 879577, 880294, 880365, 880524, 880705, 880794, 880848, 881029, 881338, 881602, 881756, 882631,
                    882918, 882935, 883182, 883243, 883376, 883488, 883501, 883662, 884940, 885611, 885614, 885818, 903689, 903686, 903332, 903354,
                    885823, 885832, 886239, 886563, 886570, 886641, 886650, 886988, 886998, 887114, 887411, 887414, 887418, 887421, 887614, 887692,
                    887809, 887813, 888192, 888195, 888401, 888583, 888587, 888596, 888777, 888909, 888945, 888952, 888962, 889347, 889350, 889364,
                    889365, 889403, 889730, 889753, 889757, 889776, 890012, 890183, 890184, 890187, 890195, 890641, 890643, 890649, 890866, 890942,
                    891044, 891052, 891493, 891496, 891500, 891510, 891753, 891910, 891919, 891924, 892064, 892241, 892246, 892247, 892269, 892525,
                    892541, 892670, 892671, 892680, 892747, 892936, 892957, 892986, 893033, 893035, 893047, 893252, 893432, 893436, 893444, 893511,
                    893849, 893853, 893856, 894053, 894142, 894251, 894253, 894257, 894259, 894261, 894264, 894540, 894543, 894605, 894608, 894610,
                    894613, 894614, 895042, 895043, 895045, 895049, 895052, 895256, 895272, 895382, 895408, 895412, 895415, 895416, 895642, 895722,
                    895778, 895791, 895793, 854564, 855126, 855305, 855440, 902830, 902857, 902895, 902935, 902946, 903337, 912186, 912322, 904110,
                    895795, 895799, 895805, 896036, 896038, 896072, 896079, 896197, 896198, 896199, 896208, 896329, 896464, 896465, 896533, 896535,
                    896536, 896554, 896733, 896734, 896744, 896746, 896796, 896797, 896851, 896931, 896932, 896933, 897020, 897178, 897258, 897272,
                    897294, 897295, 897298, 897302, 897303, 897426, 897428, 897529, 897556, 897578, 897579, 897580, 897587, 897593, 897626, 897827,
                    897829, 897910, 897939, 897946, 897948, 898252, 898253, 898255, 898256, 898258, 898280, 898462, 898464, 898674, 898677, 898679,
                    898948, 899006, 899009, 899012, 899020, 899279, 899281, 899380, 899384, 899386, 899390, 899392, 899410, 899607, 899608, 911991,
                    899609, 899673, 899766, 899771, 899774, 899794, 899795, 899805, 899806, 900090, 900130, 900157, 900158, 900159, 900160, 900162,
                    900163, 900306, 900372, 900536, 900563, 900566, 900568, 900582, 900853, 900857, 900860, 900861, 901018, 901369, 901370, 901373,
                    901386, 901392, 901454, 901457, 901474, 901576, 901579, 901619, 901706, 901738, 901740, 901789, 901791, 901806, 901807, 901810,
                    901813, 901814, 901815, 901851, 902041, 902068, 902075, 902077, 902081, 902117, 902147, 902149, 902154, 902156, 902160, 904468,
                    902237, 902249, 902375, 902465, 902466, 902521, 902563, 902564, 902565, 902569, 902573, 902575, 902580, 902648, 902651, 902711,
                    902981, 902991, 902993, 903007, 903011, 903015, 903068, 903077, 903080, 903104, 903116, 903243, 903318, 903321, 903323, 903327,
                    903328, 903329, 903332, 903337, 903348, 903354, 903371, 903388, 903421, 903582, 903635, 903658, 903686, 903689, 903705, 903713,
                    903723, 903725, 903730, 903736, 903745, 903759, 903779, 903800, 903845, 903926, 903954, 903956, 904041, 904047, 904062, 904074,
                    904103, 904104, 904105, 904107, 904108, 904110, 904113, 904127, 904140, 904183, 904191, 904266, 904273, 904379, 904394, 904402,
                    904414, 904419, 904438, 904455, 904459, 904461, 904462, 904464, 904466, 904467, 904468, 904469, 904477, 904478, 904487, 904491,
                    904560, 904706, 904707, 904725, 904775, 904776, 904787, 904788, 904790, 904793, 904797, 904798, 904802, 904813, 904832, 904846,
                    904854, 904863, 904866, 904874, 904913, 904917, 904918, 905019, 905037, 905046, 905048, 905050, 905061, 905076, 905092, 905095,
                    905096, 905097, 905099, 905100, 905101, 905104, 905114, 905128, 905162, 905177, 905191, 905201, 905206, 905210, 905263, 905338,
                    905383, 905392, 905394, 905405, 905407, 905424, 905425, 905426, 905436, 905442, 905443, 905445, 905489, 905500, 905502, 905508,
                    905541, 905544, 905577, 905682, 905742, 905751, 905753, 905754, 905756, 905760, 905762, 905788, 905814, 905860, 905934, 905936,
                    905969, 906053, 906058, 906060, 906063, 906064, 906077, 906081, 906097, 906115, 906152, 906167, 906175, 906341, 906408, 906419,
                    906422, 906428, 906432, 906484, 906495, 906529, 906561, 906661, 906664, 906677, 906685, 906708, 906713, 906734, 906750, 906769,
                    906829, 907026, 907031, 907060, 907068, 907072, 907084, 907085, 907086, 907094, 907136, 907142, 907143, 907235, 907253, 907315,
                    907333, 907359, 907391, 907396, 907403, 907405, 907406, 907411, 907414, 907415, 907420, 907421, 907422, 907467, 907495, 907649,
                    907666, 907667, 907696, 907759, 907777, 907778, 907782, 907784, 907790, 907791, 907820, 907955, 907958, 907961, 907982, 908025,
                    908032, 908041, 908088, 908093, 908121, 908127, 908128, 908131, 908132, 908134, 908157, 908237, 908289, 908291, 908292, 908296,
                    908348, 908363, 908366, 908373, 908405, 908412, 908415, 908417, 908418, 908420, 908436, 908486, 908509, 908612, 908614, 908616,
                    908630, 908634, 908651, 908656, 908696, 908738, 908743, 908748, 908755, 908761, 908800, 908827, 908828, 908975, 908977, 908982,
                    909042, 909044, 909049, 909059, 909066, 909071, 909075, 909081, 909102, 909113, 909116, 909179, 909182, 909210, 909252, 909286,
                    909299, 909305, 909309, 909319, 909337, 909365, 909382, 909385, 909390, 909398, 909406, 909411, 909422, 909428, 909430, 909431,
                    909502, 909578, 909603, 909644, 909645, 909685, 909706, 909709, 909472, 909482, 913478, 913518, 904455, 902946, 904062, 913330,
                    909735, 909753, 909765, 909766, 909769, 909805, 909807, 909859, 909932, 909968, 910022, 910029, 910030, 910037, 910041, 910049,
                    910068, 910069, 910076, 910080, 910082, 910096, 910133, 910149, 910211, 910242, 910244, 910304, 910333, 910337, 910345, 910350,
                    910375, 910379, 910385, 910387, 910390, 910397, 910398, 910401, 910445, 910548, 910562, 910578, 910595, 910597, 910639, 910640,
                    910651, 910659, 910667, 910670, 910688, 910702, 910717, 910718, 910719, 910768, 910782, 910810, 910874, 910884, 910934, 910961,
                    910970, 910971, 910984, 910986, 911039, 911051, 911052, 911053, 911072, 911092, 911100, 911116, 911136, 911174, 911175, 911220,
                    911328, 911333, 911336, 911346, 911370, 911374, 911376, 911385, 911423, 911472, 911504, 911508, 911516, 911531, 911551, 911571,
                    911572, 911580, 911609, 911618, 911631, 911644, 911651, 911655, 911657, 911660, 911661, 911669, 911674, 911714, 911716, 911719,
                    911721, 911734, 911768, 911769, 911770, 729389, 723380, 911529, 904802, 905100, 905438, 904469, 904464, 904113, 902986, 902981,
                    911809, 911787, 911790, 903475, 904918, 903956, 908630, 905337, 911735, 911714, 911674, 907010, 906658, 905019, 902163, 900536,
                    908445, 909962, 911529, 909042, 905191, 904182, 905089, 903841, 909867, 909484, 908828, 905500, 911280, 908068, 910630, 911282,
                    911574, 912078, 912063, 911999, 911973, 911970, 912288, 912563, 912281, 911946, 912285, 914107, 914151, 914136, 914112, 914106,
                    914136, 914117, 914116, 914115, 914121, 914126, 913622, 912664, 911148, 909983, 914120, 914128, 914134, 913427, 913260, 913521,
                    914134, 914101, 904725, 913726, 900853, 900372, 899609, 905096, 912345, 911892, 910874, 907784, 907414, 907791, 908139, 907069,
                    913695, 913742, 913554, 913773, 913779, 913788, 913599, 913590, 914419, 914415, 914410, 914408, 913787, 913791, 913603, 913822,
                    910265, 905263, 914055, 914383, 914373, 904849, 904828, 914283, 914272, 914273, 914275, 905094, 905210, 914295, 912577, 911328,
                    904512, 914770, 914776, 914749, 914744, 914151, 914427, 914076, 914419, 913824, 913852, 905076, 914057, 914383, 914373, 914353,
                    914343, 904413


            ));
            list.parallelStream().forEach(i -> {
                PornMovie one = pornMovieService.getOne(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getVideoId, i).last(" limit 1 "));
                if (one == null) {
                    pornMovieService.save(PornMovie.builder().videoId(i).build());
                }
            });
            ArrayList<String> authorList = new ArrayList<String>(Arrays.asList(
                    "下面有跟棒棒糖", "利达LiD", "锅锅酱", "MM陪玩-Mrtow", "EDC.", "Black_Snake", "Mrtow", "重生之我是楚留香", "大铁棍子捅主任"
                    , "91斯文禽兽x", "gc155", "91小腰精", "sunnygubby", "腰精武小姐", "91shishi", "肤絲即肉絲", "ENYQ", "waldeinsamkeit2000"
                    , "diwan98", "爱她-小二先生", "嘉嘉", "天师的淫妻俱乐部", "MRtu", "LOEWE_END", "swfbxn106", "lookoo", "算啊会", "韦小宝呀"
                    , "佳多饱", "Dom窒息", "烈Retsu_dao", "91斯文禽兽x", "西门庆在91", "Timepasserby", "阿森aipapa", "你的白月光", "拆二代Cc"
                    , "spidersex", "猫先生", "小拾柒", "做爱不会累", "cxs6687", "ADC一串三啊"
            ));
            List<PornMovie> pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda().in(PornMovie::getAuthor, authorList));
            pornMovieList.addAll(pornMovieService.list(new QueryWrapper<PornMovie>().lambda().in(PornMovie::getVideoId, list.stream().distinct().collect(Collectors.toList()))));
            pornMovieList = pornMovieList.parallelStream().filter(e -> e != null && Arrays.stream("success_PASS,success_DOWNLOAD_UNCOMPELETE".split(",")).noneMatch(sta -> StringUtils.equals(sta, e.getStatus()))).distinct().collect(Collectors.toList());
            pornMovieList.parallelStream().forEach(pornMovie -> {
                System.out.println(Thread.currentThread().getName() + pornMovie.toString());
                try {
                    hsexPornService.downPornMovie(pornMovie);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return Result.success();
        }
        List<PornMovie> pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .in(PornMovie::getStatus, Arrays.stream("unfinish,processing,fail".split(",")).collect(Collectors.toList()))
                .eq(videoId != null, PornMovie::getVideoId, videoId)
                .eq(StringUtils.isNotEmpty(author), PornMovie::getAuthor, author)
                .orderByDesc(PornMovie::getVideoId)
        ).stream().collect(Collectors.toList());
        pornMovieList.parallelStream().forEach(pornMovie -> {
            pornMovieService.update(new UpdateWrapper<PornMovie>().lambda()
                    .set(PornMovie::getStatus, "processing")
                    .set(PornMovie::getScore, 10)
                    .eq(PornMovie::getVideoId, pornMovie.getVideoId()));
        });
        pornMovieList.parallelStream().map(PornMovie::getAuthor).collect(Collectors.toSet()).parallelStream().forEach(auth -> {
            int movieCount = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, auth));
            int movieCountUnfinish = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, auth).eq(PornMovie::getStatus, "unfinish"));
            pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                    .set(PornUser::getMovieCount, movieCount)
                    .set(PornUser::getMovieCountUnfinish, movieCountUnfinish)
                    .eq(PornUser::getAuthor, auth));
        });
        return Result.success();
    }

    @GetMapping("/thumbList")
    public Result<IPage<PornMovie>> thumbList(PornUser pornUser, String status, @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                              @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        Page<PornMovie> page = new Page<>(pageNo, pageSize);
        IPage<PornMovie> iPage = pornMovieService.page(page, new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, pornUser.getAuthor())
                .eq(StringUtils.isNotBlank(status), PornMovie::getStatus, status)
                .orderByDesc(PornMovie::getVideoId)
        );
        iPage.getRecords().parallelStream().forEach(pornMovie -> {
            pornMovie.setThumb(String.format(PornMovieController.THUMB_URL, serverPort, pornMovie.getVideoId()));
            pornMovie.setThumbmp4(String.format(PornMovieController.THUMB_MP4_URL, serverPort, pornMovie.getVideoId()));
        });
        new Thread(() -> {
            int movieCount = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, pornUser.getAuthor()));
            int movieCountUnfinish = pornMovieService.count(new QueryWrapper<PornMovie>().lambda().eq(PornMovie::getAuthor, pornUser.getAuthor()).eq(PornMovie::getStatus, "unfinish"));
            pornUserService.update(new UpdateWrapper<PornUser>().lambda()
                    .set(PornUser::getMovieCount, movieCount)
                    .set(PornUser::getMovieCountUnfinish, movieCountUnfinish)
                    .eq(PornUser::getAuthor, pornUser.getAuthor()));
        }).start();
        return Result.success(iPage);
    }

//    @GetMapping("/insertUser")
//    public Result insertUser() {//http://localhost:7085/pornMovie/insertUser
//        List<String> authorList = pornUserService.list(new QueryWrapper<PornUser>().lambda().select(PornUser::getAuthor)).stream().map(PornUser::getAuthor).collect(Collectors.toList());
//        authorList = pornMovieService.list(new QueryWrapper<PornMovie>().select(" distinct author ")
//                .lambda()
//                .notIn(PornMovie::getAuthor, authorList)
//        ).stream().map(PornMovie::getAuthor).collect(Collectors.toList());
//        authorList.parallelStream().forEach(author -> {
//            PornUser one = pornUserService.getOne(new QueryWrapper<PornUser>().lambda().eq(PornUser::getAuthor, author));
//            if (one == null) {
//                pornUserService.save(PornUser.builder().author(author).status("unfinish").createMan("system").build());
//            }
//        });
//        System.out.println(authorList);
//        return Result.success(authorList);
//    }

    @GetMapping("/thumbmp4ByRestTemplate")
    public Result thumbmp4ByRestTemplate(Integer videoId) {//http://localhost:7085/pornMovie/thumbmp4ByRestTemplate?videoId=751285
        hsexPornSpiderUtils.thumbmp4ByRestTemplate(videoId);
        return Result.success();
    }

    @GetMapping("/thumbmp4Test")
    public Result thumbmp4Test(Integer videoId) {//http://localhost:7085/pornMovie/thumbmp4Test?videoId=752862
        HsexPornSpiderUtils.thumbmp4(videoId);
        return Result.success();
    }

    @GetMapping("/thumbmp4")
    public Result thumbmp4() {//http://localhost:7085/pornMovie/thumbmp4
        List<PornMovie> pornMovieList = pornMovieService.list(new QueryWrapper<PornMovie>().lambda()
                .select(PornMovie::getVideoId)
                .orderByDesc(PornMovie::getVideoId)
        );
        pornMovieList.stream().forEach(movie -> {
            HsexPornSpiderUtils.thumb(movie.getVideoId());
            HsexPornSpiderUtils.thumbmp4(movie.getVideoId());
        });
        return Result.success();
    }

    @GetMapping("/syncViewCount")
    public Result syncViewCount() {//http://localhost:7085/pornMovie/syncViewCount
        int count = pornMovieService.count();
        int pageSize = 10000;
        int pageNo = count / pageSize + 1;
        ForkJoinPool forkJoinPool = new ForkJoinPool(5);

        forkJoinPool.submit(() -> IntStream.range(1, pageNo + 1).parallel().forEach(i -> {
            Page<PornMovie> page = pornMovieService.page(new Page<>(i, pageSize));
            page.getRecords().stream().parallel().forEach(pornMovie -> {
                int viewCount = hsexPornService.getMovieViewCountByVideoId(pornMovie);
                if (viewCount != pornMovie.getViewCount()) {
                    pornMovieService.update(new UpdateWrapper<PornMovie>().lambda().set(PornMovie::getViewCount, viewCount).eq(PornMovie::getId, pornMovie.getId()));
                }
            });
        }));

        return Result.success();
    }


}
