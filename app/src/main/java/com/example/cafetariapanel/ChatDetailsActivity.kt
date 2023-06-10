package com.example.cafetariapanel

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.*
import datamodels.CurrentOrderItem

class ChatDetailsActivity : AppCompatActivity() {
    private lateinit var databaseRef: DatabaseReference
    private lateinit var sharedPref: SharedPreferences

    private lateinit var takeAwayTimeTV: TextView
    private lateinit var paymentStatusTV: TextView
    private lateinit var orderIDTV: TextView
    private lateinit var tableLayout: TableLayout
    private lateinit var totalItemPriceTV: TextView
    private lateinit var totalTaxTV: TextView
    private lateinit var subTotalTV: TextView
    private lateinit var orderNoteTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)

        databaseRef = FirebaseDatabase.getInstance().reference
        sharedPref = getSharedPreferences("user_profile_details", MODE_PRIVATE)

        val takeAwayTimeTV: TextView = findViewById(R.id.current_order_item_take_away_time_tv)
        val paymentStatusTV: TextView = findViewById(R.id.current_order_item_payment_status_tv)
        val orderIDTV: TextView = findViewById(R.id.current_order_item_order_id_tv)
        val tableLayout: TableLayout = findViewById(R.id.current_order_item_table_layout)
        val totalItemPriceTV: TextView = findViewById(R.id.current_order_item_total_price_tv)
        val totalTaxTV: TextView = findViewById(R.id.current_order_item_total_tax_tv)
        val subTotalTV: TextView = findViewById(R.id.current_order_item_sub_total_tv)
        val orderNoteTV: TextView = findViewById(R.id.orderNoteTV)

        var intent = Intent()
        intent = getIntent()
        val customerUID = intent.getStringExtra("userUID").toString()
        val orderID = intent.getStringExtra("orderID").toString()
        val shp = sharedPref.getString("emp_org","11")
        Log.d("UID", customerUID)
        Log.d("OID", orderID)

        databaseRef.child(shp!!).child("orders").child(customerUID)//.child(orderID)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap in snapshot.children){
                        val currentOrderItem =  CurrentOrderItem()
                        if(snap.child("order_id").value.toString()==orderID){
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

                            setDetails(currentOrderItem)

                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    fun setDetails(currentItem: CurrentOrderItem) {
        takeAwayTimeTV = findViewById(R.id.current_order_item_take_away_time_tv)
        paymentStatusTV = findViewById(R.id.current_order_item_payment_status_tv)
        orderIDTV= findViewById(R.id.current_order_item_order_id_tv)
        tableLayout= findViewById(R.id.current_order_item_table_layout)
        totalItemPriceTV = findViewById(R.id.current_order_item_total_price_tv)
        totalTaxTV = findViewById(R.id.current_order_item_total_tax_tv)
        subTotalTV = findViewById(R.id.current_order_item_sub_total_tv)
        orderNoteTV = findViewById(R.id.orderNoteTV)

        //BURALARI DOLDUR SETLE
        takeAwayTimeTV.text = currentItem.takeAwayTime
        paymentStatusTV.text = currentItem.paymentStatus
        orderIDTV.text = currentItem.orderID
        orderNoteTV.text = currentItem.orderNote
        totalItemPriceTV.text = ("\$%.2f".format(currentItem.totalItemPrice.toFloat())).toString() // "\$%.2f".format(currentItem.totalItemPrice.toString())
        totalTaxTV.text = ("\$%.2f".format(currentItem.tax.toFloat())).toString()
        subTotalTV.text = ("\$%.2f".format(currentItem.subTotal.toFloat())).toString()
        orderNoteTV.text=currentItem.orderNote
        addTable(currentItem)

    }

    fun addTable(currentOrderItem: CurrentOrderItem){
        val items = currentOrderItem.orderItemNames.split(";")
        val qtys = currentOrderItem.orderItemQuantities.split(";")

        for(i in items.indices) {
            //adding row in table
            tableLayout.addView(
                getTableRow(
                    items[i],
                    qtys[i]
                )
            )
        }
    }

    fun getTableRow(itemName: String, itemQty: String): TableRow{
        val tbRow = TableRow(this)
        val tbItemName = TextView(this)
        val tbQty = TextView(this)

        val typeface = ResourcesCompat.getFont(this, R.font.montserrat_semi_bold)

        //Setting Text
        tbItemName.text = itemName
        tbQty.text = itemQty

        //Changing Color
        tbItemName.setTextColor(Color.parseColor("#1C213F"))
        tbQty.setTextColor(Color.parseColor("#1C213F"))

        //Changing Font
        tbItemName.typeface = typeface
        tbQty.typeface = typeface

        tbRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tbQty.textAlignment = ViewGroup.TEXT_ALIGNMENT_TEXT_END

        //adding item name and quantity in a row
        tbRow.addView(tbItemName)
        tbRow.addView(tbQty)

        return tbRow

    }


    fun goBack(view: View) {onBackPressed()}
}