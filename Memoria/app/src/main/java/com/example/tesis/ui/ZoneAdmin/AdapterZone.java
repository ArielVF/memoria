package com.example.tesis.ui.ZoneAdmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tesis.Model.Zone;
import com.example.tesis.R;
import com.squareup.picasso.Picasso;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdapterZone extends RecyclerView.Adapter<AdapterZone.ZoneViewHolder> implements Filterable {
    private List<Zone> zones;
    private List<Zone> allzones;
    private AdapterZone.ItemClickListener clickListener;

    public AdapterZone(List<Zone> zones, AdapterZone.ItemClickListener clickListener){
        this.zones = zones;
        this.allzones = new ArrayList<>(this.zones);
        this.clickListener = clickListener;
    }

    public void setAdapterZone(List<Zone> zones){
        this.allzones = new ArrayList<>(this.zones);
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Zone> usersFiltered = new ArrayList<>();
            if(constraint.toString().isEmpty()){
                usersFiltered.addAll(allzones);
            }
            else{
                for(Zone eachEvent: allzones){
                    //Borramos tildes
                    String name = Normalizer.normalize(eachEvent.getName().toLowerCase(), Normalizer.Form.NFD);
                    name = name.replaceAll("[^\\p{ASCII}]", "");

                    String line = Normalizer.normalize(constraint.toString().toLowerCase(), Normalizer.Form.NFD);
                    line =  line.replaceAll("[^\\p{ASCII}]", "");
                    if(name.contains(line)){
                        usersFiltered.add(eachEvent);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = usersFiltered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            zones.clear();
            zones.addAll((Collection<? extends Zone>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public AdapterZone.ZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.element_zone, parent, false);
        return new AdapterZone.ZoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterZone.ZoneViewHolder holder, int index) {
        if(zones.get(index).getPermanent()) {
            holder.editZone.setVisibility(View.VISIBLE);
            holder.name.setText(this.zones.get(index).getName());
            holder.description.setText(this.zones.get(index).getDescription());
            holder.image.setImageBitmap(null);

            String urlPhoto = this.zones.get(index).getLastPhotoIndex();
            if (!urlPhoto.trim().equals("sin imagen")) {
                Picasso.get().load(urlPhoto.trim()).into(holder.image);
            } else {
                holder.image.setImageResource(R.mipmap.no_image_avaible);
            }

            holder.editZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(zones.get(index));
                }
            });
        }else{
            holder.name.setText(this.zones.get(index).getName());
            holder.description.setText("Zona de evento temporal, no se puede editar.");
            holder.image.setImageResource(R.mipmap.no_image_avaible);
            holder.editZone.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.zones.size();
    }

    public class ZoneViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;
        ImageButton editZone;
        ImageView image;

        public ZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_zone);
            description = itemView.findViewById(R.id.description_zone);
            editZone = itemView.findViewById(R.id.edit_zone);
            image = itemView.findViewById(R.id.image_icon);
        }
    }

    public interface ItemClickListener{
        public void onItemClick(Zone zone);
    }
}
