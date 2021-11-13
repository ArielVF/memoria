package com.example.tesis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.tesis.GuestSesion.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Notification extends FirebaseMessagingService {
    private FirebaseFirestore db;
    private String head_message;
    private String body_message;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.println(Log.ASSERT, "token", s);
        //db = FirebaseFirestore.getInstance();
        //testing(s);
    }

    /*
    public void testing(String s){
        Map<String, Object> data = new HashMap<>();
        data.put("id", s);

        /*
        db.collection("devices")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.println(Log.ASSERT, "SAVE", "SAVE");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });*/
    //}

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.println(Log.ASSERT, "notificacion", "en clase notificacion");
        if(remoteMessage.getData().size() > 0){
            showNotification(remoteMessage.getData().get("titulo"), remoteMessage.getData().get("detalle"));
        }else{
            Log.println(Log.ASSERT, "notificacion", "el tamanio de remotegetdata "+remoteMessage.getData().size()) ;
        }
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        super.onMessageSent(s);
    }


    public void showNotification(String title, String description){
        String id = "message";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(id, "new", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

            builder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentIntent(clickNotify())
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.utalca_logo))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentText(description)
                    .setContentInfo("new");

            Random random = new Random();
            int idNotify = random.nextInt(10000);
            assert notificationManager != null;
            notificationManager.notify(idNotify, builder.build());
        }
        else{
            //Something do...
        }
    }

    @Override
    public void onSendError (String msgId, Exception exception){
        Log.println(Log.ASSERT, "ERROR:", exception.toString());
    }

    public PendingIntent clickNotify(){
        Intent notify = new Intent(getApplicationContext(), PermissionActivity.class);
        notify.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, notify, 0);
    }
}
