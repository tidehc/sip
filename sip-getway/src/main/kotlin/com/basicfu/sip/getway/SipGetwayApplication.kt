package com.basicfu.sip.getway

import com.basicfu.sip.getway.filter.LogFilter
import com.basicfu.sip.getway.filter.PermissionFilter
import org.springframework.boot.SpringApplication
import org.springframework.cloud.client.SpringCloudApplication
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = ["com.basicfu.sip"])
@EnableZuulProxy
@SpringCloudApplication
@EnableFeignClients
class SipGetwayApplication {
    @Bean
    fun permissionFilter(): PermissionFilter {
        return PermissionFilter()
    }

    @Bean
    fun logFilter(): LogFilter {
        return LogFilter()
    }

//    @Bean
//    fun corsFilter(): CorsFilter {
//        val source = UrlBasedCorsConfigurationSource()
//        val config = CorsConfiguration()
//        config.allowCredentials = true
//        config.addAllowedOrigin("*")
//        config.addAllowedHeader("*")
//        config.addAllowedMethod("*")
//        source.registerCorsConfiguration("/**", config)
//        return CorsFilter(source)
//    }
}

fun main(args: Array<String>) {
    SpringApplication.run(SipGetwayApplication::class.java, *args)
}
