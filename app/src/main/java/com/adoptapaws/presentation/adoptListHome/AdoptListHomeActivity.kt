package com.adoptapaws.presentation.adoptListHome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.adoptapaws.domain.AddDog
import com.adoptapaws.presentation.home.Login
import com.adoptapaws.utils.SharedPrefHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
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
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val imageHeight = screenHeight * 0.75f
        val getSharedPref = SharedPrefHelper(context).getString("adoption_data")
        var addDog: List<AddDog> = emptyList()
        if (getSharedPref.isEmpty()) {
            val intent = Intent(context, Login::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP // Esto asegurará que la actividad anterior sea la única que queda en la pila
            context.startActivity(intent)
            // Terminamos la actividad actual para evitar que el usuario regrese a ella
            (context as? Activity)?.finish()
            return
        } else {
             addDog = Gson().fromJson(getSharedPref, Array<AddDog>::class.java).toList()
        }

        // Create the pager state
        val pagerState = rememberPagerState {
            addDog.size
        }

        // Use HorizontalPager for horizontal swipeable items
        HorizontalPager(
            // Correct parameter name
            state = pagerState,  // Passing the state for paging
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val dog = addDog[pageIndex]
            val lat = dog.location.latitude
            val lng = dog.location.longitude

            // Box that contains the card for each dog
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()) // Make content scrollable
                ) {
                    // First item: Image
                    Image(
                        painter = rememberAsyncImagePainter(dog.image),
                        contentDescription = "Dog Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            .clip(RectangleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Second item: Card with adoption details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color.White.copy(alpha = 0.7f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Adoption Details",
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = Color.Black
                            )
                            Text(
                                text = "Name: ${dog.name}",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Age: ${dog.age}",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Description: ${dog.description}",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Location: ${dog.location.latitude}, ${dog.location.longitude}",
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }

                    // Third item: Map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        MapViewWrapper(lat = lat, lng = lng)
                    }
                }
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