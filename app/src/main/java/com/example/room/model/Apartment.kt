package com.example.room.model

data class Apartment(
    val id: Int = 0,
    val title: String,
    val price: Double,
    val address: String,
    val description: String = "",
    val area: Double = 0.0,
    val imagePath: String = "",
    val status: String = "Còn trống",
    val id_user: Int
)