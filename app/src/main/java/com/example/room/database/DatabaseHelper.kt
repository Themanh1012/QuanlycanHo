package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.Apartment
import com.example.room.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 6) {

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

        // Bảng lịch sử xem
        db.execSQL("""
            CREATE TABLE view_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                apartment_id INTEGER,
                user_id INTEGER,
                view_time INTEGER
            )
        """.trimIndent())

        // Bảng căn hộ đã lưu
        db.execSQL("""
            CREATE TABLE saved_apartments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                apartment_id INTEGER,
                user_id INTEGER,
                saved_time INTEGER
            )
        """.trimIndent())

        insertDefaultUsers(db)
        insertDefaultApartments(db)
    }

    private fun insertDefaultUsers(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = 'admin'", null)
        if (!cursor.moveToFirst()) {
            db.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('admin', '123', 'Quản trị viên', 1)")
        }
        cursor.close()

        val cursor2 = db.rawQuery("SELECT * FROM users WHERE username = 'khach'", null)
        if (!cursor2.moveToFirst()) {
            db.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('khach', '123', 'Khách hàng', 2)")
        }
        cursor2.close()
    }

    private fun insertDefaultApartments(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM apartments", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            db.execSQL("INSERT INTO apartments (title, price, address, description, area, imagePath, status, id_user) VALUES ('Căn hộ Sunrise City', 8000000, 'Quận 7, TP.HCM', 'Căn hộ cao cấp, view sông, đầy đủ nội thất.', 65, '', 'Còn trống', 1)")
            db.execSQL("INSERT INTO apartments (title, price, address, description, area, imagePath, status, id_user) VALUES ('Căn hộ Vinhome Central Park', 15000000, 'Bình Thạnh, TP.HCM', 'Căn hộ luxury, gần trung tâm, tiện ích đầy đủ.', 90, '', 'Còn trống', 1)")
            db.execSQL("INSERT INTO apartments (title, price, address, description, area, imagePath, status, id_user) VALUES ('Căn hộ Landmark 81', 25000000, 'Bình Thạnh, TP.HCM', 'Căn hộ cao tầng, view toàn cảnh TP.HCM.', 120, '', 'Đã thuê', 1)")
            db.execSQL("INSERT INTO apartments (title, price, address, description, area, imagePath, status, id_user) VALUES ('Căn hộ Masteri Thảo Điền', 12000000, 'Thủ Đức, TP.HCM', 'Căn hộ hiện đại, gần metro, khu vực yên tĩnh.', 75, '', 'Còn trống', 1)")
        }
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
        if (oldVersion < 5) {
            insertDefaultApartments(db)
        }
        if (oldVersion < 6) {
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS view_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        apartment_id INTEGER,
                        user_id INTEGER,
                        view_time INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_apartments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        apartment_id INTEGER,
                        user_id INTEGER,
                        saved_time INTEGER
                    )
                """.trimIndent())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun ensureDefaultUsers() {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM users", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            writableDatabase.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('admin', '123', 'Quản trị viên', 1)")
            writableDatabase.execSQL("INSERT INTO users (username, password, fullName, role) VALUES ('khach', '123', 'Khách hàng', 2)")
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
            "id = ?",
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

    // =============== APARTMENTS ===============

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

    // =============== VIEW HISTORY ===============

    fun insertViewHistory(apartmentId: Int, userId: Int) {
        writableDatabase.insert("view_history", null, ContentValues().apply {
            put("apartment_id", apartmentId)
            put("user_id", userId)
            put("view_time", System.currentTimeMillis())
        })
    }

    fun getViewHistory(userId: Int): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        try {
            readableDatabase.rawQuery(
                "SELECT a.* FROM apartments a INNER JOIN view_history h ON a.id = h.apartment_id WHERE h.user_id = ? ORDER BY h.view_time DESC",
                arrayOf(userId.toString())
            ).use {
                if (it.moveToFirst()) do {
                    list.add(Apartment(
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
        return list
    }

    // =============== SAVED APARTMENTS ===============

    fun saveApartment(apartmentId: Int, userId: Int) {
        writableDatabase.insert("saved_apartments", null, ContentValues().apply {
            put("apartment_id", apartmentId)
            put("user_id", userId)
            put("saved_time", System.currentTimeMillis())
        })
    }

    fun unsaveApartment(apartmentId: Int, userId: Int) {
        writableDatabase.delete("saved_apartments", "apartment_id = ? AND user_id = ?", arrayOf(apartmentId.toString(), userId.toString()))
    }

    fun isApartmentSaved(apartmentId: Int, userId: Int): Boolean {
        return readableDatabase.rawQuery(
            "SELECT * FROM saved_apartments WHERE apartment_id = ? AND user_id = ?",
            arrayOf(apartmentId.toString(), userId.toString())
        ).use { it.moveToFirst() }
    }

    fun getSavedApartments(userId: Int): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        try {
            readableDatabase.rawQuery(
                "SELECT a.* FROM apartments a INNER JOIN saved_apartments s ON a.id = s.apartment_id WHERE s.user_id = ? ORDER BY s.saved_time DESC",
                arrayOf(userId.toString())
            ).use {
                if (it.moveToFirst()) do {
                    list.add(Apartment(
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
        return list
    }
}