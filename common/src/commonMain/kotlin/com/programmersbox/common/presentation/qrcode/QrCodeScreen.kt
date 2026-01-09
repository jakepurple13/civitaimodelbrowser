package com.programmersbox.common.presentation.qrcode

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.Screen
import com.programmersbox.common.presentation.components.LoadingImage
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.util.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.publicvalue.multiplatform.qrcode.CameraPosition
import org.publicvalue.multiplatform.qrcode.CodeType
import org.publicvalue.multiplatform.qrcode.ScannerWithPermissions

@Serializable
data class QrCodeInfo(
    val title: String,
    val url: String,
    val imageUrl: String,
    val id: String?,
    val username: String?,
    val qrCodeType: QrCodeType,
)

enum class QrCodeType {
    Model,
    User,
    Image
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareViaQrCode(
    title: String,
    url: String,
    imageUrl: String,
    qrCodeType: QrCodeType,
    onClose: () -> Unit,
    username: String? = null,
    id: String? = null,
) {
    ShareViaQrCode(
        qrCodeInfo = QrCodeInfo(
            title = title,
            url = url,
            imageUrl = imageUrl,
            id = id,
            username = username,
            qrCodeType = qrCodeType
        ),
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareViaQrCode(
    qrCodeInfo: QrCodeInfo,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val onDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }
        onClose()
    }

    val qrCodeRepository = koinInject<QrCodeRepository>()
    val logoPainter = painterResource(Res.drawable.civitai_logo)
    val painter = rememberQrCodePainter(
        remember { Json.encodeToString(qrCodeInfo) }
    ) {
        logo {
            painter = logoPainter
            padding = QrLogoPadding.Natural(.1f)
            shape = QrLogoShape.circle()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Scaffold { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                val graphicsLayer = rememberGraphicsLayer()
                SelectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.drawWithContent {
                            // call record to capture the content in the graphics layer
                            graphicsLayer.record {
                                // draw the contents of the composable into the graphics layer
                                this@drawWithContent.drawContent()
                            }
                            // draw the graphics layer on the visible canvas
                            drawLayer(graphicsLayer)
                        }
                    ) {
                        Text(
                            qrCodeInfo.title,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Image(
                            painter = painter,
                            contentDescription = "QR code",
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.onSurface,
                                    MaterialTheme.shapes.medium
                                )
                                .padding(16.dp)
                                .animateContentSize()
                        )
                    }
                }
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            //TODO: In an update, change to copy to clipboard
                            qrCodeRepository.shareImage(
                                bitmap = graphicsLayer.toImageBitmap(),
                                title = qrCodeInfo.title
                            )
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(.75f)
                ) { Text("Share") }

                ElevatedButton(
                    onClick = {
                        scope.launch {
                            qrCodeRepository.saveImage(
                                bitmap = graphicsLayer.toImageBitmap(),
                                title = qrCodeInfo.title
                            )
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(.75f)
                ) { Text("Save") }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            qrCodeRepository.shareUrl(qrCodeInfo.url, qrCodeInfo.title)
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(.75f)
                ) { Text("Share Url") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScanQrCode(
    onBack: () -> Unit,
    onNavigate: (NavKey) -> Unit,
    viewModel: QrCodeScannerViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val onDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }
            .invokeOnCompletion { onBack() }
    }

    val qrCodeInfo = viewModel.qrCodeInfo

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Scan QR code") },
                    windowInsets = WindowInsets(0.dp),
                )
            },
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
        ) { padding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(padding)
                    .animateContentSize()
                    .fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    var torchState by remember { mutableStateOf(false) }
                    ScannerWithPermissions(
                        onScanned = { scan ->
                            runCatching { Json.decodeFromString<QrCodeInfo>(scan) }
                                .onSuccess {
                                    viewModel.qrCodeInfo = it
                                    scope.launch { sheetState.expand() }
                                }
                                .onFailure { it.printStackTrace() }

                            false
                        },
                        types = listOf(CodeType.QR),
                        cameraPosition = CameraPosition.BACK,
                        enableTorch = torchState,
                        permissionDeniedContent = { permissionState ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        MaterialTheme.shapes.medium
                                    )
                            ) {
                                Text(
                                    text = "Camera is required for QR Code scanning",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(6.dp)
                                )
                                ElevatedButton(
                                    onClick = { permissionState.goToSettings() }
                                ) { Text("Open Settings") }
                            }
                        },
                        modifier = Modifier
                            .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .animateContentSize()
                    )

                    FilledTonalIconButton(
                        onClick = { torchState = !torchState },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            if (torchState)
                                Icons.Default.FlashOn
                            else
                                Icons.Default.FlashOff,
                            null
                        )
                    }
                }

                val filePicker = rememberFilePickerLauncher(
                    type = FileKitType.Image
                ) { file ->
                    println("File: $file")
                    file ?: return@rememberFilePickerLauncher
                    scope.launch {
                        runCatching { file.toImageBitmap() }
                            .onSuccess {
                                viewModel.scanQrCodeFromImage(it)
                                scope.launch { sheetState.expand() }
                            }
                            .onFailure { it.printStackTrace() }
                    }
                }

                FilledTonalButton(
                    onClick = { filePicker.launch() },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(.75f)
                ) { Text("Upload Image from Gallery") }

                if (qrCodeInfo == null) {
                    Text(
                        "Waiting for QR code",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    ListItem(
                        headlineContent = { Text(qrCodeInfo.title) },
                        overlineContent = { Text(qrCodeInfo.username ?: qrCodeInfo.url) },
                        leadingContent = {
                            LoadingImage(
                                imageUrl = qrCodeInfo.imageUrl,
                                name = qrCodeInfo.title,
                                isNsfw = false,
                                hash = null,
                                modifier = Modifier
                                    .size(
                                        width = ComposableUtils.IMAGE_WIDTH / 3,
                                        height = ComposableUtils.IMAGE_HEIGHT / 3
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            qrCodeInfo?.let {
                                when (it.qrCodeType) {
                                    QrCodeType.Model ->
                                        onNavigate(Screen.Detail(it.id.orEmpty()))

                                    QrCodeType.User ->
                                        onNavigate(Screen.User(it.username.orEmpty()))

                                    QrCodeType.Image -> onNavigate(
                                        Screen.DetailsImage(
                                            modelId = it.id.orEmpty(),
                                            modelName = it.title
                                        )
                                    )
                                }
                            }
                        }
                    },
                    enabled = qrCodeInfo != null,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(.75f)
                ) { Text("Open") }
            }
        }
    }
}

class QrCodeScannerViewModel(
    private val qrCodeRepository: QrCodeRepository,
) : ViewModel() {
    var qrCodeInfo by mutableStateOf<QrCodeInfo?>(null)

    fun scanQrCodeFromImage(bitmap: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            qrCodeRepository.getInfoFromQRCode(bitmap)
                .mapCatching { Json.decodeFromString<QrCodeInfo>(it.first()) }
                .onSuccess { qrCodeInfo = it }
                .onFailure { it.printStackTrace() }
        }
    }
}

expect class QrCodeRepository {
    suspend fun getInfoFromQRCode(
        bitmap: ImageBitmap,
    ): Result<List<String>>

    suspend fun shareImage(bitmap: ImageBitmap, title: String)
    suspend fun saveImage(bitmap: ImageBitmap, title: String)
    suspend fun shareUrl(url: String, title: String)
}