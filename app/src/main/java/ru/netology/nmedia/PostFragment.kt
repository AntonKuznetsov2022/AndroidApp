package ru.netology.nmedia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.NewPostFragment.Companion.postIdArg
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPostBinding

class PostFragment : Fragment() {

    lateinit var binding: FragmentPostBinding

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        val postId = arguments?.postIdArg

        postShow(postId, object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.shareById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(R.id.action_postFragment_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
                viewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onPlayVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }
        })

        return binding.root
    }

    private fun postShow(postId: Long?, onInteractionListener: OnInteractionListener) {
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId } ?: return@observe
            with(binding.postChoose) {
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

                if (post.video != null) groupVideo.visibility = View.VISIBLE

                videoButton.setOnClickListener {
                    onInteractionListener.onPlayVideo(post)
                }
                videoPlayer.setOnClickListener {
                    onInteractionListener.onPlayVideo(post)
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
}