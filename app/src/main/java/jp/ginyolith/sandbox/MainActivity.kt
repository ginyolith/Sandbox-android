package jp.ginyolith.sandbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityHandler
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.mobileconnectors.pinpoint.targeting.endpointProfile.EndpointProfileUser
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*


class MainActivity : AppCompatActivity() {
    private var credentialsProvider: AWSCredentialsProvider? = null
    private var configuration: AWSConfiguration? = null

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Received notification from local broadcast. Display it in a dialog.")

            val bundle = intent.extras
            val message = PushListenerService.getMessage(bundle!!)

            AlertDialog.Builder(this@MainActivity)
                    .setTitle("Push notification")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the AWS Mobile Client
        AWSMobileClient.getInstance().initialize(this) {

            // Obtain the reference to the AWSCredentialsProvider and AWSConfiguration objects
            credentialsProvider = AWSMobileClient.getInstance().credentialsProvider
            configuration = AWSMobileClient.getInstance().configuration

            // Use IdentityManager#getUserID to fetch the identity id.
            IdentityManager.getDefaultIdentityManager().getUserID(object : IdentityHandler {
                override fun onIdentityId(identityId: String) {
                    Log.d("YourMainActivity", "Identity ID = $identityId")

                    // Use IdentityManager#getCachedUserID to
                    //  fetch the locally cached identity id.
                    val cachedIdentityId = IdentityManager.getDefaultIdentityManager().cachedUserID

                }

                override fun handleError(exception: Exception) {
                    Log.d("YourMainActivity", "Error in retrieving the identity$exception")
                }
            })
        }.execute()

        val config = PinpointConfiguration(
                this@MainActivity,
                AWSMobileClient.getInstance().credentialsProvider,
                AWSMobileClient.getInstance().configuration
        )
        pinpointManager = PinpointManager(config)
        pinpointManager?.sessionClient?.startSession()
        pinpointManager?.analyticsClient?.submitEvents()

        // カスタムエンドポイント設定
        val interestsList = Arrays.asList("science", "politics", "travel")
        pinpointManager?.targetingClient?.addAttribute("Interests", interestsList)
        pinpointManager?.targetingClient?.updateEndpointProfile()


        // ユーザーIDをエンドポイントに割り当てる
        val endpointProfile = pinpointManager?.targetingClient?.currentEndpoint()
        val user = EndpointProfileUser()
        user.userId = "UserIdValue"
        endpointProfile?.user = user
        Log.d("a", "endpointId = " + endpointProfile?.endpointId)
        pinpointManager?.targetingClient?.updateEndpointProfile(endpointProfile)

        // Initialize PinpointManager
        getPinpointManager(applicationContext)


        findViewById<Button>(R.id.button_send_ev).setOnClickListener {
            val event = pinpointManager?.analyticsClient?.createEvent("clickEvButton")
                    ?.withAttribute("numata","ph")
                    ?.withAttribute("akagi","ks")
                    ?.withMetric("metric", Math.random())

            pinpointManager?.analyticsClient?.recordEvent(event)
            pinpointManager?.analyticsClient?.submitEvents()
        }
    }

    override fun onPause() {
        super.onPause()

        // Unregister notification receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
    }

    override fun onResume() {
        super.onResume()

        // Register notification receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                IntentFilter(PushListenerService.ACTION_PUSH_NOTIFICATION))
    }

    companion object {
        val TAG = MainActivity.javaClass.simpleName

        private var pinpointManager: PinpointManager? = null

        fun getPinpointManager(applicationContext: Context): PinpointManager? {
            if (pinpointManager == null) {
                val pinpointConfig = PinpointConfiguration(
                        applicationContext,
                        AWSMobileClient.getInstance().credentialsProvider,
                        AWSMobileClient.getInstance().configuration)

                pinpointManager = PinpointManager(pinpointConfig)
                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener { task ->
                            val token = task.result.token
                            Log.d(TAG, "Registering push notifications token: $token")
                            pinpointManager!!.notificationClient.registerDeviceToken(token)
                        }
            }
            return pinpointManager
        }
    }
}
