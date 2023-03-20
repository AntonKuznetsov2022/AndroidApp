package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.enumeration.AttachmentType
import android.text.format.DateFormat
import ru.netology.nmedia.databinding.TimingSeparatorsBinding
import ru.netology.nmedia.dto.*

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPicture(post: Post) {}
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is TimeSeparator -> R.layout.timing_separators
            null -> error(R.string.unknown_item_type)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            R.layout.timing_separators -> {
                val binding = TimingSeparatorsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TimeSeparatorViewHolder(binding)
            }
            else -> error("${R.string.unknown_view_type}: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is TimeSeparator -> (holder as? TimeSeparatorViewHolder)?.bind(item)
            null -> error(R.string.unknown_item_type)
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.imageAd.load("${BuildConfig.BASE_URL}/media/${ad.image}")
    }
}

class TimeSeparatorViewHolder(
    private val binding: TimingSeparatorsBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(timeSeparator: TimeSeparator) {
        binding.apply {
            timeSep.text = when (timeSeparator.time) {
                Time.TODAY -> "Сегодня" //"${R.string.today}" - выводит код строки, а не значение
                Time.YESTERDAY -> "Вчера"
                Time.LAST_WEEK -> "На прошлой неделе"
            }
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {

            val urlAvatars = "${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}"
            avatar.loadCircleCrop(urlAvatars)

            author.text = post.author
            val publishedPost =
                DateFormat.format("dd MMMM yyyy HH:mm:ss", post.published.toLong() * 1_000)
            published.text = publishedPost.toString()
            content.text = post.content
            share.text = "${countText(post.shares)}"
            views.text = "${countText(post.views)}"
            like.isChecked = post.likedByMe
            like.text = "${countText(post.likes)}"
            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            if (post.author == "Name_Name") {
                postNotLoad.isVisible = true
                groupAction.isVisible = false
            } else {
                postNotLoad.isVisible = false
                groupAction.isVisible = true
            }

            if (post.attachment?.type == AttachmentType.IMAGE) {
                postImage.visibility = View.VISIBLE
                val urlImages = "${BuildConfig.BASE_URL}/media/${post.attachment.url}"
                postImage.load(urlImages)

                postImage.setOnClickListener {
                    onInteractionListener.onPicture(post)
                }

            } else {
                postImage.visibility = View.GONE
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.isVisible = post.ownedByMe

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

fun ImageView.load(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .error(R.drawable.ic_baseline_clear_24)
        .placeholder(R.drawable.ic_baseline_replay_circle_filled_24)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)

fun ImageView.loadCircleCrop(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    load(url, CircleCrop(), *transforms)

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
