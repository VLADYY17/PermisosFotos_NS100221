package sv.edu.ufg.fis.amb.permisosfotos_ns100221

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView // Para mostrar la imagen capturada
    private var imageUri: Uri? = null // URI de la imagen para compartir y guardar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de la vista y los botones
        imgView = findViewById(R.id.imgView)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnShare = findViewById<Button>(R.id.btnShare)
        val btnRetake = findViewById<Button>(R.id.btnRetake)

        // Configurar el evento al hacer clic en el botón para tomar una foto
        btnTakePhoto.setOnClickListener {
            if (checkPermissions()) {
                openCamera() // Abre la cámara si los permisos ya están concedidos
            } else {
                requestPermissions() // Solicita permisos si no están concedidos
            }
        }

        // Configurar el evento al hacer clic en el botón para guardar la imagen
        btnSave.setOnClickListener {
            saveImage() // Guarda la imagen
        }

        // Configurar el evento al hacer clic en el botón para compartir la imagen
        btnShare.setOnClickListener {
            shareImage() // Comparte la imagen
        }

        // Configurar el evento al hacer clic en el botón para volver a tomar la foto
        btnRetake.setOnClickListener {
            if (checkPermissions()) {
                openCamera() // Abre la cámara de nuevo si los permisos ya están concedidos
            } else {
                requestPermissions() // Solicita permisos si no están concedidos
            }
        }
    }

    // Verificar permisos para cámara y almacenamiento
    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED
    }

    // Solicitar permisos de cámara y almacenamiento
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
    }

    // Manejar la respuesta del usuario a la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) { // Verificamos el código de solicitud
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos, puedes abrir la cámara
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                openCamera() // Abre la cámara después de conceder permisos
            } else {
                // Permisos rechazados, muestra un mensaje de advertencia
                Toast.makeText(this, "Permisos rechazados. No se puede usar la cámara.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lanzar la cámara y capturar la imagen como Bitmap
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imgView.setImageBitmap(bitmap) // Mostrar la imagen en el ImageView
            imageUri = saveBitmapToUri(bitmap) // Guardar URI de la imagen
        }
    }

    // Método para abrir la cámara
    private fun openCamera() {
        takePictureLauncher.launch(null) // Lanza la captura de imagen
    }

    // Método para guardar la imagen
    private fun saveImage() {
        imageUri?.let {
            Toast.makeText(this, "Imagen guardada: $it", Toast.LENGTH_SHORT).show() // Mensaje de éxito
        } ?: Toast.makeText(this, "No hay imagen para guardar", Toast.LENGTH_SHORT).show() // Mensaje de error
    }

    // Método para compartir la imagen
    private fun shareImage() {
        imageUri?.let { uri ->
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND // Crear un Intent para compartir
                putExtra(Intent.EXTRA_STREAM, uri) // Añadir la imagen a compartir
                type = "image/*" // Tipo de archivo a compartir
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir imagen")) // Lanzar el selector de compartir
        } ?: Toast.makeText(this, "No hay imagen para compartir", Toast.LENGTH_SHORT).show() // Mensaje de error
    }

    // Método para guardar el bitmap como imagen y devolver su URI
    private fun saveBitmapToUri(bitmap: Bitmap): Uri? {
        val path = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, "Imagen Capturada", null // Guardar imagen en la galería
        )
        return Uri.parse(path) // Devolver el URI de la imagen guardada
    }
}
