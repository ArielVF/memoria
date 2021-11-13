package com.example.tesis.ui.subZoneAdmin;


import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tesis.Model.SubZone;
import com.example.tesis.R;

import java.util.ArrayList;
import java.util.List;

import android.widget.Filter;
import java.util.Collection;
import java.text.Normalizer;

public class AdapterSubZone extends
        RecyclerView.Adapter<AdapterSubZone.SubZoneViewHolder> implements Filterable {

    private List<SubZone> subzones;
    private List<SubZone> allsubzones;
    private AdapterSubZone.ItemClickListener clickListener;

    public AdapterSubZone(List<SubZone> subZones, AdapterSubZone.ItemClickListener clickListener){
        this.subzones = subZones;
        this.allsubzones = new ArrayList<>(this.subzones);
        this.clickListener = clickListener;
    }

    public void setAdapterSubZone(List<SubZone> subzones){
        this.allsubzones = new ArrayList<>(subzones);
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SubZone> usersFiltered = new ArrayList<>();
            if(constraint.toString().isEmpty()){
                usersFiltered.addAll(allsubzones);
            }
            else{
                for(SubZone eachSubZone: allsubzones){
                    //Borramos tildes
                    String name = Normalizer.normalize(eachSubZone.getName().toLowerCase(), Normalizer.Form.NFD);
                    name = name.replaceAll("[^\\p{ASCII}]", "");

                    String line = Normalizer.normalize(constraint.toString().toLowerCase(), Normalizer.Form.NFD);
                    line =  line.replaceAll("[^\\p{ASCII}]", "");
                    if(name.contains(line)){
                        usersFiltered.add(eachSubZone);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = usersFiltered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            subzones.clear();
            subzones.addAll((Collection<? extends SubZone>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public AdapterSubZone.SubZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_subzone, parent, false);
        return new AdapterSubZone.SubZoneViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterSubZone.SubZoneViewHolder holder, int index) {
        holder.text_name.setText(this.subzones.get(index).getName());
        holder.edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { clickListener.onItemClick(subzones.get(index));
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.subzones.size();
    }

    public class SubZoneViewHolder extends RecyclerView.ViewHolder {
        TextView text_name;
        ImageButton edit_button;

        public SubZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            text_name = itemView.findViewById(R.id.name_subzone);
            edit_button = itemView.findViewById(R.id.edit_subzone);

        }
    }

    public interface ItemClickListener{
        public void onItemClick(SubZone subZone);
    }
}
