package com.basicfu.sip.base.controller

import com.basicfu.sip.base.model.vo.UserVo
import com.basicfu.sip.base.service.UserService
import com.basicfu.sip.core.model.Result
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 * @author basicfu
 * @date 2018/7/4
 */
@RestController
@RequestMapping("/user")
class UserController {
    @Autowired
    lateinit var userService: UserService

    @PostMapping("/insert")
    fun insert(@RequestBody vo: UserVo): Result<Any> {
        return Result(userService.insert(vo))
    }
    @PostMapping("/register")
    fun register(@RequestBody vo: UserVo): Result<Any> {
        return Result(userService.insert(vo))
    }
    @GetMapping("/update")
    fun update(@RequestBody vo: UserVo): Result<Any> {
        return Result(userService.update(vo))
    }
//
//    @PostMapping("/insert")
//    fun insert(vo: UserTemplateVo): Result<Any> {
//        return Result(userTemplateService.insert(vo))
//    }
//
//    @PostMapping("/update")
//    fun update(vo: UserTemplateVo): Result<Any> {
//        return Result(userTemplateService.update(vo))
//    }
//
//    @DeleteMapping("/delete")
//    fun delete(ids: List<Long>): Result<Any> {
//        return Result(userTemplateService.delete(ids))
//    }
}
