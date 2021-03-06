package com.example.socialproject.ViewController.ManHinhUser

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialproject.Model.Like
import com.example.socialproject.Model.Status
import com.example.socialproject.Model.User
import com.example.socialproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_status_item_row.view.*

class ManHinhUserAdapter(private val statusList: List<Status>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEMS = 1
        val TAG = "ManHinhUser"

        val CURRENT_USER = "CURRENT_USER"
    }

    // Tài khoản đang đăng nhập
    var currentUser: User? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEMS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_status_item_row, parent, false)
                statusViewHolder(view)
            }

            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_header_profile, parent, false)
                return userHeaderViewHolder(view)
            }

            else -> throw IllegalAccessException("Invalidate view type")
        }
    }

    override fun getItemCount(): Int {
        return statusList.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is userHeaderViewHolder) {

            // Lấy data từ Firebase
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")



            ref.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java)
                    //Log.d(TAG, "Data current user: ${currentUser}")

                    holder.username?.text = currentUser?.username
                    Picasso.get().load(currentUser?.profileImageUrl).into(holder.profileImage)

                    holder.following?.text = currentUser?.following.toString()
                    holder.follower?.text = currentUser?.follower.toString()
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })

        }
        else
        if (holder is statusViewHolder) {

            // Lấy data từ Firebase
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java)
                    //Log.d(TAG, "Data current user: ${currentUser}")

                    holder.username?.text = currentUser?.username
                    Picasso.get().load(currentUser?.profileImageUrl).into(holder.userAvatar)
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })

            holder.statusTextView?.text = statusList[position - 1].caption
            holder.statusLike?.text = statusList[position - 1].like.toString()
            Picasso.get().load(statusList[position - 1].imageUrl).into(holder.statusImage)

            var isLike = false
            var currentStatusLike = statusList[position - 1].like

            // Fetch Current Login User Like to Status in HeartIcon
            val likeFromToRef = FirebaseDatabase.getInstance().getReference("Like/${uid}/${uid}")

            likeFromToRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    var child = snapshot.child(statusList[position - 1].id)
                    if (child.value.toString() != "null") {
                        holder.likeButton.setBackgroundResource(R.drawable.selected_heart)
                        isLike = true
                    }

                    // Like Button Handle
                    holder.likeButton.setOnClickListener {
                        Log.d(TAG, isLike.toString())
                        if (!isLike) {
                            isLike = true
                            Log.d(TAG, "Tien hanh like status ${statusList[position - 1].id} của ${uid}")

                            val ref = FirebaseDatabase.getInstance().getReference("Like/${uid}/${uid}/${statusList[position - 1].id}")

                            val likeStatus = Like(uid.toString(), uid.toString(), statusList[position - 1].id)

                            ref.setValue(likeStatus)

                            // Increase Like Count in Firebase
                            val likeRef = FirebaseDatabase.getInstance().getReference("Status/${uid}/${statusList[position - 1].id}/like")

                            likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val tmp = p0.value.toString()

                                    var value = tmp.toInt()

                                    likeRef.setValue(value + 1)
                                }

                            })

                            currentStatusLike += 1
                            holder.statusLike?.text = currentStatusLike.toString()
                            holder.likeButton.setBackgroundResource(R.drawable.selected_heart)
                        }
                        else {
                            isLike = false
                            Log.d("ManHinhHome", "Tien hanh HUY like status ${statusList[position - 1].id} của ${uid}")

                            val ref = FirebaseDatabase.getInstance().getReference("Like/${currentUser!!.uid}/${uid}/${statusList[position - 1].id}")

                            ref.removeValue()


                            // Decrease Like Count in Firebase
                            val likeRef = FirebaseDatabase.getInstance().getReference("Status/${uid}/${statusList[position - 1].id}/like")

                            likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val tmp = p0.value.toString()

                                    var value = tmp.toInt()

                                    likeRef.setValue(value - 1)
                                }

                            })

                            currentStatusLike -= 1
                            holder.statusLike?.text = currentStatusLike.toString()
                            holder.likeButton.setBackgroundResource(R.drawable.unselected_heart)

                        }

                    }
                }

            })

        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER
        }

        return TYPE_ITEMS
    }

}

//-----------------------------------
class userHeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    val following = itemView.findViewById(R.id.profile_following_textview) as? TextView
    val follower = itemView.findViewById(R.id.profile_follower_textview) as? TextView
    val username = itemView.findViewById(R.id.profile_user_name) as? TextView
    val profileImage = itemView.findViewById(R.id.profile_profile_image) as? ImageView
    val editProfileButton = itemView.findViewById(R.id.profile_follow_button) as Button
    val uploadStatusButton = itemView.findViewById(R.id.user_upload_status) as Button

    init {
        editProfileButton.setOnClickListener {
            Log.d("ManHinhUser", "Tien Hanh edit profile")
            val intent = Intent(itemView.context, EditProfile::class.java)
            itemView.context.startActivity(intent)
        }

        uploadStatusButton.setOnClickListener {
            Log.d("ManHinhUser", "Tien hanh dang status")
            val intent = Intent(itemView.context, ManHinhPostStatus::class.java)
            itemView.context.startActivity(intent)
        }
    }

}

class statusViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
    val statusImage = itemView.findViewById(R.id.status_image_view) as? ImageView
    val statusTextView = itemView.findViewById(R.id.status_status_text_view) as? TextView
    val userAvatar = itemView.findViewById(R.id.status_profile_imageview) as? ImageView
    val username = itemView.findViewById(R.id.status_user_name) as? TextView

    val statusLike = itemView.findViewById(R.id.status_like_textview) as? TextView

    val likeButton = itemView.findViewById(R.id.status_like_button) as ImageView

}