package com.vision.birdvisionpr.verfs.presentation.di

import com.vision.birdvisionpr.verfs.data.repo.BirdVisionRepository
import com.vision.birdvisionpr.verfs.data.shar.BirdVisionSharedPreference
import com.vision.birdvisionpr.verfs.data.utils.BirdVisionPushToken
import com.vision.birdvisionpr.verfs.data.utils.BirdVisionSystemService
import com.vision.birdvisionpr.verfs.domain.usecases.BirdVisionGetAllUseCase
import com.vision.birdvisionpr.verfs.presentation.pushhandler.BirdVisionPushHandler
import com.vision.birdvisionpr.verfs.presentation.ui.load.BirdVisionLoadViewModel
import com.vision.birdvisionpr.verfs.presentation.ui.view.BirdVisionViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val birdVisionModule = module {
    factory {
        BirdVisionPushHandler()
    }
    single {
        BirdVisionRepository()
    }
    single {
        BirdVisionSharedPreference(get())
    }
    factory {
        BirdVisionPushToken()
    }
    factory {
        BirdVisionSystemService(get())
    }
    factory {
        BirdVisionGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        BirdVisionViFun(get())
    }
    viewModel {
        BirdVisionLoadViewModel(get(), get(), get())
    }
}