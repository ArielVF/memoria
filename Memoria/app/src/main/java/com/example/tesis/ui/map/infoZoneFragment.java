package com.example.tesis.ui.map;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.tesis.ui.authActivity.subZones;


public class infoZoneFragment extends Fragment {
    private FirebaseFirestore db;
    private ImageSlider imageSlider;
    private TextView title, description, numberFloor, status, textBuild, textNoZones;
    private String number, id;
    private LinearLayout linearLayout;
    private Map<Integer, List<String>> infoSubZones = new HashMap<>();
    private List<String> idZones;
    private Zone zone;
    private String haveEvent;
    private List<String> id_subZonesInZone;
    private List<Integer> floor_subZone;
    private TableLayout table;
    private ImageView no_image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        this.idZones = new ArrayList<>();
        this.floor_subZone = new ArrayList<>();

        Bundle eventObject = getArguments();
        if(eventObject != null) {
            haveEvent = (String) eventObject.getSerializable("isActiveEvent");
            zone = (Zone) eventObject.getSerializable("dataZone");
            id_subZonesInZone = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_info_zone, container, false);
        title = root.findViewById(R.id.title_zone);
        description = root.findViewById(R.id.description_zone);
        imageSlider = root.findViewById(R.id.image_slider);
        linearLayout = root.findViewById(R.id.layout_tables);
        status = root.findViewById(R.id.status_zone);
        textBuild = root.findViewById(R.id.text_build_content);
        textNoZones = root.findViewById(R.id.text_build_content);
        no_image = root.findViewById(R.id.image_zone_no_exist);

        setStatus(haveEvent);
        setTextView();
        setPhotos();

        getSubZonesBelongThisZone();
        return root;
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

    public void setTextView(){
        title.setText(zone.getName());
        description.setText(zone.getDescription());
    }

    public void setStatus(String s){
        if(s.equals("ahora")){
            status.setText("Estado: Sector con un evento activo, revise la sección de eventos para mayor información.");
        }
        else if(s.equals("si")){
            status.setText("Estado: Sector con evento próximo, revise la sección de eventos para mayor información.");
        }
        else{
            status.setText("Estado: Sector momentaneamente sin eventos.");
        }
    }

    public void getSubZonesBelongThisZone(){
       String id = zone.getId();
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

    public void createRow(String data){
        View v = new View(getContext());
        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        v.setBackgroundColor(Color.rgb(51, 51, 51));

        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);

        TextView textView = new TextView(getContext());

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

    public void createTable(int n){
        for(int i=0; i < n; i++){
            createNewTable(i);
            if(!id_subZonesInZone.isEmpty()){
                boolean noExistSubZoneIntThisFloor = true;
                for(int j = 0; j < subZones.size(); j++){
                    String idA = subZones.get(j).getId().trim();
                    for(int k =0; k < id_subZonesInZone.size(); k++){
                        String idB = id_subZonesInZone.get(k).trim();
                        int currentFloor = floor_subZone.get(k);
                        if(idA.equals(idB) && currentFloor == (i+1)){
                            createRow(subZones.get(j).getName());
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

    public TableLayout createNewTable(int curren_floor){
        table = new TableLayout(getContext());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 15);
        table.setLayoutParams(params);

        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);

        TextView textView = new TextView(getContext());

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