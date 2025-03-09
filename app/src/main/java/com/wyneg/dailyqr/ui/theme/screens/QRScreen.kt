package com.wyneg.dailyqr.ui.theme.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.wyneg.dailyqr.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun QRScreen(
    navController: NavHostController,
    context: Context,
    producto: String?,
    precio: String?
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            space = 20.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        val prod = navController.previousBackStackEntry?.savedStateHandle?.get<String>("producto").toString()

//        val prec = navController.previousBackStackEntry?.savedStateHandle?.get<String>("precio").toString()

        val p = producto?.trim()?.replace("\\s+".toRegex(), "_")

        val prodPrec = p.plus("_precio:").plus(precio)

        if (p != null) {
            Text(
                text = p.replace("_", " ").uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color(0xFF6D38B5)
            )
        }

        Text(
            text = "$".plus(precio),
            style = MaterialTheme.typography.titleMedium
        )

        val image = rememberQRBitmapPainter(prodPrec)

        Image(
            painter = BitmapPainter(image.asImageBitmap()),
            contentDescription = "QR code by Wyneg",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(150.dp)
        )

        Row {
            ElevatedButton(
                onClick = {
                    saveQRCodeToStorage(image, p, context)
                    navController.popBackStack(inclusive = false, saveState = false, route = "splash")
                }
            ) {
                Icon(
                    painter =  painterResource(R.drawable.baseline_download_24),
                    contentDescription = "Download description"
                )
            }

            Spacer(modifier = Modifier.padding(horizontal = 5.dp))

            ElevatedButton(
                onClick = {
                    shareImage(image, context)

                }
            ) {
                Icon(
                    painter =  painterResource(R.drawable.baseline_share_24),
                    contentDescription = "Share description"
                )
            }
        }
    }

    BackHandler {
        navController.popBackStack(inclusive = false, saveState = false, route = "splash")
    }
}

@Composable
fun rememberQRBitmapPainter(
    content: String,
    size: Dp = 150.dp,
    padding: Dp = 0.5.dp
): Bitmap {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val paddingPx = with(density) { padding.roundToPx() }

    var bitmap by remember(content) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) return@LaunchedEffect

        launch(Dispatchers.IO) {
            val qrCodeWriter = QRCodeWriter()

            val encodeHints = mutableMapOf<EncodeHintType, Any?>()
                .apply {
                    this[EncodeHintType.MARGIN] = paddingPx
                }

            val bitmapMatrix = try {
                qrCodeWriter.encode(
                    content, BarcodeFormat.QR_CODE,
                    sizePx, sizePx, encodeHints
                )
            } catch (e: WriterException) {
                null
            }

            val matrixWidth = bitmapMatrix?.width ?: sizePx
            val matrixHeigth = bitmapMatrix?.height ?: sizePx

            val newBitmap = Bitmap.createBitmap(
                bitmapMatrix?.width ?: sizePx,
                bitmapMatrix?.height ?: sizePx,
                Bitmap.Config.ARGB_8888,
            )

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeigth) {
                    val shouldColorPixel = bitmapMatrix?.get(x, y) ?: false
                    val pixelColor = if (shouldColorPixel) Color.BLACK else Color.WHITE

                    newBitmap.setPixel(x, y, pixelColor)
                }
            }

            bitmap = newBitmap
        }
    }

    return remember(bitmap) {
        val currentBitmap = bitmap ?: Bitmap.createBitmap(
            sizePx, sizePx, Bitmap.Config.ARGB_8888
        ).apply { eraseColor(Color.TRANSPARENT) }
        textToQRCode(currentBitmap, content)
    }
}

fun textToQRCode(bitmap: Bitmap, text: String): Bitmap {

    val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

    try {
        val newText = text.split("_precio:")

        val canvas = Canvas()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.GRAY

        paint.textSize = 15f

        val bounds = Rect()

        paint.getTextBounds(newText[0], 0, newText[0].length,  bounds)

        val x = newBitmap?.width?.div(newText[0].length)

        val y = newBitmap?.height?.minus(10)

        canvas.setBitmap(newBitmap)

        if (x != null) {
            if (y != null) {
                canvas.drawText(newText[0].replace("_", " "), x.toFloat(), y.toFloat(), paint)

            }
        }

    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
    return newBitmap

}

@RequiresApi(Build.VERSION_CODES.Q)
fun saveQRCodeToStorage(bitmap: Bitmap?, nombreproducto: String?, context: Context) {
    try {
        val prodNoTwoPoints = nombreproducto?.replace("_precio:", "_")
        val filename = prodNoTwoPoints.plus("_").plus(System.currentTimeMillis().toString())
        val imageFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            filename.plus(".png")
        )

        //Para guardar imagen en directorio y que se actualice en el MediaStore y aparezca directamente en
        //las aplicaciones que leen fotos, como Google Photos
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUri = context.contentResolver.insert(collection, values)

        imageUri?.let {
            context.contentResolver.openOutputStream(it).use { out ->
                if (out != null) {
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)

        if (imageUri != null) {
            context.contentResolver.update(imageUri, values, null, null)
        }

        val mensaje = if (imageFile.exists()) {
            context.getString(R.string.saved)
        } else {
            context.getString(R.string.not_saved)
        }

        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
}

fun shareImage(bitmap: Bitmap, context: Context){

    try {
        val toShareImage = File(context.cacheDir, "Bitmap.png")
        val outPutStream = FileOutputStream(toShareImage)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)

        outPutStream.close()

        val uri = FileProvider.getUriForFile(context, context.packageName, toShareImage)

        val shareImageIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
        }

        shareImageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

       context.startActivity(shareImageIntent)

    } catch (e: Exception){
        e.printStackTrace().toString()
    }
}