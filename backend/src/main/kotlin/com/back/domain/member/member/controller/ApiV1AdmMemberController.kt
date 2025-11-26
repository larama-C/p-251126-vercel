package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberWithUsernameDto
import com.back.domain.member.member.service.MemberService
import com.back.standard.dto.PageDto
import com.back.standard.enum.MemberSearchKeywordType
import com.back.standard.enum.MemberSearchSortType
import io.swagger.v3.oas.annotations.Operation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/adm/members")
class ApiV1AdmMemberController(
    private val memberService: MemberService
) {

    @Operation(summary = "회원 다건 조회")
    @Transactional(readOnly = true)
    @GetMapping
    fun getItems(
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("pageSize", defaultValue = "5") pageSize: Int,
        @RequestParam("kw", defaultValue = "") kw: String,
        @RequestParam("kwType", defaultValue = "ALL") kwType: MemberSearchKeywordType,
        @RequestParam("sort", defaultValue = "ID") sort: MemberSearchSortType,
    ): PageDto<MemberWithUsernameDto> {

        val page = if (page >= 1) page else 1
        val pageSize = if (pageSize > 30) 30 else pageSize

        val memberPage = memberService.findByKwPaged(page, pageSize, kwType, kw, sort)

        return PageDto(memberPage.map { MemberWithUsernameDto(it) })
    }
}
