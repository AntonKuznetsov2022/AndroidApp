package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = (CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {

            Glide.with(binding.avatar)
                .load("http://10.0.2.2:9999/avatars/${post.authorAvatar}")
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_baseline_replay_circle_filled_24)
                .error(R.drawable.ic_baseline_clear_24)
                .timeout(10_000)
                .into(binding.avatar)

            author.text = post.author
            published.text = post.published
            content.text = post.content
            share.text = "${countText(post.shares)}"
            viewsCount.text = countText(post.views).toString()
            like.isChecked = post.likedByMe
            like.text = "${countText(post.likes)}"
            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            if (post.attachment != null && post.attachment.type == "IMAGE") {
                postImage.visibility = View.VISIBLE
                Glide.with(binding.postImage)
                    .load("http://10.0.2.2:9999/images/${post.attachment.url}")
                    .placeholder(R.drawable.ic_baseline_replay_circle_filled_24)
                    .error(R.drawable.ic_baseline_clear_24)
                    .timeout(10_000)
                    .into(binding.postImage)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

fun countText(count: Int) = when (count) {
    in 999 downTo 0 -> count
    in 9999 downTo 1000 -> "${count / 1000}.${count % 1000 / 100}K"
    in 999_999 downTo 10_000 -> "${count / 1000}K"
    else -> "${count / 1_000_000}.${count % 1_000_000 / 100_000}M"
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
