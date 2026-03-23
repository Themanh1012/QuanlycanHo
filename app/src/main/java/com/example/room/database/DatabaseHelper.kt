package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.Apartment
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

    // USER FUNCTIONS
    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("fullName", user.fullName)
            put("role", user.role)
        }
        return db.insert("users", null, values)
    }

    fun checkLogin(usernameInput: String, passwordInput: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", arrayOf(usernameInput, passwordInput))
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4))
        }
        cursor.close()
        return user
    }

    fun getAllUsers(): ArrayList<User> {
        val list = ArrayList<User>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do { list.add(User(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4))) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateUser(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", user.username); put("password", user.password); put("fullName", user.fullName); put("role", user.role)
        }
        return db.update("users", values, "id = ?", arrayOf(user.id.toString()))
    }

    fun deleteUser(userId: Int): Int {
        val db = writableDatabase
        db.delete("apartments", "id_user = ?", arrayOf(userId.toString()))
        return db.delete("users", "id = ?", arrayOf(userId.toString()))
    }

    fun checkUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // APARTMENT FUNCTIONS
    fun insertApartment(apartment: Apartment): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", apartment.title); put("price", apartment.price); put("address", apartment.address); put("id_user", apartment.id_user)
        }
        return db.insert("apartments", null, values)
    }

    fun getAllApartments(): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM apartments", null)
        if (cursor.moveToFirst()) {
            do { list.add(Apartment(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getString(3), cursor.getInt(4))) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteApartment(apartmentId: Int): Int {
        val db = writableDatabase
        return db.delete("apartments", "id = ?", arrayOf(apartmentId.toString()))
    }
}