package com.programmersbox.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup

@Serializable
data class CivitAi(
    val items: List<Models>,
    val metadata: PageData,
)

@Serializable
data class Models(
    val id: Long,
    val name: String,
    val description: String?,
    val type: ModelType = ModelType.Other,
    val nsfw: Boolean,
    val allowNoCredit: Boolean,
    val allowCommercialUse: String,
    val allowDerivatives: Boolean,
    val allowDifferentLicense: Boolean,
    val tags: List<String>,
    val modelVersions: List<ModelVersion>,
) {
    fun parsedDescription() = Jsoup.parse(description.orEmpty()).text()
}

@Serializable
enum class ModelType {
    Checkpoint,
    TextualInversion,
    Hypernetwork,
    AestheticGradient,
    LORA,
    Controlnet,
    Poses,
    Other
}

@Serializable
data class ModelVersion(
    val id: Long,
    val modelId: Long,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val trainedWords: List<String>,
    val baseModel: String,
    val baseModelType: String?,
    val earlyAccessTimeFrame: Long,
    val description: String?,
    val images: List<ModelImage>,
    val downloadUrl: String,
) {
    fun parsedDescription() = description?.let { Jsoup.parse(it).text() }
}

@Serializable
data class ModelImage(
    val id: String? = "",
    val url: String,
    val nsfw: String,
    val width: Int,
    val height: Int,
)

@Serializable
data class PageData(
    val totalItems: Long,
    val currentPage: Long,
    val pageSize: Long,
    val totalPages: Long,
    val nextPage: String,
)
