package com.example.cafetariapanel

import adapters.RecyclerCurrentOrderAdapter
import android.content.DialogInterface
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import datamodels.CurrentOrderItem
import datamodels.MenuItem
import kotlinx.android.synthetic.main.current_order_item.*
import services.DatabaseHandler


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

        loadCurrentOrdersFromDatabase()
    }

    private fun loadCurrentOrdersFromDatabase() {

        val db = DatabaseHandler(this)
        val data = db.readCurrentOrdersData()

        val shp = sharedPref.getString("emp_org", "11")
        val ordersDbRef = databaseRef.child(shp!!).child("orders")

        /*if(data.isEmpty()) {
            return
        }*/

        var status:String=""
        findViewById<LinearLayout>(R.id.current_order_empty_indicator_ll).visibility = ViewGroup.GONE
        ordersDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val currentOrderItem =  CurrentOrderItem()
                    currentOrderItem.orderID = snap.child("order_id").value.toString()
                    currentOrderItem.takeAwayTime = snap.child("takeAwayTime").value.toString()
                    currentOrderItem.paymentStatus = snap.child("paymentMethod").value.toString()
                    currentOrderItem.orderItemNames = snap.child("itemNames").value.toString()
                    currentOrderItem.orderItemQuantities = snap.child("itemQty").value.toString()
                    currentOrderItem.totalItemPrice = snap.child("takeAwayTime").value.toString()
                    currentOrderItem.tax = snap.child("takeAwayTime").value.toString()
                    currentOrderItem.subTotal = snap.child("takeAwayTime").value.toString()
                    currentOrderItem.orderNote = snap.child("orderNote").value.toString()

                    currentOrderList.add(currentOrderItem)
                    currentOrderList.reverse()
                    recyclerAdapter.notifyItemRangeInserted(0, 2)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // HANDLE ERROR
            }
        })
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
        //User have to just show the QR Code, and canteen staff have to scan, so user don't have to wait more
        val bundle = Bundle()
        bundle.putString("orderID", orderID)

        val dialog = QRCodeFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "QR Code Generator")
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
                databaseRef.child(shp!!).child("orders").child(orderIDdb).child("situation").setValue("1")

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


    override fun cancelOrder(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Order Cancellation")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes, Cancel Order", DialogInterface.OnClickListener { dialogInterface, _ ->
                val result = DatabaseHandler(this).deleteCurrentOrderRecord(currentOrderList[position].orderID)

                val shp = sharedPref.getString("emp_org", "11")
                val orderIDdb=currentOrderList[position].orderID
                databaseRef.child(shp!!).child("orders").child(orderIDdb).removeValue()

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

    fun goBack(view: View) {onBackPressed()}
}