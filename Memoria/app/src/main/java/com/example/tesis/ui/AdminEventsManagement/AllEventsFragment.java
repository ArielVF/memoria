package com.example.tesis.ui.AdminEventsManagement;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tesis.Model.Event;
import com.example.tesis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static com.example.tesis.ui.authActivity.zones;

public class AllEventsFragment extends Fragment implements AdapterAllEvents.ItemClickListener{
    private FirebaseFirestore db;
    private FirebaseAuth mauth;
    private RecyclerView recyclerView;
    private AdapterAllEvents adapterAllEvents;
    private ArrayList<Event> allEvents;
    private View root;
    private TextView noAvaibleEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_all_events, container, false);

        db = FirebaseFirestore.getInstance();
        allEvents = new ArrayList<>();
        getAllEvents();

        this.noAvaibleEvent = root.findViewById(R.id.no_event_avaible);

        recyclerView = root.findViewById(R.id.list_allevents);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterAllEvents = new AdapterAllEvents(allEvents, this);
        recyclerView.setAdapter(adapterAllEvents);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_event, menu);

        MenuItem menuItem = menu.findItem(R.id.bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Buscar por titulo de evento...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterAllEvents.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getAllEvents(){
        db.collection("Eventos")
                .orderBy("fecha inicio", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e!=null){
                            Log.println(Log.ASSERT,"ERROR", "Error:"+e.getMessage());
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            String zone = getZoneName(doc.get("sector").toString());
                            Event event = new Event(doc.getId(), doc.getString("creador"), zone, doc.getString("nombre"),
                                    doc.getString("descripcion"), doc.getTimestamp("fecha inicio"),
                                    doc.getTimestamp("fecha termino"), doc.getString("foto"));
                            allEvents.add(event);
                        }
                        if(allEvents.isEmpty()){
                            noAvaibleEvent.setVisibility(View.VISIBLE);
                        }else{
                            adapterAllEvents.notifyDataSetChanged();
                            adapterAllEvents.setAdapterAllEvent(allEvents);
                            //getNameCreators();
                        }
                    }
                });
    }

    public String getZoneName(String id) {
        String name = "";
        for (int i=0; i < zones.size(); i++){
            if(zones.get(i).getId().equals(id)){
                name = zones.get(i).getName();
                i= zones.size();
            }
        }
        return name;
    }

    public void getNameCreators(){
        for(int i=0; i < allEvents.size(); i++){
            int index = i;
            db.collection("Usuarios")
                    .document(allEvents.get(i).getCreator().trim())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {

                    Log.println(Log.ASSERT, "usuario:", snapshot.get("nombre").toString()+" "+allEvents.get(index).getTitle());
                    if(index+1 == allEvents.size()){
                        allEvents.get(index).setCreator(snapshot.get("nombre").toString()+" "+snapshot.get("apellido").toString());

                    }else{
                        allEvents.get(index).setCreator(snapshot.get("nombre").toString()+" "+snapshot.get("apellido").toString());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Algo salió mal para obtener los datos", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onItemClick(Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        Navigation.findNavController(root).navigate(R.id.myEventInfoFragment, bundle);
    }

    @Override
    public void onItemTempClick(Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        Navigation.findNavController(root).navigate(R.id.editMyEventZoneTemporal, bundle);
    }
}