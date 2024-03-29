package com.amit.yoganet.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorSpace;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.yoganet.AddPostActivity;
import com.amit.yoganet.MainActivity;
import com.amit.yoganet.SettingsActivity;
import com.amit.yoganet.adapters.AdapterPost;
import com.amit.yoganet.models.ModelPost;
import com.amit.yoganet.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;


public class PostsFragment extends Fragment {

    //firebase authentication
    private FirebaseAuth firebaseAuth;
    private RecyclerView recycler_view;
    private List<ModelPost> postList;
    private AdapterPost adapterPosts;
    private ShimmerFrameLayout shimmerFrameLayout;
    private String myUid, pAudio;
    private boolean isBlocked = false;



    public PostsFragment() {
        // Required empty public constructor
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

            // Inflate the layout for this fragment
            //init
            firebaseAuth = FirebaseAuth.getInstance();
            //recycler_view and its properties

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            myUid = "0";

            //go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            if (getActivity() != null) {
                getActivity().finish();

            }

        }
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            //show  newest post first, for this load from last
        recycler_view = view.findViewById(R.id.recycler_view);


            //init post list
            postList = new ArrayList<>();

            loadPosts();

        layoutManager.setInitialPrefetchItemCount(20);
        //set layout to recycler_view
        recycler_view.setLayoutManager(layoutManager);

        SharedPreferences prefs = getActivity().getSharedPreferences("DeviceToken", MODE_PRIVATE);
        pAudio = prefs.getString("pAudio", null); // get it here

        return view;

    }




    private void loadPosts() {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                Map<String, ModelPost> latestPostsMap = new HashMap<>();

                for (DataSnapshot ds: snapshot.getChildren()) {

                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    //get the post
                    //get the date of the post
                    assert modelPost != null;
                    String userId = modelPost.getUid();

                    if (!latestPostsMap.containsKey(userId) || Long.parseLong(modelPost.getpId()) > Long.parseLong(latestPostsMap.get(userId).getpId())) {
                        latestPostsMap.put(userId, modelPost);
                    }

                }

                postList = new ArrayList<>(latestPostsMap.values());




                //the iterator.remove() gives IllegalStateException, how to fix it?
                //to fix it, use the iterator.remove() in the main thread

                //sort the list by hot score


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    postList.sort((o1, o2) -> (int) (Double.parseDouble(o2.getHotScore()) - Double.parseDouble(o1.getHotScore())));
                    shimmerFrameLayout.setVisibility(View.GONE);
                }



                //get all blocked useris of current user

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(myUid).child("BlockedUsers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Set<String> blockedUids = new HashSet<>();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            blockedUids.add("" + ds.getKey());
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            postList = postList.stream()
                                    .filter(post -> !blockedUids.contains(post.getUid()))
                                    .collect(Collectors.toList());
                        } else {
                            Iterator<ModelPost> iterator = postList.iterator();
                            while (iterator.hasNext()) {
                                ModelPost post = iterator.next();
                                if (blockedUids.contains(post.getUid())) {
                                    iterator.remove();
                                }
                            }
                        }

                        //adapter
                        adapterPosts = new AdapterPost(getActivity(), postList, recycler_view);



                        adapterPosts.setHasStableIds(true);

                        adapterPosts.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.ALLOW);
                        //set adapter to recycler_view
                        recycler_view.setHasFixedSize(true);
                        recycler_view.setItemAnimator(null);

                        //set max recycled views

                        recycler_view.setAdapter(adapterPosts);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });








            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
            }
        });



    }



    interface Callback {
        void onResult(boolean result);
    }

    /*   private long hot(long postDate, long likes, long currentDate) {
           Calendar cal2 = Calendar.getInstance();
           cal2.setTimeZone(TimeZone.getTimeZone("GMT"));
           int year = cal2.get(Calendar.YEAR);
           int yearNumber = (Math.abs(year) % 10)*10000;

           return (((currentDate - postDate) / 86400000) * 1000 - score(likes) - yearNumber * 10000)*(-1);
       }


       //return score sorted by likes and dislikes
       private long score(long likes) {
           return likes * 1000;
       }*/
    private void searchPosts(String searchQuery) {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase().toLowerCase())) {
                        postList.add(modelPost);

                    }
                    //adapter
                    adapterPosts = new AdapterPost(getActivity(), postList, recycler_view);
                    adapterPosts.setHasStableIds(true);
                    //set adapter to recyclerview
                    recycler_view.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
                Toast.makeText(getActivity(), "err"+error.getMessage(), Toast.LENGTH_LONG).show();
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
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user presses search button
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                }
                else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText);

                }
                else {
                    loadPosts();

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
        else if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));

        }
        else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


}

//why setStateRestorationPolicy doesn't work in this code?

//it doesn't work because the fragment is not attached to the activity