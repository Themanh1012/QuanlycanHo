package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        val createTableUsers = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                password TEXT,
                fullName TEXT,
                role INTEGER
            )
        """.trimIndent()
        db.execSQL(createTableUsers)


        val createTableApartments = """
            CREATE TABLE apartments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                price REAL,
                address TEXT,
                id_user INTEGER
            )
        """.trimIndent()
        db.execSQL(createTableApartments)


        db.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('admin', '123', 'Quản trị viên', 1)")
        db.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('khach', '123', 'Khách hàng', 2)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS apartments")
        onCreate(db)
    }

    fun checkLogin(usernameInput: String, passwordInput: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", arrayOf(usernameInput, passwordInput))

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getInt(0),
                username = cursor.getString(1),
                password = cursor.getString(2),
                fullName = cursor.getString(3),
                role = cursor.getInt(4)
            )
        }
        cursor.close()
        return user
    }
}