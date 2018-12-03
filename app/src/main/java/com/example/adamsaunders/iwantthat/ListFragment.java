package com.example.adamsaunders.iwantthat;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListFragment extends Fragment {

    private ListView List_View;
    private ArrayList<ListItem> entries;
    private TextView defaultText;
    private Button removeAllButton;
    private Boolean DarkModeOn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, null);

        List_View = v.findViewById(R.id.listOrderedView);
        defaultText = v.findViewById(R.id.defaultText);
        removeAllButton = v.findViewById(R.id.removeAllButton);

        // get important settings from the preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean RemoveAllButtonOn = settings.getBoolean("delete_all_preference", false);
        DarkModeOn = settings.getBoolean("darkmode_preference", false);

        // checks to see if the remove all button is enabled
        if(RemoveAllButtonOn) removeAllButton.setVisibility(View.VISIBLE);
        else removeAllButton.setVisibility(View.GONE);

        ListTask listTask = new ListTask();
        listTask.execute();

        // sets the basic backgrounds for darkmode
        if (DarkModeOn)
        {
            v.setBackgroundColor(Color.DKGRAY);
            defaultText.setTextColor(Color.parseColor("#e8e4d6"));
        }

        // when the button is pressed, strip all entries from the shopping list
        removeAllButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SQLiteOpenHelper openHelper = new DatabaseConfig(getContext());
                SQLiteDatabase database = openHelper.getWritableDatabase();

                database.execSQL("DELETE FROM ShoppingList");

                database.close();

                ListTask listTask = new ListTask();
                listTask.execute();
            }
        });

        return v;
    }

    class ListItem {
        private String imageURL;
        private String product;
        private String price;
        private String supplier;

        public ListItem(String imageURL, String product, String price, String supplier) {
            this.imageURL = imageURL;
            this.product = product;
            this.price = price;
            this.supplier = supplier;
        }

        public String getImageURL() {return  imageURL;}

        public String getProduct() {
            return product;
        }

        public String getPrice() {
            return price;
        }

        public String getSupplier() { return  supplier; }
    }

    private class ListAdapter extends ArrayAdapter<ListItem> {

        private ArrayList<ListItem> items;

        public ListAdapter(Context context, int textViewResourceId, ArrayList<ListItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_entry, null);
            }
            final ListItem o = items.get(position);
            if (o != null) {
                ImageView listImage = v.findViewById(R.id.listImage);
                TextView name = v.findViewById(R.id.listName);
                TextView price = v.findViewById(R.id.listValue);
                TextView supplier = v.findViewById(R.id.listProvider);
                Button removeButton = v.findViewById(R.id.removeButton);

                // sets darkmode options for the entries
                if (DarkModeOn)
                {
                    name.setTextColor(Color.parseColor("#e8e4d6"));
                    price.setTextColor(Color.parseColor("#e8e4d6"));
                    supplier.setTextColor(Color.parseColor("#e8e4d6"));
                }
                if (listImage != null)
                {
                    Glide.with(ListFragment.this)
                            .load(o.getImageURL())
                            .into(listImage);
                }
                if (name != null) {
                    name.setText(o.getProduct());
                }
                if (price != null) {
                    price.setText(o.getPrice());
                }
                if (supplier != null) {
                    supplier.setText(o.getSupplier());
                }
                // Makes it so the remove button strips the entry from the list
                removeButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        SQLiteOpenHelper openHelper = new DatabaseConfig(getContext());
                        SQLiteDatabase database = openHelper.getWritableDatabase();

                        database.execSQL("DELETE FROM ShoppingList WHERE ListEntry = (SELECT ListEntry FROM ShoppingList WHERE ItemName = '" + o.getProduct() + "' LIMIT 1)");

                        database.close();

                        ListTask listTask = new ListTask();
                        listTask.execute();
                    }
                });
            }
            return v;
        }
    }

    class ListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("IWantThat", "pre execute of listTask.");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // grabs all entries from the shopping list and adds them into the list
            SQLiteOpenHelper openHelper = new DatabaseConfig(getContext());
            SQLiteDatabase database = openHelper.getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM ShoppingList", null);
            cursor.moveToFirst();
            entries = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                if (!cursor.isNull(1))
                {
                    entries.add(new ListItem(cursor.getString(3), cursor.getString(1), cursor.getString(4), cursor.getString(2)));
                }
                cursor.moveToNext();
            }
            cursor.close();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("IWantThat", "post execute of ListTask!");
            if (entries.isEmpty() == false) {
                // if there are some entries populate the list
                defaultText.setText("");
                defaultText.setVisibility(View.GONE);
                ListAdapter listAdapter = new ListAdapter(getContext(), R.layout.list_entry, entries);
                listAdapter.notifyDataSetChanged();
                //set the adapter of the ListView
                List_View.setAdapter(listAdapter);
            }
            else {
                // if there are no entries clean the slate and bring up the basic text
                defaultText.setText("Sorry No entries were found!");
                defaultText.setVisibility(View.VISIBLE);

                ListAdapter listAdapter = new ListAdapter(getContext(), R.layout.list_entry, entries);
                listAdapter.notifyDataSetChanged();
                //set the adapter of the ListView
                List_View.setAdapter(listAdapter);
            }
        }
    }
}
