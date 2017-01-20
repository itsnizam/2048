package com.presto.p2048.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.presto.p2048.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends ArrayAdapter<String> {
    private List<String> data;
    private Context context;

    public ListAdapter(Context context, ArrayList<String> dataItem) {
        super(context, R.layout.saved_game_list_item, dataItem);
        this.data = dataItem;
        this.context = context;
    }
}
