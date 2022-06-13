package com.android.yoganetwork.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;




import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.yoganetwork.*;
import com.android.yoganetwork.adapters.AdapterUsers;
import com.android.yoganetwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdapterUsers adapterUsers;
    private List<ModelUsers> userList;
    //firebase authentication
    private FirebaseAuth firebaseAuth;
    public UsersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        //init recyclerview..
     recyclerView = view.findViewById(R.id.users_recyclerView);
     //set it's properties
        recyclerView.setHasFixedSize(true);
        Button cardStackBtn = view.findViewById(R.id.cardStackBtn);
        cardStackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CardStackActivity.class));
            }
                                        });
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        //init user list
        userList = new ArrayList<>();
        //getAll users
        getAllUsers();
        return view;
        

    }

    private void getAllUsers() {
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {

                    //get all users except
                    if (!dataSnapshot.getValue(ModelUsers.class).getUid().equals(fUser.getUid())) {
                        userList.add(dataSnapshot.getValue(ModelUsers.class));
                    }
                    adapterUsers = new AdapterUsers(getActivity(), userList);

                    adapterUsers.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.ALLOW);
                    //set adapter to recycler_view

                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void searchUsers(String query) {
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ModelUsers modelUsers = dataSnapshot.getValue(ModelUsers.class);
                    /*Conditions to fulfil search:
                     * 1) User not current user
                     * 2) The user pseudonym or realname contains text entered in SearchView (case insensitive) */


                    //get all search users except currently signed in user
                    if (!dataSnapshot.getValue(ModelUsers.class).getUid().equals(fUser.getUid())) {

                        if (modelUsers.getPseudonym().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getRealname().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(dataSnapshot.getValue(ModelUsers.class));
                    }
                        if (modelUsers.getRealname().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getRealname().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(dataSnapshot.getValue(ModelUsers.class));
                        }
                        if (modelUsers.getPractic().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getRealname().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(dataSnapshot.getValue(ModelUsers.class));
                        }
                        if (modelUsers.getType().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getRealname().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(dataSnapshot.getValue(ModelUsers.class));
                        }
                        if (modelUsers.getDiet().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getRealname().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(dataSnapshot.getValue(ModelUsers.class));
                        }
                    }
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //refreash adapter
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        }
        else {
            //user not signed in, go to mainactivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        //hide some options
        menu.findItem(R.id.action_create_group).setVisible(false);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //SearchListener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())) {
            //search text contains text, search it
                    searchUsers(s);
                }
                else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called when user press any single letter
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it
                    searchUsers(s);
                }
                else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });



        super.onCreateOptionsMenu(menu, inflater);
    }

    /*handle menu item clicks*/

    @Override
    //hmm
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id's
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        else if (id == R.id.action_create_group){
            //go to GroupCreateActivity
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}