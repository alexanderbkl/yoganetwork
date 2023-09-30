package com.amit.yoganet

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amit.yoganet.adapters.AdapterUsers
import com.amit.yoganet.models.ModelUsers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_users.*
import java.util.*

class UsersActivity : AppCompatActivity() {
    private var adapterUsers: AdapterUsers? = null
    private var userList: ArrayList<ModelUsers>? = null
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        setSupportActionBar(toolbar_main)


        users_recyclerView.setHasFixedSize(true)

        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)

        users_recyclerView.layoutManager = layoutManager

        userList = ArrayList()

        getAllUsers()


    }

     override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //inflating menu

        //inflating menu
        menuInflater.inflate(R.menu.menu_main, menu)
        //hide some options
        //hide some options
        menu.findItem(R.id.action_create_group).isVisible = false

        //hide addpost icon from this fragment

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).isVisible = false
        menu.findItem(R.id.action_add_participant).isVisible = false
        menu.findItem(R.id.action_groupinfo).isVisible = false

        //SearchView

        //SearchView
        val item = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView
        //SearchListener
        //SearchListener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim { it <= ' ' })) {
                    //search text contains text, search it
                    searchUsers(s)
                } else {
                    //search text empty, get all users
                    getAllUsers()
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                //called when user press any single letter
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim { it <= ' ' })) {
                    //search text contains text, search it
                    searchUsers(s)
                } else {
                    //search text empty, get all users
                    getAllUsers()
                }
                return false
            }
        })



         return super.onCreateOptionsMenu(menu)
    }

    private fun searchUsers(query: String) {
        //get current user
        val fUser = FirebaseAuth.getInstance().currentUser
        //get path of database named "Users" containing users info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        //get all data from path
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList!!.clear()
                for (dataSnapshot in snapshot.children) {
                    val modelUsers = dataSnapshot.getValue(ModelUsers::class.java)
                    /*Conditions to fulfil search:
                     * 1) User not current user
                     * 2) The user pseudonym contains text entered in SearchView (case insensitive) */


                    //get all search users except currently signed in user
                    if (dataSnapshot.getValue(ModelUsers::class.java)!!.uid != fUser!!.uid) {
                        if (modelUsers!!.pseudonym.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.purpose.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                                modelUsers.country.lowercase(Locale.getDefault())
                                        .contains(query.lowercase(Locale.getDefault()))
                        ) {
                            userList!!.add(dataSnapshot.getValue(ModelUsers::class.java)!!)
                        }
                        if (modelUsers.purpose.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.purpose.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))||
                            modelUsers.country.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))
                        ) {
                            userList!!.add(dataSnapshot.getValue(ModelUsers::class.java)!!)
                        }
                        if (modelUsers.practic.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.purpose.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.country.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))
                        ) {
                            userList!!.add(dataSnapshot.getValue(ModelUsers::class.java)!!)
                        }
                        if (modelUsers.type.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.purpose.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.country.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))
                        ) {
                            userList!!.add(dataSnapshot.getValue(ModelUsers::class.java)!!)
                        }
                        if (modelUsers.diet.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.purpose.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            modelUsers.country.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))
                        ) {
                            userList!!.add(dataSnapshot.getValue(ModelUsers::class.java)!!)
                        }
                        if (modelUsers.description != null) {
                            if (modelUsers.description.lowercase(Locale.getDefault())
                                    .contains(query.lowercase(Locale.getDefault())) ||
                                modelUsers.purpose.lowercase(Locale.getDefault())
                                    .contains(query.lowercase(Locale.getDefault())) ||
                                modelUsers.country.lowercase(Locale.getDefault())
                                    .contains(query.lowercase(Locale.getDefault()))
                            ) {
                                userList!!.add(dataSnapshot.getValue(ModelUsers::class.java)!!)
                            }
                        }
                    }
                    adapterUsers = AdapterUsers(this@UsersActivity, userList!!)
                    //refreash adapter
                    //set adapter to recycler view
                    users_recyclerView.setAdapter(adapterUsers)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getAllUsers() {
        val fUser: FirebaseUser? = firebaseAuth.currentUser

        val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        ref.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                userList!!.clear()
                for (snapshot in p0.children) {
                    val user: ModelUsers? = snapshot.getValue(
                        ModelUsers::class.java)
                    if (user != null) {
                        if (user.uid != null &&!user.getUid().equals(fUser!!.uid)) {
                            userList!!.add(user)
                        }
                    }
                }
                adapterUsers = AdapterUsers(this@UsersActivity, userList!!)
                users_recyclerView.adapter = adapterUsers
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth.currentUser
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to mainactivity
            startActivity(Intent(this@UsersActivity, MainActivity::class.java))
            this@UsersActivity.finish()
        }
    }


    /*handle menu item clicks*/
    //hmm
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id's
        val id = item.itemId
        if (id == R.id.action_logout) {
            firebaseAuth.signOut()
            checkUserStatus()
        } else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(Intent(this@UsersActivity, SettingsActivity::class.java))
        } else if (id == R.id.action_create_group) {
            //go to GroupCreateActivity
            startActivity(Intent(this@UsersActivity, GroupCreateActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}