package com.example.inventariomovilti

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.inventariomovilti.ui.theme.InventarioMovilTITheme

data class Producto(
    val nombre: String,
    val stock: Int,
    val minimo: Int
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)
            .get(InventarioViewModel::class.java)

        setContent {
            InventarioMovilTITheme {
                Inventario(viewModel)
            }
        }
    }
}

@Composable
fun Inventario(viewModel: InventarioViewModel) {

    val contexto = LocalContext.current
    val productos = viewModel.productos

    var nombre by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minimo by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var pantalla by remember { mutableStateOf("inventario") }
    var imagenProducto by remember { mutableStateOf<Bitmap?>(null) }

    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) {
            imagenProducto = foto
            mensaje = "Foto tomada correctamente"
        } else {
            mensaje = "No se tomó ninguna foto"
        }
    }

    val permisoNotificaciones = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            mostrarNotificacion(contexto)
        } else {
            mensaje = "Permiso de notificaciones denegado"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarProductos(
            RepositorioInventario.cargar(contexto)
        )
    }

    LaunchedEffect(productos) {
        RepositorioInventario.guardar(contexto, productos)
    }

    val errores = ValidadorProducto.validar(
        nombre = nombre,
        stock = stock,
        minimo = minimo,
        nombresExistentes = productos.map { it.nombre }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Inventario Movil TI",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { pantalla = "inventario" },
                modifier = Modifier.weight(1f)
            ) {
                Text("Inventario")
            }

            OutlinedButton(
                onClick = { pantalla = "resumen" },
                modifier = Modifier.weight(1f)
            ) {
                Text("Resumen")
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (pantalla == "inventario") {

            Text(
                "Registrar nuevo producto",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    mensaje = ""
                },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = enviado && errores.nombre.isNotEmpty(),
                supportingText = {
                    if (enviado && errores.nombre.isNotEmpty()) {
                        Text(errores.nombre)
                    }
                }
            )

            OutlinedTextField(
                value = stock,
                onValueChange = {
                    stock = it
                    mensaje = ""
                },
                label = { Text("Stock inicial") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = enviado && errores.stock.isNotEmpty(),
                supportingText = {
                    if (enviado && errores.stock.isNotEmpty()) {
                        Text(errores.stock)
                    }
                }
            )

            OutlinedTextField(
                value = minimo,
                onValueChange = {
                    minimo = it
                    mensaje = ""
                },
                label = { Text("Stock minimo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = enviado && errores.minimo.isNotEmpty(),
                supportingText = {
                    if (enviado && errores.minimo.isNotEmpty()) {
                        Text(errores.minimo)
                    }
                }
            )

            Button(
                onClick = {
                    enviado = true

                    val resultado = ValidadorProducto.validar(
                        nombre = nombre,
                        stock = stock,
                        minimo = minimo,
                        nombresExistentes = productos.map { it.nombre }
                    )

                    if (resultado.esValido) {
                        viewModel.registrarProducto(
                            Producto(
                                nombre = nombre.trim(),
                                stock = stock.toIntOrNull() ?: 0,
                                minimo = minimo.toIntOrNull() ?: 0
                            )
                        )

                        nombre = ""
                        stock = ""
                        minimo = ""
                        enviado = false
                        mensaje = "Producto registrado correctamente"
                    } else {
                        mensaje = "Revisa los campos marcados"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar producto")
            }

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (
                        mensaje == "Producto registrado correctamente" ||
                        mensaje == "Foto tomada correctamente"
                    ) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                "Productos registrados: ${productos.size}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(productos) { producto ->

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    producto.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text("Stock: ${producto.stock}")
                                Text("Stock minimo: ${producto.minimo}")

                                if (producto.stock <= producto.minimo) {
                                    Text(
                                        "⚠ ALERTA: STOCK BAJO",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Row {
                                    Button(
                                        onClick = {
                                            viewModel.aumentarStock(producto.nombre)
                                        }
                                    ) {
                                        Text("+ Entrada")
                                    }

                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.disminuirStock(producto.nombre)
                                        }
                                    ) {
                                        Text("- Salida")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {

            Text(
                "Resumen del inventario",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Cantidad de productos: ${productos.size}")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Unidades totales en stock: ${productos.sumOf { it.stock }}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Productos con stock bajo: ${
                    productos.count { it.stock <= it.minimo }
                }"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (
                            ContextCompat.checkSelfPermission(
                                contexto,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            mostrarNotificacion(contexto)
                        } else {
                            permisoNotificaciones.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    } else {
                        mostrarNotificacion(contexto)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar notificacion")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    camara.launch(null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomar foto de producto")
            }

            if (imagenProducto != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Image(
                    bitmap = imagenProducto!!.asImageBitmap(),
                    contentDescription = "Foto del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { pantalla = "inventario" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inventario")
            }
        }
    }
}

fun mostrarNotificacion(contexto: Context) {

    val idCanal = "inventario_alertas"

    val administrador = contexto.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            idCanal,
            "Alertas de inventario",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        administrador.createNotificationChannel(canal)
    }

    val notificacion = NotificationCompat.Builder(contexto, idCanal)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Inventario Movil TI")
        .setContentText("¡La notificación de prueba funciona!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    administrador.notify(1, notificacion)
}