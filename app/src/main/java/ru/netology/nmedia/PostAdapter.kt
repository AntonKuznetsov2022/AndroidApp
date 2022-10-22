package ru.netology.nmedia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.CardPostBinding

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit

class PostAdapter(
    private val likeClickListener: OnLikeListener,
    private val shareClickListener: OnShareListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = (CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        return PostViewHolder(binding, likeClickListener, shareClickListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val likeClickListener: OnLikeListener,
    private val shareClickListener: OnShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = countText(post.likes).toString()
            shareCount.text = countText(post.shares).toString()
            viewsCount.text = countText(post.views).toString()
            like.setImageResource(
                if (post.likedByMe) R.drawable.ic_red_like_24 else R.drawable.ic_like_24
            )
            like.setOnClickListener {
                likeClickListener(post)
            }
            share.setOnClickListener {
                shareClickListener(post)
            }
        }
    }


    private fun countText(count: Int) = when (count) {
        in 999 downTo 0 -> count
        in 9999 downTo 1000 -> "${count / 1000}.${count % 1000 / 100}K"
        in 999_999 downTo 10_000 -> "${count / 1000}K"
        else -> "${count / 1_000_000}.${count % 1_000_000 / 100_000}M"
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
