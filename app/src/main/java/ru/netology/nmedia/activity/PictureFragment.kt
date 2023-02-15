package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.load
import ru.netology.nmedia.databinding.FragmentPictureBinding

class PictureFragment : Fragment() {

    lateinit var binding: FragmentPictureBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPictureBinding.inflate(inflater, container, false)


        val urlImages = "${BuildConfig.BASE_URL}/media/${arguments?.textArg}"
        binding.picturePreview.load(urlImages)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }
}

fun ImageView.load(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .error(R.drawable.ic_baseline_clear_24)
        .placeholder(R.drawable.ic_baseline_replay_circle_filled_24)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)