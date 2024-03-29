package com.example.cafetariapanel

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.BuildConfig
import com.google.firebase.ktx.Firebase
import datamodels.MenuItem
import interfaces.MenuApi
import interfaces.RequestType
import services.DatabaseHandler
import services.FirebaseDBService


class SettingsActivity : AppCompatActivity(), MenuApi {

    //private lateinit var loadItemImageLL: LinearLayout
    //private lateinit var loadItemImageTV: TextView

    private lateinit var menuModeLL: LinearLayout
    private lateinit var menuModeTV: TextView

    private lateinit var updateMenuLL: LinearLayout
    private lateinit var deleteMenuLL: LinearLayout

    private lateinit var deleteSavedCardsLL: LinearLayout
    private lateinit var deleteOrdersHistoryLL: LinearLayout

    private lateinit var deleteAcc : LinearLayout

    private lateinit var githubImageView: ImageView
    private lateinit var linkedinImageView: ImageView


    private lateinit var sharedPref: SharedPreferences

    private lateinit var progressDialog: ProgressDialog

    private val db = DatabaseHandler(this)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPref = getSharedPreferences("user_profile_details", MODE_PRIVATE)
        //loadItemImageLL = findViewById(R.id.settings_load_item_images_ll)
        //loadItemImageTV = findViewById(R.id.settings_load_item_images_tv)
        linkedinImageView = findViewById(R.id.linkedin_logo)
        githubImageView = findViewById(R.id.github_logo)
        deleteAcc = findViewById(R.id.settings_delete_account)
        //loadItemImageLL.setOnClickListener { updateLoadItemImage() }
        linkedinImageView.setOnClickListener { directToLinkedin() }
        githubImageView.setOnClickListener { directToGithub() }
        deleteAcc.setOnClickListener { deleteAccount() }

        menuModeLL = findViewById(R.id.settings_menu_mode_ll)
        menuModeTV = findViewById(R.id.settings_menu_mode_tv)
        menuModeLL.setOnClickListener { updateMenuMode() }

        updateMenuLL = findViewById(R.id.settings_update_menu_ll)
        updateMenuLL.setOnClickListener { getOrgID() }

        deleteMenuLL = findViewById(R.id.settings_delete_menu_ll)
        deleteMenuLL.setOnClickListener { deleteOfflineMenu() }

        deleteSavedCardsLL = findViewById(R.id.settings_saved_cards_ll)
        deleteSavedCardsLL.setOnClickListener { deleteAllTheSavedCards() }

        deleteOrdersHistoryLL = findViewById(R.id.settings_delete_order_history_ll)
        deleteOrdersHistoryLL.setOnClickListener { deleteAllTheOrdersHistoryDetails() }

        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

        loadUserSettings()
        findViewById<ImageView>(R.id.settings_go_back_iv).setOnClickListener { onBackPressed() }
    }

    private fun deleteAccount(){
        val user = FirebaseAuth.getInstance().currentUser!!

        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete your account??")
            .setPositiveButton("Yes, Delete My Account", DialogInterface.OnClickListener {dialogInterface, _ ->
                user?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            logOutUser()
                        } else {
                            // Hesap silme hatası oluştu
                        }
                    }
                dialogInterface.dismiss()
            })
            .create().show()
    }


    private fun logOutUser() {
        Firebase.auth.signOut()

        getSharedPreferences("settings", MODE_PRIVATE).edit().clear()
            .apply() //deleting settings from offline
        getSharedPreferences("user_profile_details", MODE_PRIVATE).edit().clear()
            .apply() //deleting user details from offline

        //removing tables
        db.dropCurrentOrdersTable()
        db.dropOrderHistoryTable()
        db.clearSavedCards()

        startActivity(Intent(this, LoginUserActivity::class.java))
        finish()
    }

    private fun directToGithub(){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/burcualtug"))
        startActivity(i)
    }

    private fun directToLinkedin(){
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/burcu-altu%C4%9F-5816a11a2/"))
        startActivity(i)
    }
    private fun getOrgID(){
        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()
                    Log.d("GLBID",globalOrgID)

                    updateMenuForOffline(globalOrgID)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    private fun deleteAllTheOrdersHistoryDetails() {
        val db = DatabaseHandler(this)
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete all the previous order details?")
            .setPositiveButton("Yes, Delete All", DialogInterface.OnClickListener { dialogInterface, _ ->
                db.dropOrderHistoryTable()
                db.close()
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun deleteAllTheSavedCards() {
        val db = DatabaseHandler(this)
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete all the saved cards?")
            .setPositiveButton("Yes, Delete All", DialogInterface.OnClickListener { dialogInterface, _ ->
                db.clearSavedCards()
                db.close()
                dialogInterface.dismiss()
            })
            .create().show()
    }

    /*private fun updateLoadItemImage() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Load Item Images")

        val options = arrayOf("On", "Off")
        var checkedItem = sharedPref.getInt("loadItemImages", 0)
        dialog.setSingleChoiceItems(options, checkedItem, DialogInterface.OnClickListener { _, i ->
            checkedItem = i
        })
        dialog.setPositiveButton("Save", DialogInterface.OnClickListener { dialogInterface, _ ->
            when(checkedItem) {
                0 -> loadItemImageTV.text = "On"
                1 -> loadItemImageTV.text = "Off"
            }
            val editor = sharedPref.edit()
            editor.putInt("loadItemImages", checkedItem)
            editor.apply()
            dialogInterface.dismiss()
        })
        dialog.setCancelable(false)
        dialog.create()
        dialog.show()
    } */

    private fun updateMenuMode() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Menu Mode")

        val options = arrayOf("Online", "Offline")
        var checkedItem = sharedPref.getInt("menuMode", 0)
        dialog.setSingleChoiceItems(options, checkedItem, DialogInterface.OnClickListener { _, i ->
            checkedItem = i
        })
        dialog.setPositiveButton("Save", DialogInterface.OnClickListener { dialogInterface, _ ->
            when(checkedItem) {
                0 -> menuModeTV.text = "Online"
                1 -> menuModeTV.text = "Offline"
            }
            val editor = sharedPref.edit()
            editor.putInt("menuMode", checkedItem)
            editor.apply()
            dialogInterface.dismiss()
        })
        dialog.setCancelable(false)
        dialog.create()
        dialog.show()
    }

    private fun updateMenuForOffline(orgID:String) {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Updating...")
        progressDialog.setMessage("Offline Menu is preparing for you...")
        progressDialog.show()
        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef2: DatabaseReference = FirebaseDatabase.getInstance().reference

        val shp = sharedPref.getString("emp_org", "11")
        val shp1 = "02278903"
        FirebaseDBService().readAllMenu(this, RequestType.OFFLINE_UPDATE,orgID)
    }

    override fun onFetchSuccessListener(list: ArrayList<MenuItem>, requestType: RequestType) {
        val db = DatabaseHandler(this)
        db.clearTheOfflineMenuTable()

        if (requestType == RequestType.OFFLINE_UPDATE) {
            for (item in list) {
                db.insertOfflineMenuData(item)
            }
            Toast.makeText(applicationContext, "Offline Menu Updated", Toast.LENGTH_LONG).show()
        }

        progressDialog.dismiss()
    }

    private fun deleteOfflineMenu() {
        val db = DatabaseHandler(this)
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete the offline menu?")
            .setPositiveButton("Yes, Delete it", DialogInterface.OnClickListener { dialogInterface, _ ->
                db.clearTheOfflineMenuTable()
                db.close()
                Toast.makeText(this, "Offline Menu has been removed", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun loadUserSettings() {
        /*when(sharedPref.getInt("loadItemImages", 0)) {
            0 -> loadItemImageTV.text = "On"
            1 -> loadItemImageTV.text = "Off"
        }*/
        when(sharedPref.getInt("menuMode", 0)) {
            0 -> menuModeTV.text = "Online"
            1 -> menuModeTV.text = "Offline"
        }

        findViewById<TextView>(R.id.app_version_tv).text = BuildConfig.VERSION_NAME
    }

}