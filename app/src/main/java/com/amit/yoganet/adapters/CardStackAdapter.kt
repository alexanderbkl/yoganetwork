package com.amit.yoganet.adapters

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amit.yoganet.ChattingActivity
import com.amit.yoganet.ThereProfileActivity
import com.amit.yoganet.cardstack.Spot
import com.amit.yoganet.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class CardStackAdapter(
        private var spots: List<Spot> = emptyList()

) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    private val myUid: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_spot, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spot = spots[position]
        holder.name.text = spot.pseudonym
        holder.description.text = spot.description
        holder.practic.text = spot.practic
        Glide.with(holder.image)
                .load(spot.imageFull)
                .into(holder.image)
        holder.itemView.setOnClickListener { v ->
            //show dialog
            val builder = AlertDialog.Builder(v.context)
            builder.setItems(R.array.users_array) { dialog, which ->
                if (which == 0) {
                    //profile clicked
                    //click to go to ThereProfileActivity with uid, this uid is of clicked user
                    //which will be used to show user specifi data/posts
                    val intent = Intent(v.context, ThereProfileActivity::class.java)
                    intent.putExtra("uid", spot.uid)
                    intent.putExtra("myUid", myUid)
                    v.context.startActivity(intent)
                }
                if (which == 1) {
                    //chat clicked
                    /*Click use from user list to start chatting/messaging
                                 * Start activity by putting UID of receiver
                                 * we will use that UID to identify the user we are gonna chat*/
                    //not blocked, start activity
                    val intent = Intent(v.context, ChattingActivity::class.java)
                    intent.putExtra("hisUid", spot.uid)
                    v.context.startActivity(intent)                }
            }
            builder.create().show()        }
    }

    override fun getItemCount(): Int {
        return spots.size
    }

    fun setSpots(spots: List<Spot>) {
        this.spots = spots
    }

    fun getSpots(): List<Spot> {
        return spots
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        var practic: TextView = view.findViewById(R.id.item_practic)
        var description: TextView = view.findViewById(R.id.item_description)
        var image: ImageView = view.findViewById(R.id.item_image)
    }

}
