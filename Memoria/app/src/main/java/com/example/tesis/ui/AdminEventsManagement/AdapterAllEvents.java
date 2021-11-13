package com.example.tesis.ui.AdminEventsManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tesis.Model.Event;
import com.example.tesis.R;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.tesis.ui.authActivity.zones;

public class AdapterAllEvents extends
        RecyclerView.Adapter<AdapterAllEvents.AllEventViewHolder> implements Filterable {

    private List<Event> allEvents;
    private List<Event> allmyEvents;
    private ItemClickListener clickListener;

    public AdapterAllEvents(ArrayList<Event> allevents, ItemClickListener clickListener){
        this.allEvents = allevents;
        this.allmyEvents = new ArrayList<>(this.allEvents);
        this.clickListener = clickListener;
    }

    public void setAdapterAllEvent(ArrayList<Event> events){
        this.allmyEvents = new ArrayList<>(this.allEvents);
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> eventsFiltered = new ArrayList<>();
            if(constraint.toString().isEmpty()){
                eventsFiltered.addAll(allmyEvents);
            }
            else{
                for(Event eachEvent: allmyEvents){
                    //Borramos tildes
                    String title = Normalizer.normalize(eachEvent.getTitle().toLowerCase(), Normalizer.Form.NFD);
                    title = title.replaceAll("[^\\p{ASCII}]", "");

                    String line = Normalizer.normalize(constraint.toString().toLowerCase(), Normalizer.Form.NFD);
                    line =  line.replaceAll("[^\\p{ASCII}]", "");
                    if(title.contains(line)){
                        eventsFiltered.add(eachEvent);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = eventsFiltered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            allEvents.clear();
            allEvents.addAll((Collection<? extends Event>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public AdapterAllEvents.AllEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.allevents_element, parent, false);
        return new AllEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterAllEvents.AllEventViewHolder holder, int index) {
        //holder.text_creator.setText("Creador: "+this.allEvents.get(index).getCreator());
        holder.text_zone.setText("Sector: "+this.allEvents.get(index).getZone());
        holder.text_title.setText(this.allEvents.get(index).getTitle());
        holder.text_description.setText(this.allEvents.get(index).getDescription());
        try {
            holder.text_init.setText("Inicia: "+this.allEvents.get(index).converStartDateAndHour());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(checkIfZoneTemp(this.allEvents.get(index).getZone())){
            holder.see_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(allEvents.get(index));
                }
            });
        }else{
            holder.see_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemTempClick(allEvents.get(index));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.allEvents.size();
    }

    public class AllEventViewHolder extends RecyclerView.ViewHolder {
        TextView text_creator, text_zone, text_title, text_description, text_init;
        ImageButton see_more;

        public AllEventViewHolder(@NonNull View itemView) {
            super(itemView);
            //text_creator = itemView.findViewById(R.id.creator_event);
            text_zone = itemView.findViewById(R.id.zone_event);
            text_title = itemView.findViewById(R.id.title_event);
            text_description = itemView.findViewById(R.id.description_event);
            text_init = itemView.findViewById(R.id.start_event);
            see_more = itemView.findViewById(R.id.seeMoreEvent);
        }
    }

    public interface ItemClickListener{
        public void onItemClick(Event event);
        public void onItemTempClick(Event event);
    }

    public boolean checkIfZoneTemp(String nameZone){
        boolean noTemp = true;
        for(int i=0; i < zones.size(); i++){
            if(zones.get(i).getName().equals(nameZone)){
                if(!zones.get(i).getPermanent()){
                    noTemp = false;
                    i = zones.size();
                }
            }
        }
        return noTemp;
    }
}
