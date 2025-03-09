package com.wyneg.dailyqr.ui.theme.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wyneg.dailyqr.R
import java.io.File


@Composable
fun MainScreen(navMainController: NavController,context: Context) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            space = 20.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var producto by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }

        OutlinedTextField(
            value = producto,
            onValueChange = { producto = it.replace("|", "") },
            label = {
                Text(
                    text = stringResource(R.string.nompre_producto)
                )
            },
            singleLine = true
        )

        OutlinedTextField(
            value = precio,
            onValueChange = {
                val pattern = Regex("[\\d,.]+")
                if (it.matches(pattern)) {
                    precio = it
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.ingrese_solo_numeros), Toast.LENGTH_SHORT
                    ).show()
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
            value = descripcion,
            onValueChange = { descripcion = it.replace("|", "") },
            label = {
                Text(
                    text = stringResource(R.string.descripcion)
                )
            },
            singleLine = true
        )

        ElevatedButton(
            onClick = {
//                navMainController.currentBackStackEntry?.savedStateHandle?.set("producto", producto)
//                navMainController.currentBackStackEntry?.savedStateHandle?.set("precio", precio)
                if (producto.isEmpty() || precio.isEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.Toast_campos_vacios), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    writeToCsv(producto, precio, descripcion, context)
                    navMainController.navigate("qr/${producto}/${precio}")
                }

            }
        ) {
            Text(
                text = stringResource(R.string.crear_codigo_qr)
            )
        }
    }
}

fun writeToCsv(producto: String, precio: String, descripcion: String, context: Context) {
    try {
        val p = producto.trim().replace("\\s+".toRegex(), "_")
        val values = p.plus("|").plus(precio).plus("|").plus(descripcion).plus("\n")
        val fileName = "list_of_products.txt"

        val fileList = File(context.filesDir, fileName)

        val filePrev = fileList.readLines().size

        fileList.appendText(values)

        val filePost = fileList.readLines().size

        val mensaje = if (filePost > filePrev) {
            context.getString(R.string.registro_guardado_exitosamente)
        } else {
            context.getString(R.string.registro_no_guardado)
        }

        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
}