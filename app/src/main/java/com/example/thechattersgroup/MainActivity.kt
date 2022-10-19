package com.example.thechattersgroup

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thechattersgroup.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var db:FirebaseDatabase
    private lateinit var ChattersAdapter:ChattersChatAdapter
    companion object{
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }

    val  selectedImage=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->
        uri?.let { onImageSelected(uri) }

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

//        if (BuildConfig.DEBUG) {
//            Firebase.database.useEmulator("10.0.2.2", 9000)
//            Firebase.auth.useEmulator("10.0.2.2", 9099)
//            Firebase.storage.useEmulator("10.0.2.2", 9199)
//        }
        auth = FirebaseAuth.getInstance()
        checkCurrentUser()
        getUserPhotoUrl()
        getUserName()

        db= Firebase.database
        val messageReference=db.reference.child("messages")

        val options = FirebaseRecyclerOptions.Builder<ChattersChat>()
            .setQuery(messageReference, ChattersChat::class.java)
            .build()


        ChattersAdapter = ChattersChatAdapter(options,getUserName())
        binding.progressBar.visibility = ProgressBar.INVISIBLE
        var manager=LinearLayoutManager(this)
        manager.stackFromEnd=true

        binding.messageRecyclerView.layoutManager=manager
        binding.messageRecyclerView.adapter=ChattersAdapter


        binding.ivAddImage.setOnClickListener {
            selectedImage.launch(arrayOf("image/*"))
        }


        binding.etSendMessage.addTextChangedListener(MYButtonObserver(binding.sendButton))

        binding.sendButton.setOnClickListener {
            val chatMessage=ChattersChat(binding.etSendMessage.text.toString(),
            getUserName(),getUserPhotoUrl(),null)

            db.reference.child("messages").push().setValue(chatMessage)
            binding.etSendMessage.setText("")
        }
        ChattersAdapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(binding.messageRecyclerView,ChattersAdapter,manager))


    }
    private fun onImageSelected(uri: Uri) {

        val user=auth.currentUser
        val tempMessages=ChattersChat(null,getUserName(),getUserPhotoUrl(), LOADING_IMAGE_URL)
        db.reference.child("messages").push().setValue(tempMessages,DatabaseReference.CompletionListener { error, ref ->
            if (error != null) {
                Toast.makeText(this,"Unable to write message to database.",Toast.LENGTH_SHORT).show()
                return@CompletionListener
            }
            val key = ref.key
            val storageReference = Firebase.storage
                .getReference(user!!.uid)
                .child(key!!)
                .child(uri.lastPathSegment!!)
            putImageInStorage(storageReference, uri, key)

        })

    }
    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String) {


        // First upload the image to Cloud Storage
        storageReference.putFile(uri)
            .addOnSuccessListener (this){taskSnapshot->// After the image loads, get a public downloadUrl for the image
                // and add it to the message.

                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri->
                        val chatMessages=ChattersChat(null,getUserName(),getUserPhotoUrl(),uri.toString())

                        db.reference
                            .child("messages")
                            .child(key)
                            .setValue(chatMessages)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Image upload task was unsuccessful.",Toast.LENGTH_SHORT).show()
                    }


            }

    }


    override fun onPause() {
        ChattersAdapter.stopListening()
        super.onPause()
    }

    override fun onResume() {
        ChattersAdapter.startListening()
        super.onResume()
    }

    private fun checkCurrentUser() {
        if (auth.currentUser == null) {
            var intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.displayName
        } else "ANONYMOUS"
    }

    private fun getUserPhotoUrl(): String? {
        return auth.currentUser?.photoUrl?.toString()
    }

    override fun onStart() {
        super.onStart()
        checkCurrentUser()

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.logout->{
                signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}

