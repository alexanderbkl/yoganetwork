package com.amit.yoganet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.amit.yoganet.R;
import com.amit.yoganet.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ReportUtils {

    Context context;
    String uid, pId, myUid;
    boolean isBlocked = false;

    public void showUserReportDialog(Context context, String uid, String myUid) {
        this.context = context;
        this.uid = uid;
        this.myUid = myUid;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.reportuser));
        builder.setMessage(context.getString(R.string.reportpostmsg));

        // Set other properties of the dialog (optional)


        // Create a layout for the custom view
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create a text field for the prompt
        final EditText reoprtField = new EditText(context);
        reoprtField.setHint("Enter reason");
        layout.addView(reoprtField);

        // Set the custom view as the content of the dialog
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reportText = reoprtField.getText().toString().trim();
                if (TextUtils.isEmpty(reportText)) {
                    Toast.makeText(context, context.getString(R.string.report_reason), Toast.LENGTH_SHORT).show();
                    return;
                }
                //report user
                reportPost(reportText);
                //done
                dialog.dismiss();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });


        // Create the dialog
        AlertDialog dialog = builder.create();
        // Show the dialog
        dialog.show();



    }

    public void showPostReportDialog(Context context, String uid, String pId, String myUid) {
        this.context = context;
        this.uid = uid;
        this.pId = pId;
        this.myUid = myUid;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.reportpost));
        builder.setMessage(context.getString(R.string.reportpostmsg));

        // Set other properties of the dialog (optional)


        // Create a layout for the custom view
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create a text field for the prompt
        final EditText reoprtField = new EditText(context);
        reoprtField.setHint("Enter reason");
        layout.addView(reoprtField);

        // Set the custom view as the content of the dialog
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reportText = reoprtField.getText().toString().trim();
                if (TextUtils.isEmpty(reportText)) {
                    Toast.makeText(context, context.getString(R.string.report_reason), Toast.LENGTH_SHORT).show();
                    return;
                }
                //report user
                reportPost(reportText);
                //done
                dialog.dismiss();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });


        // Create the dialog
        AlertDialog dialog = builder.create();
        // Show the dialog
        dialog.show();



    }

    public void reportPost(String reportText) {
//get timestamp
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        String timestamp = dateFormat.format(calendar.getTime());

        //setup data to put in report user node
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("timestamp", timestamp);
        hashMap.put("reportText", reportText);
        if (pId != null) {
            hashMap.put("pId", pId);
        }
        hashMap.put("reporterUid", myUid);

        //put data in report user node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportedUsers");
        ref.child(uid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, context.getString(R.string.success), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, context.getString(R.string.fallida)+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void blockUser(Context context, String hisUid, String myUid) {
        this.context = context;
        this.uid = hisUid;
        this.myUid = myUid;

        //block the user, by adding uid to current user's "BlockedUsers" node

        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(uid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked successfully
                        Toast.makeText(context, context.getString(R.string.bloqueado), Toast.LENGTH_SHORT).show();

                        //stop current activity
                        ((Activity)context).finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to block
                        Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void unBlockUser(Context context, String hisUid, String myUid) {
        this.context = context;
        this.uid = hisUid;
        this.myUid = myUid;

        //unblock the user, by removing uid from current user's "BlockedUsers" node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                //remove blocked user data from current user's BlockedUsers list
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //unblocked successfully
                                                Toast.makeText(context, context.getString(R.string.desbloqueado), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unblock
                                                Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    public boolean checkIsBlocked(Context context, String hisUid, String myUid) {
        this.context = context;
        this.uid = hisUid;
        this.myUid = uid;
        //check each user, if blocked or not
        //if uid of the user exists in "BlockedUsers then that user is blocked, otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            if (ds.exists()) {
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return isBlocked;
    }
}
