package com.android.yoganetwork.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.yoganetwork.GroupCreateActivity;
import com.android.yoganetwork.MainActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.SettingsActivity;
import com.android.yoganetwork.adapters.AdapterChatlist;
import com.android.yoganetwork.models.ModelChat;
import com.android.yoganetwork.models.ModelChatlist;
import com.android.yoganetwork.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ChatListFragment extends Fragment {


    //firebase authentication
    FirebaseAuth firebaseAuth;
    CardStackView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUsers> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;
    String chatRoomId;



    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);

        chatlistList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist");

        reference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelUsers user = ds.getValue(ModelUsers.class);
                    for (ModelChatlist chatlist: chatlistList) {
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())) {
                            userList.add(user);
                            break;
                        }
                    }

                    //sort by onlineStatus
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        userList.sort(Comparator.comparing(ModelUsers::getOnlineStatus));
                        Collections.reverse(userList);

                        //adapter
                        adapterChatlist = new AdapterChatlist(getContext(), userList);
                        //set adapter
                        recyclerView.setAdapter(adapterChatlist);
                        //set last message
                        for (int i=0; i<userList.size(); i++) {
                            lastMessage(userList.get(i).getUid());
                        }
                    }
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        char[] myCharUid = currentUser.getUid().toCharArray(); for (char ch : myCharUid)
        { sb1.append((byte) ch);
        }
        char[] hisCharUid = userId.toCharArray(); for (char ch : hisCharUid)
        { sb2.append((byte) ch);
        }

        String myStringUid = String.valueOf(sb1);
        String hisStringUid = String.valueOf(sb2);

        BigInteger myBigUid = new BigInteger(myStringUid);
        BigInteger hisBigUid = new BigInteger(hisStringUid);
        chatRoomId = String.valueOf(myBigUid.add(hisBigUid));

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomId);


        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                boolean isSeen = true;

                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);


                    if (chat==null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                    chat.getReceiver().equals(userId) &&
                            chat.getSender().equals(currentUser.getUid())) {
                        //instead of displayinh url in message show "sent photo"

                        if (chat.getType().equals("image")) {
                            if (currentUser.getUid().equals(chat.getSender())) {
                                theLastMessage = "Enviaste una foto";

                            } else {
                                theLastMessage = "Envió una foto";
                                if (!chat.isSeen()) {
                                    isSeen = false;
                                }
                            }
                        }
                        else {
                                theLastMessage = chat.getMessage();
                            if (!currentUser.getUid().equals(chat.getSender()) && !chat.isSeen()) {
                                isSeen = false;
                            }


                            }
                        if (theLastMessage.length()>100) {
                            theLastMessage = chat.getMessage()+"...";
                    }}
                }

                adapterChatlist.setLastMessageMap(userId, theLastMessage, isSeen);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null)  {
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
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

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
        } else if (id == R.id.action_create_group) {
            //go to GroupCreate activity
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}