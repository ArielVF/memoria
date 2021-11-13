package com.example.tesis.ui.event;

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
import com.google.firebase.Timestamp;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class AdapterEvent
        extends RecyclerView.Adapter<AdapterEvent.EventViewHolder> implements Filterable{

    private List<Event> events;
    private List<Event> allEvents;
    private AdapterEvent.ItemClickListener clickListener;

    public AdapterEvent(List<Event> events, ItemClickListener clickListener){
       this.events = events;
       this.allEvents = new ArrayList<>(this.events);
       this.clickListener = clickListener;
    }

    public void setAdapterEvents(List<Event> events){
        this.allEvents = new ArrayList<>(this.events);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.event_element, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int index) {
        holder.text_creator.setText("Sector: "+this.events.get(index).getZone());
        holder.text_title.setText(this.events.get(index).getTitle());
        holder.text_description.setText(this.events.get(index).getDescription());
        if(checkIfCurrentEvent(this.events.get(index).getStart(), this.events.get(index).getFinish())){
            holder.isActiveNow.setVisibility(View.VISIBLE);
        }
        else{
            holder.isActiveLater.setVisibility(View.VISIBLE);
        }
        try {
            holder.text_init.setText("Inicia: "+this.events.get(index).converStartDateAndHour());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(events.get(index));
            }
        });
    }

    private boolean checkIfCurrentEvent(Timestamp start, Timestamp finish) {
        boolean isCurrent = false;
        Date now = new Date();
        if(start.toDate().compareTo(now) <= 0 && finish.toDate().compareTo(now) > 0){
            isCurrent = true;
        }
        return isCurrent;
    }

    @Override
    public int getItemCount() {
        return this.events.size();
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> eventsFiltered = new ArrayList<>();
            if(constraint.toString().isEmpty()){
                eventsFiltered.addAll(allEvents);
            }
            else{
                for(Event eachEvent: allEvents){
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
            events.clear();
            events.addAll((Collection<? extends Event>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView text_creator, text_title, text_description, text_init, isActiveNow, isActiveLater;
        //Button see_more;
        ImageButton see_more;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            text_creator = itemView.findViewById(R.id.zone_event);
            text_title = itemView.findViewById(R.id.title_event);
            text_description = itemView.findViewById(R.id.description_event);
            text_init = itemView.findViewById(R.id.start_event);
            see_more = itemView.findViewById(R.id.seeMoreEvent);
            isActiveNow = itemView.findViewById(R.id.isActive);
            isActiveLater = itemView.findViewById(R.id.isNoActive);
        }
    }

    public interface ItemClickListener{
        public void onItemClick(Event event);
    }
}
