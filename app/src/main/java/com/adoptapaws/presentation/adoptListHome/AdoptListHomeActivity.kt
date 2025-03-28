package com.adoptapaws.presentation.adoptListHome

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.adoptapaws.domain.AddDog
import com.adoptapaws.utils.SharedPrefHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

class AdoptListHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowList()
        }

    }

    @Composable
    fun ShowList() {
        val context = LocalContext.current
        val getSharedPref = SharedPrefHelper(context).getString("adoption_data")
        val addDog = Gson().fromJson(getSharedPref, AddDog::class.java)
        val lat = addDog.location.latitude
        val lng = addDog.location.longitude
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val imageHeight = screenHeight * 0.75f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = rememberAsyncImagePainter(addDog.image),
                contentDescription = "Imagen del Perro",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight) // Altura calculada: 3/4 de la pantalla
                    .clip(RectangleShape),
                contentScale = ContentScale.Crop // O ContentScale.Fit, según tu preferencia
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y= -(40.dp))
                    .align(Alignment.CenterHorizontally),
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color.White.copy(alpha = 0.7f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detalles de la Adopción",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color.Black
                    )
                    Text(
                        text = "Nombre: ${addDog.name}",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Edad: ${addDog.age}",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Descripción: ${addDog.description}",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Ubicación: ${addDog.location}",
                        style = MaterialTheme.typography.body1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                MapViewWrapper(lat = lat, lng = lng)
            }
        }
    }



    @Composable
    fun MapViewWrapper(lat: Double, lng: Double) {
        val context = LocalContext.current
        val mapView = remember { MapView(context) }

        DisposableEffect(Unit) {
            mapView.onCreate(Bundle())
            mapView.getMapAsync { googleMap ->
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
                googleMap.addMarker(MarkerOptions().position(LatLng(lat, lng)).title("Ubicación"))
            }
            onDispose {
                mapView.onDestroy()
            }
        }

        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Ajusta la altura según lo necesites
        )
    }


}