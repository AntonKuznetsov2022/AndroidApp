package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                likeCount.text = countText(post.likes).toString()
                shareCount.text = countText(post.shares).toString()
                viewsCount.text = countText(post.views).toString()
                like.setImageResource(
                    if (post.likedByMe) R.drawable.ic_red_like_24 else R.drawable.ic_like_24
                )
            }
        }
        binding.like.setOnClickListener {
            viewModel.like()
        }

        binding.share.setOnClickListener {
            viewModel.share()
        }
    }

    private fun countText(count: Int) = when (count) {
        in 999 downTo 0 -> count
        in 9999 downTo 1000 -> "${count / 1000}.${count % 1000 / 100}K"
        in 999_999 downTo 10_000 -> "${count / 1000}K"
        else -> "${count / 1_000_000}.${count % 1_000_000 / 100_000}M"
    }
}