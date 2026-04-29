package com.programmersbox.common.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import kotlin.math.pow
import kotlin.math.withSign

@Stable
class Size(val width: Int, val height: Int)

@Stable
class ParsedBlurHash(
    val size: com.programmersbox.common.presentation.components.Size,
    val colors: Array<FloatArray>
)

// Thanks to https://github.com/woltapp/blurhash
object BlurHashDecoder {
    /**
     * Parse blur hash string into number of components and maximum color component.
     */
    fun parse(blurHash: String?, punch: Float = 1f): ParsedBlurHash? {
        if (blurHash == null || blurHash.length < 6) {
            return null
        }
        val numCompEnc = decode83(blurHash, 0, 1)
        val numCompX = (numCompEnc % 9) + 1
        val numCompY = (numCompEnc / 9) + 1
        if (blurHash.length != 4 + 2 * numCompX * numCompY) {
            return null
        }
        val maxAcEnc = decode83(blurHash, 1, 2)
        val maxAc = (maxAcEnc + 1) / 166f
        val colors = Array(numCompX * numCompY) { i ->
            if (i == 0) {
                val colorEnc = decode83(blurHash, 2, 6)
                decodeDc(colorEnc)
            } else {
                val from = 4 + i * 2
                val colorEnc = decode83(blurHash, from, from + 2)
                decodeAc(colorEnc, maxAc * punch)
            }
        }
        return ParsedBlurHash(Size(numCompX, numCompY), colors)
    }

    private fun decode83(str: String, from: Int = 0, to: Int = str.length): Int {
        var result = 0
        for (i in from until to) {
            val index = charMap[str[i]] ?: -1
            if (index != -1) {
                result = result * 83 + index
            }
        }
        return result
    }

    private fun decodeDc(colorEnc: Int): FloatArray {
        val r = colorEnc shr 16
        val g = (colorEnc shr 8) and 255
        val b = colorEnc and 255
        return floatArrayOf(srgbToLinear(r), srgbToLinear(g), srgbToLinear(b))
    }

    private fun srgbToLinear(colorEnc: Int): Float {
        val v = colorEnc / 255f
        return if (v <= 0.04045f) {
            (v / 12.92f)
        } else {
            ((v + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    private fun decodeAc(value: Int, maxAc: Float): FloatArray {
        val r = value / (19 * 19)
        val g = (value / 19) % 19
        val b = value % 19
        return floatArrayOf(
            signedPow2((r - 9) / 9.0f) * maxAc,
            signedPow2((g - 9) / 9.0f) * maxAc,
            signedPow2((b - 9) / 9.0f) * maxAc
        )
    }

    private fun signedPow2(value: Float) = value.pow(2f).withSign(value)

    private val charMap = listOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '#', '$', '%', '*', '+', ',',
        '-', '.', ':', ';', '=', '?', '@', '[', ']', '^', '_', '{', '|', '}', '~'
    )
        .mapIndexed { i, c -> c to i }
        .toMap()

}

@Stable
class ComputedColorMatrix(
    val size: com.programmersbox.common.presentation.components.Size,
    val colors: FloatArray
)

// Thanks to peerless2012 (https://github.com/peerless2012/blurhash-android)
val blurhashComposeSKSL = """
        uniform vec2 startPos;
        uniform vec2 iResolution;
        uniform vec2 num;
        uniform vec4 colors[32];
        float linearTosRGB(float value) {
            float v = max(0, min(1, value));
            if (v <= 0.0031308) {
                return v * 12.92;
            } else {
                return pow(v, 1.0 / 2.4) * 1.055 - 0.055;
            }
        }
        vec4 main(float2 fragCoord) {
            vec2 uv = (fragCoord.xy - startPos.xy) / iResolution.xy;
            vec3 color = vec3(0.0);
            vec2 uvpi = uv * 3.14159265358979323846;
            int size = int(num.x * num.y);
            for (int index = 0; index < 32; index++) {
                if (index >= size) break;
                vec3 sColor = colors[index].rgb;
                float fIndex = float(index);
                float row = floor(fIndex / num.x);
                float col = floor(fIndex - (row * num.x));
                vec2 loopPos = vec2(col, row);
                vec2 basics = uvpi * loopPos;
                color += sColor * cos(basics.x) * cos(basics.y);
            }
            return vec4(linearTosRGB(color.r), linearTosRGB(color.g), linearTosRGB(color.b), 1.0);
}
    """.trimIndent()

fun calculateColorMatrix(hash: String): ComputedColorMatrix? {
    val computedColors = BlurHashDecoder.parse(hash) ?: return null

    // Each color takes up 4 numbers (r, g, b, a)
    val colorsMatrix = FloatArray(4 * 32)
    var index = 0

    for (color in computedColors.colors) {
        color.copyInto(colorsMatrix, index, startIndex = 0, endIndex = color.size)
        index += color.size
        colorsMatrix[index] = 0f
        index++
    }

    return ComputedColorMatrix(computedColors.size, colorsMatrix)
}

@Composable
fun CreateImageBlurHash(hash: String, contentDescription: String, modifier: Modifier = Modifier) {
    val compositeRuntimeEffect = remember { PlatformShader(blurhashComposeSKSL) }
    //val compositeRuntimeBuilder = RuntimeShaderBuilder(compositeRuntimeEffect)
    val computedMatrix = remember { calculateColorMatrix(hash) } ?: return

    compositeRuntimeEffect.setFloatUniform(
        "num",
        computedMatrix.size.height.toFloat(),
        computedMatrix.size.width.toFloat()
    )
    compositeRuntimeEffect.setMatrixUniform("colors", computedMatrix.colors)
    compositeRuntimeEffect.setFloatUniform("startPos", 0f, 0f)

    Box(
        modifier = Modifier
            .drawWithCache {
                compositeRuntimeEffect.setFloatUniform(
                    "iResolution",
                    this.size.width,
                    this.size.height
                )
                val brush = ShaderBrush(compositeRuntimeEffect.makeShader())

                onDrawBehind {
                    drawRect(brush, size = Size(this.size.width, this.size.height))
                }

            }
            .then(modifier)
    )
}

@Composable
expect fun BlurHashImage(hash: String, contentDescription: String, modifier: Modifier = Modifier)

expect class PlatformShader(shaderCode: String) {
    fun setFloatUniform(name: String, vararg value: Float)

    // Add other uniform setters as needed (e.g., setIntUniform, setColorUniform)
    fun setMatrixUniform(name: String, matrix: FloatArray)

    fun makeShader(): Shader
}