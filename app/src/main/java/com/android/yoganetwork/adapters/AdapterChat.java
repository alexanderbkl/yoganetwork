package com.android.yoganetwork.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.android.yoganetwork.R;
import com.android.yoganetwork.ThereProfileActivity;
import com.android.yoganetwork.models.ModelChat;
import com.android.yoganetwork.models.ModelPost;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {


    private  static final  int MSG_TYPE_LEFT = 0;
    private  static final  int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;


    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        /*inflate layouts: row_chat_left.xml for receiver, row_chat_right.xml for sender*/
        if (i==MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
                return new MyHolder(view);
            }

        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, @SuppressLint("RecyclerView") int i) {
        //get data
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimestamp();
        String type = chatList.get(i).getType();


        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM HH:mm", cal).toString();

        if (type.equals("text")) {
            //text message
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);

        }
        else {
            //image message
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);
            Glide.with(context).load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);

            new Zoomy.Builder((Activity) context)
                    .target(myHolder.messageIv)
                    .tapListener(view -> {
                        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        /*Logic:
                         * Get timestamp of clicked message
                         * Compare the timestamp of the clicked message with all messages in Chats
                         * Where both values matches delete that message*/

                        //delete message from database
                        String msgTimeStamp = chatList.get(i).getTimestamp();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
                        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    /*If you want to allow sender o delete only his message then
                                     * compare sender value with current user's uid
                                     * if the match means its the message of sende that is trying to delete*/

                                    if (ds.child("sender").getValue().equals(myUID)) {



                                        //show delete message confirm dialog

                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle(R.string.eliminar);
                                        builder.setMessage(R.string.seguro);
                                        //delete button
                                        builder.setPositiveButton(R.string.eliminar, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                deleteMessage(i);
                                            }
                                        });
                                        //cancel delete button
                                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //dismiss dialog
                                                dialog.dismiss();
                                            }
                                        });
                                        //create and show dialog
                                        builder.create().show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                            })
                    .longPressListener(view ->{

                                DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                                long ts = System.currentTimeMillis();
                                cal.setTimeInMillis(ts * 1000L);
                                String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
                                Uri uri = Uri.parse(message);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setTitle("YogaNet:"+date);
                                request.setDescription("Downloading...");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "YogaNet chat: "+date+".jpg");

                                downloadmanager.enqueue(request);
                            }
                            )
                    .register();

            }


        //cal in am or pm
       /* Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();*/
        //on user's chat layout click go to profile
        //set data
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try {
            Glide.with(context).load(imageUrl).into(myHolder.profileIv);
        } catch (Exception e) {

        }
        //click to show delete dialog
        myHolder.messageLayout.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                /*Logic:
                 * Get timestamp of clicked message
                 * Compare the timestamp of the clicked message with all messages in Chats
                 * Where both values matches delete that message*/

                //delete message from database
                String msgTimeStamp = chatList.get(i).getTimestamp();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
                Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            /*If you want to allow sender o delete only his message then
                             * compare sender value with current user's uid
                             * if the match means its the message of sende that is trying to delete*/

                            if (ds.child("sender").getValue().equals(myUID)) {



                            }
                            //show delete message confirm dialog

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(R.string.eliminar);
                            builder.setMessage(R.string.seguro);
                            //delete button
                            builder.setPositiveButton(R.string.eliminar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteMessage(i);
                                }
                            });
                            //cancel delete button
                            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //dismiss dialog
                                    dialog.dismiss();
                                }
                            });
                            //create and show dialog
                            builder.create().show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        });


        //set seen/delivered state of essage
        if (i == chatList.size() - 1) {
            if (chatList.get(i).isSeen()) {
                myHolder.isSeenTv.setText("Visto");
            } else {
                myHolder.isSeenTv.setText("Enviado");
            }
        } else {
            myHolder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*Logic:
        * Get timestamp of clicked message
        * Compare the timestamp of the clicked message with all messages in Chats
        * Where both values matches delete that message*/

        //delete message from database
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    /*If you want to allow sender o delete only his message then
                     * compare sender value with current user's uid
                     * if the match means its the message of sende that is trying to delete*/

                    if (ds.child("sender").getValue().equals(myUID)) {
                        /*We can do one of two things here
                         * 1) Remove the message from Chats
                         * 2) Set the value of message "This message was deleted..."
                         * So do whatever you want*/
                        //1) Remove the message from Chats
                         ds.getRef().removeValue();
                        //2) Set the value of message "This message was deleted"
                  //      HashMap<String, Object> hashMap = new HashMap<>();
           //             hashMap.put("message", "Este mensaje fue eliminado...");
          //              ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Mensaje eliminado...", Toast.LENGTH_SHORT).show();
                    }
                    //    Toast.makeText(context, "Sólo puedes eliminar tus mensajes...", Toast.LENGTH_SHORT).show();

                }
                //delete image from storage
                String message = chatList.get(position).getMessage();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference contentRef;
                try {
                    contentRef = storage.getReferenceFromUrl(message);
                    contentRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error al eliminar imagen: "+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error al eliminar mensaje: "+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout; //for click listener to show delete
      //  LinearLayout userLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
          //  userLayout = itemView.findViewById(R.id.userLayout);

        }
    }
}
