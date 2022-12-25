package com.amit.yoganet.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.ablanco.zoomy.Zoomy;
import com.amit.yoganet.models.ModelChat;
import com.amit.yoganet.R;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {


    private  static final  int MSG_TYPE_LEFT = 0;
    private  static final  int MSG_TYPE_RIGHT = 1;
    private final Context context;
    private ArrayList<ModelChat> chatList;
    private final String imageUrl;
    private String chatRoomId;


    public AdapterChat(Context context, ArrayList<ModelChat> chatList, String imageUrl) {
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
        ModelChat chat = chatList.get(i);
        String message = chat.getMessage();
        String timeStamp = chat.getTimestamp();
        String type = chat.getType();


        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM HH:mm", cal).toString();
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        char[] myCharUid = myUID.toCharArray(); for (char ch : myCharUid)
        { sb1.append((byte) ch);
        }
        char[] hisCharUid = chatList.get(i).getReceiver().toCharArray(); for (char ch : hisCharUid)
        { sb2.append((byte) ch);
        }

        String myStringUid = String.valueOf(sb1);
        String hisStringUid = String.valueOf(sb2);

        BigInteger myBigUid = new BigInteger(myStringUid);
        BigInteger hisBigUid = new BigInteger(hisStringUid);
        chatRoomId = String.valueOf(myBigUid.add(hisBigUid));

        if (type.equals("text")) {
            //text message
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);
            myHolder.audioPlayer.setVisibility(View.GONE);




            myHolder.messageLayout.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("message", message);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            });

        }
        else if (type.equals("image")) {
            myHolder.audioPlayer.setVisibility(View.GONE);
            //image message
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);
            Glide.with(context).load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);

            new Zoomy.Builder((Activity) context)
                    .target(myHolder.messageIv)
                    .tapListener(view -> {

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

                    })
                    .longPressListener(view ->{

                                /*Logic:
                                 * Get timestamp of clicked message
                                 * Compare the timestamp of the clicked message with all messages in Chats
                                 * Where both values matches delete that message*/

                                //delete message from database
                                String msgTimeStamp = chatList.get(i).getTimestamp();
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomId);
                                Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            /*If you want to allow sender o delete only his message then
                                             * compare sender value with current user's uid
                                             * if the match means its the message of sende that is trying to delete*/

                                            if (Objects.equals(ds.child("sender").getValue(), myUID)) {



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
                                });   }
                            )
                    .register();

            }
        else if (type.equals("audio")) {

            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.audioPlayer.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);
            myHolder.audioPlayer.setControllerHideOnTouch(false);

            myHolder.audioPlayer.setVisibility(View.VISIBLE);
            myHolder.messageTv.setVisibility(View.GONE);
            SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(context)
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(5000)
                    .build();
            myHolder.audioPlayer.setPlayer(simpleExoPlayer);
            myHolder.audioPlayer.setKeepScreenOn(true);
            myHolder.audioPlayer.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 40);
                }
            });

            myHolder.audioPlayer.setClipToOutline(true);
            Uri audioSource = Uri.parse(message);
            MediaItem mediaItem = MediaItem.fromUri(audioSource);
            simpleExoPlayer.setMediaItem(mediaItem);
            simpleExoPlayer.prepare();
            simpleExoPlayer.setPlayWhenReady(false);

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
            try {
                Glide.with(context).load(R.drawable.ic_default_img).into(myHolder.profileIv);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        //click to show delete dialog




        if (type.equals("audio") && chatList.get(i).getSender().equals(myUID)) {
            myHolder.mRelativeLayout.setOnClickListener(v -> {
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


            });

        }




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
        myHolder.messageLayout.setOnLongClickListener (v -> {
            String myUID1 = FirebaseAuth.getInstance().getCurrentUser().getUid();

            /*Logic:
             * Get timestamp of clicked message
             * Compare the timestamp of the clicked message with all messages in Chats
             * Where both values matches delete that message*/

            //delete message from database
            String msgTimeStamp = chatList.get(i).getTimestamp();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomId);
            Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        /*If you want to allow sender o delete only his message then
                         * compare sender value with current user's uid
                         * if the match means its the message of sende that is trying to delete*/

                        if (Objects.equals(ds.child("sender").getValue(), myUID1)) {




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

            return true;
        });
    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*Logic:
        * Get timestamp of clicked message
        * Compare the timestamp of the clicked message with all messages in Chats
        * Where both values matches delete that message*/

        //delete message from database
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(chatRoomId);
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    contentRef.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(context, "Error al eliminar mensaje: "+error, Toast.LENGTH_SHORT).show();
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
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    public void setChatList(ArrayList<ModelChat> chatList) {
        this.chatList = chatList;
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        //views
        private ImageView profileIv, messageIv;
        private TextView messageTv, timeTv, isSeenTv;
        private LinearLayout messageLayout;//for click listener to show delete
        private PlayerView audioPlayer;
        private RelativeLayout mRelativeLayout;
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
            audioPlayer = itemView.findViewById(R.id.audioPlayer);
            mRelativeLayout = itemView.findViewById(R.id.mRelativeLayout);

        }
    }
}
