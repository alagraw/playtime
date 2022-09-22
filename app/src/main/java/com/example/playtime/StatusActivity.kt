package com.example.playtime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult

class StatusActivity : AppCompatActivity() {

    private val CHANNEL_ID = "playtime_channel"
    private val notificationId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        val officeName = findViewById<TextView>(R.id.officeName)
        val playAreaName = findViewById<TextView>(R.id.playAreaName)
        val notificationButton = findViewById<Button>(R.id.notifyAvailability)
        var flag = 0
        var count = 0
        val intent = getIntent()

        val bundle = intent.extras
        if (bundle != null)
        {
            officeName.text = bundle.getString("officeLocation")
            playAreaName.text = bundle.getString("playArea")
        }

        val availabilityStatus    = findViewById<TextView>(R.id.statusValue)

        var subscribeCallback: SubscribeCallback = object : SubscribeCallback()  {
            override fun status(pubnub: PubNub, status: PNStatus) {
                Log.d("Status", status.toString())
            }
            override fun message(pubnub: PubNub, message: PNMessageResult) {
                runOnUiThread {
                    Log.d("Pubnub", "Printing value")
                    val status = message.message.asString
                    availabilityStatus.text = status
                    if (status == "Unoccupied")
                    {
                        count++
                        if(flag == 1 && count >= 3)
                        {
                            sendNotification()
                            count = 0
                            flag = 0
                        }
                        availabilityStatus.setTextColor(Color.GREEN)
                    }
                    else
                    {
                        flag = 0
                        count = 0
                        availabilityStatus.setTextColor(Color.RED)
                    }
                }
            }
            override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {
            }
        }

        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = "sub-c-840d3687-421d-40f7-856e-595581f68d2e"
        pnConfiguration.publishKey = "pub-c-d038582a-e09c-48eb-b5ef-62bd6d7aa15a"
        pnConfiguration.secretKey = "true"
        val pubNub = PubNub(pnConfiguration)

        pubNub.run {
            addListener(subscribeCallback)
            subscribe()
                .channels(listOf("playtime"))
                .execute()
        }

        createNotificationChannel()
        notificationButton.setOnClickListener {
            flag = 1
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Play Area Available!"
            val descriptionText = "Your play area is now available"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Play Area Available!")
            .setContentText("Your play area is now available")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }
}