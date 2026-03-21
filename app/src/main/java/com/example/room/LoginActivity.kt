package com.example.room

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.room.admin.ManageUserActivity
import com.example.room.database.DatabaseHelper


class LoginActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button> (R.id.btnLogin)
        val tvGoToRegister =findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = dbHelper.checkLogin(username, password)

            if(user != null){
                if(user.role == 1){
                    Toast.makeText(this, "Chào Admin: ${user.fullName}" , Toast.LENGTH_SHORT).show()
                    val intent= Intent(this, ManageUserActivity::class.java)
                    startActivity(intent)
                    finish()
                }else if(user.role == 2){
                    Toast.makeText(this, "Chào khách hàng : ${user.fullName}", Toast.LENGTH_SHORT).show()
                //CUSSTOMER NHớ LÀM
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu",Toast.LENGTH_SHORT).show()
                }
            }
        }
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity:: class.java)
            startActivity(intent)
        }

    }
}