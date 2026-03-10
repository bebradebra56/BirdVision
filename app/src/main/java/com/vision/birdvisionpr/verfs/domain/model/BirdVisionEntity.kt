package com.vision.birdvisionpr.verfs.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BirdVisionEntity (
    @SerialName("ok")
    val birdVisionOk: Boolean,
    @SerialName("url")
    val birdVisionUrl: String,
    @SerialName("expires")
    val birdVisionExpires: Long,
)