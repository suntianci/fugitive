package com.stc.fugitive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * porn用户表
 * </p>
 *
 * @author suntianci
 * @since 2022-06-11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "PornUser对象", description = "porn用户表")
public class PornUser extends Model<PornUser> {


    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "作者uid")
    private String authorUid;

    @ApiModelProperty(value = "主播作品数")
    private Integer movieCount;

    @ApiModelProperty(value = "主播作品数(未处理)")
    private Integer movieCountUnfinish;

    @ApiModelProperty(value = "主播评分")
    private Integer score;

    @ApiModelProperty(value = "状态")//unfinish,processing,fail,success
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
    private List<String> thumbList;

    @TableField(exist = false)
    private List<PornMovie> pornMovieList;

    //@DateTimeFormat来控制入参，@JsonFormat来控制出参
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date createTimeStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date createTimeEnd;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date updateTimeStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date updateTimeEnd;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
