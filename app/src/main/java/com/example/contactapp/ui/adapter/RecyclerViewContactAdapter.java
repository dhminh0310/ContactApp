package com.example.contactapp.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactapp.R;
import com.example.contactapp.data.model.Contact;

import java.util.List;

public class RecyclerViewContactAdapter extends RecyclerView.Adapter<RecyclerViewContactAdapter.ContactViewHolder> {
    private List<Contact> listContacts = null;
    private IAdapterCallback callback = null;

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact currentContact = listContacts.get(position);

        holder.tvName.setText(currentContact.getName());
        holder.tvNumber.setText(currentContact.getPhoneNumber());

        holder.btnEdit.setOnClickListener(view -> {
            if(callback != null){
                callback.onEditContact(currentContact);
            }
        });

        holder.btnDelete.setOnClickListener(view -> {
            if(callback != null){
                callback.onDeleteContact(currentContact);
            }
        });

        holder.containerLayout.setOnClickListener(view -> {
            if(callback != null){
                callback.onClickContactItem(currentContact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listContacts.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setListContact(List<Contact> listContacts){
        this.listContacts = listContacts;
        notifyDataSetChanged();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout containerLayout;
        TextView tvName;
        TextView tvNumber;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            containerLayout = itemView.findViewById(R.id.containerLayout);
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvPhoneNumber);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface IAdapterCallback{
        void onClickContactItem(Contact contact);
        void onEditContact(Contact contact);
        void onDeleteContact(Contact contact);
    }

    public void setCallback(IAdapterCallback callback) {
        this.callback = callback;
    }
}
