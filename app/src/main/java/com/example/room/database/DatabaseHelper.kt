package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.Apartment
import com.example.room.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT, password TEXT, fullName TEXT, role INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE apartments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT, price REAL, address TEXT,
                description TEXT, area REAL, imagePath TEXT, id_user INTEGER
            )
        """.trimIndent())

        db.execSQL("INSERT INTO users VALUES (null, 'admin', '123', 'Quản trị viên', 1)")
        db.execSQL("INSERT INTO users VALUES (null, 'khach', '123', 'Khách hàng', 2)")
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS apartments")
        onCreate(db)
    }

    // USER FUNCTIONS
    fun insertUser(user: User): Long = writableDatabase.insert("users", null, ContentValues().apply {
        put("username", user.username); put("password", user.password)
        put("fullName", user.fullName); put("role", user.role)
    })

    fun checkLogin(u: String, p: String): User? = readableDatabase.rawQuery(
        "SELECT * FROM users WHERE username=? AND password=?", arrayOf(u, p)
    ).use { if (it.moveToFirst()) User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4)) else null }

    fun getAllUsers(): ArrayList<User> = ArrayList<User>().apply {
        readableDatabase.rawQuery("SELECT * FROM users ORDER BY id DESC", null).use {
            if (it.moveToFirst()) do { add(User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))) } while (it.moveToNext())
        }
    }

    fun updateUser(user: User): Int = writableDatabase.update("users", ContentValues().apply {
        put("username", user.username); put("password", user.password)
        put("fullName", user.fullName); put("role", user.role)
    }, "id=?", arrayOf(user.id.toString()))

    fun deleteUser(id: Int): Int = writableDatabase.let {
        it.delete("apartments", "id_user=?", arrayOf(id.toString()))
        it.delete("users", "id=?", arrayOf(id.toString()))
    }

    // APARTMENT FUNCTIONS
    fun insertApartment(a: Apartment): Long = writableDatabase.insert("apartments", null, ContentValues().apply {
        put("title", a.title); put("price", a.price); put("address", a.address)
        put("description", a.description); put("area", a.area)
        put("imagePath", a.imagePath); put("id_user", a.id_user)
    })

    fun getAllApartments(): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        readableDatabase.rawQuery("SELECT * FROM apartments ORDER BY id DESC", null).use {
            if (it.moveToFirst()) do { add(Apartment(it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3), it.getString(4)?:"", it.getDouble(5), it.getString(6)?:"", it.getInt(7))) } while (it.moveToNext())
        }
    }

    fun getApartmentById(id: Int): Apartment? = readableDatabase.rawQuery("SELECT * FROM apartments WHERE id=?", arrayOf(id.toString())).use {
        if (it.moveToFirst()) Apartment(it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3), it.getString(4)?:"", it.getDouble(5), it.getString(6)?:"", it.getInt(7)) else null
    }

    fun updateApartment(a: Apartment): Int = writableDatabase.update("apartments", ContentValues().apply {
        put("title", a.title); put("price", a.price); put("address", a.address)
        put("description", a.description); put("area", a.area)
        put("imagePath", a.imagePath); put("id_user", a.id_user)
    }, "id=?", arrayOf(a.id.toString()))

    fun deleteApartment(id: Int): Int = writableDatabase.delete("apartments", "id=?", arrayOf(id.toString()))
}