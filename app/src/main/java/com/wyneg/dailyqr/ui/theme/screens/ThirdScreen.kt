package com.wyneg.dailyqr.ui.theme.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wyneg.dailyqr.R
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ThirdScreen(navController: NavController, context: Context){

    updateDatabase(context)

    RegisteredSales(navController, context)

}

@SuppressLint("MutableCollectionMutableState")
@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegisteredSales (navController: NavController, context: Context) {

    var fileName by remember { mutableStateOf("") }
    val listItems = context.filesDir.list()?.filter { it.contains(".csv") }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var state by remember { mutableIntStateOf(0) }
    val arrayFiles = remember { mutableStateOf(listItems) }
    var showCsv by remember { mutableStateOf(false) }
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
                text = stringResource(R.string.escanea_qr_para_mostrar_info),
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
            val textButton = if (state == 1) stringResource(R.string.aceptar_borrar_archivo) else stringResource(R.string.guardar_en_excel)

                Button(
                    onClick = {
                        if (state == 1) {
                            if (listItems != null) {
                                deleteCsvFile(fileName, listItems, arrayFiles, context)
                                isVisible = true
                            }
                            showBottomSheet = false
                        } else {
                            saveExcelFile(fileName, navController, context)
                        }
                    }
                ) {
                    Text(
                        text = textButton
                    )
                }
            }
        }
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        try {
            arrayFiles.value?.let {
                items(it.size) { item ->
                    val dismissState =  rememberSwipeToDismissBoxState(
                        confirmValueChange = { swipe ->
                            if (swipe == SwipeToDismissBoxValue.Settled) {
                                showBottomSheet = true
                                fileName = arrayFiles.value?.get(item).toString()
                                state = 1
                                true
                            } else {
                                false
                            }
                        }
                    )

                    if (showCsv){
                        val listaCsv = readCsv(fileName, context).toList()

                        AlertDialog(
                            icon = {},
                            title = {
                                Text(
                                    text = stringResource(
                                        R.string.registros_dia,
                                        fileName.replace(".csv", "")
                                            .replace("_", "-")
                                    ),
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.size(40.dp))
                            },
                            text = {
                                LazyColumn {
                                    items(listaCsv.size) { item ->
                                        val listOfList = listaCsv[item].toList()
                                        LazyRow {
                                            items(listOfList) { i ->
                                                Box(
                                                    modifier = Modifier.size(height = 60.dp, width = 100.dp),
                                                ) {
                                                    Text(
                                                        text = i
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onDismissRequest = { showCsv = false },
                            confirmButton = {
                                TextButton(
                                    onClick = { showCsv = false },
                                ) {
                                    Text(
                                        text = stringResource(R.string.aceptar)
                                    )
                                }
                            }
                        )
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 100.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ){
                                Icon(
                                    painter = painterResource(R.drawable.baseline_delete_sweep_24),
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    ) {
                            isVisible = false
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                                    .height(60.dp)
                                    .combinedClickable(
                                        onClick = {
                                            showCsv = true
                                            fileName = arrayFiles.value?.get(item).toString()
                                        },
                                        onLongClick = {
                                            showBottomSheet = true
                                            state = 0
                                            fileName = arrayFiles.value?.get(item).toString()
                                        }
                                    ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 8.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFFFF0)
                                ),

                                )
                            {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = arrayFiles.value?.get(item).toString()
                                            .replace(".csv", ""),
                                        color = Color(0xFF000000)
                                    )
                                }

                            }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace().toString()
            navController.popBackStack(inclusive = false, saveState = false, route = "splash")
        }
    }
}

fun readCsv(fileName: String, context: Context): Sequence<List<String>> {
    val reader = File(context.filesDir, fileName).inputStream().bufferedReader()

    return reader.lineSequence().filter { it.isNotBlank() }
        .map {
            it.split("|", ignoreCase = false, limit = 6)
        }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun saveExcelFile(fileName: String, navController: NavController, context: Context){
    try {
        val newFileName = fileName
            .replace(".csv", ".xlsx")

        val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, newFileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Files.FileColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val file = resolver.insert(uri, contentValues)
        val out = file?.let { resolver.openOutputStream(it) }

        val workbook = XSSFWorkbook()
        val workSheet = workbook.createSheet()
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = IndexedColors.WHITE.index

        //Aqui se escribe el HEADER del archivo excel

        val firstRowfirstCell = workSheet.createRow(0).createCell(0)
        firstRowfirstCell.setCellValue(context.getString(R.string.fecha_hora_venta))
        firstRowfirstCell.cellStyle = cellStyle

        val firstRowsecondCell = workSheet.getRow(0).createCell(1)
        firstRowsecondCell.setCellValue(context.getString(R.string.nompre_producto))
        firstRowsecondCell.cellStyle = cellStyle

        val firstRowthirdCell = workSheet.getRow(0).createCell(2)
        firstRowthirdCell.setCellValue(context.getString(R.string.venta))
        firstRowthirdCell.cellStyle = cellStyle

        val firstRowfourthCell = workSheet.getRow(0).createCell(3)
        firstRowfourthCell.setCellValue(context.getString(R.string.precio))
        firstRowfourthCell.cellStyle = cellStyle

        val firstRowfifthCell = workSheet.getRow(0).createCell(4)
        firstRowfifthCell.setCellValue(context.getString(R.string.cantidad))
        firstRowfifthCell.cellStyle = cellStyle

        val firstRowsixthCell = workSheet.getRow(0).createCell(5)
        firstRowsixthCell.setCellValue(context.getString(R.string.total))
        firstRowsixthCell.cellStyle = cellStyle

        //Aqui se escriben los datos del archivo diario CSV en el archivo Excel

        val fileLines = File(context.filesDir, fileName).readLines()

        val lines = fileLines.iterator()

        var rowIndex = 1

        while (lines.hasNext()){
            val line = lines.next().split("|")

            val index = rowIndex++

            val rowFirstCell = workSheet.createRow(index).createCell(0)
            rowFirstCell.setCellValue(line[0])
            rowFirstCell.cellStyle = cellStyle

            val rowSecondCell = workSheet.getRow(index).createCell(1)
            rowSecondCell.setCellValue(line[1])
            rowSecondCell.cellStyle = cellStyle

            val rowThirdCell = workSheet.getRow(index).createCell(2)
            rowThirdCell.setCellValue(line[2])
            rowThirdCell.cellStyle = cellStyle

            val rowFourthCell = workSheet.getRow(index).createCell(3)
            rowFourthCell.setCellValue(line[3].toDouble())
            rowFourthCell.cellStyle = cellStyle

            val rowFifthCell = workSheet.getRow(index).createCell(4)
            rowFifthCell.setCellValue(line[4].toDouble())
            rowFifthCell.cellStyle = cellStyle

            val rowSixthCell = workSheet.getRow(index).createCell(5)
            rowSixthCell.setCellValue(line[5].toDouble())
            rowSixthCell.cellStyle = cellStyle
        }

        workbook.write(out)
        workbook.close()
        out?.close()

        contentValues.clear()
        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)

        if (file != null) {
            resolver.update(file, contentValues, null, null)
        }

        updateDatabase(context)
        navController.popBackStack(inclusive = false, saveState = false, route = "splash")

    } catch (e: Exception) {
        e.printStackTrace().toString()
        navController.popBackStack(inclusive = false, saveState = false, route = "splash")
        updateDatabase(context)
    }
}

fun deleteCsvFile(filepath: String, listFile: List<String>, arrayList: MutableState<List<String>?>, context: Context) {
    try {
        val toDelete = File(context.filesDir, filepath)

        val arraySizePrev = arrayList.value?.size


        if (toDelete.exists() && toDelete.isFile) {
            toDelete.delete()
            arrayList.value = listFile.filter { it != listFile[listFile.indexOf(filepath)] }
        }

        val arraySizePost = arrayList.value?.size

        val mensaje = if (arraySizePrev!! > arraySizePost!!) {
            context.getString(R.string.registro_eliminado_exitosamente)
        } else {
            context.getString(R.string.registro_no_se_pudo_eliminar)
        }

        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
}

fun updateDatabase(context: Context) {
    try {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        MediaScannerConnection.scanFile(context,
            arrayOf(downloadsDirectory),
            null,
            null)
    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
}