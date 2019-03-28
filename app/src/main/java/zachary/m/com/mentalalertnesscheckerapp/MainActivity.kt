package zachary.m.com.mentalalertnesscheckerapp

import android.content.Intent
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.AlertDialog
//import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this,""+p0.errorMessage,Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val PERMISSION_CODE =9999
    }

    lateinit var mGoogleApiClient:GoogleApiClient
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var alertDialog:AlertDialog

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PERMISSION_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data) //result:GoogleSignInResult!
            if(result.isSuccess) {
                val account:GoogleSignInAccount? = result.signInAccount
                val idToken:String? = account!!.idToken

                val credential = GoogleAuthProvider.getCredential(idToken,null)
                firebaseAuthWithGoogle(credential)
            }
            else{
                Log.d("EDMT_ERROR","Login Unsuccessful")
                Toast.makeText(this,"Login Unsuccessful",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(credential: AuthCredential) { // changed from AuthCredential?
        firebaseAuth.signInWithCredential(credential) //took out !! from firebaseAuth
            .addOnSuccessListener { authResult ->
                val logged_email = authResult.user.email
                val logged_activity = Intent(this@MainActivity,LoggedActivity::class.java)
                logged_activity.putExtra("email",logged_email)
                startActivity(logged_activity)
            }
            .addOnFailureListener{
                e-> Toast.makeText(this,""+e.message,Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureGoogleClient()

        firebaseAuth = FirebaseAuth.getInstance()

        alertDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Please wait")
            .setCancelable(false)
            .build()

        btn_sign_in.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient) //maybe add Intent:Intent!
        startActivityForResult(intent, PERMISSION_CODE)
    }

    private fun configureGoogleClient() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // maybe change to options:GoogleSignInOptions! =
            .requestIdToken("215806368732-6ct2hcj5gnreoqhfue3ihgu6vqo63v7f.apps.googleusercontent.com")  //< --(getString(R.string.default_web_client_id)) used to be here until it can fix
            .requestEmail()
            .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this,this)
            .addApi(Auth.GOOGLE_SIGN_IN_API,options)
            .build()
        mGoogleApiClient.connect()  // don't forget it
    }
}


