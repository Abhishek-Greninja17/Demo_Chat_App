package com.skynet.skynettest

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skynet.skynettest.admin.AdminMainActivity
import com.skynet.skynettest.employee.EmployeeMainActivity
import com.skynet.skynettest.superAdmin.SuperAdminMainActivity
import com.skynet.skynettest.utils.ConnectionManager

lateinit var inputUsername: EditText
lateinit var inputPassword: TextInputEditText
lateinit var btnLogin: Button
lateinit var progressBar: ProgressBar
//lateinit var checkBox: CheckBox
lateinit var forgot_password: TextView
lateinit var auth: FirebaseAuth
lateinit var user: FirebaseUser
lateinit var layoutLogin: RelativeLayout
//lateinit var sharedPreferences: SharedPreferences
lateinit var type:String

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        inputUsername = findViewById(R.id.username)
        inputPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
        forgot_password =findViewById(R.id.forgot_password)
        layoutLogin =findViewById(R.id.layoutLogin)

        layoutLogin.setOnClickListener{
            hideKeyBoard()
        }

//        if (auth.currentUser != null) {
//            user = auth.currentUser!!
//            val db= FirebaseDatabase.getInstance()
//            val uid= user.uid
//
//            val dbRef=db.getReference("/Users/$uid")
//            dbRef.addValueEventListener(object: ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    type = snapshot.child("type").value.toString()
//                    when (type) {
//                        "Super Admin" -> {
//                            startActivity(Intent(this@LoginActivity, SuperAdminMainActivity::class.java).putExtra("name", snapshot.child("name").value.toString()))
//                            finish()
//                        }
//                        "admin" -> {
//                            startActivity(Intent(this@LoginActivity, AdminMainActivity::class.java))
//                            finish()
//                        }
//                        else -> {
//                            startActivity(Intent(this@LoginActivity, EmployeeMainActivity::class.java))
//                            finish()
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.i("Failed to read value.", error.toException().toString())
//                }
//
//            })
//        }

        btnLogin.setOnClickListener {
            if (!isValidInput()){
                return@setOnClickListener
            }
            if(ConnectionManager().checkConnectivity(this)){
                progressBar.visibility= View.VISIBLE
                signIn()
            }
            else{
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
        forgot_password.setOnClickListener {
            progressBar.visibility= View.VISIBLE
            val intent= Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
            progressBar.visibility= View.GONE
        }
    }

    private fun signIn() {
        val userEmail: String = inputUsername.text.toString()
        val userPassword: String = inputPassword.text.toString()


        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { loginTask ->
            if (loginTask.isSuccessful) {

                user = auth.currentUser!!
                val db=FirebaseDatabase.getInstance()
                val uid= user.uid

                val dbRef=db.getReference("/Users/$uid")
                dbRef.addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        progressBar.visibility= View.VISIBLE
                        type = snapshot.child("type").value.toString()
                        when (type) {
                            "Super Admin" -> {
                                startActivity(Intent(this@LoginActivity, SuperAdminMainActivity::class.java))
                                finish()
                            }
                            "admin" -> {
                                if (snapshot.child("install").value.toString() == "no") dbRef.child("install").setValue("yes")
                                startActivity(Intent(this@LoginActivity, AdminMainActivity::class.java))
                                finish()
                            }
                            else -> {
                                if (snapshot.child("install").value.toString() == "no") dbRef.child("install").setValue("yes")
                                startActivity(Intent(this@LoginActivity, EmployeeMainActivity::class.java))
                                finish()
                            }
                        }
                        Toast.makeText(this@LoginActivity, "Authentication successful", Toast.LENGTH_SHORT).show();
                        progressBar.visibility= View.GONE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Failed to read value.", error.toException())
                    }

                })

                progressBar.visibility= View.INVISIBLE

            } else {
                progressBar.visibility= View.INVISIBLE
                val msg=loginTask.exception.toString()
                println("error:  $msg")
                when {
                    msg.contains("badly formatted",true) -> {
                        Toast.makeText(this, "Enter a correct email address", Toast.LENGTH_SHORT).show()
                    }
                    msg.contains("no user record",true) -> {
                        Toast.makeText(this, "No user found on this email id", Toast.LENGTH_SHORT).show()
                    }
                    msg.contains("password is invalid",true) -> {
                        Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
                    }
                    msg.contains("We have blocked",true) -> {
                        Toast.makeText(this, "Access to this account has been temporarily disabled " +
                                "due to many failed login attempts." +
                                " You can immediately restore it by resetting your " +
                                "password or you can try again later", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            progressBar.visibility= View.INVISIBLE
        }
    }

    private fun hideKeyBoard(){
        val view=this.currentFocus
        if (view!=null){
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun isValidInput(): Boolean {
        val email:String= inputUsername.text.toString()
        val password:String= inputPassword.text.toString()
        var valid = true
        if (TextUtils.isEmpty(email.trim())) {
            inputUsername.error = "Required"
            valid = false
        }
        if (!TextUtils.isEmpty(email.trim())&&!email.trim().contains("@")){
            inputUsername.error = "Invalid email"
            valid = false
        }
        if (TextUtils.isEmpty(password.trim())) {
            inputPassword.error = "Required"
            valid = false
        }
        return valid
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}