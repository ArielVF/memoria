package com.example.tesis.ui.myEvents;

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

public class AdapterMyEvent extends
        RecyclerView.Adapter<AdapterMyEvent.MyEventViewHolder> implements Filterable {

    private List<Event> myevents;
    private List<Event> allmyEvents;
    private ItemClickListener clickListener;

    public AdapterMyEvent(ArrayList<Event> myevents, ItemClickListener clickListener){
        this.myevents = myevents;
        this.allmyEvents = new ArrayList<>(this.myevents);
        this.clickListener = clickListener;
    }

    public void setMyAdapterEvent(ArrayList<Event> events){
        this.allmyEvents = new ArrayList<>(this.myevents);
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
            myevents.clear();
            myevents.addAll((Collection<? extends Event>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.myevent_element, parent, false);
        return new MyEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int index) {
        holder.text_creator.setText("Sector: "+this.myevents.get(index).getZone());
        holder.text_title.setText(this.myevents.get(index).getTitle());
        holder.text_description.setText(this.myevents.get(index).getDescription());
        try {
            holder.text_init.setText("Inicia: "+this.myevents.get(index).converStartDateAndHour());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(checkIfZoneTemp(this.myevents.get(index).getZone())){
            holder.see_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(myevents.get(index));
                }
            });
        }else{
            holder.see_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemTempClick(myevents.get(index));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.myevents.size();
    }

    public class MyEventViewHolder extends RecyclerView.ViewHolder {
        TextView text_creator, text_title, text_description, text_init;
        ImageButton see_more;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            text_creator = itemView.findViewById(R.id.zone_event);
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
