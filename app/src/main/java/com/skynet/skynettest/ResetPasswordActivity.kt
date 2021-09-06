package com.skynet.skynettest

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skynet.skynettest.utils.ConnectionManager

lateinit var emailForResetPassword: EditText
lateinit var btnSendEmail: Button
lateinit var btnGotoLogin: Button
private lateinit var layout: RelativeLayout


class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        title="Reset Password"

        layout =findViewById(R.id.resetPasswordLayout)
        emailForResetPassword = findViewById(R.id.emailForResetPassword)
        btnSendEmail = findViewById(R.id.btnSendEmail)
        btnGotoLogin =findViewById(R.id.btnGotoLogin)
        progressBar =findViewById(R.id.progressBar)
        progressBar.visibility= View.GONE

//        val email = emailForResetPassword.text.toString().trim()

        layout.setOnClickListener{
            hideKeyBoard()
        }

        btnSendEmail.setOnClickListener {
            progressBar.visibility= View.VISIBLE
            if (emailForResetPassword.text.toString().isEmpty()) {
                emailForResetPassword.error = "Required"
                progressBar.visibility= View.GONE
                return@setOnClickListener
            }
            if (ConnectionManager().checkConnectivity(this)) {

                btnSendEmail.isPressed = true
                auth.sendPasswordResetEmail(emailForResetPassword.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        btnSendEmail.text = "Resend Email"
                        Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
                        btnGotoLogin.visibility = View.VISIBLE
                        btnSendEmail.isPressed = false
                        progressBar.visibility = View.GONE
                    } else {
                        btnSendEmail.isPressed = false
                        val msg = task.exception.toString()
                        if (msg.contains("badly formatted",true)){
                            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                        }
                        else if (msg.contains("There is no user",true)){
                            Toast.makeText(this, "Email id is not registered", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                        }
                    }
                    btnSendEmail.isPressed = false
                    progressBar.visibility = View.GONE
                }
            } else {
                //connect to internet
                progressBar.visibility= View.GONE
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Connection error")
                dialog.setMessage("No internet found")
                dialog.setPositiveButton("Open Internet Settings") { dialogInterface, i ->
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                }
                dialog.setNegativeButton("Exit") { x, y ->
                    finishAffinity()
                }
                dialog.setNeutralButton("Cancel"){ dialogInterface, i ->
                    dialogInterface.cancel()
                }
                dialog.create()
                dialog.show()
            }
        }
        btnGotoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun hideKeyBoard(){
        val view=this.currentFocus
        if (view!=null){
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}