package com.example.thechattersgroup

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private var providers= arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        signUpUser()
    }
    private var signUpLauncher=registerForActivityResult(FirebaseAuthUIActivityResultContract()){
        onSignUpResult(it)
    }

    private fun onSignUpResult(result: FirebaseAuthUIAuthenticationResult?) {
        if(result?.resultCode== Activity.RESULT_OK){
            var intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this,"Authentication Failed", Toast.LENGTH_SHORT).show()
        }


    }

    private fun signUpUser() {
        val signUpIntent= AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

            signUpLauncher.launch(signUpIntent)

    }
}