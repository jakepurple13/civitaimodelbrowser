package com.programmersbox.common.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asComposeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

@Composable
actual fun BlurHashImage(hash: String, contentDescription: String, modifier: Modifier) =
    CreateImageBlurHash(hash, contentDescription, modifier)

actual class PlatformShader actual constructor(shaderCode: String) {
    private val effect = RuntimeEffect.makeForShader(shaderCode)
    private val builder = RuntimeShaderBuilder(effect)

    actual fun setFloatUniform(name: String, vararg value: Float) {
        builder.uniform(name, value)
    }

    actual fun setMatrixUniform(name: String, matrix: FloatArray) {
        builder.uniform(name, matrix)
    }

    actual fun makeShader(): Shader {
        // org.jetbrains.skia.Shader maps perfectly to Compose's Shader type here
        return builder.makeShader().asComposeShader()
    }
}