package com.basicfu.sip.client.feign

import com.alibaba.fastjson.JSONObject
import com.basicfu.sip.client.model.Result
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(value = "sip-permission", url = "\${sip.permission.url:}")
interface RoleFeign {
    @GetMapping("/role/get/permission/{uid}")
    fun getPermissionByUid(@PathVariable("uid") uid: Long): Result<JSONObject>

    @PostMapping("/user/update/role")
    fun updateRole(@RequestParam("id") id: Long, @RequestParam("roleIds") roleIds: Array<Long>): Result<JSONObject>

}
