package com.example.tesis.ui.event;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tesis.Model.Event;
import com.example.tesis.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import static com.example.tesis.ui.authActivity.activeEvents;
import static com.example.tesis.ui.authActivity.zones;

public class EventFragment extends Fragment implements AdapterEvent.ItemClickListener{
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Event> events;
    private AdapterEvent adapterEvent;
    private String authorName, zoneName;
    private View root;

    private String name = "";
    private String zone = "";
    private TextView noAvaibleEvent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_gallery, container, false);
        db = FirebaseFirestore.getInstance();
        getCurrentEvents();

        recyclerView = root.findViewById(R.id.list_events);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        noAvaibleEvent = root.findViewById(R.id.no_event_avaible);

        this.events = new ArrayList<>();
        adapterEvent = new AdapterEvent(activeEvents, this);
        recyclerView.setAdapter(adapterEvent);

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
                adapterEvent.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getCurrentEvents(){
        db.collection("Eventos").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                for(int i=0; i < activeEvents.size(); i++) {
                    name = activeEvents.get(i).getCreator();
                    zone = getZoneName(activeEvents.get(i).getZone());
                    if(!zone.trim().equals("")){
                        activeEvents.get(i).setCreator(name);
                        activeEvents.get(i).setZone(zone);
                    }
                }
                adapterEvent.notifyDataSetChanged();
                adapterEvent.setAdapterEvents(activeEvents);
                if(activeEvents.isEmpty()){
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
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onItemClick(Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        Navigation.findNavController(root).navigate(R.id.infoEventFragment, bundle);
    }
}