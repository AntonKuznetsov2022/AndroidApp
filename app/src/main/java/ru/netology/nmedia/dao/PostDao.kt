package ru.netology.nmedia.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity WHERE newPostHidden = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE postStatusOnSever = 1 ORDER BY id DESC")
    suspend fun getPostOnServer(): List<PostEntity>

    @Query("SELECT COUNT(*) FROM PostEntity WHERE newPostHidden = 1")
    suspend fun newerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()

    @Query("UPDATE PostEntity SET newPostHidden = 0")
    suspend fun readAll()

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

   suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + 1,
        likedByMe = 1
        WHERE id = :id
        """
    )
    suspend fun likedById(id: Long)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes - 1,
        likedByMe = 0
        WHERE id = :id
        """
    )
    suspend fun unlikedById(id: Long)
}
