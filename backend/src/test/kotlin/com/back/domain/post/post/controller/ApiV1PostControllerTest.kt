package com.back.domain.post.post.controller

import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.post.post.repository.PostRepository
import com.back.standard.extentions.getOrThrow
import com.back.standard.ut.Ut.jwt.toString
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.Map

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Value("\${custom.jwt.secretPattern}")
    private lateinit var secretPattern: String

    @Value("\${custom.jwt.expireSeconds}")
    private val expireSeconds: Long = 0

    @Test
    @DisplayName("글 다건 조회")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        resultActions
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].id", Matchers.containsInRelativeOrder(3, 1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].createDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].modifyDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("제목3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("내용3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].authorId").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].authorName").value("유저2"))


        // 하나 또는 2개 정도만 검증


//        for(int i = 0; i < posts.size(); i++) {
//            Post post = posts.get(i);
//
//            resultActions
//                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(post.getId()))
//                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(matchesPattern(post.getCreateDate().toString().replaceAll("0+$", "") + ".*")))
//                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(matchesPattern(post.getModifyDate().toString().replaceAll("0+$", "") + ".*")))
//                    .andExpect(jsonPath("$[%d].title".formatted(i)).value(post.getTitle()))
//                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(post.getContent()));
//        }
    }

    @Test
    @DisplayName("글 단건 조회")
    @Throws(Exception::class)
    fun t2() {
        val targetId: Long = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/%d".formatted(targetId))
            )
            .andDo(MockMvcResultHandlers.print())

        val post = postRepository.findByIdOrNull(targetId).getOrThrow()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItem"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.matchesPattern(post.createDate.toString().replace("0+$".toRegex(), "") + ".*"))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.matchesPattern(post.modifyDate.toString().replace("0+$".toRegex(), "") + ".*"))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("제목1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("내용1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authorId").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authorName").value("유저1"))
    }

    @Test
    @DisplayName("글 작성")
    @Throws(Exception::class)
    fun t3() {
        val title = "제목입니다"
        val content = "내용입니다"

        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("createItem"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("4번 게시물이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.id").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.createDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.modifyDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.title").value(title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.content").value(content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.authorId").value(author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postDto.authorName").value(author.name))
    }

    @Test
    @DisplayName("글 작성, 인증 헤더 정보가 없을 때")
    @Throws(Exception::class)
    fun t4() {
        val title = "제목입니다"
        val content = "내용입니다"

        val author = memberRepository.findByUsername("user1")

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("글 작성, 제목이 입력되지 않은 경우")
    @Throws(Exception::class)
    fun t5() {
        val title = ""
        val content = "내용입니다"
        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("createItem"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                        title-NotBlank-must not be blank
                        title-Size-size must be between 2 and 10
                        
                        """.trimIndent().stripIndent().trim { it <= ' ' })
            )
    }

    @Test
    @DisplayName("글 작성, 내용이 입력되지 않은 경우")
    @Throws(Exception::class)
    fun t6() {
        val title = "제목입니다."
        val content = ""
        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("createItem"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                        content-NotBlank-must not be blank
                        content-Size-size must be between 2 and 100
                        
                        """.trimIndent().stripIndent().trim { it <= ' ' })
            )
    }

    @Test
    @DisplayName("글 작성, JSON 양식이 잘못된 경우")
    @Throws(Exception::class)
    fun t7() {
        val title = "제목입니다."
        val content = "내용입니다"
        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s"
                                            "content": "%s"
                                        
                                        
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("createItem"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("잘못된 형식의 요청 데이터입니다."))
    }

    @Test
    @DisplayName("글 작성, 유효한 엑세스 토큰, 잘못된 apiKey")
    @Throws(Exception::class)
    fun t8() {
        val title = "제목입니다"
        val content = "내용입니다"
        val author = memberRepository.findByUsername("user1").getOrThrow()

        val accessToken = toString(
            secretPattern,
            expireSeconds,
            Map.of(
                "id", author.id,
                "username", author.username,
                "nickname", author.nickname
            )
        )

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer wrong-api-key %s".formatted(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("createItem"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
    }


    @Test
    @DisplayName("글 수정")
    @Throws(Exception::class)
    fun t9() {
        val targetId: Long = 1
        val title = "제목 수정"
        val content = "내용 수정"

        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/%d".formatted(targetId))
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        // 필수 검증
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modifyItem"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%d번 게시물이 수정되었습니다.".formatted(targetId)))

        // 선택적 검증
        val post = postRepository.findById(targetId).get()

        Assertions.assertThat(post.title).isEqualTo(title)
        Assertions.assertThat(post.content).isEqualTo(content)
    }

    @Test
    @DisplayName("글 삭제")
    @Throws(Exception::class)
    fun t10() {
        val targetId: Long = 1

        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/%d".formatted(targetId))
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
            )
            .andDo(MockMvcResultHandlers.print())

        // 필수 검증
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("deleteItem"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%d번 게시물이 삭제되었습니다.".formatted(targetId)))

        // 선택적 검증
        val post = postRepository.findById(targetId).orElse(null)
        Assertions.assertThat(post).isNull()
    }

    @Test
    @DisplayName("글 단건 조회, 존재하지 않는 글")
    @Throws(Exception::class)
    fun t11() {
        val targetId = Int.MAX_VALUE.toLong()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/%d".formatted(targetId))
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItem"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    @Test
    @DisplayName("글 작성, 올바르지 않은 헤더 형식")
    @Throws(Exception::class)
    fun t12() {
        val title = "제목입니다"
        val content = "내용입니다"

        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "wrong %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Authorization 헤더가 Bearer 형식이 아닙니다."))
    }

    @Test
    @DisplayName("글 작성, 잘못된/없는 API 키")
    @Throws(Exception::class)
    fun t13() {
        val title = "제목입니다"
        val content = "내용입니다"

        val author = memberRepository.findByUsername("user1").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer %s".formatted(author.apiKey + "2"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("API 키가 유효하지 않습니다."))
    }

    @Test
    @DisplayName("글 수정, 권한 체크 - 글 작성자가 아닌 경우")
    @Throws(Exception::class)
    fun t14() {
        val targetId: Long = 1
        val title = "제목 수정"
        val content = "내용 수정"

        val author = memberRepository.findByUsername("user2").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/%d".formatted(targetId))
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s"
                                        }
                                        
                                        """.trimIndent().formatted(title, content)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        // 필수 검증
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modifyItem"))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("수정 권한이 없습니다."))
    }

    @Test
    @DisplayName("글 삭제, 권한 체크 - 글 작성자가 아닌 경우")
    @Throws(Exception::class)
    fun t15() {
        val targetId: Long = 1

        val author = memberRepository.findByUsername("user2").getOrThrow()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/%d".formatted(targetId))
                    .header("Authorization", "Bearer %s".formatted(author.apiKey))
            )
            .andDo(MockMvcResultHandlers.print())

        // 필수 검증
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("deleteItem"))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("삭제 권한이 없습니다."))
    }
}
