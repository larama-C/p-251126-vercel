package com.back.domain.post.post.controller

import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.post.post.repository.PostRepository
import com.back.standard.extentions.getOrThrow
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1AdmPostControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("글 전체 개수 조회, count")
    @Throws(Exception::class)
    fun t1() {
        val actor = memberRepository.findByUsername("admin").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/posts/count")
                    .cookie(Cookie("apiKey", actor.apiKey))
            )
            .andDo(MockMvcResultHandlers.print())


        val count = postRepository.count()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmPostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("count"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(count))
    }

    @Test
    @DisplayName("글 전체 개수 조회, count, 권한이 없는 경우")
    @Throws(Exception::class)
    fun t2() {
        val actor = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/posts/count")
                    .cookie(Cookie("apiKey", actor.apiKey))
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("권한이 없습니다."))
    }
}
