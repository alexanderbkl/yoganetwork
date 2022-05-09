package com.android.yoganetwork.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ablanco.zoomy.Zoomy;
import com.android.yoganetwork.*;
import com.android.yoganetwork.R;
import com.android.yoganetwork.models.ModelGroupChat;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    FirebaseDatabase firebaseDatabase;

    private final Context context;
    private ArrayList<ModelGroupChat> modelGroupChatList;

    private final FirebaseAuth firebaseAuth;
    private final String groupId;
    private String myUID;


    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList, String groupId) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;
        this.groupId = groupId;
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
    public void onBindViewHolder(@NonNull HolderGroupChat holder, @SuppressLint("RecyclerView") int position) {
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

        firebaseDatabase = FirebaseDatabase.getInstance();
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (messageType.equals("text")) {
            //text message, hide imageview, show messageTv
                holder.audioMessageLayout.setVisibility(View.GONE);
                holder.imageMessageLayout.setVisibility(View.GONE);
                holder.textMessageLayout.setVisibility(View.VISIBLE);
                holder.messageTv.setText(message);

                //onLongPress copy to clipboard
                holder.textMessageLayout.setOnLongClickListener(v -> {

                    /*Logic:
                     * Get timestamp of clicked message
                     * Compare the timestamp of the clicked message with all messages in Chats
                     * Where both values matches delete that message*/
                    //delete message from database
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
                    Query query = ref.orderByChild("timestamp").equalTo(timestamp);
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

                                            deleteMessage(position);
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
                //on click delete message dialog
                holder.textMessageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("message", message);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                });



        }
        else if (messageType.equals("image")) {
            //image message
                holder.imageMessageLayout.setVisibility(View.VISIBLE);
                holder.textMessageLayout.setVisibility(View.GONE);
                holder.audioMessageLayout.setVisibility(View.GONE);


            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.ic_image_black);
            }


            new Zoomy.Builder((Activity) context)
                    .target(holder.messageIv)
                    .tapListener(view -> {
                        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        long ts = System.currentTimeMillis();
                        cal.setTimeInMillis(ts * 1000L);
                        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
                        Uri uri = Uri.parse(message);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setTitle("YogaNet_" + date);
                        request.setDescription("Downloading...");
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "YogaNet group_" + date + ".jpg");

                        downloadmanager.enqueue(request);
                    })
                    .longPressListener(view -> {

                        /*Logic:
                         * Get timestamp of clicked message
                         * Compare the timestamp of the clicked message with all messages in Chats
                         * Where both values matches delete that message*/
                        //delete message from database
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
                        Query query = ref.orderByChild("timestamp").equalTo(timestamp);
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

                                                deleteMessage(position);
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
                    .register();



        }

        else if (messageType.equals("audio")) {
            holder.imageMessageLayout.setVisibility(View.GONE);
            holder.textMessageLayout.setVisibility(View.GONE);
            holder.audioMessageLayout.setVisibility(View.VISIBLE);
            holder.audioPlayer.setControllerHideOnTouch(false);
            holder.messageTv.setVisibility(View.GONE);
            SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(context)
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(5000)
                    .build();
            holder.audioPlayer.setPlayer(simpleExoPlayer);
            holder.audioPlayer.setKeepScreenOn(true);
            holder.audioPlayer.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 40);
                }
            });

            holder.audioPlayer.setClipToOutline(true);
            Uri audioSource = Uri.parse(message);
            MediaItem mediaItem = MediaItem.fromUri(audioSource);
            simpleExoPlayer.setMediaItem(mediaItem);
            simpleExoPlayer.prepare();
            simpleExoPlayer.setPlayWhenReady(false);

            if (modelGroupChatList.get(position).getSender().equals(myUID)) {
                //delete on long click
                holder.audioPlayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.eliminar);
                        builder.setMessage(R.string.seguro);
                        //delete button
                        builder.setPositiveButton(R.string.eliminar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteMessage(position);
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
                });

            }


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
        } else if (holder.pseudonymAudioTv != null) {
            holder.pseudonymAudioTv.setOnClickListener(v -> {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", senderUid);
                context.startActivity(intent);
            });
        }


    }

    private void deleteMessage(int position) {
        String myUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        /*Logic:
         * Get timestamp of clicked message
         * Compare the timestamp of the clicked message with all messages in Chats
         * Where both values matches delete that message*/

        //delete message from database
        String msgTimeStamp = modelGroupChatList.get(position).getTimestamp();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Messages");
        Query query = ref.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    /*If you want to allow sender o delete only his message then
                     * compare sender value with current user's uid
                     * if the match means its the message of sende that is trying to delete*/

                    if (Objects.equals(ds.child("sender").getValue(), myUID)) {
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
                String message = modelGroupChatList.get(position).getMessage();
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

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        //get sender info from uid in model
        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String pseudonym = ""+ds.child("pseudonym").getValue();
                            holder.pseudonymTv.setText(pseudonym+": ");
                            if (holder.pseudonymImageTv != null) {
                                holder.pseudonymImageTv.setText(pseudonym);
                            } else if (holder.pseudonymAudioTv != null) {
                                holder.pseudonymAudioTv.setText(pseudonym);
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

    public void setChatList(ArrayList<ModelGroupChat> groupChatList) {
        this.modelGroupChatList = groupChatList;
    }

    class HolderGroupChat extends RecyclerView.ViewHolder{

        private TextView pseudonymTv, messageTv, timeTv, pseudonymImageTv, pseudonymAudioTv;
        private ImageView messageIv;
        private LinearLayout textMessageLayout, imageMessageLayout, audioMessageLayout;
        PlayerView audioPlayer;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            pseudonymTv = itemView.findViewById(R.id.pseudonymTv);
            pseudonymImageTv = itemView.findViewById(R.id.pseudonymImageTv);
            pseudonymAudioTv = itemView.findViewById(R.id.pseudonymAudioTv);
            imageMessageLayout = itemView.findViewById(R.id.imageMessageLayout);
            textMessageLayout = itemView.findViewById(R.id.textMessageLayout);
            audioMessageLayout = itemView.findViewById(R.id.audioMessageLayout);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            audioPlayer = itemView.findViewById(R.id.audioPlayer);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }


}
