package com.stc.fugitive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PornMovieDto {


    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "时长")
    private String duration;

    @ApiModelProperty(value = "时长(秒)")
    private Integer durationSecond;

    @ApiModelProperty(value = "时长")
    private String ffmpegDuration;

    @ApiModelProperty(value = "时长(秒)")
    private Integer ffmpegDurationSecond;

    @ApiModelProperty(value = "时长百分比")
    private BigDecimal durationRate;

    @ApiModelProperty(value = "视频大小")
    private Long videoLength;

    @ApiModelProperty(value = "添加时间")
    private String addTime;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "视频评分")
    private Integer score;

    @ApiModelProperty(value = "作者uid")
    private String authorUid;

    @ApiModelProperty(value = "观看次数")
    private Integer viewCount;

    @ApiModelProperty(value = "收藏数")
    private Integer collect;

    @ApiModelProperty(value = "留言数")
    private Integer messageNumber;

    @ApiModelProperty(value = "积分")
    private Integer integration;

    @ApiModelProperty(value = "页面地址")
    private String pageDetailUrl;

    @ApiModelProperty(value = "下载地址")
    private String downloadUrl;

    @ApiModelProperty(value = "视频ID")
    private Integer videoId;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "key")
    @TableField("`key`")
    private String key;

    @ApiModelProperty(value = "添加时间")
    private Date addDateTime;

    @ApiModelProperty(value = "状态")//unfinish,processing,pending,fail,success
    private String status;

    @ApiModelProperty(value = "创建者姓名")
    private String createMan;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新者")
    private String updateMan;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField(exist = false)
    private String thumb;
    @TableField(exist = false)
    private String thumbmp4;
    @TableField(exist = false)
    private String filePath;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date createTimeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date createTimeEnd;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date updateTimeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date updateTimeEnd;

    @TableField(exist = false)
    private PornUser pornUser;
}
