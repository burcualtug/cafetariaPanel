package com.example.cafetariapanel

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.activity_add_item.*

class AddItem : AppCompatActivity() {
    private lateinit var itemName: TextInputLayout
    private lateinit var itemCategory: TextInputLayout
    private lateinit var itemDescription: TextInputLayout
    private lateinit var itemPrice: TextInputLayout


    private lateinit var databaseRef2: DatabaseReference

    private lateinit var itemIV: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var registerProgressDialog: ProgressDialog

    private lateinit var add: Button
    private lateinit var employeeIDCardUri: Uri
    var idUploaded = false
    var orgID:String=""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        registerProgressDialog = ProgressDialog(this)
        auth = FirebaseAuth.getInstance()
        databaseRef2 = FirebaseDatabase.getInstance().reference

        storageRef = FirebaseStorage.getInstance().reference.child("itemImages")
        itemIV = findViewById(R.id.item_iv)
        add = findViewById(R.id.add)
        itemCategory=findViewById(R.id.item_category)
        itemName=findViewById(R.id.item_name)
        itemDescription=findViewById(R.id.item_name)
        itemPrice=findViewById(R.id.item_price)

        val currentUser = auth.currentUser

        add.setOnClickListener { registerMenu() }
        //uploadIDCardToFirebaseStorage(currentUser)
    }
    fun returnOrgID(globalOrgID:String){
        orgID=globalOrgID

        Toast.makeText(this, orgID, Toast.LENGTH_SHORT).show()
    }
    private fun getOrgID(){

        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef2: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef2.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()

                    Log.d("GLBID",globalOrgID)
                    returnOrgID(globalOrgID)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun chooseItemImage(view: View) {
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                itemIV.visibility = ViewGroup.VISIBLE
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                itemIV.setImageBitmap(bitmap)
                employeeIDCardUri = uri
                idUploaded = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun addItemImage(user: FirebaseUser){

        val reference = storageRef.child("${user.uid}.jpeg")

        registerProgressDialog.setTitle("Uploading ID Card...")

        reference.putFile(employeeIDCardUri)
            .addOnSuccessListener { _ ->
                // Image uploaded successfully
                // Dismiss dialog
                registerProgressDialog.dismiss()
                Toast.makeText(this, "ID Card Uploaded!!", Toast.LENGTH_SHORT).show()
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val model = IdCardModel(uri.toString())
                    addMenuDetailsToDatabase(user, model)
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
    }
    private fun registerMenu() {

        if(!idUploaded) {
            Toast.makeText(this, "Please Upload your ID Card", Toast.LENGTH_SHORT).show()
            return
        }

        registerProgressDialog.setTitle("Registering...")
        registerProgressDialog.setMessage("We are creating your account")
        registerProgressDialog.show()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            addItemImage(currentUser)
        }

    }

    fun addMenuDetailsToDatabase(user: FirebaseUser, idCardDownloadUri: IdCardModel){
        registerProgressDialog.setMessage("Uploading details to database")
        getOrgID()

        Toast.makeText(this, orgID, Toast.LENGTH_SHORT).show()

        val company = databaseRef2.child(orgID).child("menu")

        company.child("category").setValue(itemCategory)
        company.child("name").setValue(itemName)
        company.child("description").setValue(itemDescription)
        company.child("price").setValue(itemPrice)
        company.child("item_iv").setValue(idCardDownloadUri)

    }

}