package com.wyneg.dailyqr.ui.theme.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.wyneg.dailyqr.R
import java.io.File

@Composable
fun ButtonsScreen(
    navController: NavHostController,
    context: Context
) {

    FloatingButtonAppDescription(context)

    val fileName = "list_of_products.txt"
    if (!File(context.filesDir, fileName).exists()) {
        File(context.filesDir, fileName).createNewFile()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val modSize = Modifier.fillMaxWidth(0.8f)

        ElevatedButton(
            modifier = modSize,
            onClick = { navController.navigate("main") }) {
            Text(
                text = stringResource(R.string.crear_codigo_qr)
            )
        }

        ElevatedButton(
            modifier = modSize,
            onClick = { navController.navigate("second") }) {
            Text(
                text = stringResource(R.string.escanear_codigo_qr)
            )
        }

        ElevatedButton(
            modifier = modSize,
            onClick = { navController.navigate("third") }) {
            Text(
                text = stringResource(R.string.registro_ventas)
            )
        }

        ElevatedButton(
            modifier = modSize,
            onClick = { navController.navigate("fourth") }) {
            Text(
                text = stringResource(R.string.productos_registrados)
            )
        }
    }
}

@Composable
fun FloatingButtonAppDescription(context: Context) {

    var fab by remember { mutableStateOf(false) }

    val density = LocalConfiguration.current

    density.screenWidthDp

    SmallFloatingActionButton(
        modifier = Modifier.offset((density.screenWidthDp - 60).dp, 40.dp),
        elevation = FloatingActionButtonDefaults.elevation(),
        onClick = {
            fab = true
        }
    )
    {
        Icon(
            painter = painterResource(R.drawable.baseline_question_mark_24),
            contentDescription = "About app"
        )
    }

    if (fab) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            icon = {
            },
            title = {},
            text = {
                Column {
                    Text(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        text = buildAnnotatedString {
                            withStyle(
                                style = ParagraphStyle(
                                    lineHeight = 30.sp,
                                    lineBreak = LineBreak.Paragraph,
                                    textAlign = TextAlign.Center
                                )
                            ) {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF6650a4)
                                    )
                                ) {
                                    append(context.getString(R.string.bienvenido_a_daily_qr))
                                }

                                withStyle(style = SpanStyle()) {
                                    append(context.getString(R.string.texto1))
                                }

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(context.getString(R.string.crear_codigo_qr).plus(": "))
                                }

                                withStyle(style = SpanStyle()) {
                                    append(context.getString(R.string.texto2))
                                }

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(
                                        context.getString(R.string.escanear_codigo_qr).plus(": ")
                                    )
                                }

                                withStyle(style = SpanStyle()) {
                                    append(context.getString(R.string.texto3))
                                }

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(context.getString(R.string.registro_ventas).plus(": "))
                                }

                                withStyle(style = SpanStyle()) {
                                    append(context.getString(R.string.texto4))
                                }

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(context.getString(R.string.texto4punto5))
                                }

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(
                                        context.getString(R.string.productos_registrados).plus(": ")
                                    )
                                }

                                withStyle(style = SpanStyle()) {
                                    append(context.getString(R.string.texto5))
                                }

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(context.getString(R.string.texto4punto5))
                                }
                            }

                        }
                    )
                }
            },
            onDismissRequest = {

            },
            dismissButton = {

            },
            confirmButton = {
                TextButton(
                    onClick = {
                        fab = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.aceptar)
                    )
                }

            }
        )
    }

}

