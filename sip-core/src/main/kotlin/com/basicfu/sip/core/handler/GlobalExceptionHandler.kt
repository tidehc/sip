package com.basicfu.sip.core.handler

import com.basicfu.sip.core.exception.CustomException
import com.basicfu.sip.core.model.Result
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    internal var request: HttpServletRequest? = null
    //加入未授权异常
//    response.setStatus(403)

    /**
     * 全局异常
     */
    @ResponseBody
    @ExceptionHandler(Exception::class)
    private fun errorHandler(response: HttpServletResponse, e: Exception): Result {
        log.error("全局异常：", e)
        response.status = 500
        return Result.error(-1, com.basicfu.sip.core.Enum.SERVER_ERROR.msg)
    }

    /**
     * spring接收参数异常
     */
    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException::class)
    private fun httpMessageNotReadableException(e: HttpMessageNotReadableException): Result {
        log.error("\${Enum.INVALID_PARAMETER.msg}-【msg】--" + e.message)
        return Result.error(com.basicfu.sip.core.Enum.INVALID_PARAMETER.value, com.basicfu.sip.core.Enum.INVALID_PARAMETER.msg)
    }

    /**
     * 自定义异常
     */
    @ResponseBody
    @ExceptionHandler(CustomException::class)
    private fun customException(e: CustomException): Result {
        log.error("自定义异常--【code】--" + e.code + "--【msg】--" + e.msg + "--【data】--" + e.data)
        return Result.error(e.code, e.msg, e.data!!)
    }

    /**
     * 前台传递参数限制异常
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException::class)
    private fun notValidException(e: Exception): Result {
        log.error(e.message)
        return Result.error(com.basicfu.sip.core.Enum.INVALID_PARAMETER.value, com.basicfu.sip.core.Enum.INVALID_PARAMETER.msg)
    }


}
