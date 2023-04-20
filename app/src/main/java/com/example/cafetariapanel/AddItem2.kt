package com.example.cafetariapanel

import android.app.ProgressDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import datamodels.IdCardModel
import kotlinx.android.synthetic.main.activity_add_item2.*
import java.math.BigInteger
import java.util.*

class AddItem2 : AppCompatActivity() {
    private lateinit var itemName: TextInputLayout
    private lateinit var itemCategory: TextInputLayout
    private lateinit var itemDescription: TextInputLayout
    private lateinit var itemPrice: TextInputLayout

    private lateinit var itemIV: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var registerProgressDialog: ProgressDialog

    private lateinit var add: Button

    private lateinit var sharedPref: SharedPreferences

    var cat:String=""

    var flag:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item2)

        val spinner = findViewById<Spinner>(R.id.category_spinner)

        sharedPref = getSharedPreferences("user_profile_details", MODE_PRIVATE)
        registerProgressDialog = ProgressDialog(this)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        storageRef = FirebaseStorage.getInstance().reference.child("itemImages")
        itemIV = findViewById(R.id.item_iv)
        add = findViewById(R.id.add)
        itemName = findViewById(R.id.item_name)
        itemDescription = findViewById(R.id.item_description)
        itemPrice = findViewById(R.id.item_price)

        add.setOnClickListener {//registerMenu(cat)
         registerMenu()
        //flag=true
            //sendCategory()
            //uploadIDCardToFirebaseStorage() }
        }
        val languages = resources.getStringArray(R.array.Categories)

        val currentUser = auth.currentUser
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, languages
            )
            spinner.adapter = adapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                    //registerMenu(languages[position])

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    }
    fun uuidGenerator():String {
        return String.format("%040d", BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)).dropLast(32)
    }

    /*fun chooseImage(view: View) {
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    private fun uploadIDCardToFirebaseStorage() {

        val reference = storageRef.child("${itemID}.jpeg")

        registerProgressDialog.setTitle("Uploading ID Card...")

        reference.putFile(employeeIDCardUri)
            .addOnSuccessListener { _ ->
                registerProgressDialog.dismiss()
                Toast.makeText(this, "ID Card Uploaded!!", Toast.LENGTH_SHORT).show()
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val model = IdCardModel(uri.toString())
                    registerMenu(model)
                }
            }
            .addOnFailureListener { e -> // Error, Image not uploaded
                registerProgressDialog.dismiss()
                Toast.makeText(this, "Failed to upload ID Card" + e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0
                        * taskSnapshot.bytesTransferred
                        / taskSnapshot.totalByteCount)
                registerProgressDialog.setMessage(
                    "Uploaded " + progress.toInt() + "%"
                )
            }
    }*/
    fun turnBack(view: View) {onBackPressed()}
    fun registerMenu(){
        Log.d("REGISTER","register")

        val catSpin = category_spinner.selectedItem.toString()
        Toast.makeText(this,catSpin,Toast.LENGTH_SHORT).show()
        Log.d("CATEGORY",catSpin)
        val orgID = sharedPref.getString("emp_org", "11")
        val uuid=uuidGenerator()
        val company = databaseRef.child(orgID!!).child("menu").child(uuid)
        val name = itemName.editText!!.text.toString()
        val description = itemDescription.editText!!.text.toString()
        val price = itemPrice.editText!!.text.toString().toFloat()

            company.child("item_id").setValue(uuid)
            company.child("item_category").setValue(catSpin)
            company.child("item_name").setValue(name)
            company.child("item_desc").setValue(description)
            company.child("item_price").setValue(price)

    }
}