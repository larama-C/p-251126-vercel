package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember
import com.back.standard.enum.MemberSearchKeywordType
import com.back.standard.enum.MemberSearchSortType
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

class MemberRepositoryImpl(
    private val jpaQuery: JPAQueryFactory
) : MemberRepositoryCustom {

    override fun findQById(id: Long): Member? {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(member.id.eq(id)) // where member.id = id
            .fetchOne() // limit 1
    }

    override fun findQByUsername(username: String): Member? {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetchOne()
    }

    override fun findQByIdIn(ids: List<Long>): List<Member> {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(member.id.`in`(ids))
            .fetch()
    }

    override fun findQByUsernameAndNickname(username: String, nickname: String): Member? {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .and(member.nickname.eq(nickname))
            )
            .fetchOne()
    }

    override fun findQByUsernameOrNickname(username: String, nickname: String): List<Member> {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .or(member.nickname.eq(nickname))
            )
            .fetch()
    }

    override fun findQByUsernameAndEitherPasswordOrNickname(
        username: String,
        password: String,
        nickname: String
    ): List<Member> {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .and(
                        member.password.eq(password)
                            .or(member.nickname.eq(nickname))
                    )
            )
            .fetch()
    }

    override fun findQByNicknameContaining(nickname: String): List<Member> {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(
                member.nickname.contains(nickname)
            )
            .fetch()
    }

    override fun countQByNicknameContaining(nickname: String): Long {
        val member = QMember.member

        return jpaQuery
            .select(member.count())
            .from(member)
            .where(
                member.nickname.contains(nickname)
            )
            .fetchOne() ?: 0L
    }

    override fun existsQByNicknameContaining(nickname: String): Boolean {
        val member = QMember.member

        return jpaQuery
            .selectOne()
            .from(member)
            .where(
                member.nickname.contains(nickname)
            )
            .fetchFirst() != null
    }

    override fun findQByNicknameContaining(nickname: String, pageable: Pageable): Page<Member> {
        val member = QMember.member

        // content 쿼리
        val content = jpaQuery
            .selectFrom(member)
            .where(member.nickname.contains(nickname))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            jpaQuery
                .select(member.count())
                .from(member)
                .where(member.nickname.contains(nickname))
                .fetchOne() ?: 0L
        }
    }

    override fun findQByNicknameContainingOrderByIdDesc(nickname: String): List<Member> {
        val member = QMember.member

        return jpaQuery
            .selectFrom(member)
            .where(
                member.nickname.contains(nickname)
            )
            .orderBy(member.id.desc())
            .fetch()
    }

    override fun findQByUsernameContaining(username: String, pageable: Pageable): Page<Member> {

        val member = QMember.member

        val query = jpaQuery
            .selectFrom(member)
            .where(member.username.contains(username))

        pageable.sort.forEach { order ->
            when (order.property) {
                "id" -> query.orderBy(if (order.isAscending) member.id.asc() else member.id.desc())
                "username" -> query.orderBy(if (order.isAscending) member.username.asc() else member.username.desc())
                "nickname" -> query.orderBy(if (order.isAscending) member.nickname.asc() else member.nickname.desc())
            }
        }

        val content = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            jpaQuery
                .select(member.count())
                .from(member)
                .where(member.nickname.contains(username))
                .fetchOne() ?: 0L
        }
    }

    override fun findByKwPaged(kwType: MemberSearchKeywordType, kw: String, pageable: Pageable): Page<Member> {

        val member = QMember.member

        val builder = BooleanBuilder()?.apply {
            when (kwType) {
                MemberSearchKeywordType.USERNAME -> this.and(member.username.contains(kw))
                MemberSearchKeywordType.NICKNAME -> this.and(member.nickname.contains(kw))
                MemberSearchKeywordType.ALL -> {
                    this.and(
                        member.username.contains(kw).or(
                            member.nickname.contains(kw)
                        )
                    )
                }
            }
        }


        val query = jpaQuery
            .selectFrom(member)
            .where(builder)

        pageable.sort.forEach { order ->
            val path = when (order.property.lowercase()) {
                MemberSearchSortType.ID.property -> member.id
                MemberSearchSortType.USERNAME.property -> member.username
                MemberSearchSortType.NICKNAME.property -> member.nickname
                else -> null
            }

            path?.let { property ->
                OrderSpecifier(
                    if (order.isAscending) Order.ASC else Order.DESC,
                    property
                )?.also {
                    query.orderBy(it)
                }
            }

        }

        val content = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            jpaQuery
                .select(member.count())
                .from(member)
                .where(builder)
                .fetchOne() ?: 0L
        }
    }

}