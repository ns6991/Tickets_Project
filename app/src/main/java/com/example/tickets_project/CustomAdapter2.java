package com.example.tickets_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter2 extends BaseAdapter {
    Context context;
    // List<String[]> eventsInfo;
    List<String[]> eventsNames;
    LayoutInflater inflter;


    public CustomAdapter2(Context applicationContext, List<String[]> eventsNames) {
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
    public String[] getItem(int position) {
        return eventsNames.get(position);
    }


    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.adp_lv, null);
        TextView title = (TextView) view.findViewById(R.id.titleTextView);
        TextView sub  = view.findViewById(R.id.subtitleTextView);
        title.setText(eventsNames.get(i)[0]);
        title.setTextSize(18);
        sub.setText("");

        return view;
    }


}
