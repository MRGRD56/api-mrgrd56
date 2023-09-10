package ru.mrgrd56.api

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.apply {
            addMapping("/ip").allowCors(pattern = "*", allowWrite = false)
            addMapping("/time/**").allowCors(pattern = "*", allowWrite = false)

            addMapping("/mock/**").allowCors(pattern = "*", allowWrite = true)

//            addMapping("/proxy/**").apply {
//                allowedOrigins()
//                allowedOriginPatterns()
//            }

//            addMapping("/**").allowCors(allowWrite = true, "*.mrgrd56.ru")
        }
    }
}

private fun CorsRegistration.allowCors(
    pattern: String? = null,
    origin: String? = null,
    allowWrite: Boolean = false,
    allowCredentials: Boolean = false
) {
    if (pattern != null) {
        allowedOriginPatterns(pattern)
    }

    if (origin != null) {
        allowedOrigins(origin)
    }

    if (allowWrite) {
        allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    } else {
        allowedMethods("GET", "HEAD", "OPTIONS")
    }

    if (allowCredentials) {
        allowCredentials(true)
    } else {
        allowedHeaders("*")
    }
}
