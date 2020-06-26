package com.example.forvenders.activity

import android.content.Intent
import android.util.Log
import android.util.Log.e
import android.view.View
import com.example.forvenders.base.BaseActivity
import com.example.forvenders.databinding.ActivityLoginBinding
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.example.forvenders.R
import com.example.forvenders.base.PreferanceRepository
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity  :  BaseActivity<ActivityLoginBinding>(){

    lateinit var mBinding: ActivityLoginBinding
    private var mCallbackManager: CallbackManager? = null
    private var mAuthListener : FirebaseAuth.AuthStateListener? = null
    private var accessTokenTracker : AccessTokenTracker? = null
    private var mGoogleClient : GoogleSignInClient? = null


    override fun onPermissionsGranted(requestCode: Int) {
    }
    override fun contentView() = R.layout.activity_login

    override fun initUI(binding: ActivityLoginBinding) {
        mBinding  = binding

        FacebookSdk.sdkInitialize(applicationContext)
        mBinding.loginButton.setReadPermissions("email", "public_profile")
        mCallbackManager = CallbackManager.Factory.create()

        mBinding.loginButton.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                e("For Access Token", "$result")
                handleFacebookToken(result?.accessToken)
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException?) {
                e("OnError", error?.message.toString())
            }

        })

        mBinding.txtLogin.setOnClickListener {

            mBinding.progressBar.visibility = View.VISIBLE
            mAuth.signInWithEmailAndPassword("abhi000@gmail.com", "qwerty")
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        showToast("Done")
                        mBinding.progressBar.visibility = View.GONE
                        startActivity(Intent(this, DashboardActivity::class.java))
                    } else {
                        showToast("failed")
                        mBinding.progressBar.visibility = View.GONE
                    }
                    // ...
                }.addOnFailureListener {
                    showToast(it.message.toString())
                    mBinding.progressBar.visibility = View.GONE
                }
        }

        mBinding.toolBar.txtSignOut.setOnClickListener {
            mAuth.signOut()
            LoginManager.getInstance().logOut()
            PreferanceRepository.logout()
            finish()
        }
        mBinding.toolBar.imgBack.setOnClickListener {
            onBackPressed()
        }

        mAuthListener = FirebaseAuth.AuthStateListener {
            /*if(mAuth.currentUser!=null) {
                if(PreferanceRepository.getString("user_id")!=null && PreferanceRepository.getString("user_id").isNotEmpty()
                    && PreferanceRepository.getString("service")!=null && PreferanceRepository.getString("service").isNotEmpty()){
                    startActivity(Intent(this,  VenderInformationActivity::class.java).putExtra("service_for", PreferanceRepository.getString("service")))
                }else{
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
            }*/
            if(mAuth.currentUser!=null){
                startActivity(Intent(this, DashboardActivity::class.java))
            }
        }

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {
                if(currentAccessToken == null){
                    mAuth.signOut()
                    LoginManager.getInstance().logOut()
                }
            }
        }



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleClient = GoogleSignIn.getClient(this, gso)
        mBinding.goolge.setOnClickListener {
            signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 103) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Done")
                    startActivity(Intent(this, DashboardActivity::class.java))
                } else {
                    showToast("Something went wrong")
                }
            }
    }

    private fun signIn() {
        val signInIntent = mGoogleClient?.signInIntent
        startActivityForResult(signInIntent, 103)
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener!!)
    }

    fun handleFacebookToken(accessToken: AccessToken?) {
        val credential : AuthCredential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            showToast("Login Done")
            startActivity(Intent(this, DashboardActivity::class.java))
        }.addOnFailureListener {
            showToast("Login Failed")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finishAffinity()
    }
}
