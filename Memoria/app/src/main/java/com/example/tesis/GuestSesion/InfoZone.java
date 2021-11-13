package com.example.tesis.GuestSesion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.tesis.Model.Zone;
import com.example.tesis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.tesis.GuestSesion.MainActivity.subZonesMain;


public class InfoZone extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageSlider imageSlider;
    private TextView title, description, numberFloor;
    private String number;
    private Zone zone;
    private Button bacKMap;
    private List<String> id_subZonesInZone;
    private List<Integer> floor_subZone;
    private TableLayout table;
    private ImageView no_image;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_zone);
        db = FirebaseFirestore.getInstance();
        this.floor_subZone = new ArrayList<>();
        id_subZonesInZone = new ArrayList<>();
        Bundle bundle = getIntent().getBundleExtra("data");
        if(bundle!=null){
            zone = (Zone) bundle.getSerializable("dataZone");
            getSubZonesBelongThisZone();
        }

        title = findViewById(R.id.title_zone);
        description = findViewById(R.id.description_zone);
        imageSlider = findViewById(R.id.image_slider);
        bacKMap = findViewById(R.id.back_map);
        no_image = findViewById(R.id.image_zone_no_exist);
        linearLayout = findViewById(R.id.layout_tables);

        setPhotos();
        setTextView();

        bacKMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }

    public void setTextView(){
        title.setText(zone.getName());
        description.setText(zone.getDescription());
    }

    public void setPhotos(){
        List<SlideModel> slideModels = new ArrayList<>();
        String[] photos = zone.getPhoto();
        for(int i=0; i< photos.length; i++){
            if(!photos[i].trim().equals("sin imagen")){
                slideModels.add(new SlideModel(photos[i].trim()));
            }
        }
        if(slideModels.isEmpty() || slideModels.size() == 0){
            no_image.setVisibility(View.VISIBLE);
            imageSlider.setVisibility(View.GONE);
        }else{
            no_image.setVisibility(View.GONE);
            imageSlider.setVisibility(View.VISIBLE);
            imageSlider.setImageList(slideModels, true);
        }
    }

    public void getSubZonesBelongThisZone(){
        String id = zone.getId().trim();
        if(zone.getFloor() > 0){
            db.collection("Pertenece a").whereEqualTo("Zona", id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot value : queryDocumentSnapshots) {
                        id_subZonesInZone.add(value.getString("SubZona"));
                        floor_subZone.add(Integer.valueOf(value.get("piso").toString()));
                    }
                    createTable(zone.getFloor());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.println(Log.ASSERT, "Algo salió mal", "No se obtuvieron los datos");
                }
            });
        }
    }

    public void createTable(int n){
        for(int i=0; i < n; i++){
            createNewTable(i);
            if(!id_subZonesInZone.isEmpty()){
                boolean noExistSubZoneIntThisFloor = true;
                for(int j = 0; j < subZonesMain.size(); j++){
                    String idA = subZonesMain.get(j).getId().trim();
                    for(int k =0; k < id_subZonesInZone.size(); k++){
                        String idB = id_subZonesInZone.get(k).trim();
                        int currentFloor = floor_subZone.get(k);
                        if(idA.equals(idB) && currentFloor == (i+1)){
                            createRow(subZonesMain.get(j).getName());
                            noExistSubZoneIntThisFloor = false;
                        }
                    }
                }
                if(noExistSubZoneIntThisFloor){
                    createRow("Sin salas en este piso");
                }
            }else{
                createRow("Sin salas en este piso");
            }
            linearLayout.addView(table);
        }
    }

    public void createRow(String data){
        View v = new View(getApplicationContext());
        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        v.setBackgroundColor(Color.rgb(51, 51, 51));

        TableRow row = new TableRow(getApplicationContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);

        TextView textView = new TextView(getApplicationContext());

        TableRow.LayoutParams layoutParams;
        layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f);
        layoutParams.weight = 1;
        textView.setLayoutParams(layoutParams);

        textView.setBackgroundResource(R.color.gray);
        textView.setText(data);
        textView.setTextColor( getResources().getColor(R.color.black));
        textView.setTextSize(20);
        row.addView(textView);
        table.addView(row);
        table.addView(v);
    }

    public TableLayout createNewTable(int curren_floor){
        table = new TableLayout(getApplicationContext());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 15);
        table.setLayoutParams(params);

        TableRow row = new TableRow(getApplicationContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);

        TextView textView = new TextView(getApplicationContext());

        TableRow.LayoutParams layoutParams;
        layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f);
        layoutParams.weight = 1;
        textView.setLayoutParams(layoutParams);

        textView.setBackgroundResource(R.color.third);
        textView.setText("Piso "+(curren_floor+1));
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(textView);
        table.addView(row);
        return table;
    }
}