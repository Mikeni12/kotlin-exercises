package coroutines.comment

import domain.comment.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val commentFactory: CommentFactory
) {
    suspend fun addComment(
        token: String,
        collectionKey: String,
        body: AddComment
    ) {
        val user = userService.readUserId(token)
        val comment = commentFactory.toCommentDocument(
            userId = user,
            collectionKey = collectionKey,
            body = body
        )
        commentRepository.addComment(comment)
    }

    suspend fun getComments(
        collectionKey: String
    ): CommentsCollection = coroutineScope {
        val comments = commentRepository.getComments(collectionKey)
            .map {
                async {
                    CommentElement(
                        id = it._id,
                        collectionKey = it.collectionKey,
                        user = userService.findUserById(it.userId),
                        comment = it.comment,
                        date = it.date
                    )
                }
            }
            .awaitAll()
        CommentsCollection(collectionKey, comments)
    }
}
