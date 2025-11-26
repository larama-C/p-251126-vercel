package com.back.domain.member.member.controller

import com.back.domain.member.member.repository.MemberRepository
import com.back.standard.extentions.getOrThrow
import com.back.standard.ut.Ut.jwt.isValid
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Value("\${custom.jwt.secretPattern}")
    private lateinit var secretPattern: String

    @Test
    @DisplayName("회원 가입")
    fun t1() {
        val username = "newUser"
        val password = "1234"
        val nickname = "새유저"

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "username": "%s",
                                            "password": "%s",
                                            "nickname": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(username, password, nickname)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원가입이 완료되었습니다. %s님 환영합니다.".formatted(nickname)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.id").value(9))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.createDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.modifyDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.name").value(nickname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.isAdmin").value(false))
    }


    @Test
    @DisplayName("회원 가입, 이미 존재하는 username으로 가입 - user1로 가입")
    @Throws(
        Exception::class
    )
    fun t2() {
        val username = "user1"
        val password = "1234"
        val nickname = "새유저"

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "username": "%s",
                                            "password": "%s",
                                            "nickname": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(username, password, nickname)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isConflict())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("409-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("이미 사용중인 아이디입니다."))
    }

    @Test
    @DisplayName("로그인")
    @Throws(Exception::class)
    fun t3() {
        val username = "user1"
        val password = "1234"

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "username": "%s",
                                            "password": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(username, password)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberRepository.findByUsername(username).getOrThrow()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%s님 환영합니다.".formatted(username)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.apiKey").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.memberDto.createDate").value(
                    Matchers.startsWith(
                        member.createDate.toString().substring(0, 20)
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.memberDto.modifyDate").value(
                    Matchers.startsWith(
                        member.modifyDate.toString().substring(0, 20)
                    )
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.name").value(member.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.isAdmin").value(member.isAdmin))

        resultActions.andExpect { result: MvcResult ->
            result.response.getCookie("apiKey")
                ?.let { apiKeyCookie ->
                    Assertions.assertThat(apiKeyCookie).isNotNull()

                    Assertions.assertThat(apiKeyCookie.path).isEqualTo("/")
                    Assertions.assertThat(apiKeyCookie.domain).isEqualTo("localhost")
                    Assertions.assertThat(apiKeyCookie.isHttpOnly).isEqualTo(true)

                    if (apiKeyCookie != null) {
                        Assertions.assertThat(apiKeyCookie.value).isNotBlank()
                    }
                }
            result.response.getCookie("accessToken")
                ?.let { accessTokenCookie ->
                    Assertions.assertThat(accessTokenCookie).isNotNull()

                    Assertions.assertThat(accessTokenCookie.path).isEqualTo("/")
                    Assertions.assertThat(accessTokenCookie.domain).isEqualTo("localhost")
                    Assertions.assertThat(accessTokenCookie.isHttpOnly).isEqualTo(true)
                }
        }
    }

    @Test
    @DisplayName("로그아웃")
    @Throws(Exception::class)
    fun t4() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/members/logout")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("logout"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그아웃 되었습니다."))
            .andExpect { result: MvcResult ->
                result.response.getCookie("apiKey")
                    ?.let {
                        Assertions.assertThat(it.value).isEmpty()
                        Assertions.assertThat(it.maxAge).isEqualTo(0)
                        Assertions.assertThat(it.path).isEqualTo("/")
                        Assertions.assertThat(it.isHttpOnly).isTrue()
                    }

            }
    }

    @Test
    @DisplayName("내 정보")
    @Throws(Exception::class)
    fun t5() {
        val actor = memberRepository.findByUsername("user1").getOrThrow()
        val actorApiKey = actor.apiKey

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .header("Authorization", "Bearer $actorApiKey")
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberRepository.findByUsername("user1").getOrThrow()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("OK"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.memberDto.createDate").value(
                    Matchers.startsWith(
                        member.createDate.toString().substring(0, 20)
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.memberDto.modifyDate").value(
                    Matchers.startsWith(
                        member.createDate.toString().substring(0, 20)
                    )
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.name").value(member.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("내 정보, 올바른 API KEY, 유효하지 않은 accessToken")
    @Throws(
        Exception::class
    )
    fun t6() {
        val actor = memberRepository.findByUsername("user1").getOrThrow()
        val actorApiKey = actor.apiKey
        val wrongAccessToken = "wrong-access-token"

        Assertions.assertThat(isValid(wrongAccessToken, secretPattern)).isFalse()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .cookie(Cookie("apiKey", actorApiKey), Cookie("accessToken", wrongAccessToken))
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        resultActions
            .andExpect { result: MvcResult ->
                val apiKeyCookie = result.response.getCookie("accessToken")
                Assertions.assertThat(apiKeyCookie).isNotNull()

                apiKeyCookie
                    ?.let {
                        Assertions.assertThat(it.path).isEqualTo("/")
                        Assertions.assertThat(it.domain).isEqualTo("localhost")
                        Assertions.assertThat(it.isHttpOnly).isEqualTo(true)
                        apiKeyCookie.value
                    }
                    ?.let {
                        Assertions.assertThat(isValid(it, secretPattern)).isTrue()
                    }

            }
    }
}
