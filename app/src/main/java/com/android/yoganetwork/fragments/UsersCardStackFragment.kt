package com.android.yoganetwork.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.android.yoganetwork.R
import com.android.yoganetwork.adapters.CardStackAdapter
import com.android.yoganetwork.cardstack.Spot
import com.android.yoganetwork.cardstack.SpotDiffCallback
import com.android.yoganetwork.models.ModelUsers
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.fragment_users_card_stack.view.*
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [UsersCardStackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersCardStackFragment : Fragment(), CardStackListener {
    private val manager by lazy { CardStackLayoutManager(context, this) }
    private val adapter by lazy { CardStackAdapter(createUsers()) }

    private fun createUsers(): List<Spot> {

        val spots = ArrayList<Spot>()

        //get current user
        val fUser = FirebaseAuth.getInstance().currentUser
        //get path of database named "Users" containing users info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        //get all data from path
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                spots.clear()
                for (dataSnapshot in snapshot.children) {

                    //get all users except
                    if (dataSnapshot.getValue(ModelUsers::class.java)!!.uid != fUser!!.uid) {
                        dataSnapshot.getValue(Spot::class.java)?.let { spots.add(it) }
                    }


                }

                //print spots
                Log.d("suka1", spots.toString())

                //sort descending spots by onlineStatus
                spots.sortBy { it.onlineStatus }
                spots.reverse();
                Log.d("suka2", spots.toString())


            }

            override fun onCancelled(error: DatabaseError) {}
        })


        //print sorted spots
        spots.add(Spot(
            pseudonym = "Yasaka Shrine",
            description = "Kyoto",
            image = "https://source.unsplash.com/Xq1ntWruZQI/600x800",
            practic = "de la buena",
            type = "yogui",
            uid = "289735987f",
            onlineStatus = "123525"
        ))

        return spots
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }





    private lateinit var viewOfLayout: View

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        //skip button
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_users_card_stack, container, false)
        val cardStackView by lazy { viewOfLayout.findViewById<CardStackView>(R.id.card_stack_view) }

        fun setupButton() {

            viewOfLayout.skip_button.setOnClickListener {
                val setting = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager.setSwipeAnimationSetting(setting)
                cardStackView?.swipe()
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
                }
            }
        }


        fun initialize() {
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
            cardStackView?.layoutManager = manager
            cardStackView?.adapter = adapter
            cardStackView?.itemAnimator.apply {
                if (this is DefaultItemAnimator) {
                    supportsChangeAnimations = false
                }
            }
        }
        fun setupCardStackView() {
            initialize()
        }

        setupButton()
        setupCardStackView()



        return viewOfLayout
    }

    companion object {
        
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UsersCardStackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UsersCardStackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (manager.topPosition == adapter.itemCount - 5) {
            paginate()
        }
    }

    private fun paginate() {
        val old = adapter.getSpots()
        val new = old.plus(createUsers())
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }
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