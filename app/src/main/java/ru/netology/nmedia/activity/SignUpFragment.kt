package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    lateinit var binding: FragmentSignUpBinding
    private val viewModel by viewModels<SignUpViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

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
                        viewModel.changeAvatar(uri.toFile(), uri)
                    }
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.stateSignUp.observe(viewLifecycleOwner) { state ->
            if (state.passCheck) {
                Snackbar.make(binding.root, R.string.pass_check, Snackbar.LENGTH_LONG)
                    .show()
            }
            if (state.signUpError) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.clearBut.setOnClickListener {
            viewModel.clearAvatar()
        }

        binding.avatarBut.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle(resources.getString(R.string.choose))
                .setNegativeButton(R.string.gallery) { _, _ ->
                    ImagePicker.with(this)
                        .galleryOnly()
                        .crop()
                        .compress(192)
                        .createIntent(photoLauncher::launch)
                }
                .setPositiveButton(R.string.photo) { _, _ ->
                    ImagePicker.with(this)
                        .cameraOnly()
                        .crop()
                        .compress(192)
                        .createIntent(photoLauncher::launch)
                }
                .show()
        }


        viewModel.mediaAvatar.observe(viewLifecycleOwner) { mediaAvatar ->
            if (mediaAvatar == null) {
                binding.previewAvatarContainer.isGone = true
                return@observe
            }
            binding.previewAvatarContainer.isVisible = true
            binding.previewAvatar.setImageURI(mediaAvatar.uri)
        }

        binding.registrationBut.setOnClickListener {
            if (binding.loginUp.text.isNullOrEmpty()
                || binding.passwordUp.text.isNullOrEmpty()
                || binding.returnPasswordUp.text.isNullOrEmpty()
                || binding.name.text.isNullOrEmpty()
            ) {
                Snackbar.make(binding.root, R.string.reg_check, Snackbar.LENGTH_LONG)
                    .show()
            } else {
                viewModel.signUp(
                    binding.loginUp.text.toString(),
                    binding.passwordUp.text.toString(),
                    binding.returnPasswordUp.text.toString(),
                    binding.name.text.toString()
                )
            }
        }

        viewModel.signUpApp.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigateUp()
            findNavController().navigateUp()
            Snackbar.make(binding.root, R.string.reg_ok, Snackbar.LENGTH_LONG)
                .show()
        }

        return binding.root
    }
}
