package ru.netology.nmedia.activity

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.LongArg
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.StringArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class NewPostFragment : Fragment() {

    lateinit var binding: FragmentNewPostBinding
    private var APP_NAME = "editText"

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.postIdArg: Long by LongArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.photo_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        viewModel.changePhoto(uri.toFile(), uri)
                    }
                }
            }


        val editText = context?.getSharedPreferences(APP_NAME, MODE_PRIVATE)
        if (editText != null) {
            binding.edit.setText(editText.getString(APP_NAME, ""))
        }

        if (arguments?.textArg != null) {
            with(binding.edit) {
                requestFocus()
                setSelection(text.toString().length)
            }
        }
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            saveText(binding.edit.text.toString())
            findNavController().navigateUp()
        }

        arguments?.textArg
            ?.let(binding.edit::setText)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.new_post -> {
                        saveText("")
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)

        binding.clear.setOnClickListener {
            viewModel.clearPhoto()
        }

        binding.photo.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }

        binding.gallery.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }

        viewModel.media.observe(viewLifecycleOwner) {media ->
            if (media == null) {
                binding.previewContainer.isGone = true
                return@observe
            }

            binding.previewContainer.isVisible = true
            binding.preview.setImageURI(media.uri)
        }

/*        binding.cancel.setOnClickListener {
            saveText("")
            viewModel.cancel()
            findNavController().navigateUp()
        }*/

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            viewModel.loadPosts()
        }
        return binding.root
    }

    fun saveText(text: String) {
        val editText = context?.getSharedPreferences(APP_NAME, MODE_PRIVATE)
        editText?.edit()?.apply { putString(APP_NAME, text).apply() }
    }
}