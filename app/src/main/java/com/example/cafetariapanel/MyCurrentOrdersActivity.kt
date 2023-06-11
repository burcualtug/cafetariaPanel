package com.example.cafetariapanel

import adapters.RecyclerCurrentOrderAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafetariapanel.databinding.CurrentOrderItemBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import datamodels.CurrentOrderItem
import datamodels.MenuItem
import datamodels.NotificationData
import datamodels.PushNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.current_order_item.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import services.DatabaseHandler
import services.FirebaseService
import services.RetrofitInstance


class MyCurrentOrdersActivity : AppCompatActivity(), RecyclerCurrentOrderAdapter.OnItemClickListener {

    private val currentOrderList = ArrayList<CurrentOrderItem>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerCurrentOrderAdapter

    private lateinit var databaseRef: DatabaseReference

    private lateinit var sharedPref: SharedPreferences
    //lateinit var binding: CurrentOrderItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_current_orders)
        //val binding: ViewDataBinding? = DataBindingUtil.setContentView(this, R.layout.current_order_item)
        //val showQRBtn: ExtendedFloatingActionButton = binding.currentOrderItemShowQrBtn
        databaseRef = FirebaseDatabase.getInstance().reference
        sharedPref = getSharedPreferences("user_profile_details", MODE_PRIVATE)

        recyclerView = findViewById(R.id.current_order_recycler_view)
        recyclerAdapter = RecyclerCurrentOrderAdapter(this, currentOrderList, this)
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        refreshPage()

        //loadCurrentOrdersFromDatabase()
        getOrgIDLoad()
    }

    fun refreshPage(){
        swipeRefresh.setOnRefreshListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
            Toast.makeText(this,"Page refreshed!",Toast.LENGTH_SHORT).show()
            swipeRefresh.isRefreshing=false
        }
    }
    private fun getOrgIDLoad(){

        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()

                    loadCurrentOrdersFromDatabase(globalOrgID)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadCurrentOrdersFromDatabase(orgID:String) {

        val db = DatabaseHandler(this)
        val data = db.readCurrentOrdersData()

        //val shp = sharedPref.getString("emp_org", "11")
        val ordersDbRef = databaseRef.child(orgID).child("orders")

        /*if(data.isEmpty()) {
            return
        }*/

        var status:String=""
        findViewById<LinearLayout>(R.id.current_order_empty_indicator_ll).visibility = ViewGroup.GONE

        //Log.d("snap1","snap1.toString()")
        ordersDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap1 in snapshot.children){
                    for(snap in snap1.children){
                        val currentOrderItem =  CurrentOrderItem()
                        currentOrderItem.userUID = snap.child("userUID").value.toString()
                        currentOrderItem.orderID = snap.child("order_id").value.toString()
                        currentOrderItem.takeAwayTime = snap.child("takeAwayTime").value.toString()
                        currentOrderItem.paymentStatus = snap.child("paymentMethod").value.toString()
                        currentOrderItem.orderItemNames = snap.child("itemNames").value.toString()
                        currentOrderItem.orderItemQuantities = snap.child("itemQty").value.toString()
                        currentOrderItem.totalItemPrice = snap.child("totalItemPrice").value.toString()
                        currentOrderItem.tax = snap.child("totalTaxPrice").value.toString()
                        currentOrderItem.subTotal = snap.child("subTotalPrice").value.toString()
                        currentOrderItem.orderNote = snap.child("orderNote").value.toString()
                        currentOrderItem.situation = snap.child("situation").value.toString()

                        currentOrderList.add(currentOrderItem)
                        currentOrderList.reverse()
                        recyclerAdapter.notifyItemRangeInserted(0, 2)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        /*ordersDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val currentOrderItem =  CurrentOrderItem()
                    currentOrderItem.orderID = snap.child("order_id").value.toString()
                    currentOrderItem.takeAwayTime = snap.child("takeAwayTime").value.toString()
                    currentOrderItem.paymentStatus = snap.child("paymentMethod").value.toString()
                    currentOrderItem.orderItemNames = snap.child("itemNames").value.toString()
                    currentOrderItem.orderItemQuantities = snap.child("itemQty").value.toString()
                    currentOrderItem.totalItemPrice = snap.child("totalItemPrice").value.toString()
                    currentOrderItem.tax = snap.child("totalTaxPrice").value.toString()
                    currentOrderItem.subTotal = snap.child("subTotalPrice").value.toString()
                    currentOrderItem.orderNote = snap.child("orderNote").value.toString()

                    currentOrderList.add(currentOrderItem)
                    currentOrderList.reverse()
                    recyclerAdapter.notifyItemRangeInserted(0, 2)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // HANDLE ERROR
            }
        })*/
        /*for(i in 0 until data.size) {
            val currentOrderItem =  CurrentOrderItem()
            currentOrderItem.orderID = data[i].orderID
            currentOrderItem.takeAwayTime = data[i].takeAwayTime
            currentOrderItem.paymentStatus = data[i].paymentStatus
            currentOrderItem.orderItemNames = data[i].orderItemNames
            currentOrderItem.orderItemQuantities = data[i].orderItemQuantities
            currentOrderItem.totalItemPrice = data[i].totalItemPrice
            currentOrderItem.tax = data[i].tax
            currentOrderItem.subTotal = data[i].subTotal

            /*currentOrderItem.orderID = data[i].orderID
            currentOrderItem.takeAwayTime = data[i].takeAwayTime
            currentOrderItem.paymentStatus = data[i].paymentStatus
            currentOrderItem.orderItemNames = data[i].orderItemNames
            currentOrderItem.orderItemQuantities = data[i].orderItemQuantities
            currentOrderItem.totalItemPrice = data[i].totalItemPrice
            currentOrderItem.tax = data[i].tax
            currentOrderItem.subTotal = data[i].subTotal*/
            currentOrderList.add(currentOrderItem)
            currentOrderList.reverse()
            recyclerAdapter.notifyItemRangeInserted(0, data.size)
        } */
    }

    override fun showQRCode(orderID: String) {
        val bundle = Bundle()
        bundle.putString("orderID", orderID)

        val dialog = QRCodeFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "QR Code Generator")
    }

    fun getUserID(){
        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        //val orderID = currentOrderList[position].

        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun orderDetails(position: Int){

        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userUID=currentOrderList[position].userUID
        intent.putExtra("userUID", currentOrderList[position].userUID)
        //intent.putExtra("orderID", currentOrderList[position].orderID)
        //intent.putExtra("paymentMethod")
        startActivity(intent)
    }

    override fun acceptOrder(position:Int){
        AlertDialog.Builder(this)
            .setTitle("Order Acceptance")
            .setMessage("Are you sure you want to accept this order?")
            .setPositiveButton("Yes, Accept Order", DialogInterface.OnClickListener { dialogInterface, _ ->
                val result = DatabaseHandler(this).deleteCurrentOrderRecord(currentOrderList[position].orderID)
                //binding.currentOrderItemShowQrBtn.isClickable=false
                //binding.currentOrderItemShowQrBtn.text = "Accepted"
                //currentOrderList.removeAt(position)
                //recyclerAdapter.notifyItemRemoved(position)
                //recyclerAdapter.notifyItemRangeChanged(position, currentOrderList.size)
                //Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

                val shp = sharedPref.getString("emp_org", "11")
                val orderIDdb=currentOrderList[position].orderID
                val orderStRef = databaseRef.child(shp!!).child("orders") //.child(orderIDdb).child("situation").setValue("1")

                orderStRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (snap1 in snapshot.children) {
                            for(snap in snap1.children){
                                Log.d("TAG","userUID")
                                if(snap.child("order_id").value.toString() == orderIDdb){
                                    val userUID = snap.child("userUID").value.toString()
                                    Log.d("TAGAMK","ACCEPTEDAMK")
                                    snap.ref.child("situation").setValue("1")
                                    //Toast.makeText(applicationContext,"ACCEPTED AQ",Toast.LENGTH_SHORT).show()
                                    //orderStRef.child(userUID).child("situation").setValue("1")
                                }
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // HANDLE ERROR
                    }
                })

                if(currentOrderList.isEmpty()) {
                    findViewById<LinearLayout>(R.id.current_order_empty_indicator_ll).visibility = ViewGroup.VISIBLE
                }

                dialogInterface.dismiss()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()

    }


    override fun contactOrder(position: Int){

        val userUID=currentOrderList[position].userUID
        val orderID = currentOrderList[position].orderID

       // Toast.makeText(this,userUID,Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("userUID", userUID)
        intent.putExtra("orderID",orderID)
        startActivity(intent)

        /*startActivity(
            Intent(
                this,
                ChatActivity::class.java
            )
        )*/
    }

    override fun profileInfos(position: Int){
        currentOrderList[position].userUID
    }

    override fun cancelOrder(position: Int) {
        //pushCancelOrderNotification(position)
        getOrgID(position)
        AlertDialog.Builder(this)
            .setTitle("Order Cancellation")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes, Cancel Order", DialogInterface.OnClickListener { dialogInterface, _ ->
                val result = DatabaseHandler(this).deleteCurrentOrderRecord(currentOrderList[position].orderID)

                val shp = sharedPref.getString("emp_org", "11")
                val orderIDdb=currentOrderList[position].orderID
                val orderUserUID = currentOrderList[position].userUID
                databaseRef.child(shp!!).child("orders").child(orderUserUID).child(orderIDdb).removeValue()


                currentOrderList.removeAt(position)
                recyclerAdapter.notifyItemRemoved(position)
                recyclerAdapter.notifyItemRangeChanged(position, currentOrderList.size)
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

                if(currentOrderList.isEmpty()) {
                    findViewById<LinearLayout>(R.id.current_order_empty_indicator_ll).visibility = ViewGroup.VISIBLE
                }

                dialogInterface.dismiss()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun getOrgID(position:Int){

        val user = FirebaseAuth.getInstance().currentUser!!
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

        databaseRef.child("matches").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val globalOrgID = snapshot.child("organizationID").value.toString()

                    pushCancelOrderNotification(globalOrgID,position)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun pushCancelOrderNotification(orgID:String, position: Int){
        //val orgID = sharedPref.getString("emp_org", "11")
        FirebaseService.sharedPref2 = getSharedPreferences("sharedPref2", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener{ task ->
            if(!task.isSuccessful){
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("FCMTOKEN",token)

            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

            val orderID = currentOrderList[position].orderID

            databaseRef.child(orgID).child("tokens").child(currentOrderList[position].userUID)
                .get().addOnSuccessListener {
                    if(it.exists()){
                        val orgToken = it.child("token").value.toString()
                        sendNotification(
                            PushNotification(
                                NotificationData("Your order cancelled!", "Order ID: $orderID"),
                                orgToken)
                        )
                    }
                }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }


    fun goBack(view: View) {onBackPressed()}
}