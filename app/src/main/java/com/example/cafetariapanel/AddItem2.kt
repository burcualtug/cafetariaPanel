package com.example.cafetariapanel

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import datamodels.IdCardModel
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
    private lateinit var employeeIDCardUri: Uri
    var idUploaded = false
    var orgID:String=""
    var itemID: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item2)


        registerProgressDialog = ProgressDialog(this)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        storageRef = FirebaseStorage.getInstance().reference.child("itemImages")
        itemIV = findViewById(R.id.item_iv)
        add = findViewById(R.id.add)
        itemCategory=findViewById(R.id.item_category)
        itemName=findViewById(R.id.item_name)
        itemDescription=findViewById(R.id.item_description)
        itemPrice=findViewById(R.id.item_price)

        val currentUser = auth.currentUser

        add.setOnClickListener {
            getOrgID()
        //uploadIDCardToFirebaseStorage() }
        }
    }

    private fun getOrgID(){

        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef2: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef2.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()
                    Log.d("GLBID",globalOrgID)
                    registerMenu(globalOrgID)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
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
    fun registerMenu(orgID:String){

        val uuid=uuidGenerator()
        val company = databaseRef.child(orgID).child("menu").child(uuid)

        val category = itemCategory.editText!!.text.toString()
        val name = itemName.editText!!.text.toString()
        val description = itemDescription.editText!!.text.toString()
        val price = itemPrice.editText!!.text.toString().toFloat()

        company.child("item_id").setValue(uuid)
        company.child("item_category").setValue(category)
        company.child("item_name").setValue(name)
        company.child("item_desc").setValue(description)
        company.child("item_price").setValue(price)

    }
}