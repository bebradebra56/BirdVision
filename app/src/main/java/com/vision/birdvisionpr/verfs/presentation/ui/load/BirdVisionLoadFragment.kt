package com.vision.birdvisionpr.verfs.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.vision.birdvisionpr.MainActivity
import com.vision.birdvisionpr.R
import com.vision.birdvisionpr.databinding.FragmentLoadBirdVisionBinding
import com.vision.birdvisionpr.verfs.data.shar.BirdVisionSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class BirdVisionLoadFragment : Fragment(R.layout.fragment_load_bird_vision) {
    private lateinit var birdVisionLoadBinding: FragmentLoadBirdVisionBinding

    private val birdVisionLoadViewModel by viewModel<BirdVisionLoadViewModel>()

    private val birdVisionSharedPreference by inject<BirdVisionSharedPreference>()

    private var birdVisionUrl = ""

    private val birdVisionRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        birdVisionSharedPreference.birdVisionNotificationState = 2
        birdVisionNavigateToSuccess(birdVisionUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        birdVisionLoadBinding = FragmentLoadBirdVisionBinding.bind(view)

        birdVisionLoadBinding.birdVisionGrandButton.setOnClickListener {
            val birdVisionPermission = Manifest.permission.POST_NOTIFICATIONS
            birdVisionRequestNotificationPermission.launch(birdVisionPermission)
        }

        birdVisionLoadBinding.birdVisionSkipButton.setOnClickListener {
            birdVisionSharedPreference.birdVisionNotificationState = 1
            birdVisionSharedPreference.birdVisionNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            birdVisionNavigateToSuccess(birdVisionUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                birdVisionLoadViewModel.birdVisionHomeScreenState.collect {
                    when (it) {
                        is BirdVisionLoadViewModel.BirdVisionHomeScreenState.BirdVisionLoading -> {

                        }

                        is BirdVisionLoadViewModel.BirdVisionHomeScreenState.BirdVisionError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is BirdVisionLoadViewModel.BirdVisionHomeScreenState.BirdVisionSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val birdVisionNotificationState = birdVisionSharedPreference.birdVisionNotificationState
                                when (birdVisionNotificationState) {
                                    0 -> {
                                        birdVisionLoadBinding.birdVisionNotiGroup.visibility = View.VISIBLE
                                        birdVisionLoadBinding.birdVisionLoadingGroup.visibility = View.GONE
                                        birdVisionUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > birdVisionSharedPreference.birdVisionNotificationRequest) {
                                            birdVisionLoadBinding.birdVisionNotiGroup.visibility = View.VISIBLE
                                            birdVisionLoadBinding.birdVisionLoadingGroup.visibility = View.GONE
                                            birdVisionUrl = it.data
                                        } else {
                                            birdVisionNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        birdVisionNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                birdVisionNavigateToSuccess(it.data)
                            }
                        }

                        BirdVisionLoadViewModel.BirdVisionHomeScreenState.BirdVisionNotInternet -> {
                            birdVisionLoadBinding.birdVisionStateGroup.visibility = View.VISIBLE
                            birdVisionLoadBinding.birdVisionLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun birdVisionNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_birdVisionLoadFragment_to_birdVisionV,
            bundleOf(BIRD_VISION_D to data)
        )
    }

    companion object {
        const val BIRD_VISION_D = "birdVisionData"
    }
}