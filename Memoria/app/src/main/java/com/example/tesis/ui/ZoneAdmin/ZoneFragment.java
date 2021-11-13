package com.example.tesis.ui.ZoneAdmin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import static com.example.tesis.ui.authActivity.zones;

import com.example.tesis.Model.Zone;
import com.example.tesis.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class ZoneFragment extends Fragment implements AdapterZone.ItemClickListener {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AdapterZone adapterZone;
    private View root;
    private ImageButton addZone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_zone, container, false);
        db = FirebaseFirestore.getInstance();

        threadZoneHaveChange();

        addZone = root.findViewById(R.id.addNewZone);
        recyclerView = root.findViewById(R.id.list_zones);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterZone = new AdapterZone(zones, this);
        recyclerView.setAdapter(adapterZone);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        setHasOptionsMenu(true);

        addZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.createZoneFragment);
            }
        });
        return root;
    }

    //Escucha si se agregan más eventos, el llamado principal es en el activity Auth.
    public void threadZoneHaveChange(){
        db.collection("Zona").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                adapterZone.notifyDataSetChanged();
                adapterZone.setAdapterZone(zones);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_event, menu);

        MenuItem menuItem = menu.findItem(R.id.bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Buscar por nombre");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterZone.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(Zone zone) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("zone", zone);
        Navigation.findNavController(root).navigate(R.id.editZoneFragment, bundle);
    }
}