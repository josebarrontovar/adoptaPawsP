package com.adoptapaws.domain

import com.google.android.gms.maps.model.LatLng

class AddDog (
    val name:String,
    val age:String,
    val description:String,
    val image:String,
    val location: LatLng
)