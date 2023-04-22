package com.example.tickets_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomAdapter  extends BaseAdapter {
    Context context;
   // List<String[]> eventsInfo;
    List<List<String>> eventsNames;
    LayoutInflater inflter;


    public CustomAdapter(Context applicationContext,  List<List<String>> eventsNames) {
        this.context = applicationContext;
        //this.eventsInfo = eventsInfo;
        this.eventsNames = eventsNames;
        //this.filteredList = eventsNames;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return eventsNames.size();
    }

    @Override
    public String getItem(int position) {
        return eventsNames.get(position).get(0);
    }



    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.adp_lv, null);
        TextView title = (TextView) view.findViewById(R.id.titleTextView);
        TextView sub = (TextView) view.findViewById(R.id.subtitleTextView);
        title.setText(eventsNames.get(i).get(0));
        sub.setText(eventsNames.get(i).get(1));
        return view;
    }




}

