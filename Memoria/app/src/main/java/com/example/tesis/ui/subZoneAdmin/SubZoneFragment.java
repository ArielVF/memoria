package com.example.tesis.ui.subZoneAdmin;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.example.tesis.Model.SubZone;
import com.example.tesis.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.tesis.ui.authActivity.subZones;

public class SubZoneFragment extends Fragment implements AdapterSubZone.ItemClickListener {
    private View root;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AdapterSubZone adapterSubZone;
    private ImageButton addNewSubZone;
    private TextView noSubZone;

    /**
     * Método para instanciar elemenentos no gráficos.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        getSubZones();
        setHasOptionsMenu(true);
    }

    /**
     * Método de instancia elementos gráficos.
     * @return la Vista creada.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_sub_zone, container, false);
        noSubZone = root.findViewById(R.id.no_subzone_avaible);
        addNewSubZone = root.findViewById(R.id.addNewSubZone);
        existSubzone();
        recyclerView = root.findViewById(R.id.list_subZones);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterSubZone = new AdapterSubZone(subZones, this);
        recyclerView.setAdapter(adapterSubZone);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        addNewSubZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSubZone();
            }
        });
        return root;
    }

    /**
     * Setea un textview en caso de que el arreglo subzonas no contenga
     * elementos.
     */
    public void existSubzone(){
        if(subZones.isEmpty()){
            noSubZone.setVisibility(View.VISIBLE);
        }else {
            noSubZone.setVisibility(View.GONE);
        }
    }

    /**
     * Método que llama a la vista para crear subzonas.
     */
    public void createSubZone(){
        Navigation.findNavController(root).navigate(R.id.createSubZoneFragment);
    }

    /**
     * Método para crear el buscador en la zona superior de la pantalla.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_event, menu);
        MenuItem menuItem = menu.findItem(R.id.bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Buscar por nombre ...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterSubZone.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Método que cumple la función de quedarse escuchando para actualizar cambios en la vista
     *     de subzonas
     */
    public void getSubZones(){
            db.collection("SubZona")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@com.google.firebase.database.annotations.Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            adapterSubZone.notifyDataSetChanged();
                            adapterSubZone.setAdapterSubZone(subZones);
                        }
                    });

    }

    /**
     * Método para obtener el elemento seleccionado por el usuario desplegado en
     * el listView
     * @param subZone Elemento de clase SubZone que contiene los datos de la subzona seleccionada
     *                por el usuario.
     */
    @SuppressLint("ResourceType")
    @Override
    public void onItemClick(SubZone subZone) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("subZone", subZone);
        Navigation.findNavController(root).navigate(R.id.editSubZoneFragment, bundle);
    }
}