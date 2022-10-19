package com.example.thechattersgroup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thechattersgroup.databinding.MessagesImagesBinding
import com.example.thechattersgroup.databinding.MessagesSimpleBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.zip.Inflater

class ChattersChatAdapter(
    private val options:FirebaseRecyclerOptions<ChattersChat>,private val currentUserName: String?)
    :FirebaseRecyclerAdapter<ChattersChat, RecyclerView.ViewHolder>(options) {


    inner class SimpleChatViewHolder(private val binding:MessagesSimpleBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(item:ChattersChat){

            binding.tvMessage.text=item.chat
            setTextColor(item.name,binding.tvMessage)
            binding.messengerTextView.text=if(item.name==null) "ANONYMOUS" else item.name
            if(item.photoUrl!=null){
                  LoadImageIntoView(binding.ivMessagner,item.photoUrl)
            }else{
                binding.ivMessagner.setImageResource(R.drawable.ic_account_circle)
            }

        }
        private fun setTextColor(userName: String?, textView: TextView) {
            if (userName != "ANONYMOUS" && currentUserName == userName && userName != null) {
                textView.setBackgroundResource(R.drawable.round_message_blue)
                textView.setTextColor(Color.WHITE)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray)
                textView.setTextColor(Color.BLACK)
            }
        }


    }
    inner class ImageChatViewHolder(private val binding: MessagesImagesBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(item:ChattersChat){
            item.imageUrl?.let { LoadImageIntoView(binding.messageImageView, it) }
            binding.messengerTextView.text=if(item.name==null) "ANONYMOUS" else item.name
            if(item.photoUrl!=null){
                LoadImageIntoView(binding.messengerImageView,item.photoUrl)
            }else{
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle)
            }


        }

    }

    override fun getItemViewType(position: Int): Int {
        if(position<0) return -1
        return if(options.snapshots[position].chat!=null) VIEW_TYPE_TEXT else VIEW_TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        return if(viewType == VIEW_TYPE_IMAGE){
            val view=inflater.inflate(R.layout.messages_images,parent,false)
            val binding=MessagesImagesBinding.bind(view)
            ImageChatViewHolder(binding)
        }else{
            val view=inflater.inflate(R.layout.messages_simple,parent,false)
            val binding=MessagesSimpleBinding.bind(view)
            SimpleChatViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: ChattersChat) {
        if(position<0) return
       if(options.snapshots[position].chat!=null){
           (holder as SimpleChatViewHolder).bind(model)
       }else if(options.snapshots[position].imageUrl!=null){
           (holder as ImageChatViewHolder).bind(model)
       }
    }

    private fun LoadImageIntoView(view:ImageView,url:String){
        if(url.startsWith("gs://")){
            val storageReference=Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener {uri->
                    val downloadUrl = uri.toString()
                    Glide.with(view.context)
                        .load(downloadUrl)
                        .into(view)
                }
                .addOnFailureListener{
                    Toast.makeText(view.context,"Getting download url was not successful.",Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object{
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
    }
}