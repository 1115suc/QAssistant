package course.QAssistant.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 * @TableName sys_user
 */
@TableName(value ="sys_user")
@Data
public class SysUser {
    /**
     * 用户id
     */
    @TableId
    private Long id;

    /**
     * 账户
     */
    private String username;

    /**
     * 用户密码密文
     */
    private String password;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 真实名称
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱(唯一)
     */
    private String email;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String description;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 是否允许添加好友(0.同意后加好友 1.直接加好友 2.不允许加好友)
     */
    private Integer addFriends;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 地区名称
     */
    private String areaName;

    /**
     * 地区编码
     */
    private String areaCode;

    /**
     * 性别(0.未知 1.男 2.女)
     */
    private Integer sex;

    /**
     * 账户状态(0.正常 1.锁定 )
     */
    private Integer status;

    /**
     * 是否删除(0未删除；1已删除)
     */
    private Integer deleted;

    /**
     * 创建来源(1.web 2.android 3.ios )
     */
    private Integer createWhere;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}