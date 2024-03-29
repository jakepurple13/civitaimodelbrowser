package com.programmersbox.common

import kotlinx.serialization.SerialName
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
    val allowDerivatives: Boolean,
    val allowDifferentLicense: Boolean,
    val tags: List<String>,
    val modelVersions: List<ModelVersion>,
    val creator: Creator? = null,
) {
    fun parsedDescription() = Jsoup.parse(description.orEmpty()).text()
}

@Serializable
data class Creator(
    val username: String? = null,
    val image: String? = null,
)

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
    //val createdAt: Instant? = null,
    //val updatedAt: Instant,
    //val trainedWords: List<String>,
    val baseModel: String,
    //val baseModelType: String?,
    val description: String? = null,
    val images: List<ModelImage>,
    val downloadUrl: String? = null,
) {
    fun parsedDescription() = description?.let { Jsoup.parse(it).text() }
}

@Serializable
data class ModelImage(
    val id: String? = "",
    val url: String,
    val nsfw: NsfwLevel = NsfwLevel.None,
    val width: Int,
    val height: Int,
    val meta: ImageMeta? = null,
)

@Serializable
data class CivitAiCustomImages(
    val items: List<CustomModelImage>,
)

@Serializable
data class CustomModelImage(
    val id: String? = "",
    val url: String,
    val nsfwLevel: NsfwLevel = NsfwLevel.None,
    val width: Int,
    val height: Int,
    val meta: ImageMeta? = null,
    val postId: Long? = null,
    val username: String? = null,
)

@Serializable
enum class NsfwLevel {
    None,
    Soft,
    Mature,
    X,
    Blocked;

    fun canShow() = this == None || this == Soft
    fun canNotShow() = !(this == None || this == Soft)
}

@Serializable
data class ImageMeta(
    @SerialName("Size")
    val size: String? = null,
    val seed: Long? = null,
    @SerialName("Model")
    val model: String? = null,
    val steps: Long? = null,
    val prompt: String? = null,
    val sampler: String? = null,
    val cfgScale: Double? = null,
    @SerialName("Clip skip")
    val clipSkip: String? = null,
    val negativePrompt: String? = null,
)

@Serializable
data class PageData(
    val totalItems: Long? = null,
    val currentPage: Long? = null,
    val pageSize: Long? = null,
    val totalPages: Long? = null,
    val nextPage: String? = null,
)
