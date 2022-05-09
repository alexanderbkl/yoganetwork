package com.android.yoganetwork.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.yoganetwork.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class FirebaseMessaging extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID = "admin_channel";
    private String notificationType;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //get current user from shared preferences
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");


        /*Now there are two types of notifications
        *           > notificationType="PostNotification"
        *           > notificationType="ChatNotification"*/
        notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("PostNotification")) {
            //post notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");
            
            //if user is same that has posted don't show notification
            if(!sender.equals(savedCurrentUser)) {
                showPostNotification(""+pId, ""+pTitle,""+pDescription);
            }
        }
        else if (notificationType.equals("ChatNotification")) {
            //chat notification
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null && sent.equals(fUser.getUid())) {
                if (!savedCurrentUser.equals(user)) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            sendOAndAboveNotification(remoteMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        } else if (notificationType.equals("LikeNotification")) {
            //chat notification
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null && sent.equals(fUser.getUid())) {
                if (!savedCurrentUser.equals(user)) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            sendOAndAboveNotification(remoteMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        } else {
            //other notification
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null && sent.equals(fUser.getUid())) {
                if (!savedCurrentUser.equals(user)) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            sendOAndAboveNotification(remoteMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }
        }

    private void showPostNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        /*Apps targeting SDK 26 or above (Android O and above) must implement notification
        * channels and add its notifications to at least one of them
        * Let's add check if version is Oreo or higher then setup notification channel*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification clicked
       
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //LargeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_lotus);

        //sound for notification
        Uri notificationSoundUri =RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lotus)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

                //show notification
                notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel (NotificationManager notificationManager) {
        CharSequence channelName = "New Notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager!=null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        PendingIntent pIntent = null;
        if (notificationType.equals("LikeNotification")) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("fragPos", "4");
            intent.putExtra("extra", "profileLikes");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        } else if (notificationType.equals("ChatNotification")) {
            Intent intent = new Intent(this, ChattingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("hisUid", user);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        }
        
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int icone = getApplicationContext().getResources().getIdentifier(icon, "drawable", getApplicationContext().getPackageName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_lotus)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), icone))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0) {
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOAndAboveNotification(RemoteMessage remoteMessage) throws IOException {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));

        PendingIntent pIntent = null;
        if (notificationType.equals("LikeNotification")) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("fragPos", "4");
            intent.putExtra("extra", "profileLikes");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        } else if (notificationType.equals("ChatNotification")) {
            Intent intent = new Intent(this, ChattingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("hisUid", user);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        }

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        int icone = getApplicationContext().getResources().getIdentifier(icon, "drawable", getApplicationContext().getPackageName());

        Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, BitmapFactory.decodeResource(getApplicationContext().getResources(), icone));

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0) {
            j=i;
        }
        notification1.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
       //update user token
            //signed in, update token
            Log.e("newToken", s);
            getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }


    public static String getToken(Context context) {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");


    }


}

