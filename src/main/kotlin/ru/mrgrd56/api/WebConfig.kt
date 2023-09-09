package ru.mrgrd56.api

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.apply {
            addMapping("/ip").allowCors(allowWrite = false)
            addMapping("/time/**").allowCors(allowWrite = false)

            addMapping("/mock/**").allowCors(allowWrite = true)

//            addMapping("/proxy/**").apply {
//                allowedOrigins()
//                allowedOriginPatterns()
//            }

//            addMapping("/**").allowCors(allowWrite = true, "*.mrgrd56.ru")
        }
    }
}

private fun CorsRegistration.allowCors(allowWrite: Boolean = false, origin: String = "*") {
    allowedOriginPatterns(origin)
    if (allowWrite) {
        allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    } else {
        allowedMethods("GET", "HEAD", "OPTIONS")
    }
    allowCredentials(true)
}
