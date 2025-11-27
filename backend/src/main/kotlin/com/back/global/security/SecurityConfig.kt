package com.back.global.security

import com.back.global.appConfig.SiteProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val customOAuth2LoginSuccessHandler: CustomOAuth2LoginSuccessHandler,
    private val customOAuth2AuthorizationRequestResolver: CustomOAuth2AuthorizationRequestResolver,
    private val siteProperties: SiteProperties
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http {
            // CORS 활성화
            cors { }

            authorizeHttpRequests {
                authorize("/favicon.ico", permitAll)
                authorize("/h2-console/**", permitAll)

                // Posts GET
                authorize(HttpMethod.GET, "/api/*/posts", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/comments", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/comments/{commentId:\\d+}", permitAll)

                // Member API
                authorize(HttpMethod.POST, "/api/v1/members/login", permitAll)
                authorize(HttpMethod.POST, "/api/v1/members/join", permitAll)
                authorize(HttpMethod.DELETE, "/api/v1/members/logout", permitAll)

                // Admin
                authorize("/api/*/adm/**", hasRole("ADMIN"))

                // Normal API
                authorize("/api/*/**", authenticated)

                // Swagger, static files 등 허용
                authorize(anyRequest, permitAll)
            }

            csrf { disable() }

            headers {
                frameOptions { sameOrigin = true }
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(customAuthenticationFilter)

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            oauth2Login {
                authenticationSuccessHandler = customOAuth2LoginSuccessHandler
                authorizationEndpoint {
                    authorizationRequestResolver = customOAuth2AuthorizationRequestResolver
                }
            }

            // 인증/인가 실패 응답 설정
            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, _ ->
                    response.contentType = "application/json; charset=UTF-8"
                    response.status = 401
                    response.writer.write(
                        """
                        {
                            "resultCode": "401-1",
                            "msg": "로그인 후 이용해주세요."
                        }
                        """.trimIndent()
                    )
                }

                accessDeniedHandler = AccessDeniedHandler { _, response, _ ->
                    response.contentType = "application/json; charset=UTF-8"
                    response.status = 403
                    response.writer.write(
                        """
                        {
                            "resultCode": "403-1",
                            "msg": "권한이 없습니다."
                        }
                        """.trimIndent()
                    )
                }
            }
        }

        return http.build()
    }

    // 전역 CORS 설정
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // frontend URL (반드시 https)
            allowedOrigins = listOf(
                siteProperties.frontUrl,
                "https://api.larama.site",
                "https://fe.larama.site"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            // 모든 API 요청에 대해 CORS 적용
            registerCorsConfiguration("/api/**", configuration)
        }
    }
}
