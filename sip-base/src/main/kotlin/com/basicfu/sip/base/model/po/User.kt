package com.basicfu.sip.base.model.po

import javax.persistence.*

class User {
    /**
     * @return id
     */
    /**
     * @param id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    /**
     * 租户ID
     */
    /**
     * 获取租户ID
     *
     * @return app_id - 租户ID
     */
    /**
     * 设置租户ID
     *
     * @param appId 租户ID
     */
    @Column(name = "app_id")
    var appId: Long? = null

    /**
     * 用户名
     */
    /**
     * 获取用户名
     *
     * @return username - 用户名
     */
    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    var username: String? = null
    var nickname: String? = null

    /**
     * 用户json信息(mysql8)
     */
    /**
     * 获取用户json信息(mysql8)
     *
     * @return content - 用户json信息(mysql8)
     */
    /**
     * 设置用户json信息(mysql8)
     *
     * @param content 用户json信息(mysql8)
     */
    var content: String? = null

    /**
     * 创建时间
     */
    /**
     * 获取创建时间
     *
     * @return cdate - 创建时间
     */
    /**
     * 设置创建时间
     *
     * @param cdate 创建时间
     */
    var cdate: Int? = null

    /**
     * 更新时间
     */
    /**
     * 获取更新时间
     *
     * @return udate - 更新时间
     */
    /**
     * 设置更新时间
     *
     * @param udate 更新时间
     */
    var udate: Int? = null
    var cuid: Long? = null

    /**
     * 用户类型0系统用户,1租户,2普通用户
     */
    /**
     * 获取用户类型0系统用户,1租户,2普通用户
     *
     * @return type - 用户类型0系统用户,1租户,2普通用户
     */
    /**
     * 设置用户类型0系统用户,1租户,2普通用户
     *
     * @param type 用户类型0系统用户,1租户,2普通用户
     */
    var type: String? = null

    /**
     * 0正常,1删除,2黑名单
     */
    /**
     * 获取0正常,1删除,2黑名单
     *
     * @return status - 0正常,1删除,2黑名单
     */
    /**
     * 设置0正常,1删除,2黑名单
     *
     * @param status 0正常,1删除,2黑名单
     */
    var status: Int? = null
}
