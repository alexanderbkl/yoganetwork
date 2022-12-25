package com.amit.yoganet.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.amit.yoganet.PostDetailActivity;
import com.amit.yoganet.ThereProfileActivity;
import com.amit.yoganet.models.ModelNotification;
import com.amit.yoganet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private final Context context;
    private final ArrayList<ModelNotification> notificationsList;

    private final FirebaseAuth firebaseAuth;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       //inflate view row_notification
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);


        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position) {
        //get and set data to views

        //get data
        ModelNotification model = notificationsList.get(position);
        String pseudonym = model.getsPseudonym();
        String notification = model.getNotification();
        String image = model.getsImage();
        String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();
        String pId = model.getpId();
        //convert timestamp to dd/mm/yyyy HH:mm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        if (timestamp != null) {
            calendar.setTimeInMillis(Long.parseLong(timestamp));
        }
        String pTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();

        //we will get the name, email, image of the user of notification from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String pseudonym = ""+ds.child("pseudonym").getValue();
                            String image = ""+ds.child("image").getValue();
                            String email = ""+ds.child("email").getValue();

                            //add to model
                            model.setsPseudonym(pseudonym);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set to views
                            holder.pseudonymTv.setText(pseudonym);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
                            }
                            catch (Exception e) {
                                holder.avatarIv.setImageResource(R.drawable.ic_default_img);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

        //click notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pId.equals("like")) {
                    //start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", senderUid);
                    intent.putExtra("myUid", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                    context.startActivity(intent);
                }


            }
        });
        //long press to show!!!! delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.eliminar));
                builder.setMessage(context.getString(R.string.eliminarnotif));
                builder.setPositiveButton(context.getString(R.string.eliminar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete notification

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //deleted
                                Toast.makeText(context, context.getString(R.string.eliminado), Toast.LENGTH_SHORT).show();
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                builder.setNegativeButton(context.getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
    //holder class for views of row_notifications.xml
    class HolderNotification extends RecyclerView.ViewHolder {

        //declare views
        ImageView avatarIv;
        TextView pseudonymTv, notificationTv, timeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv = itemView.findViewById(R.id.avatarIv);
            pseudonymTv = itemView.findViewById(R.id.pseudonymTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
