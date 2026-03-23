package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.Apartment
import com.example.room.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 4) {

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
                description TEXT, area REAL, imagePath TEXT, status TEXT DEFAULT 'Còn trống', id_user INTEGER
            )
        """.trimIndent())

        // Insert default users
        insertDefaultUsers(db)
    }

    private fun insertDefaultUsers(db: SQLiteDatabase) {
        // Check and insert admin if not exists
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = 'admin'", null)
        if (!cursor.moveToFirst()) {
            db.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('admin', '123', 'Quản trị viên', 1)")
        }
        cursor.close()

        // Check and insert khach if not exists
        val cursor2 = db.rawQuery("SELECT * FROM users WHERE username = 'khach'", null)
        if (!cursor2.moveToFirst()) {
            db.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('khach', '123', 'Khách hàng', 2)")
        }
        cursor2.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE apartments ADD COLUMN status TEXT DEFAULT 'Còn trống'")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (oldVersion < 4) {
            insertDefaultUsers(db)
        }
    }

    // =============== USERS ===============

    fun insertUser(user: User): Long = writableDatabase.insert("users", null, ContentValues().apply {
        put("username", user.username)
        put("password", user.password)
        put("fullName", user.fullName)
        put("role", user.role)
    })

    fun checkLogin(username: String, password: String): User? {
        return try {
            readableDatabase.rawQuery(
                "SELECT * FROM users WHERE username=? AND password=?", arrayOf(username, password)
            ).use {
                if (it.moveToFirst()) User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))
                else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAllUsers(): ArrayList<User> = ArrayList<User>().apply {
        try {
            readableDatabase.rawQuery("SELECT * FROM users ORDER BY id DESC", null).use {
                if (it.moveToFirst()) do {
                    add(User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4)))
                } while (it.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserById(id: Int): User? {
        return try {
            readableDatabase.rawQuery(
                "SELECT * FROM users WHERE id=?", arrayOf(id.toString())
            ).use {
                if (it.moveToFirst()) User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))
                else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
//PASSWORD
    fun changePassword(userId: Int, newPassword: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("password", newPassword)

        return db.update(
            "users",
            contentValues,
            "id = ?",
            arrayOf(userId.toString())
        )
    }
    fun checkPassword(userId: Int, password: String): Boolean {
        return try {
            readableDatabase.rawQuery(
                "SELECT * FROM users WHERE id=? AND password=?",
                arrayOf(userId.toString(), password)
            ).use { it.moveToFirst() }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    fun getOccupiedApartmentsCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM apartments WHERE status = 'Đã thuê'", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    fun updateUserInfo(userId: Int, fullName: String, role: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("fullName", fullName)
        contentValues.put("role", role)

        return db.update(
            "users",
            contentValues,
            "id = ?",  // Đổi từ user_id thành id
            arrayOf(userId.toString())
        )
    }
    fun updateUser(user: User): Int = writableDatabase.update("users", ContentValues().apply {
        put("username", user.username)
        put("password", user.password)
        put("fullName", user.fullName)
        put("role", user.role)
    }, "id=?", arrayOf(user.id.toString()))

    fun deleteUser(id: Int): Int {
        writableDatabase.delete("apartments", "id_user=?", arrayOf(id.toString()))
        return writableDatabase.delete("users", "id=?", arrayOf(id.toString()))
    }

//CAN HO

    fun insertApartment(apartment: Apartment): Long = writableDatabase.insert("apartments", null, ContentValues().apply {
        put("title", apartment.title)
        put("price", apartment.price)
        put("address", apartment.address)
        put("description", apartment.description)
        put("area", apartment.area)
        put("imagePath", apartment.imagePath)
        put("status", apartment.status)
        put("id_user", apartment.id_user)
    })

    fun getAllApartments(): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        try {
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
                        it.getString(7) ?: "Còn trống",
                        if (it.columnCount > 8) it.getInt(8) else 0
                    ))
                } while (it.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getApartmentById(id: Int): Apartment? {
        return try {
            readableDatabase.rawQuery(
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
                    it.getString(7) ?: "Còn trống",
                    if (it.columnCount > 8) it.getInt(8) else 0
                )
                else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateApartment(apartment: Apartment): Int = writableDatabase.update("apartments", ContentValues().apply {
        put("title", apartment.title)
        put("price", apartment.price)
        put("address", apartment.address)
        put("description", apartment.description)
        put("area", apartment.area)
        put("imagePath", apartment.imagePath)
        put("status", apartment.status)
        put("id_user", apartment.id_user)
    }, "id=?", arrayOf(apartment.id.toString()))

    fun deleteApartment(id: Int): Int = writableDatabase.delete("apartments", "id=?", arrayOf(id.toString()))

    fun getTotalApartments(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM apartments", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun getAvailableApartmentsCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM apartments WHERE status = 'Còn trống'", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }
}