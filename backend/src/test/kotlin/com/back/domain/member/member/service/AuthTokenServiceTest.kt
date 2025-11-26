package com.back.domain.member.member.service

import com.back.domain.member.member.repository.MemberRepository
import com.back.standard.extentions.getOrThrow
import com.back.standard.ut.Ut.jwt.isValid
import com.back.standard.ut.Ut.jwt.payloadOrNull
import com.back.standard.ut.Ut.jwt.toString
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenServiceTest {
    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Value("\${custom.jwt.secretPattern}")
    private lateinit var secretPattern: String

    @Value("\${custom.jwt.expireSeconds}")
    private val expireSeconds: Long = 0

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    fun t1() {
        Assertions.assertThat(authTokenService).isNotNull()
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 JWT 생성, {name=\"Paul\", age=23}")
    fun t2() {
        // 토큰 만료기간: 1년
        val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))

        // 발행 시간과 만료 시간 설정
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + expireSeconds)

        val payload = mapOf("name" to "Paul", "age" to 23)

        val jwt = Jwts.builder()
            .claims(payload) // 내용
            .issuedAt(issuedAt) // 생성날짜
            .expiration(expiration) // 만료날짜
            .signWith(secretKey) // 키 서명
            .compact()

        val parsedPayload = Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parse(jwt)
            .payload as Map<String, Any>

        Assertions.assertThat(parsedPayload)
            .containsAllEntriesOf(payload)

        Assertions.assertThat(jwt).isNotBlank()

        println("jwt = $jwt")
    }

    @Test
    @DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
    fun t3() {
        val payload = mapOf("name" to "Paul", "age" to 23)

        val jwt = toString(
            secretPattern,
            expireSeconds,
            payload
        )

        Assertions.assertThat(jwt).isNotBlank()

        val validResult = isValid(jwt, secretPattern)
        Assertions.assertThat(validResult).isTrue()

        val parsedPayload = payloadOrNull(
            jwt,
            secretPattern
        )

        Assertions.assertThat(parsedPayload)
            .containsAllEntriesOf(payload)

        println("jwt = $jwt")
    }

    @Test
    @DisplayName("AuthTokenService를 통해서 accessToken 생성")
    fun t4() {
        val member1 = memberRepository.findByUsername("user3").getOrThrow()
        val accessToken = authTokenService.genAccessToken(member1)
        Assertions.assertThat(accessToken).isNotBlank()

        val payload = authTokenService.payloadOrNull(accessToken)

        Assertions.assertThat(payload).containsAllEntriesOf(
            mapOf(
                "id" to member1.id,
                "username" to member1.username
            )
        )

        println("accessToken = $accessToken")
    }
}