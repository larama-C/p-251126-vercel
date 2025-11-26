package com.back.domain.post.post.repository

import com.back.standard.enum.PostSearchKeywordType
import com.back.standard.enum.PostSearchSortType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostRepositoryTest {
    @Autowired
    private lateinit var postRepository: PostRepository

    @Test
    @DisplayName("findQPagedByKw")
    fun t1() {
        val postPage = postRepository.findQPagedByKw(
            PostSearchKeywordType.TITLE,
            "제목",
            PageRequest.of(
                0,
                10,
                PostSearchSortType.ID.sortBy
            ),
        )

        val content = postPage.content

        assertThat(content).isNotEmpty
    }

    @Test
    @DisplayName("제목 키워드 전체 검색 + 정렬(ID 오름차순) + 페이징(0페이지, size=10)")
    fun find_by_title_keyword_paged_sorted() {
        val pageable = PageRequest.of(
            0, 10,
            PostSearchSortType.ID.sortBy       // ID 오름차순 정렬 가정
        )

        val page = postRepository.findQPagedByKw(
            PostSearchKeywordType.TITLE,
            "제목",
            pageable
        )

        // 전체 건수 및 페이지 내용 크기
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.content).hasSize(3)

        // 검색어 포함 여부
        assertThat(page.content).allSatisfy { post ->
            assertThat(post.title).contains("제목")
        }

        // 정렬(ID 오름차순) 검증
        val ids = page.content.map { it.id }
        assertThat(ids).isSortedAccordingTo(reverseOrder<Long>())

        // 타이틀 순서(작성 순서대로 ID가 1,2,3이라고 가정)
        val titles = page.content.map { it.title }
        assertThat(titles).containsExactly("제목3", "제목2", "제목1")
    }

    @Test
    @DisplayName("부분 키워드(제목1)로 정확히 1건 매칭")
    fun find_by_title_keyword_exact_one() {
        val pageable = PageRequest.of(0, 10, PostSearchSortType.ID.sortBy)

        val page = postRepository.findQPagedByKw(
            PostSearchKeywordType.TITLE,
            "제목1",
            pageable
        )

        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().title).isEqualTo("제목1")
    }

    @Test
    @DisplayName("페이징 동작 검증: size=2로 0페이지/1페이지 내용 확인")
    fun paging_next_page_check() {
        val first = postRepository.findQPagedByKw(
            PostSearchKeywordType.TITLE,
            "제목",
            PageRequest.of(0, 2, PostSearchSortType.ID.sortBy)
        )
        assertThat(first.totalElements).isEqualTo(3)
        assertThat(first.totalPages).isEqualTo(2)
        assertThat(first.content.map { it.title }).containsExactly("제목3", "제목2")

        val second = postRepository.findQPagedByKw(
            PostSearchKeywordType.TITLE,
            "제목",
            PageRequest.of(1, 2, PostSearchSortType.ID.sortBy)
        )
        assertThat(second.content).hasSize(1)
        assertThat(second.content.first().title).isEqualTo("제목1")
    }

    @Test
    @DisplayName("findQPagedByKw")
    fun t5() {
        val postPage = postRepository.findQPagedByKw(
            PostSearchKeywordType.AUTHOR_NICKNAME,
            "유저",
            PageRequest.of(
                0,
                10,
                PostSearchSortType.ID.sortBy
            ),
        )

        val content = postPage.content

        println(content[0].title)
        println(content[1].title)
        println(content[2].title)

        println(content[0].author.nickname)
        println(content[2].author.nickname)

        assertThat(content).isNotEmpty
    }
}