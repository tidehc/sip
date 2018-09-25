package com.basicfu.sip.base.service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.basicfu.sip.base.common.Enum
import com.basicfu.sip.base.common.Enum.FieldType.*
import com.basicfu.sip.base.mapper.UserAuthMapper
import com.basicfu.sip.base.mapper.UserMapper
import com.basicfu.sip.base.model.po.User
import com.basicfu.sip.base.model.po.UserAuth
import com.basicfu.sip.base.model.vo.UserVo
import com.basicfu.sip.base.util.PasswordUtil
import com.basicfu.sip.client.util.DictUtil
import com.basicfu.sip.core.common.Constant
import com.basicfu.sip.core.common.exception.CustomException
import com.basicfu.sip.core.common.mapper.example
import com.basicfu.sip.core.common.mapper.generate
import com.basicfu.sip.core.model.dto.ResourceDto
import com.basicfu.sip.core.model.dto.UserDto
import com.basicfu.sip.core.service.BaseService
import com.basicfu.sip.core.util.RedisUtil
import com.basicfu.sip.core.util.SqlUtil
import com.basicfu.sip.core.util.TokenUtil
import com.basicfu.sip.core.util.UserUtil
import com.github.pagehelper.PageInfo
import org.apache.ibatis.session.RowBounds
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.SimpleDateFormat

/**
 * @author basicfu
 * @date 2018/6/30
 */
@Service
class UserService : BaseService<UserMapper, User>() {
    @Autowired
    lateinit var userAuthMapper: UserAuthMapper
    @Autowired
    lateinit var userTemplateService: UserTemplateService

    fun getCurrentUser(): JSONObject? {
        var user = TokenUtil.getCurrentUser()
        if (user == null) {
            user = TokenUtil.getGuestUser()
        }
        return UserUtil.toJson(user)
    }

    fun get(id: Long): JSONObject? {
        val result = to<UserDto>(mapper.selectByPrimaryKey(id))
        result?.let {
            val userAuths = userAuthMapper.select(generate {
                uid = id
            })
            val userAuthMap = userAuths.associateBy({ it.type }, { it })
            it.mobile = userAuthMap[1]?.username
            it.email = userAuthMap[2]?.username
            it.ldate = userAuths.map { it.ldate!! }.max()
        }
        return UserUtil.toJson(result)
    }

    fun list(vo: UserVo): PageInfo<JSONObject> {
        val pageList = selectPage<UserDto>(example<User> {
            andLike {
                username = vo.username
            }
            orderByDesc(User::cdate)
        })
        val users = pageList.list
        if (users.isNotEmpty()) {
            val userAuths = userAuthMapper.selectByExample(example<UserAuth> {
                andIn(UserAuth::uid, users.map { it.id })
            }).groupBy({ it.uid }, { it })
            users.forEach {
                val userAuth = userAuths[it.id]
                if (userAuth != null) {
                    val userAuthMap = userAuth.associateBy({ it.type!! }, { it })
                    it.mobile = userAuthMap[1]?.username
                    it.email = userAuthMap[2]?.username
                    it.ldate = userAuth.map { it.ldate!! }.max()
                }
            }
        }
        val result = PageInfo<JSONObject>()
        BeanUtils.copyProperties(pageList, result)
        result.list = UserUtil.toJson(users)
        return result
    }

    fun listByIds(ids: List<Long>): List<JSONObject>? {
        val users = to<UserDto>(mapper.selectByExample(example<User> {
            andIn(User::id, ids)
        }))
        if (users.isNotEmpty()) {
            val userAuths = userAuthMapper.selectByExample(example<UserAuth> {
                andIn(UserAuth::uid, users.map { it.id })
            }).groupBy({ it.uid }, { it })
            users.forEach {
                val userAuth = userAuths[it.id]
                if (userAuth != null) {
                    val userAuthMap = userAuth.associateBy({ it.type!! }, { it })
                    it.mobile = userAuthMap[1]?.username
                    it.email = userAuthMap[2]?.username
                    it.ldate = userAuth.map { it.ldate!! }.max()
                }
            }
        }
        return UserUtil.toJson(users)
    }

    fun listUsernameByIds(ids: List<Long>): List<JSONObject>? {
        val users = to<UserDto>(mapper.selectByExample(example<User> {
            select(User::id, User::username)
            andIn(User::id, ids)
        }))
        return UserUtil.toJson(users)
    }

    /**
     * type
     * ROLE:按照角色code查询,如果使用按角色查询需保证permission和base在一个数据库地址中
     * ALL:(default)包含以下三种登录方式
     * USERNAME:只按照用户名
     * MOBILE:只按照手机号
     * EMAIL:只按照邮箱
     */
    fun suggest(q: String, roleCode: String?, size: Int): List<JSONObject>? {
        //可测试性能
//        mapper.selectBySql("SELECT u.id,u.tenant_id,u.content,u.cdate,u.udate,status,ua.username,ua.type from user u LEFT JOIN user_auth ua on u.id=ua.uid INNER JOIN (select DISTINCT(uid) from user_auth WHERE username like '%1%' limit 10) as sub on u.id=sub.uid;")
        val userIds: List<Long>
        if (roleCode.isNullOrBlank()) {
            userIds = userAuthMapper.selectByExampleAndRowBounds(example<UserAuth> {
                select(UserAuth::uid)
                distinct()
                andLike {
                    username = q
                }
            }, RowBounds(0, size)).mapNotNull { it.uid }
        } else {
            val users = mapper.selectBySql(
                "SELECT DISTINCT uid as id FROM `sip-base`.user_auth ua " +
                        "RIGHT JOIN `sip-permission`.user_role ur on ua.uid=ur.user_id " +
                        "LEFT JOIN `sip-permission`.role r on ur.role_id=r.id " +
                        "WHERE ua.username LIKE ${SqlUtil.dealLikeValue(q)} AND r.code='$roleCode' LIMIT 0,$size"
            )
            userIds = users.map { it.id!! }
        }
        val users = to<UserDto>(selectByIds(userIds))
        if (users.isNotEmpty()) {
            val userAuths = userAuthMapper.selectByExample(example<UserAuth> {
                select(UserAuth::uid, UserAuth::type, UserAuth::username, UserAuth::ldate)
                andIn(UserAuth::uid, users.map { it.id })
            }).groupBy({ it.uid }, { it })
            users.forEach {
                val userAuth = userAuths[it.id]
                if (userAuth != null) {
                    val userAuthMap = userAuth.associateBy({ it.type!! }, { it })
                    it.mobile = userAuthMap[1]?.username
                    it.email = userAuthMap[2]?.username
                    it.ldate = userAuth.map { it.ldate!! }.max()
                }
            }
        }
        return UserUtil.toJson(users)
    }

    /**
     * 登录
     * 用户名登录
     * 后期密码使用加密后的值
     * TODO 登录后清除该用户其他token
     * TODO 界面配置登录模式
     * 模式一：系统配置允许用户同时在线用户数,如1，再次登录只能修改密码
     * 模式二：每次登录后上次用户自动过期，登录后清除该用户其他token
     * TODO 校验手机和邮箱拥有者后可开启手机或邮箱登录
     * TODO 登录查询表过多，可优化连为连表查询
     */
    fun login(vo: UserVo): JSONObject? {
        val userAuth = userAuthMapper.selectOne(generate {
            username = vo.username
            type = 0
        }) ?: throw CustomException(Enum.User.USERNAME_OR_PASSWORD_ERROR)
        if (!PasswordUtil.matches(vo.username + vo.password!!, userAuth.password!!)) throw CustomException(
            Enum.User.USERNAME_OR_PASSWORD_ERROR
        )
        //TODO 界面配置登录模式
        val loginModal = 2
        val keys = RedisUtil.keys(TokenUtil.getRedisUserTokenPrefix(vo.username!!) + "*")
        @Suppress("ConstantConditionIf")
        if (loginModal == 1) {
            if (keys.size >= 2) {
                throw CustomException(Enum.User.THEN_USER_MAX_ONLINE)
            }
        } else {
            RedisUtil.del(keys.map { it })
        }
        val user = to<UserDto>(mapper.selectByPrimaryKey(userAuth.uid))
        val currentTime = (System.currentTimeMillis() / 1000).toInt()
        userAuthMapper.updateByPrimaryKeySelective(generate {
            id = userAuth.id
            ldate = currentTime
        })
        val permission =
            com.basicfu.sip.client.util.UserUtil.getPermissionByUidJson(user!!.id!!) ?: throw CustomException(
                Enum.User.LOGIN_ERROR
            )
        val userAuths = userAuthMapper.select(generate {
            uid = userAuth.uid
        }).associateBy({ it.type }, { it })
        user.mobile = userAuths[1]?.username
        user.email = userAuths[2]?.username
        user.ldate = currentTime
        user.roles = permission.getJSONArray("roles")
        user.menus = permission.getJSONArray("menus")
        user.permissions = permission.getJSONArray("permissions")
        user.resources = permission.getJSONArray("resources").toJavaList(ResourceDto::class.java)
            .groupBy({ it.serviceId.toString() }, { "/" + it.method + it.url })
        //TODO 系统设置登录过期时间
        val userToken = TokenUtil.generateUserToken(vo.username!!)
        val redisToken = TokenUtil.getRedisToken(userToken)
        RedisUtil.set(redisToken, user, Constant.System.SESSION_TIMEOUT)
        val frontToken = TokenUtil.generateFrontToken(userToken) ?: throw CustomException(Enum.User.LOGIN_ERROR)
        val result = JSONObject()
        result["token"] = frontToken
        result["time"] = System.currentTimeMillis() / 1000
        result["username"] = user.username
        result["roles"] = user.roles
        return result
    }

    /**
     * 登出
     * TODO 如果是模式2需要移除所有token
     */
    fun logout() {
        TokenUtil.getCurrentToken()?.let {
            RedisUtil.del(Constant.Redis.TOKEN_PREFIX + it)
        }
    }

    /**
     * 用户模板在添加时已做强制校验就检查过默认值
     */
    fun insert(map: Map<String, Any>): Int {
        val vo = UserUtil.toUser<UserVo>(map)
        //检查用户名重复
        if (userAuthMapper.selectCount(generate {
                username = vo.username
            }) > 0) throw CustomException(Enum.User.EXIST_USER)
        //添加用户
        val user = dealInsert(generate<User> {
            username = vo.username
            content = dealUserTemplate(vo.content).toJSONString()
            type = 2
        })
        mapper.insertSelective(user)
        //处理用户角色
        if (vo.roleIds!=null) {
            com.basicfu.sip.client.util.UserUtil.updateRole(user.id!!, vo.roleIds!!)
        }
        //添加用户授权
        val userAuth = dealInsert(generate<UserAuth> {
            uid = user.id
            username = vo.username
            password = BCryptPasswordEncoder().encode(vo.username + vo.password)
            type = 0
        })
        userAuthMapper.insertSelective(userAuth)
        //mobile、phone特殊处理
        if (vo.mobile != null) {
            userAuthMapper.insertSelective(dealInsert(generate<UserAuth> {
                uid = user.id
                username = vo.mobile
                password = BCryptPasswordEncoder().encode(vo.mobile + vo.password)
                type = 1
            }))
        }
        if (vo.email != null) {
            userAuthMapper.insertSelective(dealInsert(generate<UserAuth> {
                uid = user.id
                username = vo.email
                password = BCryptPasswordEncoder().encode(vo.email + vo.password)
                type = 2
            }))
        }
        return 1
    }

    fun update(map: Map<String, Any>): Int {
        val vo = UserUtil.toUser<UserVo>(map)
        //检查用户名重复
        val checkUsername = mapper.selectOne(generate {
            username = vo.username
        })
        if (checkUsername != null && checkUsername.id != vo.id) throw CustomException(Enum.User.EXIST_USER)
        //更新用户内容
        mapper.updateByPrimaryKeySelective(dealUpdate(generate<User> {
            id = vo.id
            content = dealUserTemplate(vo.content).toJSONString()
        }))
        //处理用户角色
        if (vo.roleIds!=null) {
            com.basicfu.sip.client.util.UserUtil.updateRole(vo.id!!, vo.roleIds!!)
        }
        //更新用户授权
        //判断用户密码和上次是否一致，不一致进行更新
        val userAuths = userAuthMapper.select(generate {
            uid = vo.id
        }).associateBy({ it.type }, { it })
        val usernameAuth = userAuths[0]!!
        val mobileAuth = userAuths[1]
        val emailAuth = userAuths[2]
        if (!vo.password.isNullOrBlank() && !PasswordUtil.matches(
                vo.username + vo.password!!,
                usernameAuth.password!!
            )
        ) {
            userAuthMapper.updateByPrimaryKeySelective(dealUpdate(generate<UserAuth> {
                id = usernameAuth.id
                password = BCryptPasswordEncoder().encode(vo.username + vo.password)
            }))
        }
        //mobile、phone特殊处理
        if (vo.mobile != null) {
            if (mobileAuth == null) {
                userAuthMapper.insertSelective(dealInsert(generate<UserAuth> {
                    uid = vo.id
                    username = vo.mobile
                    password = BCryptPasswordEncoder().encode(vo.mobile + vo.password)
                    type = 1
                }))
            } else if (vo.mobile != mobileAuth.username) {
                userAuthMapper.updateByPrimaryKeySelective(dealUpdate(generate<UserAuth> {
                    id = mobileAuth.id
                    username = vo.mobile
                    password = BCryptPasswordEncoder().encode(vo.mobile + vo.password)
                }))
            }
        } else if (mobileAuth != null) {
            userAuthMapper.deleteByPrimaryKey(mobileAuth.id)
        }
        if (vo.email != null) {
            if (emailAuth == null) {
                userAuthMapper.insertSelective(dealInsert(generate<UserAuth> {
                    uid = vo.id
                    username = vo.email
                    password = BCryptPasswordEncoder().encode(vo.email + vo.password)
                    type = 2
                }))
            } else if (vo.email != emailAuth.username) {
                userAuthMapper.updateByPrimaryKeySelective(dealUpdate(generate<UserAuth> {
                    id = emailAuth.id
                    username = vo.email
                    password = BCryptPasswordEncoder().encode(vo.email + vo.password)
                }))
            }
        } else if (emailAuth != null) {
            userAuthMapper.deleteByPrimaryKey(emailAuth.id)
        }
        return 1
    }

    fun delete(ids: List<Long>?): Int {
        return deleteByIds(ids)
    }

    /**
     * 处理json串内容
     */
    fun dealUserTemplate(contentJson: JSONObject): JSONObject {
        //获取该租户下的用户模板信息,传过来空值也要根据模板处理默认值
        val userTemplateList = userTemplateService.all()
        val contentResult = JSONObject()
        userTemplateList.forEach { it ->
            val extra = it.extra!!
            val enName = it.enName!!
            val required = it.required!!
            val name = it.name!!
            val defaultValue = it.defaultValue!!
            val value = contentJson.getString(enName)
            when (valueOf(it.type!!)) {
                TEXT -> {
                    if (value == null) {
                        if (required) {
                            throw CustomException("字段名[$name]不能为空")
                        }
                        contentResult[enName] = defaultValue
                    } else {
                        val extraArray = extra.split("~")
                        val startLength = extraArray[0].toInt()
                        val endLength = extraArray[1].toInt()
                        if (value.length !in startLength..endLength) {
                            throw CustomException("字段名[$name]长度需要[$startLength~$endLength]个字符")
                        }
                        contentResult[enName] = value
                    }
                }
                NUMBER -> {
                    if (value == null) {
                        if (required) {
                            throw CustomException("字段名[$name]不能为空")
                        }
                        contentResult[enName] = defaultValue
                    } else {
                        val extraArray = extra.split("&")
                        val lengthRange = extraArray[0].split(",")
                        val valueRange = extraArray[1].split("~")
                        if (!value.isNullOrBlank()) {
                            val startLength = lengthRange[0].toInt()
                            val endLength = lengthRange[1].toInt()
                            val startValue = valueRange[0].toFloat()
                            val endValue = valueRange[1].toFloat()
                            val splitValue = value.split(".")
                            if (splitValue.size == 1) {
                                //移除符号判断长度
                                if (Math.abs(splitValue[0].toLong()).toString().length !in 1..startLength) {
                                    throw CustomException("字段名[$name]整数位需要[1~$startLength]位")
                                }
                                if (splitValue[0].toLong() !in startValue..endValue) {
                                    throw CustomException(
                                        "字段名[$name]值范围需要[${BigDecimal(startValue.toString()).setScale(
                                            endLength
                                        )}~${BigDecimal(endValue.toString()).setScale(endLength)}]之间"
                                    )
                                }
                                contentResult[enName] = splitValue[0]
                            }
                            if (splitValue.size == 2) {
                                if (Math.abs(splitValue[0].toLong()).toString().length !in 1..startLength) {
                                    throw CustomException("字段名[$name]整数位需要[1~$startLength]位")
                                }
                                if (splitValue[1].length > endLength) {
                                    throw CustomException("字段名[$name]小数位不能大于$endLength]位")
                                }
                                //小数为不够自动补0
                                val floatValue = BigDecimal(value).setScale(endLength).toFloat()
                                if (floatValue !in startValue..endValue) {
                                    throw CustomException(
                                        "字段名[$name]值范围需要[${BigDecimal(startValue.toString()).setScale(
                                            endLength
                                        )}~${BigDecimal(endValue.toString()).setScale(endLength)}]之间"
                                    )
                                }
                                contentResult[enName] = floatValue
                            }
                        }
                    }
                }
            //不支持默认值、必选
                CHECK -> {
                    if (value == null) {
                        contentResult[enName] = "[]"
                    } else {
                        val dictMap = DictUtil.getMap(extra)
                        JSON.parseArray(value).forEach { item ->
                            if (!dictMap.containsKey(item.toString())) {
                                throw CustomException("找不到字典[$extra]下为[$item]的值")
                            }
                        }
                        contentResult[enName] = value
                    }
                }
            //不支持默认值、必选
                RADIO -> {
                    if (value == null) {
                        contentResult[enName] = ""
                    } else {
                        val dictMap = DictUtil.getMap(extra)
                        if (!dictMap.containsKey(value.toString())) {
                            throw CustomException("找不到字典[$extra]下为[$value]的值")
                        }
                        contentResult[enName] = value
                    }
                }
            //下拉不支持默认值，支持必选
                SELECT -> {
                    if (value == null) {
                        if (required) {
                            throw CustomException("字段名[$name]不能为空")
                        }
                        contentResult[enName] = ""
                    } else {
                        val dictMap = DictUtil.getMap(extra)
                        if (!dictMap.containsKey(value.toString())) {
                            throw CustomException("找不到字典[$extra]下为[$value]的值")
                        }
                        contentResult[enName] = value
                    }
                }
                DATE -> {
                    if (value == null) {
                        if (required) {
                            throw CustomException("字段名[$name]不能为空")
                        }
                        contentResult[enName] = ""
                    } else {
                        val sdf = SimpleDateFormat(extra)
                        contentResult[enName] = sdf.format(sdf.parse(value))
                    }
                }
            }
        }
        return contentResult
    }
}
