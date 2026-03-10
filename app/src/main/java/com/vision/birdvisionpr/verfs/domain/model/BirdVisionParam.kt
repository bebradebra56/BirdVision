package com.vision.birdvisionpr.verfs.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


private const val BIRD_VISION_A = "com.vision.birdvisionpr"
private const val BIRD_VISION_B = "birdvision-f1b0e"
@Serializable
data class BirdVisionParam (
    @SerialName("af_id")
    val birdVisionAfId: String,
    @SerialName("bundle_id")
    val birdVisionBundleId: String = BIRD_VISION_A,
    @SerialName("os")
    val birdVisionOs: String = "Android",
    @SerialName("store_id")
    val birdVisionStoreId: String = BIRD_VISION_A,
    @SerialName("locale")
    val birdVisionLocale: String,
    @SerialName("push_token")
    val birdVisionPushToken: String,
    @SerialName("firebase_project_id")
    val birdVisionFirebaseProjectId: String = BIRD_VISION_B,
    )