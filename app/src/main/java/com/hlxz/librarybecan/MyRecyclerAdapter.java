package com.hlxz.librarybecan;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>{

    final ArrayList<Contact> list;


    public MyRecyclerAdapter(ArrayList<Contact> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        //LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = list.get(position);
        holder.setValues(contact);
    }

    @Override
    public int getItemCount() {
        return list==null ? 0 : list.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView textViewName;
        final TextView textViewnum;
        final TextView textViewfloor;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.name);
            textViewnum = (TextView) itemView.findViewById(R.id.num);
            textViewfloor = (TextView) itemView.findViewById(R.id.floor);

        }

        public void setValues(Contact contact){
            textViewName.setText(contact.getName());
            textViewnum.setText(contact.getNum());
            textViewfloor.setText(contact.getfloor());
        }
    }
}
