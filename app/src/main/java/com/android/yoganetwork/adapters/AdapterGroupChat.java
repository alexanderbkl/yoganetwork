package com.android.yoganetwork.adapters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.renderscript.Sampler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ablanco.zoomy.Zoomy;
import com.android.yoganetwork.GroupChatImageActivity;
import com.android.yoganetwork.GroupInfoActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.ThereProfileActivity;
import com.android.yoganetwork.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChatList;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layouts
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
        return new HolderGroupChat(view);
        } else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        //get data
        ModelGroupChat model = modelGroupChatList.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();  //if text message then contain message, if image message then contain url of the image stored in firebase storage
        String senderUid = model.getSender();
        String messageType = model.getType();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM HH:mm", cal).toString();

        if (messageType.equals("text")) {
            //text message, hide imageview, show messageTv
            if (holder.imageMessageLayout != null && holder.textMessageLayout != null) {
                holder.imageMessageLayout.setVisibility(View.GONE);
                holder.textMessageLayout.setVisibility(View.VISIBLE);
                holder.messageTv.setText(message);
            }


        }
        else {
            //image message
            if (holder.imageMessageLayout != null && holder.textMessageLayout != null) {
                holder.imageMessageLayout.setVisibility(View.VISIBLE);
                holder.textMessageLayout.setVisibility(View.GONE);
            }


            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.ic_image_black);
            }


            new Zoomy.Builder((Activity) context)
                    .target(holder.messageIv)
                    .longPressListener(view -> {
                        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        long ts = System.currentTimeMillis();
                        cal.setTimeInMillis(ts * 1000L);
                        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
                        Uri uri = Uri.parse(message);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setTitle("YogaNet_"+date);
                        request.setDescription("Downloading...");
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "YogaNet group_"+date+".jpg");

                        downloadmanager.enqueue(request);
                    })
                    .register();



        }


        //set data
        holder.timeTv.setText(dateTime);

        setUserName(model, holder);
        holder.pseudonymTv.setOnClickListener(v -> {
           Intent intent = new Intent(context, ThereProfileActivity.class);
           intent.putExtra("uid", senderUid);
           context.startActivity(intent);
        });

        if (holder.pseudonymImageTv != null) {
            holder.pseudonymImageTv.setOnClickListener(v -> {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", senderUid);
                context.startActivity(intent);
            });
        }


    }

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        //get sender info from uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String pseudonym = ""+ds.child("pseudonym").getValue();
                            holder.pseudonymTv.setText(pseudonym+": ");
                            if (holder.pseudonymImageTv != null) {
                                holder.pseudonymImageTv.setText(pseudonym);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
    if (modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())) {
        return MSG_TYPE_RIGHT;
    } else {
        return  MSG_TYPE_LEFT;
     }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder{

        private TextView pseudonymTv, messageTv, timeTv, pseudonymImageTv;
        private ImageView messageIv;
        private LinearLayout textMessageLayout, imageMessageLayout;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            pseudonymTv = itemView.findViewById(R.id.pseudonymTv);
            pseudonymImageTv = itemView.findViewById(R.id.pseudonymImageTv);
            imageMessageLayout = itemView.findViewById(R.id.imageMessageLayout);
            textMessageLayout = itemView.findViewById(R.id.textMessageLayout);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }
}
