package com.android.yoganetwork.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.yoganetwork.AddToGroupActivity;
import com.android.yoganetwork.GroupChatActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.models.ModelGroupChatList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AdapterAddToGroup extends RecyclerView.Adapter<AdapterAddToGroup.HolderAddToGroup> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;
    private String hisUid;

    public AdapterAddToGroup(Context context, ArrayList<ModelGroupChatList> groupChatLists, String hisUid) {
        this.context = context;
        this.groupChatLists = groupChatLists;
        this.hisUid = hisUid;
    }

    @NonNull
    @Override
    public HolderAddToGroup onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list, parent, false);
        return new HolderAddToGroup(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAddToGroup holder, int position) {
        //get data
        ModelGroupChatList model = groupChatLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();


        holder.pseudonymTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message and message-time
        loadLastMessage(model, holder);


        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconIv);
        } catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary);
        }

        //handle group click
        holder.itemView.setOnClickListener(v -> {
            Intent intent  = new Intent(context, AddToGroupActivity.class);
            intent.putExtra("groupId",groupId);
            intent.putExtra("hisUid", hisUid);
            context.startActivity(intent);


        });
    }


    private void loadLastMessage(ModelGroupChatList model, HolderAddToGroup holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1) //get last item (message) from that child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType = ""+ds.child("type").getValue();

                            //convert time
                            //convert timestamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM HH:mm", cal).toString();

                            if (messageType.equals("image")) {
                                holder.messageTv.setText(R.string.imagen);

                            }
                            else {
                                holder.messageTv.setText(message);
                            }

                            holder.timeTv.setText(dateTime);




                            //get info of sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()){
                                                String pseudonym = ""+ds.child("pseudonym").getValue()+": ";
                                                holder.pseudonymTv.setText(pseudonym);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //view holder class
    class HolderAddToGroup extends RecyclerView.ViewHolder{

        //ui views
        private ImageView groupIconIv;
        private TextView groupTitleTv, pseudonymTv, messageTv, timeTv;

        public HolderAddToGroup(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            pseudonymTv = itemView.findViewById(R.id.pseudonymTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageTv = itemView.findViewById(R.id.messageTv);
        }
    }
}
