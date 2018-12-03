package com.example.adamsaunders.iwantthat;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SearchFragment extends Fragment {


    private String SearchTarget;
    private ListView ResultView;
    private ResultAdapter resultAdapter;
    private ArrayList<ResultEntry> entries;
    private int numOfResultsFound;
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private SearchView searchView;
    private TextView noResultText;
    private boolean DarkModeOn;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, null);

        ResultView = v.findViewById(R.id.result_view);
        noResultText = v.findViewById(R.id.noResultText);

        //Intent intent = getIntent();
        //SearchTarget = intent.getStringExtra("search");

        searchView = v.findViewById(R.id.searchView);
        searchView.setQueryHint("What product do you want?");
        searchView.setIconifiedByDefault(false);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        DarkModeOn = settings.getBoolean("darkmode_preference", false);

        // overrides the basic handling of the query widget and inserts ResultTask
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchTarget = query;
                ResultTask resultTask = new ResultTask();
                resultTask.execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//              if (searchView.isExpanded() && TextUtils.isEmpty(newText)) {
                callSearch(newText);
//              }
                return true;
            }

            public void callSearch(String query) {
                //Do searching
            }
        });

        // sets darkmode features for the background and the search
        if (DarkModeOn)
        {
            v.setBackgroundColor(Color.DKGRAY);
            noResultText.setTextColor(Color.parseColor("#e8e4d6"));
            EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchEditText.setTextColor(Color.parseColor("#e8e4d6"));
            searchEditText.setHintTextColor(Color.parseColor("#e8e4d6"));

            ImageView icon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
            icon.setColorFilter(Color.parseColor("#e8e4d6"));
        }

        return v;
    }

    private class ResultAdapter extends ArrayAdapter<ResultEntry> {

        private ArrayList<ResultEntry> items;

        public ResultAdapter(Context context, int textViewResourceId, ArrayList<ResultEntry> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.result_entry, null);
            }
            final ResultEntry o = items.get(position);
            if (o != null) {
                TextView product = v.findViewById(R.id.productText);
                TextView price = v.findViewById(R.id.priceText);
                final TextView supplier = v.findViewById(R.id.supplierText);
                Button listButton = v.findViewById(R.id.listButton);
                Button wantButton = v.findViewById(R.id.wantButton);
                if (product != null) {
                    product.setText(o.getProduct());
                }
                if (price != null) {
                    price.setText(o.getPrice());
                }
                if (supplier != null) {
                    supplier.setText(o.getSupplier());
                }
                // adds darkmode for the new entries as they are made
                if (DarkModeOn)
                {
                    product.setTextColor(Color.parseColor("#e8e4d6"));
                    price.setTextColor(Color.parseColor("#e8e4d6"));
                    supplier.setTextColor(Color.parseColor("#e8e4d6"));;
                }
                // adds the contents of the entry to the shopping list
                listButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        openHelper = new DatabaseConfig(getContext());
                        database = openHelper.getWritableDatabase();

                        Cursor cursor = database.rawQuery("SELECT * FROM WalmartProducts WHERE Name LIKE '%" + o.getProduct() + "%'", null);

                        if (cursor.moveToFirst()) {
                            ContentValues values = new ContentValues();
                            values.put("ItemName", o.getProduct());
                            values.put("Provider", o.getSupplier());
                            values.put("PictureURL", cursor.getString(2));
                            values.put("ItemValue", o.getPrice());
                            database.insert("ShoppingList", null, values);
                        }

                        cursor.close();
                        database.close();
                    }
                });
                // Calls for directions to the store to buy the product
                wantButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), DirectionsActivity.class);
                        String holder = supplier.getText().toString();
                        String destination = holder.substring(holder.lastIndexOf(":" + 1));
                        intent.putExtra("type", "direct");
                        intent.putExtra("destination", destination);
                        startActivity(intent);
                    }
                });
            }
            return v;
        }

    }

    // Basic object to hold a product
    class ResultEntry {
        private String product;
        private String price;
        private String supplier;

        public ResultEntry(String product, String price, String supplier) {
            this.product = product;
            this.price = price;
            this.supplier = supplier;
        }

        public String getProduct() {
            return product;
        }

        public String getPrice() {
            return price;
        }

        public String getSupplier() { return  supplier; }
    }

    class ResultTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("IWantThat", "pre execute!");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // calls get options to find all possible items that match the search
            entries = GetOptions();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("IWantThat", "post execute!");
            //if the entries list isnt empty it populates the result view
            if (entries.isEmpty() == false) {
                noResultText.setText("");
                noResultText.setVisibility(View.GONE);
                resultAdapter = new ResultAdapter(getContext(), R.layout.result_entry, entries);
                resultAdapter.notifyDataSetChanged();
                //set the adapter of the ListView
                ResultView.setAdapter(resultAdapter);
            }
            // if there are no entries it cleans the list view and displays a default message
            else {
                noResultText.setText("Sorry No entries were found!");
                noResultText.setVisibility(View.VISIBLE);

                resultAdapter = new ResultAdapter(getContext(), R.layout.result_entry, entries);
                resultAdapter.notifyDataSetChanged();
                //set the adapter of the ListView
                ResultView.setAdapter(resultAdapter);
            }
        }
    }

    private ArrayList<ResultEntry> GetOptions()
    {
        ArrayList<ResultEntry> StoreOptions = new ArrayList<ResultEntry>();

        this.openHelper = new DatabaseConfig(getContext());
        this.database = openHelper.getWritableDatabase();

        // grabs all database master entries that contain the entered search
        numOfResultsFound = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM Products WHERE ItemName LIKE '%" + SearchTarget + "%'", null);
        cursor.moveToFirst();
        // this is the expandable handling area, right now it only checks for a walmart entry.
        while (!cursor.isAfterLast()) {
            if (!cursor.isNull(2))
            {
                // finds the walmart id of the product and generates a query for the API
                String walmartIdQuery = "http://api.walmartlabs.com/v1/items/" + cursor.getString(2) + "?apiKey=jg6ghhrss9kkkqwqwbdw2s2x&format=json";
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                String walmartResult = "";
                String itemPrice= "";
                try {
                    // sends the query and buffers the response
                    URL url = new URL(walmartIdQuery);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();


                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line+"\n");
                    }

                    walmartResult = buffer.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try
                {
                    // Searches the returned JSON for the sale price of the item
                    JSONObject mainObject = new JSONObject(walmartResult);
                    itemPrice = String.format("%.2f", Double.parseDouble(mainObject.getString("salePrice")));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                // adds the complete entry to the display list
                StoreOptions.add(new ResultEntry(cursor.getString(1), "$" + itemPrice, "Walmart:1000 Taylor Ave, Winnipeg, MB"));
                numOfResultsFound++;
            }

            cursor.moveToNext();
        }
        cursor.close();
        database.close();

        return StoreOptions;
    }
}
