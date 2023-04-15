package services


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import datamodels.MenuItem
import interfaces.MenuApi
import interfaces.RequestType

public var globalOrgID: String = ""
class FirebaseDBService {
    private var databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val foodMenu = "food_menu"

    init {
        getOrgID()
    }
    fun getOrgID(){
        val user = FirebaseAuth.getInstance().currentUser!!
        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()

                    Log.d("GLBID",globalOrgID)
                    returnOrgID(globalOrgID)
                    //loadUserProfile(globalOrgID)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
    fun returnOrgID(orgID:String){globalOrgID=orgID}

    fun readAllMenu(menuApi: MenuApi, requestType: RequestType) {
        val menuList = ArrayList<MenuItem>()

        //val menuDbRef = databaseRef.child(foodMenu)
        val menuDbRef = databaseRef.child("01833540").child("menu")
        Log.d("GLOBAL", globalOrgID)

        menuDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val item = MenuItem(
                        //itemID = (1..1000).random().toString(), // TODO NEED TO UPDATE
                        itemID = snap.child("item_id").value.toString(),
                        imageUrl = snap.child("item_image_url").value.toString(),
                        itemName = snap.child("item_name").value.toString(),
//                        itemPrice = snap.child("item_price").value.toString().toFloat(),
                        itemShortDesc = snap.child("item_desc").value.toString(),
                        itemTag = snap.child("item_category").value.toString()
//                        itemStars = snap.child("stars").value.toString().toFloat()
                    )
                    menuList.add(item)
                }
                menuList.shuffle() //so that every time user can see different items on opening app
                menuApi.onFetchSuccessListener(menuList, requestType)
            }

            override fun onCancelled(error: DatabaseError) {
                // HANDLE ERROR
            }
        })
    }

    fun insertMenuItem(item: MenuItem) {
        val menuRef = databaseRef.child(foodMenu)

        menuRef.setValue(item)
    }
}