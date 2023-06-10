package com.example.cafetariapanel

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import datamodels.IdCardModel
import datamodels.MenuItem
import kotlinx.android.synthetic.main.activity_add_item2.*
import java.math.BigInteger
import java.util.*

class AddItem2 : AppCompatActivity() {
    private lateinit var itemName: TextInputLayout
    private lateinit var itemCategory: TextInputLayout
    private lateinit var itemDescription: TextInputLayout
    private lateinit var itemPrice: TextInputLayout
    private lateinit var itemImage: ImageView

    private lateinit var itemIV: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var registerProgressDialog: ProgressDialog

    private lateinit var employeeIDCardUri: Uri

    private lateinit var add: Button

    private lateinit var sharedPref: SharedPreferences
    var idUploaded = false

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
        itemImage = findViewById(R.id.item_iv)
        val currentUser = auth.currentUser

        add.setOnClickListener {//registerMenu(cat)
            uploadIDCardToFirebaseStorage(currentUser!!)
            //registerMenu()

        }
        val languages = resources.getStringArray(R.array.Categories)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                itemImage.visibility = ViewGroup.VISIBLE
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                itemImage.setImageBitmap(bitmap)
                employeeIDCardUri = uri
                idUploaded = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    fun uuidGenerator():String {
        return String.format("%040d", BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)).dropLast(32)
    }

    fun itemUIDGenerator():String{
        return String.format("%040d", BigInteger(UUID.randomUUID().toString().replace("-", ""), 16))
    }

    fun chooseImage(view: View) {
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }


    private fun uploadIDCardToFirebaseStorage(user:FirebaseUser) {

        val reference = storageRef.child("${itemUIDGenerator()}.jpeg")

        registerProgressDialog.setTitle("Uploading ID Card...")

        reference.putFile(employeeIDCardUri)
            .addOnSuccessListener { _ ->
                registerProgressDialog.dismiss()
                Toast.makeText(this, "ID Card Uploaded!!", Toast.LENGTH_SHORT).show()
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val model = uri.toString() // IdCardModel(uri.toString())
                    getOrgID(model)//registerMenu(model)
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

    fun getOrgID(idCardDownloadUri: String){
        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()

                    registerMenu(idCardDownloadUri,globalOrgID)

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun turnBack(view: View) {onBackPressed()}

    fun registerMenu(idCardDownloadUri: String, orgID:String){
        val catSpin = category_spinner.selectedItem.toString()
        Toast.makeText(this,catSpin,Toast.LENGTH_SHORT).show()
        Log.d("CATEGORY",catSpin)
        //val orgID = sharedPref.getString("emp_org", "11")
        val uuid=uuidGenerator()
        val company = databaseRef.child(orgID).child("menu").child(uuid)
        val name = itemName.editText!!.text.toString()
        val description = itemDescription.editText!!.text.toString()
        val price = itemPrice.editText!!.text.toString().toFloat()

            company.child("item_id").setValue(uuid)
            company.child("item_category").setValue(catSpin)
            company.child("item_name").setValue(name)
            company.child("item_desc").setValue(description)
            company.child("item_price").setValue(price)
            company.child("item_image_url").setValue(idCardDownloadUri)

    }
}