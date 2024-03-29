package com.example.cafetariapanel


import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.example.cafetariapanel.MainActivity
import com.example.cafetariapanel.R

class LoginUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var sharedPref: SharedPreferences

    private lateinit var emailTIL: TextInputLayout
    private lateinit var passwordTIL: TextInputLayout

    private var doubleBackToExit = false
    override fun onBackPressed() {
        if(doubleBackToExit) {
            super.onBackPressed()
            return
        }
        doubleBackToExit = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExit = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        emailTIL = findViewById(R.id.login_email_til)
        passwordTIL = findViewById(R.id.login_password_til)

        sharedPref = getSharedPreferences("user_profile_details", MODE_PRIVATE)
        findViewById<TextView>(R.id.login_forgot_password_tv).setOnClickListener {userForgotPassword()}
    }

    //Checking if user is logged in already or not
    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if(user != null && user.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun getOrgID(){

        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()

                    Log.d("GLBID",globalOrgID)
                    checkGenderSavedOrNot(globalOrgID)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun validateEmail(): Boolean {
        val email = emailTIL.editText!!.text.toString().trim()
        if(email.isEmpty()) {
            emailTIL.error = getString(R.string.field_empty)
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTIL.error = getString(R.string.invalid_email)
            return false
        }
        emailTIL.error = null
        return true
    }

    private fun validatePassword(): Boolean {

        val pass = passwordTIL.editText!!.text.toString().trim()

        if(pass.isEmpty()) {
            passwordTIL.error = getString(R.string.field_empty)
            return false
        }

        if(pass.length < 6) {
            passwordTIL.error = "Password is too short (Min. 6 Characters)"
            return false
        }

        passwordTIL.error = null
        return true
    }

    fun loginEmployee(view: View) {
        if(!validateEmail() or !validatePassword())
            return

        findViewById<ProgressBar>(R.id.login_progress_bar).visibility = ViewGroup.VISIBLE
        auth.signInWithEmailAndPassword(emailTIL.editText!!.text.toString(), passwordTIL.editText!!.text.toString())
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    if(auth.currentUser!!.isEmailVerified) {
                        getOrgID()//checkGenderSavedOrNot()
                    } else {
                        findViewById<ProgressBar>(R.id.login_progress_bar).visibility = ViewGroup.INVISIBLE
                        AlertDialog.Builder(this)
                            .setTitle("Email Verification")
                            .setMessage("Please verify your Email address.\nA verification link has been sent to your Email address")
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            })
                            .setCancelable(false)
                            .create()
                            .show()
                    }
                }
            }

            .addOnFailureListener {
                AlertDialog.Builder(this)
                    .setTitle("Attention")
                    .setMessage("${it.message}")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    })
                    .setCancelable(false)
                    .create()
                    .show()
                findViewById<ProgressBar>(R.id.login_progress_bar).visibility = ViewGroup.INVISIBLE
            }

    }

    private fun checkGenderSavedOrNot(globalOrgID:String) {
        val user = auth.currentUser!!
        val empName = user.displayName!!
        val shp = sharedPref.getString("emp_org","11")

        databaseRef.child(globalOrgID).child("company").child(user.uid)
            .get().addOnSuccessListener {
                if(it.exists()){
                    val empGender = it.child("gender").value.toString()

                    if(empGender == "none") {
                        val intent = Intent(this@LoginUserActivity, GenderSelectionActivity::class.java)
                        intent.putExtra("name", empName)
                        intent.putExtra("uid", user.uid)
                        startActivity(intent)
                        finish()
                    } else {
                        startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
                        finish()
                        Toast.makeText(this@LoginUserActivity, "Welcome to Locaf", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        /*databaseRef.child(shp!!).child("company").child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val empGender = snapshot.child("gender").value.toString()
                    if(empGender == "none") {
                        val intent = Intent(this@LoginUserActivity, GenderSelectionActivity::class.java)
                        intent.putExtra("name", empName)
                        intent.putExtra("uid", user.uid)
                        startActivity(intent)
                        finish()
                    } else {
                        startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
                        finish()
                        Toast.makeText(this@LoginUserActivity, "Welcome to Locaf", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }) */
    }

    fun openRegisterActivity(view: View) {
        startActivity(Intent(this, RegisterUserActivity::class.java))
        finish()
    }

    private fun userForgotPassword() {
        val forgotDialog = ForgotPassword()
        forgotDialog.show(supportFragmentManager, "Forgot Password Dialog")
    }
}