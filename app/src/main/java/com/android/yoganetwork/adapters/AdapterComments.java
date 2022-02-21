package com.android.yoganetwork.adapters;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.yoganetwork.ChatActivity;
import com.android.yoganetwork.CommentLikedByActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.ThereProfileActivity;
import com.android.yoganetwork.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;
    String myUid, postId;
    ImageButton moreBtn;
    ProgressDialog pd;
    boolean mProcessLike = false;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        String uid = commentList.get(position).getUid();
        String pseudonym = commentList.get(position).getuPseudonym();
        String practic = commentList.get(position).getuPractic();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String time = commentList.get(position).getTimestamp();
        String edited = commentList.get(position).getEdited();
        String cLikes = commentList.get(position).getcLikes();
        //convert timestamp to dd/mm/yy HH:mm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(time));
        String pTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
        //set the data
        holder.pseudonymTv.setText(pseudonym);
        holder.practicTv.setText(practic);
        holder.commentTv.setText(comment);
        if (Integer.parseInt(cLikes) != 0) {
            holder.cLikesTv.setText(cLikes);
        } else {
            holder.cLikesTv.setText("");
        }

        holder.cLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentLikedByActivity.class);
                intent.putExtra("cid", cid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        });

        if (edited == null) {
            holder.timeTv.setText(pTime);
        }
        else {
            String editedStr = pTime+" (editado)";
            holder.timeTv.setText(editedStr);
        }

        //set user dp
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        }
        catch (Exception e) {
            Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
        }
            //comment click listener
        moreBtn.setOnClickListener(v -> {
            //check if this comment is by currently signed in user or not

                showMoreOptions(uid, cid, v, holder.commentTv);


        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeComment(cLikes, cid);
            }
        });
        setLikes(holder.likeBtn, cid);

    }

    private void setLikes(ImageButton likeBtn, String cid) {
        //when details of post are loading, also check if current user has liked it or not
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("CommentLikes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(cid).hasChild(myUid)) {
                    //user has liked this post
                    /*To indicate that the post is liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "like" to "liked"*/
                    likeBtn.setImageResource(R.drawable.ic_liked);
                }
                else {
                    //user has noy liked this post
                    /*To indicate that the post is not liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "liked" to "like"*/
                    likeBtn.setImageResource(R.drawable.ic_like_black);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void likeComment(String cLikes, String cid) {
            /*Get total number of likes for the post, whose like button clicked
             * if currently signed in user has not liked before
             * increase value by 1, otherwise decrease value by 1*/
            mProcessLike = true;

      //  final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments").child(cid);

            //get id of the post liked
            DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("CommentLikes");
            DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).child("Comments").child(cid);

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (mProcessLike) {
                        if (snapshot.child(cid).hasChild(myUid)) {
                            //already liked, so remove like
                            commentsRef.child("cLikes").setValue(""+(Integer.parseInt(cLikes)-1));
                            likesRef.child(cid).child(myUid).removeValue();
                            mProcessLike = false;

                        }
                        else {
                            //not liked, like it
                            commentsRef.child("cLikes").setValue(""+(Integer.parseInt(cLikes)+1));
                            likesRef.child(cid).child(myUid).setValue("Liked"); //set any value
                            mProcessLike = false;
                        } } }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });




         }

    private void showMoreOptions(String uid, String cid, View v, TextView commentTv) {
            //creating popup menu currently having option Delete, we will add more options later
            PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

            //add items in menu
            if (uid.equals(myUid)) {
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Eliminar");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar");

            }
            else {
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Abrir perfil");
                popupMenu.getMenu().add(Menu.NONE, 3, 0, "Escribir mensaje");

            }


        //item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id==0) {
                //delete is clicked
                deleteComment(cid);
            }
            else   if (id==1) {

               commentUpdate(cid, v, commentTv);
            }
            else if (id ==2) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            else if (id == 3) {
                imBlockedOrNot(uid);
            }

            return false;
        });
        //show menu
        popupMenu.show();

    }

    private void commentUpdate(String cid, View v, TextView commentTv) {


        String value = commentTv.getText().toString();

        Intent intent = new Intent("custom-message");
        //            intent.putExtra("quantity",Integer.parseInt(quantity.getText().toString()));
        intent.putExtra("value",value);
        intent.putExtra("cid", cid);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);




    }

    private void imBlockedOrNot(String uid) {

        //first check if sender (current user) is blocked by receiver or not
        //Logic: if uid of the sender (current user) exists in "BlockedUsers" of receiver, then sender (current user) is blocked; otherwise not.
        //if blocked then just display a message i.e. "You're blocked by that user, can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(context, R.string.errormsg, Toast.LENGTH_SHORT).show();
                                //blocked, dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", uid);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue(); //it will delete the comment

        //now update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+ snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) -1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        //declare view from row_comments.xml

        ImageView avatarIv;
        TextView pseudonymTv, practicTv, commentTv, timeTv, cLikesTv;
        ImageButton likeBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            pseudonymTv = itemView.findViewById(R.id.pseudonymTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            practicTv = itemView.findViewById(R.id.practicTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            pd = new ProgressDialog(context);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            cLikesTv = itemView.findViewById(R.id.cLikesTv);
        }


    }

}
