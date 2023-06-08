package com.example.cafetariapanel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import datamodels.NotificationData
import datamodels.PushNotification
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import services.FirebaseService
import services.RetrofitInstance


const val TOPIC = "/topics/myTopic2"

class NotificationActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        FirebaseService.sharedPref2 = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener{ task ->
            if(!task.isSuccessful){
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("FCMTOKEN",token)
            etToken.setText(token)
        })
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        btnSend.setOnClickListener {
            val title = etTitle.text.toString()
            val message = etMessage.text.toString()
            val recipientToken = etToken.text.toString()
            if(title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
                PushNotification(
                    NotificationData(title, message),
                    recipientToken
                ).also {
                    sendNotification(it)
                }
            }
        }
    }

    //cpbJhgNVTaqOZuCgFnkiAr:APA91bF6Nc4VDfbLJFoaOh6ZDwZ8kKMkPU9YZd4Tv-7k9sP3OA0bTw5-Ws5dErQUQjbp45NnukV_R-KMsnug4h9eJqvFPvEk-oV9xOjheFCQukEHIW-OK6AethoYX3icps6TZHbIUDYc
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}