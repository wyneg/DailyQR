package com.wyneg.dailyqr.ui.theme.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wyneg.dailyqr.R
import java.io.File
import kotlin.io.path.appendText
import kotlin.io.path.copyTo
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FourthScreen(navController: NavController, context: Context) {

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val fileName = "list_of_products.txt"
    val listaProductos = File(context.filesDir, fileName).readLines()
    var productoABorrar by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.crea_codigos_qr_para_mostrar_info),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.height(120.dp),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
        )
        {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Button(
                    onClick = {
                        deleteRecordProduct(fileName, productoABorrar, listaProductos, context)
                        showBottomSheet = false
                        isVisible = true
                    }
                ) {
                    Text(
                        text = stringResource(R.string.seguro_borrar_registro)
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        try {
            items(listaProductos) { producto ->
                val itemsCardList = producto.split("|")
                isVisible = false
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .combinedClickable(
                            onClick = {
                                navController.navigate("qr/${itemsCardList[0]}/${itemsCardList[1]}")
                            },
                            onLongClick = {
                                showBottomSheet = true
                                productoABorrar = producto
                            }
                        ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFF0)
                    ),
                ) {
                    Text(
                        text = itemsCardList[0].replace("_", " ").uppercase(),
                        modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp, bottom = 10.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6D38B5)
                    )
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Text(
                            text = stringResource(R.string.precio)
                                .plus(": \n$").plus(itemsCardList[1]),
                            modifier = Modifier
                                .size(height = 80.dp, width = 90.dp)
                                .padding(top = 5.dp, start = 15.dp, bottom = 5.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF000000)
                        )

                        TextField(
                            value = stringResource(R.string.descripcion)
                                .plus(": \n").plus(itemsCardList[2]),
                            onValueChange = {},
                            modifier = Modifier
                                .size(height = 100.dp, width = 380.dp)
                                .padding(top = 0.dp, start = 35.dp, bottom = 5.dp),
                            readOnly = true,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFFFFFF0),
                                focusedContainerColor = Color(0xFFFFFFF0),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace().toString()
        }
    }
}

fun deleteRecordProduct(filename: String, productoABorrar: String, linesProducts: List<String>, context: Context){
    try {
        val newTempFile = createTempFile(filename)
        linesProducts.let { lineas ->
            val newList = lineas.filter {
                it != productoABorrar
            }
            newList.forEach { linea ->
                newTempFile.appendText(linea.plus("\n"))
            }
        }

        val newFile = File(context.filesDir, filename)

        val newFilePrev = newFile.readLines().size

        newTempFile.copyTo(newFile.toPath(), overwrite = true)

        val newFilePost = newFile.readLines().size

        val mensaje = if (newFile.exists() && newFilePrev > newFilePost) {
            context.getString(R.string.registro_eliminado_exitosamente)
        } else {
            context.getString(R.string.registro_no_se_pudo_eliminar)
        }

        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

        if (mensaje == context.getString(R.string.registro_eliminado_exitosamente)) {
            newTempFile.deleteIfExists()
        }

    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
}
