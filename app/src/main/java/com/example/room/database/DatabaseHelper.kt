package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.Apartment
import com.example.room.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT, password TEXT, fullName TEXT, role INTEGER
            )
        """.trimIndent())

        // THÊM CỘT status
        db.execSQL("""
            CREATE TABLE apartments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT, price REAL, address TEXT,
                description TEXT, area REAL, imagePath TEXT, status TEXT DEFAULT 'Còn trống', id_user INTEGER
            )
        """.trimIndent())

        db.execSQL("INSERT INTO users VALUES (null, 'admin', '123', 'Quản trị viên', 1)")
        db.execSQL("INSERT INTO users VALUES (null, 'khach', '123', 'Khách hàng', 2)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS apartments")
        onCreate(db)
    }

    // =============== USERS ===============

    fun insertUser(user: User): Long = writableDatabase.insert("users", null, ContentValues().apply {
        put("username", user.username)
        put("password", user.password)
        put("fullName", user.fullName)
        put("role", user.role)
    })

    fun checkLogin(username: String, password: String): User? = readableDatabase.rawQuery(
        "SELECT * FROM users WHERE username=? AND password=?", arrayOf(username, password)
    ).use {
        if (it.moveToFirst()) User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))
        else null
    }

    fun getAllUsers(): ArrayList<User> = ArrayList<User>().apply {
        readableDatabase.rawQuery("SELECT * FROM users ORDER BY id DESC", null).use {
            if (it.moveToFirst()) do {
                add(User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4)))
            } while (it.moveToNext())
        }
    }

    fun getUserById(id: Int): User? = readableDatabase.rawQuery(
        "SELECT * FROM users WHERE id=?", arrayOf(id.toString())
    ).use {
        if (it.moveToFirst()) User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))
        else null
    }

    fun updateUser(user: User): Int = writableDatabase.update("users", ContentValues().apply {
        put("username", user.username)
        put("password", user.password)
        put("fullName", user.fullName)
        put("role", user.role)
    }, "id=?", arrayOf(user.id.toString()))

    fun updateUserInfo(id: Int, fullName: String, role: Int): Int = writableDatabase.update(
        "users", ContentValues().apply {
            put("fullName", fullName)
            put("role", role)
        }, "id=?", arrayOf(id.toString())
    )

    fun changePassword(id: Int, newPassword: String): Int = writableDatabase.update(
        "users", ContentValues().apply { put("password", newPassword) },
        "id=?", arrayOf(id.toString())
    )

    fun checkPassword(id: Int, oldPassword: String): Boolean = readableDatabase.rawQuery(
        "SELECT * FROM users WHERE id=? AND password=?", arrayOf(id.toString(), oldPassword)
    ).use { it.moveToFirst() }

    fun deleteUser(id: Int): Int {
        writableDatabase.delete("apartments", "id_user=?", arrayOf(id.toString()))
        return writableDatabase.delete("users", "id=?", arrayOf(id.toString()))
    }

    fun getTotalUsers(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM users", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun getAdminCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM users WHERE role=1", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun getCustomerCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM users WHERE role=2", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    // =============== APARTMENTS ===============

    fun insertApartment(apartment: Apartment): Long = writableDatabase.insert("apartments", null, ContentValues().apply {
        put("title", apartment.title)
        put("price", apartment.price)
        put("address", apartment.address)
        put("description", apartment.description)
        put("area", apartment.area)
        put("imagePath", apartment.imagePath)
        put("status", apartment.status)  // THÊM DÒNG NÀY
        put("id_user", apartment.id_user)
    })

    fun getAllApartments(): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        readableDatabase.rawQuery("SELECT * FROM apartments ORDER BY id DESC", null).use {
            if (it.moveToFirst()) do {
                add(Apartment(
                    it.getInt(0),
                    it.getString(1),
                    it.getDouble(2),
                    it.getString(3),
                    it.getString(4) ?: "",
                    it.getDouble(5),
                    it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống",  // THÊM status
                    it.getInt(8)
                ))
            } while (it.moveToNext())
        }
    }

    fun getApartmentById(id: Int): Apartment? = readableDatabase.rawQuery(
        "SELECT * FROM apartments WHERE id=?", arrayOf(id.toString())
    ).use {
        if (it.moveToFirst()) Apartment(
            it.getInt(0),
            it.getString(1),
            it.getDouble(2),
            it.getString(3),
            it.getString(4) ?: "",
            it.getDouble(5),
            it.getString(6) ?: "",
            it.getString(7) ?: "Còn trống",  // THÊM status
            it.getInt(8)
        )
        else null
    }

    fun updateApartment(apartment: Apartment): Int = writableDatabase.update("apartments", ContentValues().apply {
        put("title", apartment.title)
        put("price", apartment.price)
        put("address", apartment.address)
        put("description", apartment.description)
        put("area", apartment.area)
        put("imagePath", apartment.imagePath)
        put("status", apartment.status)  // THÊM DÒNG NÀY
        put("id_user", apartment.id_user)
    }, "id=?", arrayOf(apartment.id.toString()))

    fun deleteApartment(id: Int): Int = writableDatabase.delete("apartments", "id=?", arrayOf(id.toString()))

    fun getTotalApartments(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM apartments", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    // SỬA: Đếm theo status thay vì id_user
    fun getOccupiedApartmentsCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM apartments WHERE status = 'Đã thuê'", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun getAvailableApartmentsCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM apartments WHERE status = 'Còn trống'", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun getApartmentsByPriceRange(minPrice: Double, maxPrice: Double): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        readableDatabase.rawQuery(
            "SELECT * FROM apartments WHERE price BETWEEN ? AND ? ORDER BY price ASC",
            arrayOf(minPrice.toString(), maxPrice.toString())
        ).use {
            if (it.moveToFirst()) do {
                add(Apartment(
                    it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                    it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống", it.getInt(8)
                ))
            } while (it.moveToNext())
        }
    }

    fun getApartmentsByAreaRange(minArea: Double, maxArea: Double): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        readableDatabase.rawQuery(
            "SELECT * FROM apartments WHERE area BETWEEN ? AND ? ORDER BY area ASC",
            arrayOf(minArea.toString(), maxArea.toString())
        ).use {
            if (it.moveToFirst()) do {
                add(Apartment(
                    it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                    it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống", it.getInt(8)
                ))
            } while (it.moveToNext())
        }
    }

    fun searchApartments(query: String): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        readableDatabase.rawQuery(
            "SELECT * FROM apartments WHERE title LIKE ? OR address LIKE ? ORDER BY id DESC",
            arrayOf("%$query%", "%$query%")
        ).use {
            if (it.moveToFirst()) do {
                add(Apartment(
                    it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                    it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống", it.getInt(8)
                ))
            } while (it.moveToNext())
        }
    }

    fun getAveragePrice(): Double = readableDatabase.rawQuery("SELECT AVG(price) FROM apartments", null).use {
        if (it.moveToFirst()) it.getDouble(0) else 0.0
    }

    fun getMinPrice(): Double = readableDatabase.rawQuery("SELECT MIN(price) FROM apartments WHERE price > 0", null).use {
        if (it.moveToFirst()) it.getDouble(0) else 0.0
    }

    fun getMaxPrice(): Double = readableDatabase.rawQuery("SELECT MAX(price) FROM apartments", null).use {
        if (it.moveToFirst()) it.getDouble(0) else 0.0
    }
}