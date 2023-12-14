package com.stc.fugitive.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * //字段添加填充内容
 *
 * @author suntianci on 2022/6/16
 * @TableField(fill = FieldFill.INSERT)
 * private Date createTime;
 * @TableField(fill = FieldFill.INSERT_UPDATE)
 * private Date updateTime;
 */
@Slf4j//日志
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     * 配合
     *
     * @param metaObject
     * @TableField(fill = FieldFill.INSERT_UPDATE)
     * @TableField(fill = FieldFill.INSERT)
     */
    @Override
    public void insertFill(MetaObject metaObject) {
//        this.setFieldValByName("addTime", new Date(), metaObject);
//        this.setFieldValByName("editTime", new Date(), metaObject);
//        this.setFieldValByName("modifierId", new Long(111), metaObject);
//        this.setFieldValByName("gmtModified", new Date(), metaObject);
//        this.setFieldValByName("creatorId", new Long(111), metaObject);
//        this.setFieldValByName("gmtCreat", new Date(), metaObject);
//        this.setFieldValByName("availableFlag", true, metaObject);
    }

    //更新时的填充策略
    @Override
    public void updateFill(MetaObject metaObject) {
//        this.setFieldValByName("editTime",new Date(),metaObject);
    }
}