package ru.netology.nmedia

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edit.requestFocus()

        val text = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (text != null) {
            binding.cancel.visibility = View.VISIBLE
            with(binding.edit) {
                setText(text)
                setSelection(text.toString().length)
            }
        }

        binding.cancel.setOnClickListener {
                setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }

        binding.add.setOnClickListener {
            val intent = Intent()
            if (binding.edit.text.isNullOrBlank()) {
                setResult(Activity.RESULT_CANCELED, intent)
            } else {
                val content = binding.edit.text.toString()
                intent.putExtra(Intent.EXTRA_TEXT, content)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
    }
}