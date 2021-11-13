package com.example.tesis.ui.userControl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tesis.Model.User;
import com.example.tesis.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class AdapterUser extends
        RecyclerView.Adapter<AdapterUser.UserViewHolder> implements Filterable {

    private List<User> users;
    private List<User> allusers;
    private ItemClickListener clickListener;

    public AdapterUser(ArrayList<User> users, ItemClickListener clickListener){
        this.users = users;
        this.allusers = new ArrayList<>(this.users);
        this.clickListener = clickListener;
    }

    public void setAdapterUser(ArrayList<User> users){
        this.allusers = new ArrayList<>(this.users);
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> usersFiltered = new ArrayList<>();
            if(constraint.toString().isEmpty()){
                usersFiltered.addAll(allusers);
            }
            else{
                for(User eachEvent: allusers){
                    //Borramos tildes
                    String rut = Normalizer.normalize(eachEvent.getRut().toLowerCase(), Normalizer.Form.NFD);
                    rut = rut.replaceAll("[^\\p{ASCII}]", "");

                    String line = Normalizer.normalize(constraint.toString().toLowerCase(), Normalizer.Form.NFD);
                    line =  line.replaceAll("[^\\p{ASCII}]", "");
                    if(rut.contains(line)){
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
            users.clear();
            users.addAll((Collection<? extends User>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }


    @NonNull
    @Override
    public AdapterUser.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.element_user, parent, false);
        return new AdapterUser.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUser.UserViewHolder holder, int index) {
        holder.text_name.setText(this.users.get(index).getName()+" "+this.users.get(index).getLastname());
        holder.text_rut.setText("Rut: "+this.users.get(index).formatRut());
        holder.text_rol.setText("Rol: "+this.users.get(index).getRol());
        holder.text_email.setText("Correo: "+this.users.get(index).getEmail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { clickListener.onItemClick(users.get(index));
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView text_name, text_rut, text_email, text_rol;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            text_name = itemView.findViewById(R.id.name_user);
            text_email = itemView.findViewById(R.id.email_user);
            text_rol = itemView.findViewById(R.id.rol_user);
            text_rut = itemView.findViewById(R.id.rut_user);
        }
    }

    public interface ItemClickListener{
        public void onItemClick(User user);
    }
}
