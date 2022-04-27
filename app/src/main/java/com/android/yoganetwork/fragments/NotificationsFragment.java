package com.android.yoganetwork.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.yoganetwork.DashboardActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.adapters.AdapterNotification;
import com.android.yoganetwork.models.ModelNotification;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    //recyclerview
    RecyclerView notificationsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelNotification> notificationsList;
    private String extra;
    private AdapterNotification adapterNotification;
    boolean profileLikes = false;

    TabLayout tabs;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        //init recyclerview

        tabs = view.findViewById(R.id.tabs);

        tabs.addTab(tabs.newTab().setIcon(R.drawable.ic_chat_black));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.ic_heart_red));

        notificationsRv = view.findViewById(R.id.notificationsRv);

        firebaseAuth = FirebaseAuth.getInstance();

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        profileLikes = false;
                        getAllNotifications();
                        break;
                    case 1:
                        profileLikes = true;
                        getAllNotifications();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        Bundle args = getArguments();
        assert args != null;
        extra = args.getString("extra", "");

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {
        String notifications;
        if (!Objects.equals(extra, "") && Objects.equals(extra, "profileLikes")) {
            notifications = "profileLikes";
            tabs.selectTab(tabs.getTabAt(1));
            extra = "";
        }
        else if (!profileLikes) {
             notifications = "Notifications";
        } else {
             notifications = "profileLikes";
        }

        notificationsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child(notifications)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationsList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            //get data
                            ModelNotification model = ds.getValue(ModelNotification.class);

                            //add to list
                            notificationsList.add(model);
                        }
                        //adapter
                        adapterNotification = new AdapterNotification(getActivity(), notificationsList);

                        //set to recyclerview
                        notificationsRv.setAdapter(adapterNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}