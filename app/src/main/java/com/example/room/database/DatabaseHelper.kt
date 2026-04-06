package com.example.room.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.room.model.Apartment
import com.example.room.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.Charset

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 11) {

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
                description TEXT, area REAL, imagePath TEXT, status TEXT DEFAULT 'Còn trống', 
                id_user INTEGER, id_renter INTEGER DEFAULT NULL, badge TEXT DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE view_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                apartment_id INTEGER,
                user_id INTEGER,
                view_time INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE saved_apartments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                apartment_id INTEGER,
                user_id INTEGER,
                saved_time INTEGER
            )
        """.trimIndent())

        insertDefaultUsers(db)
        importApartmentsFromJson(db)
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

    fun ensureDefaultUsers() {
        val db = writableDatabase
        insertDefaultUsers(db)
    }

    private fun importApartmentsFromJson(db: SQLiteDatabase) {
        try {
            val inputStream: InputStream = context.assets.open("apartments.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charset.forName("UTF-8"))
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val cv = ContentValues().apply {
                    put("title", obj.getString("title"))
                    put("price", obj.getDouble("price"))
                    put("address", obj.getString("address"))
                    put("description", obj.getString("description"))
                    put("area", obj.getDouble("area"))
                    put("imagePath", obj.getString("imagePath"))
                    put("status", obj.getString("status"))
                    put("id_user", obj.getInt("id_user"))
                    if (obj.has("id_renter") && !obj.isNull("id_renter")) {
                        put("id_renter", obj.getInt("id_renter"))
                    }
                    if (obj.has("badge")) {
                        put("badge", obj.getString("badge"))
                    }
                }
                db.insert("apartments", null, cv)
            }
        } catch (e: Exception) {
            db.execSQL("INSERT INTO apartments (title, price, address, description, area, imagePath, status, id_user, badge) VALUES ('Căn hộ Sunrise City', 8000000, 'Quận 7, TP.HCM', 'Căn hộ cao cấp...', 65, '', 'Còn trống', 1, 'VIP KIM CƯƠNG')")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS apartments")
            db.execSQL("DROP TABLE IF EXISTS users")
            db.execSQL("DROP TABLE IF EXISTS view_history")
            db.execSQL("DROP TABLE IF EXISTS saved_apartments")
            onCreate(db)
        }
    }

    fun exportApartmentsToJson(): String {
        val list = getAllApartments()
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("title", item.title)
            obj.put("price", item.price)
            obj.put("address", item.address)
            obj.put("description", item.description)
            obj.put("area", item.area)
            obj.put("imagePath", item.imagePath)
            obj.put("status", item.status)
            obj.put("id_user", item.id_user)
            obj.put("id_renter", item.id_renter)
            obj.put("badge", item.badge)
            jsonArray.put(obj)
        }
        return jsonArray.toString(4)
    }

    fun checkLogin(username: String, password: String): User? {
        readableDatabase.rawQuery("SELECT * FROM users WHERE username=? AND password=?", arrayOf(username, password)).use {
            if (it.moveToFirst()) return User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))
        }
        return null
    }

    fun getUserById(id: Int): User? {
        readableDatabase.rawQuery("SELECT * FROM users WHERE id=?", arrayOf(id.toString())).use {
            if (it.moveToFirst()) return User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4))
        }
        return null
    }

    fun getAllUsers(): ArrayList<User> = ArrayList<User>().apply {
        readableDatabase.rawQuery("SELECT * FROM users ORDER BY id DESC", null).use {
            if (it.moveToFirst()) do {
                add(User(it.getInt(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4)))
            } while (it.moveToNext())
        }
    }

    fun checkPassword(userId: Int, password: String): Boolean {
        readableDatabase.rawQuery("SELECT * FROM users WHERE id=? AND password=?", arrayOf(userId.toString(), password)).use {
            return it.moveToFirst()
        }
    }

    fun changePassword(userId: Int, newPassword: String): Int {
        val cv = ContentValues().apply {
            put("password", newPassword)
        }
        return writableDatabase.update("users", cv, "id=?", arrayOf(userId.toString()))
    }

    fun deleteUser(id: Int): Int {
        writableDatabase.delete("apartments", "id_user=?", arrayOf(id.toString()))
        return writableDatabase.delete("users", "id=?", arrayOf(id.toString()))
    }

    fun insertUser(user: User): Long = writableDatabase.insert("users", null, ContentValues().apply {
        put("username", user.username)
        put("password", user.password)
        put("fullName", user.fullName)
        put("role", user.role)
    })

    fun updateUserInfo(userId: Int, fullName: String, role: Int): Int {
        val cv = ContentValues().apply {
            put("fullName", fullName)
            put("role", role)
        }
        return writableDatabase.update("users", cv, "id=?", arrayOf(userId.toString()))
    }

    fun getAllApartments(): ArrayList<Apartment> = ArrayList<Apartment>().apply {
        readableDatabase.rawQuery("SELECT * FROM apartments ORDER BY id DESC", null).use {
            if (it.moveToFirst()) do {
                add(Apartment(
                    it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                    it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống", it.getInt(8),
                    if (it.isNull(9)) null else it.getInt(9),
                    it.getString(10) ?: ""
                ))
            } while (it.moveToNext())
        }
    }

    fun getApartmentById(id: Int): Apartment? {
        readableDatabase.rawQuery("SELECT * FROM apartments WHERE id=?", arrayOf(id.toString())).use {
            if (it.moveToFirst()) return Apartment(
                it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                it.getString(7) ?: "Còn trống", it.getInt(8),
                if (it.isNull(9)) null else it.getInt(9),
                it.getString(10) ?: ""
            )
        }
        return null
    }

    fun insertApartment(apartment: Apartment): Long = writableDatabase.insert("apartments", null, ContentValues().apply {
        put("title", apartment.title)
        put("price", apartment.price)
        put("address", apartment.address)
        put("description", apartment.description)
        put("area", apartment.area)
        put("imagePath", apartment.imagePath)
        put("status", apartment.status)
        put("id_user", apartment.id_user)
        put("id_renter", apartment.id_renter)
        put("badge", apartment.badge)
    })

    fun updateApartment(apartment: Apartment): Int = writableDatabase.update("apartments", ContentValues().apply {
        put("title", apartment.title)
        put("price", apartment.price)
        put("address", apartment.address)
        put("description", apartment.description)
        put("area", apartment.area)
        put("imagePath", apartment.imagePath)
        put("status", apartment.status)
        put("id_user", apartment.id_user)
        put("id_renter", apartment.id_renter)
        put("badge", apartment.badge)
    }, "id=?", arrayOf(apartment.id.toString()))

    fun deleteApartment(id: Int): Int = writableDatabase.delete("apartments", "id=?", arrayOf(id.toString()))

    fun rentApartment(apartmentId: Int, renterId: Int): Int {
        val cv = ContentValues().apply {
            put("status", "Đã thuê")
            put("id_renter", renterId)
        }
        return writableDatabase.update("apartments", cv, "id=?", arrayOf(apartmentId.toString()))
    }

    fun getAvailableApartmentsCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM apartments WHERE status = 'Còn trống'", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    fun getOccupiedApartmentsCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM apartments WHERE status = 'Đã thuê'", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun insertViewHistory(apartmentId: Int, userId: Int) {
        writableDatabase.insert("view_history", null, ContentValues().apply {
            put("apartment_id", apartmentId)
            put("user_id", userId)
            put("view_time", System.currentTimeMillis())
        })
    }

    fun getViewHistory(userId: Int): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        readableDatabase.rawQuery(
            "SELECT a.* FROM apartments a INNER JOIN view_history h ON a.id = h.apartment_id WHERE h.user_id = ? ORDER BY h.view_time DESC",
            arrayOf(userId.toString())
        ).use {
            if (it.moveToFirst()) do {
                list.add(Apartment(
                    it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                    it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống", it.getInt(8),
                    if (it.isNull(9)) null else it.getInt(9),
                    it.getString(10) ?: ""
                ))
            } while (it.moveToNext())
        }
        return list
    }

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
        readableDatabase.rawQuery("SELECT * FROM saved_apartments WHERE apartment_id = ? AND user_id = ?", arrayOf(apartmentId.toString(), userId.toString())).use { return it.moveToFirst() }
    }

    fun getSavedApartments(userId: Int): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        readableDatabase.rawQuery(
            "SELECT a.* FROM apartments a INNER JOIN saved_apartments s ON a.id = s.apartment_id WHERE s.user_id = ? ORDER BY s.saved_time DESC",
            arrayOf(userId.toString())
        ).use {
            if (it.moveToFirst()) do {
                list.add(Apartment(
                    it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                    it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                    it.getString(7) ?: "Còn trống", it.getInt(8),
                    if (it.isNull(9)) null else it.getInt(9),
                    it.getString(10) ?: ""
                ))
            } while (it.moveToNext())
        }
        return list
    }
}
