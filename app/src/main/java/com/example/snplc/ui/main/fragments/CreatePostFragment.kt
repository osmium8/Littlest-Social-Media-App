package com.example.snplc.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.example.snplc.R
import com.example.snplc.ui.main.viewmodels.CreatePostViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.snplc.other.EventObserver
import com.example.snplc.ui.slideUpViews
import com.example.snplc.ui.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_post.*
import javax.inject.Inject

@AndroidEntryPoint
class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    /**
     * to inject an object declared in an module,
     * have @AndroidEntryPoint
     * and use @Inject
     */
    @Inject
    lateinit var glide: RequestManager


    /**
     * inject viewModel
     */
    private val viewModel: CreatePostViewModel by viewModels()

    /**
     * @ActivityResultContract
     * getting results from an activity, crop activity(must be added to manifest)
     */
    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>() {
        /**
         * specify the intent from which we want our result
         */
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(16, 9)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        /**
         * specify the function to parse Result to the required type, Uri
         */
        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
            // return extracted data from intent as Uri
        }
    }
    private var curImageUri: Uri? = null
    private lateinit var cropContent: ActivityResultLauncher<Any?> // ActivityResultContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract) {
            /**
             * if Uri != null
             */
            it?.let {
//              curImageUri = it WON'T survive screen rotations
                viewModel.setCurImageUri(it)
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        /**
         * launch crop content when we either click on image or button
         */
        btnSetPostImage.setOnClickListener {
            cropContent.launch(null)
        }
        ivPostImage.setOnClickListener {
            cropContent.launch(null)
        }
        btnPost.setOnClickListener {
            /**
             * if null, user chosen no image
             */
            curImageUri?.let { uri ->
                viewModel.createPost(uri, etPostDescription.text.toString())
            } ?: snackbar(getString(R.string.error_no_image_chosen))
        }

        /**
         * slide up multiple views using
         */
        slideUpViews(requireContext(), ivPostImage, btnSetPostImage, tilPostText, btnPost)
    }

    private fun subscribeToObservers() {
        /**
         * Use custom Event observer to emit error just once
         */
        viewModel.createPostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                createPostProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                createPostProgressBar.isVisible = true
            }
        ) {
            createPostProgressBar.isVisible = false
            findNavController().popBackStack() // pop CreatePostFragment when user creates a post
        })


        viewModel.curImageUri.observe(viewLifecycleOwner) {
            curImageUri = it
            btnSetPostImage.isVisible = false
            glide.load(curImageUri).into(ivPostImage)
        }
    }
}