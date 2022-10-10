package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likedByMe: Boolean = false,
    var likes: Int = 999,
    var shares: Int = 999,
    var views: Int = 1_014_223
)

fun countText (count: Int) = when (count) {
    in 999 downTo 0 -> count
    in 9999 downTo 1000 -> "${count/1000}.${count%1000/100}K"
    in 999_999 downTo 10_000 -> "${count/1000}K"
    else -> "${count/1_000_000}.${count%1_000_000/100_000}M"
}

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.like.setOnClickListener {
            binding.like.setImageResource(R.drawable.ic_red_like_24)
        }

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
        )

        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            if (post.likedByMe) {
                like.setImageResource(R.drawable.ic_red_like_24)
            }

            likeCount.text = countText(post.likes).toString()
            shareCount.text = countText(post.shares).toString()
            viewsCount.text = countText(post.views).toString()

            like.setOnClickListener {
                post.likedByMe = !post.likedByMe
                like.setImageResource(
                    if (post.likedByMe) R.drawable.ic_red_like_24 else R.drawable.ic_like_24
                )
                if (post.likedByMe) post.likes++ else post.likes--
                likeCount.text = countText(post.likes).toString()
            }

            share.setOnClickListener {
                post.shares++
                shareCount.text = countText(post.shares).toString()
            }

        }
    }
}