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

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "QuanLyCanHo.db", null, 15) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE, password TEXT, fullName TEXT, role INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS apartments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT, price REAL, address TEXT,
                description TEXT, area REAL, imagePaths TEXT, status TEXT DEFAULT 'Còn trống', 
                id_user INTEGER, id_renter INTEGER DEFAULT NULL, badge TEXT DEFAULT ''
            )
        """.trimIndent())

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
        val countCursor = db.rawQuery("SELECT COUNT(*) FROM apartments", null)
        countCursor.moveToFirst()
        val count = countCursor.getInt(0)
        countCursor.close()
        
        if (count > 0) return

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
                    put("imagePaths", if (obj.has("imagePaths")) obj.getString("imagePaths") else if (obj.has("imagePath")) obj.getString("imagePath") else "")
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
            db.execSQL("INSERT INTO apartments (title, price, address, description, area, imagePaths, status, id_user, badge) VALUES ('Căn hộ Sunrise City', 8000000, 'Quận 7, TP.HCM', 'Căn hộ cao cấp...', 65, 'canho01', 'Còn trống', 1, 'VIP KIM CƯƠNG')")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 15) {
            // Xóa và tạo lại để nạp dữ liệu mới nhất nếu cần, hoặc chỉ cần thêm cột
            // Ở đây tôi chọn xóa bảng apartments và nạp lại để đảm bảo dữ liệu hiển thị đúng cấu trúc mới
            db.execSQL("DROP TABLE IF EXISTS apartments")
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
            obj.put("imagePaths", item.imagePaths)
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

    fun isUsernameExists(username: String): Boolean {
        readableDatabase.rawQuery("SELECT id FROM users WHERE username = ?", arrayOf(username)).use {
            return it.count > 0
        }
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

    fun insertUser(user: User): Long = writableDatabase.insert("users", null, ContentValues().apply {
        put("username", user.username)
        put("password", user.password)
        put("fullName", user.fullName)
        put("role", user.role)
    })

    fun updateUserInfo(id: Int, fullName: String, role: Int): Int = writableDatabase.update("users", ContentValues().apply {
        put("fullName", fullName)
        put("role", role)
    }, "id=?", arrayOf(id.toString()))

    fun deleteUser(id: Int): Int = writableDatabase.delete("users", "id=?", arrayOf(id.toString()))

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
        put("imagePaths", apartment.imagePaths)
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
        put("imagePaths", apartment.imagePaths)
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

    fun getAvailableApartmentsCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM apartments WHERE status LIKE '%Còn trống%'", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    fun getOccupiedApartmentsCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM apartments WHERE status LIKE '%Đã thuê%'", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun isApartmentSaved(apartmentId: Int, userId: Int): Boolean {
        readableDatabase.rawQuery("SELECT * FROM saved_apartments WHERE apartment_id = ? AND user_id = ?", arrayOf(apartmentId.toString(), userId.toString())).use { return it.moveToFirst() }
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

    fun getSavedApartments(userId: Int): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        val query = "SELECT apartments.* FROM apartments INNER JOIN saved_apartments ON apartments.id = saved_apartments.apartment_id WHERE saved_apartments.user_id = ? ORDER BY saved_apartments.saved_time DESC"
        readableDatabase.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.moveToFirst()) {
                do {
                    list.add(Apartment(
                        it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                        it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                        it.getString(7) ?: "Còn trống", it.getInt(8),
                        if (it.isNull(9)) null else it.getInt(9),
                        it.getString(10) ?: ""
                    ))
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun getViewHistory(userId: Int): ArrayList<Apartment> {
        val list = ArrayList<Apartment>()
        val query = "SELECT apartments.* FROM apartments INNER JOIN view_history ON apartments.id = view_history.apartment_id WHERE view_history.user_id = ? ORDER BY view_history.view_time DESC"
        readableDatabase.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.moveToFirst()) {
                do {
                    list.add(Apartment(
                        it.getInt(0), it.getString(1), it.getDouble(2), it.getString(3),
                        it.getString(4) ?: "", it.getDouble(5), it.getString(6) ?: "",
                        it.getString(7) ?: "Còn trống", it.getInt(8),
                        if (it.isNull(9)) null else it.getInt(9),
                        it.getString(10) ?: ""
                    ))
                } while (it.moveToNext())
            }
        }
        return list
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
}
