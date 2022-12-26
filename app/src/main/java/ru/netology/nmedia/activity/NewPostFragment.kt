package ru.netology.nmedia.activity


import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.LongArg
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.StringArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding


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
    ): View? {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)

        val editText = context?.getSharedPreferences(APP_NAME, MODE_PRIVATE)
        if (editText != null) {
            binding.edit.setText(editText.getString(APP_NAME, ""))
        }

        if (arguments?.textArg != null) {
            binding.cancel.visibility = View.VISIBLE
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

        binding.cancel.setOnClickListener {
            saveText("")
            viewModel.cancel()
            findNavController().navigateUp()
        }

        binding.add.setOnClickListener {
            if (binding.edit.text.isNullOrBlank()) {
                findNavController().navigateUp()
            } else {
                saveText("")
                viewModel.changeContent(binding.edit.text.toString())
                viewModel.save()
                AndroidUtils.hideKeyboard(requireView())
            }
        }
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        return binding.root
    }

    fun saveText(text: String) {
        val editText = context?.getSharedPreferences(APP_NAME, MODE_PRIVATE)
        editText?.edit()?.apply { putString(APP_NAME, text).apply() }
    }
}