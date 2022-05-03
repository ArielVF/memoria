package com.example.tesis;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class WorkManagerNotify extends Worker {
    public WorkManagerNotify(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void scheduleNotify(long duration, Data data, String tag){
        OneTimeWorkRequest notify = new OneTimeWorkRequest.Builder(WorkManagerNotify.class)
                .setInitialDelay(duration, TimeUnit.MILLISECONDS).addTag(tag)
                .setInputData(data).build();
        WorkManager intance = WorkManager.getInstance();
        intance.enqueue(notify);
    }

    @NonNull
    @Override
    public Result doWork() {
        String titleNotification = getInputData().getString("title");
        String descriptionNotification = getInputData().getString("description");
        int id = (int) getInputData().getLong("idNotification", 0);
        pushNotify(titleNotification, descriptionNotification);
        return Result.success();
    }

    public void pushNotify(String title, String description){
        RequestQueue myrequest = Volley.newRequestQueue(getApplicationContext());
        JSONObject json = new JSONObject();

        try{
            String token = "your_secret_token";
            json.put("to", token);
            JSONObject notification = new JSONObject();
            notification.put("titulo", title);
            notification.put("detalle", description);
            json.put("data", notification);
            String URL = "https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, null, null){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "your_key");
                    return header;
                }
            };
            myrequest.add(request);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
