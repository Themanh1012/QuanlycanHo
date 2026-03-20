package com.example.room.model

data class User(
                var id: Int = 0,
                var username: String,
                var password: String,
                var fullName: String,
                var role: Int
)
