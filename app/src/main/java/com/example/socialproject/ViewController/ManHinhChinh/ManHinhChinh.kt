package com.example.socialproject.ViewController.ManHinhChinh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.socialproject.R
import com.example.socialproject.Helper.VerticalSpaceItemDecoration
import com.example.socialproject.Model.Status
import com.example.socialproject.Model.User
import com.example.socialproject.View.StatusView.StatusItem
import com.example.socialproject.ViewController.ManHinhCoSo.ManHinhBase
import com.example.socialproject.ViewController.ManHinhTinNhan.ManHinhSearchAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class ManHinhChinh : Fragment() {

    companion object {
        val TAG = "ManHinhHome"
    }

    var rec: RecyclerView? = null
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_man_hinh_chinh, container, false)
        rec = view.findViewById(R.id.home_recycler_view) as RecyclerView

        adapter.clear()

        rec!!.setHasFixedSize(true)
        rec!!.addItemDecoration(
            VerticalSpaceItemDecoration(
                10
            )
        )

        // Tiến hành search Account
        val button = view.findViewById(R.id.home_search_button) as Button
        button.setOnClickListener {
            Log.d(TAG, "Tien hanh search account")

            val intent = Intent(this.context, ManHinhSearchAccount::class.java)
            intent.putExtra("Caller", "SearchUser")
            startActivity(intent)
        }

        rec!!.adapter = adapter

        fetchFollowing()

        return view
    }

    fun fetchFollowing() {
        var currentUser = ManHinhBase.currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("Following/$currentUser")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    //Log.d(TAG, " Following:  ${it.key.toString()}")

                    fetchStatusData(it.key.toString())

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    fun fetchStatusData(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Status/$uid")

        val userRef = FirebaseDatabase.getInstance().getReference("Users/$uid")


        // Fetch User
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                // Fetch Status
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {

                        snapshot.children.forEach {
                            val status = it.getValue(Status::class.java)

//                            Log.d(TAG, it.key.toString())

                            if (status != null)
                                adapter.add(StatusItem(status, user))

                            //Log.d(TAG, "${user?.username} has ${status?.caption}")
                        }
                    }
                })
            }
        })
    }
}


