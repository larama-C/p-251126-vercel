package com.back.domain.post.post.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.comment.entity.Comment
import com.back.global.exception.ServiceException
import com.back.global.jpa.entity.BaseEntity
import com.back.standard.extentions.getOrThrow
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Post(
    @field:ManyToOne(fetch = FetchType.LAZY)
    var author: Member,

    var title: String,

    var content: String
) : BaseEntity() {

    @OneToMany(
        mappedBy = "post",
        cascade = [PERSIST, REMOVE],
        orphanRemoval = true
    )
    private val comments: MutableList<Comment> = mutableListOf()

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun addComment(author: Member, content: String): Comment {
        val comment = Comment(author, content, this)
        comments.add(comment)

        return comment
    }

    fun deleteComment(commentId: Long) {
        val comment = findCommentById(commentId).getOrThrow()
        comments.remove(comment)
    }

    fun updateComment(commentId: Long, content: String): Comment {
        val comment = findCommentById(commentId).getOrThrow()
        comment.update(content)
        return comment
    }

    fun findCommentById(commentId: Long): Comment? {
        return comments.firstOrNull { it.id == commentId }
    }

    fun checkActorModify(actor: Member) {
        if (author != actor) throw ServiceException("403-1", "수정 권한이 없습니다.")
    }

    fun checkActorDelete(actor: Member) {
        if (author != actor) throw ServiceException("403-2", "삭제 권한이 없습니다.")
    }

    fun getComments(): List<Comment> {
        return comments
    }
}
