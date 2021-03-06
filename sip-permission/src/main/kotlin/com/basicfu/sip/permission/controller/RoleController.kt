package com.basicfu.sip.permission.controller

import com.basicfu.sip.core.model.Result
import com.basicfu.sip.permission.model.vo.RoleVo
import com.basicfu.sip.permission.service.RoleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 * @author basicfu
 * @date 2018/7/9
 */
@RestController
@RequestMapping("/role")
class RoleController {
    @Autowired
    lateinit var roleService: RoleService

    @GetMapping("/get/permission/{uid}")
    fun getPermissionByUid(@PathVariable uid: Long): Result<Any> {
        return Result.success(roleService.getPermissionByUid(uid))
    }

    @GetMapping("/list")
    fun list(vo: RoleVo): Result<Any> {
        return Result.success(roleService.list(vo))
    }

    @GetMapping("/all")
    fun all(): Result<Any> {
        return Result.success(roleService.all())
    }

    @PostMapping("/insert")
    fun insert(@RequestBody vo: RoleVo): Result<Any> {
        return Result.success(roleService.insert(vo))
    }

    @PostMapping("/insert/user")
    fun insertUser(@RequestBody vo: RoleVo): Result<Any> {
        return Result.success(roleService.insertUser(vo))
    }

    @PostMapping("/insert/menu")
    fun insertMenu(@RequestBody vo: RoleVo): Result<Any> {
        return Result.success(roleService.insertMenu(vo))
    }

    @PostMapping("/insert/permission")
    fun insertPermission(@RequestBody vo: RoleVo): Result<Any> {
        return Result.success(roleService.insertPermission(vo))
    }

    @PostMapping("/update")
    fun update(@RequestBody vo: RoleVo): Result<Any> {
        return Result.success(roleService.update(vo))
    }

    @DeleteMapping("/delete")
    fun delete(ids: List<Long>): Result<Any> {
        return Result.success(roleService.delete(ids))
    }

    @DeleteMapping("/delete/user")
    fun deleteUser(vo: RoleVo): Result<Any> {
        return Result.success(roleService.deleteUser(vo))
    }

    @DeleteMapping("/delete/menu")
    fun deleteMenu(vo: RoleVo): Result<Any> {
        return Result.success(roleService.deleteMenu(vo))
    }

    @DeleteMapping("/delete/permission")
    fun deletePermission(vo: RoleVo): Result<Any> {
        return Result.success(roleService.deletePermission(vo))
    }
}
