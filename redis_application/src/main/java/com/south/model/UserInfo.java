package com.south.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 用户信息表
 */
@Data
@TableName(value = "t_user_info")
public class UserInfo {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 用户名
     */
    @TableField(value = "user_name")
    private String userName;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 修改时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 最后修改人
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 删除标识：0：未删除 1：已删除
     */
    @TableField(value = "deleted")
    private Byte deleted;

    public static final String COL_ID = "id";

    public static final String COL_PHONE = "phone";

    public static final String COL_USER_NAME = "user_name";

    public static final String COL_CREATED_AT = "created_at";

    public static final String COL_UPDATED_AT = "updated_at";

    public static final String COL_CREATE_BY = "create_by";

    public static final String COL_UPDATE_BY = "update_by";

    public static final String COL_DELETED = "deleted";
}