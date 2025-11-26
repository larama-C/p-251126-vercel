package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom {

    fun findByUsername(username: String): Member?
    fun findByApiKey(apiKey: String): Member?
    fun findByIdIn(ids: List<Long>): List<Member>
    fun findByUsernameAndNickname(username: String, nickname: String): Member?
    fun findByUsernameOrNickname(username: String, nickname: String): List<Member>

    @Query("SELECT m FROM Member m WHERE m.username = :username AND (m.password = :password OR m.nickname = :nickname)")
    fun findCByUsernameAndEitherPasswordOrNickname(username: String, password: String, nickname: String): List<Member>

    fun findByNicknameContaining(nickname: String): List<Member>
    fun findByNicknameContaining(nickname: String, pageable: Pageable): Page<Member>
    fun findByNicknameContainingOrderByIdDesc(nickname: String): List<Member>

    fun countByNicknameContaining(nickname: String): Long
    fun existsByNicknameContaining(nickname: String): Boolean

    fun findByUsernameContaining(username: String, pageable: Pageable): Page<Member>
}
