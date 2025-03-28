package  com.adoptapaws.presentation.addAdoption

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import coil.compose.rememberAsyncImagePainter
import com.adoptapaws.domain.AddDog
import com.adoptapaws.presentation.adoptListHome.AdoptListHomeActivity
import com.adoptapaws.presentation.map.MapFragment
import com.adoptapaws.utils.SharedPrefHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

class AddAdoptionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null
    private var imageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            AddAdoptionScreen(
                imageUri = imageUri,
                onImageUriChange = { newUri -> imageUri = newUri }
            )
        }
        checkCameraPermission()
        requestLocationPermissions()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }

            else -> {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(this, "Could not get last known location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("JGBT", "onMapReady called" + currentLocation)
        currentLocation?.let {
            // Si currentLocation no es nulo, mueve la cámara a la ubicación actual
            val cameraPosition = CameraPosition.Builder()
                .target(it) // La ubicación actual
                .zoom(15f) // Nivel de zoom
                .build()

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            val markerOptions = MarkerOptions()
                .position(it) // Usar la ubicación actual
                .title("Tu Ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Puntito rojo

            googleMap.addMarker(markerOptions)
        } ?: run {
            // Si currentLocation es nulo, puedes usar una ubicación predeterminada
            val defaultLocation = LatLng(19.432608, -99.133209) // Ciudad de México
            val cameraPosition = CameraPosition.Builder()
                .target(defaultLocation)
                .zoom(15f)
                .build()

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            val markerOptions = MarkerOptions()
                .position(defaultLocation)
                .title("Ubicacion Default")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }

        Log.d("JGBT", "onMapReady called with location: $currentLocation")
    }

    fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), 1001)
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }


    /**
     * Composable function for the screen to add a dog for adoption.
     *
     * This screen allows the user to enter details about a dog they want to put up for adoption,
     * including its name, age, and a description. It also provides functionality to take a photo
     * of the dog and to select a location using a map.
     *
     * @param imageUri The URI of the image of the dog. Can be null if no image has been taken.
     * @param onImageUriChange A callback function to update the `imageUri`. It takes a nullable
     *   [Uri] as its parameter.
     */
    @Composable
    fun AddAdoptionScreen(
        imageUri: Uri?,
        onImageUriChange: (Uri?) -> Unit
    ) {
        val scrollState = rememberScrollState()
        var name by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        val context = LocalContext.current
        var currentImageUri by remember { mutableStateOf<Uri?>(null) }
        var googleMap by remember { mutableStateOf<GoogleMap?>(null) }  // Add this state

        val takePictureLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    onImageUriChange(currentImageUri)
                    Toast.makeText(context, "Foto tomada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Operación cancelada", Toast.LENGTH_SHORT).show()
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Agregar Perro en Adopción", fontWeight = FontWeight.Bold)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Perro") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Edad del Perro") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        val uri = createImageUri(context)
                        if (uri != Uri.EMPTY) {
                            currentImageUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            Toast.makeText(context, "Error al crear URI", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(text = "Tomar Foto")
                }

                imageUri?.let {
                    if (it != Uri.EMPTY) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Imagen del Perro",
                            modifier = Modifier
                                .size(250.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }

                // Asegúrate de que el fragmento del mapa esté listo antes de llamar a onMapReady
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    factory = { context ->
                        val container = FragmentContainerView(context).apply {
                            id = View.generateViewId()
                        }
                        val fragment = MapFragment()
                        (context as? AppCompatActivity)?.supportFragmentManager?.commit {
                            replace(container.id, fragment)
                        }
                        container
                    },
                    update = { container ->
                        val fragment =
                            (container.context as? AppCompatActivity)?.supportFragmentManager?.findFragmentById(
                                container.id
                            ) as? MapFragment

                        fragment?.getSupportMapFragment()
                            ?.getMapAsync(this@AddAdoptionActivity)  // Aquí usamos 'this' para el callback
                    }
                )

                Button(
                    onClick = {
                        val adoptionData = saveAdoptionData(name, age, description)
                        //convertirmos a json
                        val adoptionDataJson = Gson().toJson(adoptionData)
                        val existDataSharedPref= SharedPrefHelper(context).getString("adoption_data")
                        if (existDataSharedPref.isNotEmpty()) {
                            // Primero parseamos el JSON existente
                            val existingData = Gson().fromJson(existDataSharedPref, Array<AddDog>::class.java).toMutableList()
                            existingData.add(adoptionData)  // Agregamos el nuevo objeto

                            // Convertimos la lista completa a JSON
                            val adoptionDataMixJson = Gson().toJson(existingData)
                            SharedPrefHelper(context).save("adoption_data", adoptionDataMixJson)
                        } else {
                            // Si no existe ningún dato, guardamos el objeto como una lista
                            val adoptionDataList = mutableListOf(adoptionData)
                            val adoptionDataListJson = Gson().toJson(adoptionDataList)
                            SharedPrefHelper(context).save("adoption_data", adoptionDataListJson)
                        }

                        if (adoptionData.image.isNotEmpty()) {
                            val intent = Intent(context, AdoptListHomeActivity::class.java)
                            context.startActivity(intent)
                            finish()
                        }

                        Toast.makeText(context, "Adopción registrada", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "Registrar Adopción")
                }
            }
        }
    }

    fun saveAdoptionData(name: String, age: String, description: String): AddDog {
        return AddDog(
            name = name,
            age = age,
            description = description,
            image = imageUri.toString(),
            location = currentLocation ?: LatLng(0.0, 0.0)
        )
    }

    fun createImageUri(context: Context): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: Uri.EMPTY
    }
}
