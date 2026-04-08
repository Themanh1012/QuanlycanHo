package com.example.room.model

data class Apartment(
    val id: Int = 0,
    val title: String,
    val price: Double,
    val address: String,
    val description: String = "",
    val area: Double = 0.0,
    val imagePaths: String = "",
    val status: String = "Còn trống",
    val id_user: Int,
    val id_renter: Int? = null,
    val badge: String = ""
) {
    fun getFinalPrice(): Double {
        return if (badge == "GIẢM GIÁ HOT") {
            price * 0.9
        } else {
            price
        }
    }
}
