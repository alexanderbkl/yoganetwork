package com.amit.yoganet.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amit.yoganet.ChattingActivity;
import com.amit.yoganet.models.ModelUsers;
import com.amit.yoganet.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUsers> userList; //get user info
    private final HashMap<String, String> lastMessageMap;
    private boolean isSeen;
    private boolean lastMessageExecuted = false;
    private String userId;

    //constructor
    public AdapterChatlist(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUid = userList.get(position).getUid();
        String pseudonym = userList.get(position).getPseudonym();
        String userImage = userList.get(position).getImage();
        String lastMessage = lastMessageMap.get(hisUid);
        String isSeen = lastMessageMap.get(hisUid+"isSeen");

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //set data
        holder.pseudonymTv.setText(pseudonym);
        if (lastMessage == null || lastMessage.equals("default")) {
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            if (lastMessage.contains("https://")) {
                lastMessage = "Audio";
            }
                holder.lastMessageTv.setText(lastMessage);
            if (hisUid.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                //set text color of lastMessageTv to blue
                holder.lastMessageTv.setTextColor(context.getResources().getColor(R.color.colorBlue));
            }
        }
        try {
            Glide.with(context).load(userImage).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        } catch (Exception e) {
            Glide.with(context).load(R.drawable.ic_default_img).into(holder.profileIv);
        }
        //set online status of other users in chatlist
        if (userList.get(position).getOnlineStatus().equals("Online")) {
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        } else {
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        if (!Objects.equals(isSeen, "true") && !hisUid.equals(myUid) ) {
            holder.isSeenStatusIv.setVisibility(View.VISIBLE);
            holder.isSeenStatusIv.setImageResource(R.drawable.circle_seen);
        } else {
            holder.isSeenStatusIv.setVisibility(View.GONE);
        }
        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChattingActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage, boolean isSeen) {
      //  this.isSeen = isSeen;
        this.isSeen = isSeen;
        lastMessageMap.put(userId, lastMessage);
        lastMessageMap.put(userId+"isSeen", String.valueOf(isSeen));
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public int getItemCount() {
        return userList.size(); //size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder {
        //views of row_chatlist.xml
        ImageView profileIv, onlineStatusIv, isSeenStatusIv;
        TextView pseudonymTv, lastMessageTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            isSeenStatusIv = itemView.findViewById(R.id.isSeenStatusIv);
            pseudonymTv = itemView.findViewById(R.id.pseudonymTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }

        }
    }

