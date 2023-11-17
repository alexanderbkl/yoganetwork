package com.amit.yoganet.fragments

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.amit.yoganet.R
import com.amit.yoganet.UsersActivity
import com.amit.yoganet.adapters.CardStackAdapter
import com.amit.yoganet.cardstack.Spot
import com.amit.yoganet.models.ModelUsers
import com.amit.yoganet.notifications.Data
import com.amit.yoganet.notifications.Sender
import com.amit.yoganet.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.yuyakaido.android.cardstackview.*
import com.amit.yoganet.databinding.FragmentUsersCardStackBinding
import org.json.JSONException
import org.json.JSONObject


// the fragment initialization parameters



/**
 * A simple [Fragment] subclass.
 * Use the [UsersCardStackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersCardStackFragment : Fragment(), CardStackListener {
    private var spots = ArrayList<Spot>()
    private lateinit var cardStackBinding: FragmentUsersCardStackBinding


    private val manager by lazy { CardStackLayoutManager(context, this) }
    //private val adapter by lazy { CardStackAdapter(createUsers())}

    fun readData(callback: UsersCallback) {
        spots = ArrayList<Spot>()

        val fUser = FirebaseAuth.getInstance().currentUser

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                spots.clear()
                for (dataSnapshot in snapshot.children) {

                    //get all users except current user
                    if (dataSnapshot.getValue(ModelUsers::class.java)!!.uid != fUser!!.uid) {
                        dataSnapshot.getValue(Spot::class.java)?.let { spots.add(it) }
                    }


                }

                //checkIsLiked
                for (spot in spots) {
                    //checkIsLiked(spot.uid, spot.id)
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    val ref2 = FirebaseDatabase.getInstance().getReference("/Users/$userId/LikedUsers").orderByChild("uid").equalTo(spot.uid)
                    ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Log.d("checkIsLiked", "onCancelled"+p0.message)
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            for (snapshote in p0.children) {
                                if (snapshot.exists()) {
                                    //remove from spots where id coincides
                                    val spote = spots.find { it.uid == spot.uid }
                                    if (spote != null) {
                                        spots.remove(spote)
                                        //spots.add(spots.size, spot)


                                    }
                                }
                            }

                            val ref3 = FirebaseDatabase.getInstance().getReference("/Users/${spot.uid}/LikedUsers").orderByChild("uid").equalTo(userId)
                            ref3.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    Log.d("checkIsLiked", "onCancelled"+p0.message)
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    for (snapshote in p0.children) {
                                        if (snapshot.exists()) {
                                            //remove from spots where id coincides
                                            val spote = spots.find { it.uid == spot.uid }
                                            if (spote != null) {
                                                for (spotf in spots) {
                                                    if (spotf.uid == spote.uid) {
                                                        spotf.isLiked = true
                                                    }
                                                }
                                                //spots.add(spots.size, spot)
                                            }}}}})

                            callback.onCallback(spots)

                        }

                    })

                }


            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    interface UsersCallback {
        fun onCallback(spot: ArrayList<Spot>)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }





    private lateinit var viewOfLayout: View

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        //skip button
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_users_card_stack, container, false)
        val cardStackView by lazy { viewOfLayout.findViewById<CardStackView>(R.id.card_stack_view) }

        fun setupButton() {

            cardStackBinding.skipButton.setOnClickListener {
                val setting = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager.setSwipeAnimationSetting(setting)
                cardStackView?.swipe()
            }

            cardStackBinding.buttonUsersList.setOnClickListener {
                //start UsersActivity.kt
                val intent = Intent(context, UsersActivity::class.java)
                startActivity(intent)
                /*
                val fragment = UsersFragment()
                val fragmentManager = activity!!.supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.content, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()*/
            }
            val rewind = viewOfLayout.findViewById<View>(R.id.rewind_button)
            if (rewind != null) {
                rewind.setOnClickListener {
                    val setting = RewindAnimationSetting.Builder()
                        .setDirection(Direction.Bottom)
                        .setDuration(Duration.Normal.duration)
                        .setInterpolator(DecelerateInterpolator())
                        .build()
                    manager.setRewindAnimationSetting(setting)
                    cardStackView?.rewind()
                }
            }

            val like = viewOfLayout.findViewById<View>(R.id.like_button)
            if (like != null) {
                like.setOnClickListener {
                    val setting = SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(Duration.Normal.duration)
                        .setInterpolator(AccelerateInterpolator())
                        .build()
                    manager.setSwipeAnimationSetting(setting)
                    cardStackView?.swipe()


                    /*  try {
                          val spot = manager.topPosition
                          val uid = spots[spot].uid
                          val user = FirebaseAuth.getInstance().currentUser
                          val userId = user?.uid

                          spots.removeAt(manager.topPosition)
                          adapter.setSpots(spots)

                          val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/likes/$userId")
                          ref.setValue(true)
                          val ref2 = FirebaseDatabase.getInstance().getReference("/users/$userId/likes/$uid")
                          ref2.setValue(true)

                      } catch (e: Exception) {
                          e.printStackTrace()
                      }*/
                }
            }
        }


        fun initialize() {
            readData(object : UsersCallback {
                override fun onCallback(spot: ArrayList<Spot>) {



                    //sort descending spots by onlineStatus
                    spot.sortBy { it.onlineStatus }
                    spot.reverse()

                    spots = spot

                    for (spote in spots) {
                        val hisUid: String = spote.uid
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        val ref3 = FirebaseDatabase.getInstance().getReference("/Users/$hisUid/LikedUsers").orderByChild("uid").equalTo(userId)
                        ref3.addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot) {
                                for (snapshot2 in p0.children) {
                                    if (snapshot2.exists()) {
                                        //remove from spots where id coincides
                                        val spotes = spots.find { it.uid == hisUid }
                                        if (spotes != null) {
                                            spots.remove(spotes)
                                            spots.add(0, spotes)


                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("checkIsLiked", "onCancelled"+error.message)
                            }
                        })
                    }





                    manager.setStackFrom(StackFrom.None)
                    manager.setVisibleCount(3)
                    manager.setTranslationInterval(8.0f)
                    manager.setScaleInterval(0.95f)
                    manager.setSwipeThreshold(0.3f)
                    manager.setMaxDegree(20.0f)
                    manager.setDirections(Direction.HORIZONTAL)
                    manager.setCanScrollHorizontal(true)
                    manager.setCanScrollVertical(true)
                    manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
                    manager.setOverlayInterpolator(LinearInterpolator())
                    try {
                        cardStackView?.layoutManager = manager
                    } catch (e: Exception) {
                        cardStackView?.layoutManager = CardStackLayoutManager(context, this@UsersCardStackFragment)
                    }
                    cardStackView?.adapter = CardStackAdapter(spot)
                    cardStackView?.itemAnimator.apply {
                        if (this is DefaultItemAnimator) {
                            supportsChangeAnimations = false
                        }
                    }
                }


            })


        }
        fun setupCardStackView() {
            initialize()
        }

        setupButton()
        setupCardStackView()

        //get drawer_layout
        val layout: FrameLayout? = cardStackBinding.drawerLayout
        val layoutParams: FrameLayout.LayoutParams = layout?.layoutParams as FrameLayout.LayoutParams
        //get height of display
        val display: Display = activity?.windowManager?.defaultDisplay!!
        val size = Point()
        display.getSize(size)
        val height: Int = size.y
        //set height of drawer_layout
        layoutParams.height = height




        return viewOfLayout
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        //print spot data
        val spot = spots[manager.topPosition-1]

        Log.d("CardStackView", "onCardSwiped: uid = ${spot.uid}, name = ${spot.pseudonym}, onlineStatus = ${spot.onlineStatus}")
        /*if (manager.topPosition == adapter.itemCount - 5) {
            paginate()
        }*/

        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        val directionString = "$direction"


        if (directionString.equals("Right")) {
            //add to liked
            var hashMap: HashMap<String, String> = HashMap()
            val userUid = FirebaseAuth.getInstance().currentUser!!.uid
            hashMap.put("uid", spot.uid)
            var ref = FirebaseDatabase.getInstance().getReference("Users/$userUid/LikedUsers/${spot.uid}")
            ref.setValue(hashMap)
                .addOnSuccessListener {
                Log.d("checkIsLiked", "onSuccess")
                    //get my pseudonym from Users database reference
                    var ref2 = FirebaseDatabase.getInstance().getReference("Users/$userUid/")
                    var myPseudonym: String? = null
                    var myPic: String? = null
                    ref2.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            myPseudonym = snapshot.child("pseudonym").getValue(String::class.java)
                            myPic = snapshot.child("image").getValue(String::class.java)

                            //send notification
                            sendNotification(
                                "$myPseudonym te ha dado like!",
                                spot.uid,
                                "$myPic",
                                userUid
                            )

                            if (spot.isLiked == false) {
                                spot.isLiked = true
                            } else {
                                //itsAMatch()
                            }

                            myPseudonym = null
                            myPic = null
                            spot.uid = null.toString()

                            //Toast.makeText(context, "Like! $myPseudonym ${spot.uid} $myPic  $userUid", Toast.LENGTH_SHORT).show()

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("checkIsLiked", "onCancelled")
                        }

                    })
                    }

                .addOnFailureListener{
                Log.d("checkIsLiked", "onFailure"+it.message) }
        }


    }

    private fun itsAMatch() {
        TODO("Not yet implemented")
    }

    private fun sendNotification(name: String, hisUid: String, myPic: String, myUid: String) {
        var allTokens = FirebaseDatabase.getInstance().getReference("Tokens")
        var query = allTokens.orderByKey().equalTo(hisUid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children) {
                    var token = ds.getValue(
                        Token::class.java
                    )
                    val data = Data(
                        "" + myUid,
                        "" + name,
                        "Someone liked you",
                        "" + hisUid,
                        "LikeNotification",
                        "" + myPic
                    )
                    assert(token != null)
                    val sender = Sender(data, token?.token ?: "")

                    //fcm json object request
                    try {

                        var senderJsonObj = JSONObject(Gson().toJson(sender))
                        var jsonObjReq: JsonObjectRequest? = com.amit.yoganet.utils.JsonObjectRequest()
                            .jsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj)
                        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
                        //add this request to queue
                        requestQueue.add(jsonObjReq)
                        jsonObjReq  = null
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(context, "eRROR!"+e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error!"+error, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun checkIsLiked(uid: String, id: Long) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid/LikedUsers").orderByChild("uid").equalTo(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("checkIsLiked", "onCancelled"+p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    if (snapshot.exists()) {
                        //remove from spots where id coincides
                        val spot = spots.find { it.uid == uid }
                        if (spot != null) {
                            spots.remove(spot)
                             spots.add(0, spot)


                        }
                    }
                }

            }

        })
    }



    /*private fun paginate() {
        val old = adapter.getSpots()
        val new = old.plus(createUsers())
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }*/
    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }
}