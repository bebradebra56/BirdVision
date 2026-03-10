package com.vision.birdvisionpr.verfs.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import com.vision.birdvisionpr.verfs.presentation.ui.load.BirdVisionLoadFragment
import org.koin.android.ext.android.inject

class BirdVisionV : Fragment(){

    private lateinit var birdVisionPhoto: Uri
    private var birdVisionFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val birdVisionTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        birdVisionFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        birdVisionFilePathFromChrome = null
    }

    private val birdVisionTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            birdVisionFilePathFromChrome?.onReceiveValue(arrayOf(birdVisionPhoto))
            birdVisionFilePathFromChrome = null
        } else {
            birdVisionFilePathFromChrome?.onReceiveValue(null)
            birdVisionFilePathFromChrome = null
        }
    }

    private val birdVisionDataStore by activityViewModels<BirdVisionDataStore>()


    private val birdVisionViFun by inject<BirdVisionViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (birdVisionDataStore.birdVisionView.canGoBack()) {
                        birdVisionDataStore.birdVisionView.goBack()
                        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "WebView can go back")
                    } else if (birdVisionDataStore.birdVisionViList.size > 1) {
                        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "WebView can`t go back")
                        birdVisionDataStore.birdVisionViList.removeAt(birdVisionDataStore.birdVisionViList.lastIndex)
                        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "WebView list size ${birdVisionDataStore.birdVisionViList.size}")
                        birdVisionDataStore.birdVisionView.destroy()
                        val previousWebView = birdVisionDataStore.birdVisionViList.last()
                        birdVisionAttachWebViewToContainer(previousWebView)
                        birdVisionDataStore.birdVisionView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (birdVisionDataStore.birdVisionIsFirstCreate) {
            birdVisionDataStore.birdVisionIsFirstCreate = false
            birdVisionDataStore.birdVisionContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return birdVisionDataStore.birdVisionContainerView
        } else {
            return birdVisionDataStore.birdVisionContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "onViewCreated")
        if (birdVisionDataStore.birdVisionViList.isEmpty()) {
            birdVisionDataStore.birdVisionView = BirdVisionVi(requireContext(), object :
                BirdVisionCallBack {
                override fun birdVisionHandleCreateWebWindowRequest(birdVisionVi: BirdVisionVi) {
                    birdVisionDataStore.birdVisionViList.add(birdVisionVi)
                    Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "WebView list size = ${birdVisionDataStore.birdVisionViList.size}")
                    Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "CreateWebWindowRequest")
                    birdVisionDataStore.birdVisionView = birdVisionVi
                    birdVisionVi.birdVisionSetFileChooserHandler { callback ->
                        birdVisionHandleFileChooser(callback)
                    }
                    birdVisionAttachWebViewToContainer(birdVisionVi)
                }

            }, birdVisionWindow = requireActivity().window).apply {
                birdVisionSetFileChooserHandler { callback ->
                    birdVisionHandleFileChooser(callback)
                }
            }
            birdVisionDataStore.birdVisionView.birdVisionFLoad(arguments?.getString(
                BirdVisionLoadFragment.BIRD_VISION_D) ?: "")
//            ejvview.fLoad("www.google.com")
            birdVisionDataStore.birdVisionViList.add(birdVisionDataStore.birdVisionView)
            birdVisionAttachWebViewToContainer(birdVisionDataStore.birdVisionView)
        } else {
            birdVisionDataStore.birdVisionViList.forEach { webView ->
                webView.birdVisionSetFileChooserHandler { callback ->
                    birdVisionHandleFileChooser(callback)
                }
            }
            birdVisionDataStore.birdVisionView = birdVisionDataStore.birdVisionViList.last()

            birdVisionAttachWebViewToContainer(birdVisionDataStore.birdVisionView)
        }
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "WebView list size = ${birdVisionDataStore.birdVisionViList.size}")
    }

    private fun birdVisionHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        birdVisionFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Launching file picker")
                    birdVisionTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Launching camera")
                    birdVisionPhoto = birdVisionViFun.birdVisionSavePhoto()
                    birdVisionTakePhoto.launch(birdVisionPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                birdVisionFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun birdVisionAttachWebViewToContainer(w: BirdVisionVi) {
        birdVisionDataStore.birdVisionContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            birdVisionDataStore.birdVisionContainerView.removeAllViews()
            birdVisionDataStore.birdVisionContainerView.addView(w)
        }
    }


}