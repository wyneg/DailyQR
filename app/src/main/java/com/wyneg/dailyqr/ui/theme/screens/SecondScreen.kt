package com.wyneg.dailyqr.ui.theme.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.wyneg.dailyqr.R
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SecondScreen(navcontroller: NavController, context: Context){

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    if (cameraPermissionState.status.isGranted) {
        ScanningScreen(navcontroller, context)
    } else {

        if (cameraPermissionState.status.shouldShowRationale) {
            Toast.makeText(context,
                stringResource(R.string.favor_aceptar_permiso), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context,
                stringResource(R.string.camara_necesita_permiso_funcionar), Toast.LENGTH_SHORT).show()
        }

        Column (
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button( onClick = {
                cameraPermissionState.launchPermissionRequest()
                if (cameraPermissionState.status.isGranted) {
                    Toast.makeText(context, context.getString(R.string.permiso_concedido), Toast.LENGTH_SHORT).show()
                }
            },
                modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(
                    text = stringResource(R.string.aceptar_permiso)
                )
            }
        }
    }
}

@Composable
fun ScanningScreen(navcontroller: NavController, context: Context){
    var scanFlag by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }
    var lastReadQRCode by remember { mutableStateOf<String?>(null) }
    var torchState by remember { mutableStateOf(false) }
    var recomposeFlag by remember { mutableIntStateOf(Random.nextInt()) }

        lastReadQRCode?.let {
            if (URLUtil.isValidUrl(lastReadQRCode)){
                val urlIntent = Intent(Intent.ACTION_VIEW, lastReadQRCode!!.toUri())

                urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                RedirectDialog(lastReadQRCode!!, urlIntent, navcontroller, context)

            } else if (lastReadQRCode!!.contains("_precio:")) {
                HowManyProductsToSave(navcontroller, it, context)
            } else {
                Toast.makeText(context, stringResource(R.string.qr_invalido), Toast.LENGTH_SHORT).show()
            }
        }

    key (recomposeFlag) {
        AndroidView(
            factory = { context ->
                val preview = CompoundBarcodeView(context)
                preview.setStatusText("")
                preview.cameraSettings.isAutoTorchEnabled = torchState

                preview.apply {
                    val capture = CaptureManager(context as Activity, this)
                    capture.initializeFromIntent(context.intent, null)
                    capture.decode()
                    this.decodeContinuous { result ->
                        if (scanFlag){
                            return@decodeContinuous
                        }
                        scanFlag = true
                        result.text?.let { //qRCode ->
                            lastReadQRCode = result.text
                            scanFlag = true
                            showResult = true
                            showModal = true
                        }
                    }
                    this.resume()
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { androidView ->
                androidView.pause()
                androidView.removeAllViews()
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(100.dp))
        IconButton(onClick = {
            torchState = !torchState
            recomposeFlag = Random.nextInt()
        },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondary))
        {
            Icon(
                painter = painterResource(id = if (torchState) R.drawable.baseline_flashlight_off_24 else R.drawable.baseline_flashlight_on_24),
                contentDescription = "Localized description"
            )
        }
    }
}

@Composable
fun RedirectDialog(
    lastReadQRCode: String,
    urlIntent: Intent,
    navcontroller: NavController,
    context: Context
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_web_24),
                contentDescription = "Web description"
            )
        },
        title = {
            Text(
                text = stringResource(R.string.aviso_redireccion),
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
            text = stringResource(R.string.desea_visitar, lastReadQRCode)
        )},
        onDismissRequest = {
            navcontroller.popBackStack(inclusive = false, saveState = false, route = "splash")
        },
        dismissButton = {
            TextButton(
                onClick = {
                    navcontroller.popBackStack(inclusive = false, saveState = false, route = "splash")
                }
            ) {
                Text(
                    text = stringResource(R.string.cancelar)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    context.startActivity(urlIntent)
                    navcontroller.popBackStack(inclusive = false, saveState = false, route = "splash")
                }
            ) {
                Text(
                    text = stringResource(R.string.aceptar)
                )
            }

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowManyProductsToSave(navcontroller: NavController, result: String, context: Context)
{
    val currentDate = LocalDate.now().toString().replace("-", "_")

    val fileName = currentDate.plus(".csv")

    val file = File(context.filesDir, fileName)

    if(!file.exists()){
        file.createNewFile()
    }

    var cantidad by remember { mutableStateOf("1") }
    val splitedResult = result.split("_precio:")
    val producto = splitedResult[0].replace("_", " ").uppercase()
    var precio by remember { mutableStateOf(splitedResult[1]) }
    val radioButtons = listOf(stringResource(R.string.efectivo),
        stringResource(R.string.debito), stringResource(R.string.credito))
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioButtons[0]) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet (
        modifier = Modifier.fillMaxHeight(0.63f),
        sheetState = sheetState,
        onDismissRequest = {
            navcontroller.popBackStack(inclusive = false, saveState = false, route = "splash")
        }
    ) {

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = producto,
                onValueChange = {},
                label = {
                    Text(
                        text = stringResource(R.string.nompre_producto)
                    )
                },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledLabelColor = MaterialTheme.colorScheme.primary,
                )
            )

            OutlinedTextField(
                value = precio,
                onValueChange = {
                    val pattern = Regex("[\\d,.]+")
                    if (it.matches(pattern)) {
                        precio = it
                    } else {
                        Toast.makeText(context,
                            context.getString(R.string.ingrese_solo_numeros), Toast.LENGTH_SHORT).show()
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.precio)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = {
                    Text(
                        text = stringResource(R.string.cantidad)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Column (
                modifier = Modifier.selectableGroup()
            )
            {
                radioButtons.forEach{ text ->
                    Row (
                      Modifier
                          .fillMaxWidth()
                          .height(50.dp)
                          .selectable(
                              selected = (text == selectedOption),
                              onClick = { onOptionSelected(text) },
                              role = Role.RadioButton
                          )
                          .padding(horizontal = 44.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }

            }

            Spacer(
                modifier = Modifier.size(25.dp)
            )

            Button(
                onClick = {
                    val total = precio.toInt() * cantidad.toInt()
                    val fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString()

                    val fileSizePrev = file.readLines().size

                    file.appendText("${fechaHora}|${producto}|${selectedOption}|${precio}|${cantidad}|${total}\n")

                    val fileSizePost = file.readLines().size

                    val mensaje = if (fileSizePost > fileSizePrev) {
                        context.getString(R.string.registro_guardado_exitosamente)
                    } else {
                        context.getString(R.string.registro_no_guardado)
                    }

                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                    navcontroller.popBackStack(inclusive = false, saveState = false, route = "splash")
                }
            ) {
                Text(
                    text = stringResource(R.string.registrar_venta)
                )
            }
        }
    }
}
