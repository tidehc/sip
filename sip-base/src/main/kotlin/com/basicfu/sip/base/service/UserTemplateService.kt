package com.basicfu.sip.base.service

import com.basicfu.sip.base.common.Enum
import com.basicfu.sip.base.mapper.UserTemplateMapper
import com.basicfu.sip.base.model.dto.UserTemplateDto
import com.basicfu.sip.base.model.po.UserTemplate
import com.basicfu.sip.base.model.vo.UserTemplateVo
import com.basicfu.sip.core.common.exception.CustomException
import com.basicfu.sip.core.common.mapper.example
import com.basicfu.sip.core.common.mapper.generate
import com.basicfu.sip.core.model.dto.UserDto
import com.basicfu.sip.core.service.BaseService
import com.github.pagehelper.PageInfo
import org.springframework.stereotype.Service

/**
 * @author basicfu
 * @date 2018/6/30
 */
@Service
class UserTemplateService : BaseService<UserTemplateMapper, UserTemplate>() {

    fun list(vo: UserTemplateVo): PageInfo<UserTemplateDto> {
        val pageInfo = selectPage<UserTemplateDto>(example<UserTemplate> {
            orLike {
                name = vo.q
                enName = vo.q
            }
        })
        pageInfo.list.sortBy { it.sort }
        return pageInfo
    }

    fun all(): List<UserTemplateDto> {
        val sortedBy = mapper.selectAll().sortedBy { it.sort }
        return to(sortedBy)
    }

    fun insert(vo: UserTemplateVo): Int {
        if (UserDto::class.java.declaredFields.map { it.name }.contains(vo.enName)) throw CustomException(Enum.UserTemplate.SYSTEM_FIELD)
        if (mapper.selectCount(generate {
                name = vo.name
            }) != 0) throw CustomException(Enum.UserTemplate.FIELD_NAME_EXISTS)
        if (mapper.selectCount(generate {
                enName = vo.enName
            }) != 0) throw CustomException(Enum.UserTemplate.FIELD_EN_NAME_EXISTS)
        val po = dealInsert(to<UserTemplate>(vo))
        return mapper.insertSelective(po)
    }

    fun update(vo: UserTemplateVo): Int {
        if (UserDto::class.java.declaredFields.map { it.name }.contains(vo.enName)) throw CustomException(Enum.UserTemplate.SYSTEM_FIELD)
        val po = dealUpdate(to<UserTemplate>(vo))
        return mapper.updateByPrimaryKeySelective(po)
    }

    fun delete(ids: List<Long>?): Int {
        return deleteByIds(ids)
    }
}
