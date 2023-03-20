package ru.netology.nmedia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.*
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FeedFragment : Fragment() {

    lateinit var binding: FragmentFeedBinding

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)

        val token = context?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("TOKEN_KEY", null)

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {

                if (token == null) {
                    MaterialAlertDialogBuilder(context!!)
                        .setTitle(R.string.log_account)
                        .setMessage(R.string.enter_in_app)
                        .setNegativeButton(R.string.guest) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setPositiveButton(R.string.login) { _, _ ->
                            findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                            Snackbar.make(binding.root, R.string.login_exit, Snackbar.LENGTH_LONG)
                                .show()
                        }
                        .show()
                } else {
                    if (!post.likedByMe) {
                        viewModel.likeById(post.id)
                    } else {
                        viewModel.unlikeById(post.id)
                    }
                }
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
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
                viewModel.edit(post)
            }

            override fun onPicture(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_pictureFragment,
                    Bundle().apply { textArg = post.attachment?.url })
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }
        })

        binding.posts.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {adapter.retry()},
            footer = PostLoadingStateAdapter {adapter.retry()}
        )

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .show()
            }
        }

        binding.newPostsBut.setOnClickListener {
            binding.newPostsBut.isVisible = false
            viewModel.showNewPosts()
        }

/*        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.newPostsBut.isVisible = true
            } else {
                binding.newPostsBut.isGone = true
            }
        }*/

        adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.posts.smoothScrollToPosition(0)
                    }
                }
            })

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }
/*        viewModel.data.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.posts)
            binding.emptyText.isVisible = data.empty
        }*/

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.newPostsBut.isVisible = false
            adapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        binding.fab.setOnClickListener {
            if (token == null) {
                MaterialAlertDialogBuilder(this.requireContext())
                    .setTitle(R.string.log_account)
                    .setMessage(R.string.enter_in_app)
                    .setNegativeButton(R.string.guest) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(R.string.login) { _, _ ->
                        findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                    }
                    .show()
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }
        return binding.root
    }
}
