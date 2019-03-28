package zachary.m.com.mentalalertnesscheckerapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_message.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import zachary.m.com.mentalalertnesscheckerapp.model.Message

import java.text.SimpleDateFormat
import java.util.Calendar
import zachary.m.com.mentalalertnesscheckerapp.model.User
import com.google.firebase.iid.FirebaseInstanceId


/* activity_message]  */

class MessageActivity : AppCompatActivity() {

    private val TAG = "MessageActivity"
    private val REQUIRED = "Required"

    private var user: FirebaseUser? = null

    private var mDatabase: DatabaseReference? = null
    private var mMessageReference: DatabaseReference? = null
    private var mMessageListener: ValueEventListener? = null

    private val CHANNEL_ID = "simplified_coding"
    private val CHANNEL_NAME = "Simplified Coding"
    private val CHANNEL_DESC = "Simplified Coding Notifications"

    /* Blue tooth updates */
    //val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)


        mDatabase = FirebaseDatabase.getInstance().reference
        mMessageReference = FirebaseDatabase.getInstance().getReference("message")
        user = FirebaseAuth.getInstance().currentUser

        btnSend.setOnClickListener {
            submitMessage()
            edtSentText.setText("")
        }

        btnBack.setOnClickListener {
            finish()
        }

        //super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_message) // changed from activity_main to activity_message

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = CHANNEL_DESC
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

        }

        findViewById<View>(R.id.buttonNotify).setOnClickListener { displayNotification() }
    }

    private fun displayNotification() {

        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("It's Working...")
            .setContentText("The First notification of many...")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // can be default, high or low

        val mNotificationMgr = NotificationManagerCompat.from(this)
        mNotificationMgr.notify(1, mBuilder.build())
    }


    override fun onStart() {
        super.onStart()

        val messageListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val message = dataSnapshot.getValue(Message::class.java)

                    Log.e(
                        TAG,
                        "onDataChange: Message data is updated: " + message!!.author + ", " + message.time + ", " + message.body
                    )

                    tvAuthor.text = message.author
                    tvTime.text = message.time
                    tvBody.text = message.body
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
                Log.e(TAG, "onCancelled: Failed to read message")

                tvAuthor.text = ""
                tvTime.text = ""
                tvBody.text = "onCancelled: Failed to read message!"
            }
        }

        mMessageReference!!.addValueEventListener(messageListener)

        // copy for removing at onStop()
        mMessageListener = messageListener
    }

    override fun onStop() {
        super.onStop()

        if (mMessageListener != null) {
            mMessageReference!!.removeEventListener(mMessageListener!!) // added non null to the end
        }
    }

    private fun submitMessage() {
        val body = edtSentText.text.toString()

        if (TextUtils.isEmpty(body)) {
            edtSentText.error = REQUIRED
            return
        }

        // User data change listener
        mDatabase!!.child("users").child(user!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)

                if (user == null) {
                    Log.e(TAG, "onDataChange: User data is null!")
                    Toast.makeText(this@MessageActivity, "onDataChange: User data is null!", Toast.LENGTH_SHORT).show()
                    return
                }

                writeNewMessage(body)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e(TAG, "onCancelled: Failed to read user!")
            }
        })
    }

    private fun writeNewMessage(body: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
        val message = Message(getUsernameFromEmail(user!!.email), body, time)

        mMessageReference!!.setValue(message)
    }

    private fun getUsernameFromEmail(email: String?): String {  //changed from private to public
        return if (email!!.contains("@")) {
            email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            email
        }
    }

}

