package com.example.tesis.ui.myEvents;

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

import com.example.tesis.Model.Event;
import com.example.tesis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static com.example.tesis.ui.authActivity.zones;


public class MyEventFragment extends Fragment implements AdapterMyEvent.ItemClickListener {
    private String id;
    private FirebaseFirestore db;
    private FirebaseAuth mauth;
    private RecyclerView recyclerView;
    private AdapterMyEvent adapterMyEvent;
    private ArrayList<Event> myEvents;
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
        root = inflater.inflate(R.layout.fragment_my_events, container, false);

        mauth = FirebaseAuth.getInstance();
        id = mauth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        myEvents = new ArrayList<>();
        getMyEvents();

        this.noAvaibleEvent = root.findViewById(R.id.no_event_avaible);

        recyclerView = root.findViewById(R.id.list_myevents);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterMyEvent = new AdapterMyEvent(myEvents, this);
        recyclerView.setAdapter(adapterMyEvent);

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
        searchView.setQueryHint("Buscar por título de evento...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterMyEvent.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getMyEvents(){
        db.collection("Eventos")
                .whereEqualTo("creador", mauth.getCurrentUser().getUid())
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
                            myEvents.add(event);
                        }
                        adapterMyEvent.notifyDataSetChanged();
                        adapterMyEvent.setMyAdapterEvent(myEvents);
                        if(myEvents.isEmpty()){
                            noAvaibleEvent.setVisibility(View.VISIBLE);
                        }
                        else{
                            noAvaibleEvent.setVisibility(View.GONE);
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