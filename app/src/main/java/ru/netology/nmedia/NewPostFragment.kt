package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding

class NewPostFragment : Fragment() {

    lateinit var binding: FragmentNewPostBinding

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onPause() {
        Toast.makeText(getActivity(), "Back button pressed", Toast.LENGTH_LONG)
            .show()
        super.onPause()
    }

    override fun onResume() {
        Toast.makeText(getActivity(), "On Resume", Toast.LENGTH_LONG)
            .show()
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)

        if (arguments?.textArg != null) {
            binding.cancel.visibility = View.VISIBLE
            with(binding.edit) {
                requestFocus()
                setSelection(text.toString().length)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.cancel.setOnClickListener {
            viewModel.cancel()
            findNavController().navigateUp()
        }

        binding.add.setOnClickListener {
            if (binding.edit.text.isNullOrBlank()) {
                findNavController().navigateUp()
            } else {
                viewModel.changeContent(binding.edit.text.toString())
                viewModel.save()
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}