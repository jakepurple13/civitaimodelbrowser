package com.programmersbox.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.fleeksoft.ksoup.Ksoup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Stable
@Immutable
@Serializable
data class CivitAi(
    val items: List<Models>,
    val metadata: PageData,
)

@Stable
@Immutable
@Serializable
data class Models(
    val id: Long,
    val name: String,
    val description: String?,
    val type: ModelType = ModelType.Other,
    val nsfw: Boolean,
    val allowNoCredit: Boolean = false,
    val allowDerivatives: Boolean = false,
    val allowDifferentLicense: Boolean = false,
    val tags: List<String>,
    val modelVersions: List<ModelVersion> = emptyList(),
    val creator: Creator? = null,
) {
    fun parsedDescription() = Ksoup.parse(description.orEmpty()).text()
}

@Stable
@Immutable
@Serializable
data class Creator(
    val username: String? = null,
    val image: String? = null,
)

@Immutable
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

@Stable
@Immutable
@Serializable
data class ModelVersion(
    val id: Long,
    val modelId: Long? = null,
    val name: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val publishedAt: Instant? = null,
    //val trainedWords: List<String>,
    val baseModel: String,
    //val baseModelType: String?,
    val description: String? = null,
    val images: List<ModelImage> = emptyList(),
    val downloadUrl: String? = null,
) {
    fun parsedDescription() = description?.let { Ksoup.parse(it).text() }
}

@Stable
@Immutable
@Serializable
data class ModelImage(
    val id: String? = "",
    val url: String,
    val nsfw: NsfwLevel = NsfwLevel.None,
    val width: Int,
    val height: Int,
    val meta: ImageMeta? = null,
    val hash: String? = null,
)

@Stable
@Immutable
@Serializable
data class CivitAiCustomImages(
    val items: List<CustomModelImage>,
    val metadata: PageData,
)

@Stable
@Immutable
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
    val hash: String? = null,
)

@Immutable
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

@Stable
@Immutable
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

@Stable
@Immutable
@Serializable
data class PageData(
    val totalItems: Long? = null,
    val currentPage: Long? = null,
    val pageSize: Long? = null,
    val totalPages: Long? = null,
    val nextPage: String? = null,
    val prevPage: String? = null,
)
