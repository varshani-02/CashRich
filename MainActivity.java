package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView coinRecyclerView;
    private CoinListAdapter coinListAdapter;

    private TextView itemCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar appToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(appToolbar);

        itemCountTextView = findViewById(R.id.itemCountTextView);
        coinRecyclerView = findViewById(R.id.coinRecyclerView);
        coinRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coinListAdapter = new CoinListAdapter();
        coinRecyclerView.setAdapter(coinListAdapter);

        fetchDataWithAsyncTask();
    }

    private void setSupportActionBar(Toolbar toolbar) {
        // Custom implementation for setting the support action bar
        // You may add your implementation here
    }

    private void fetchDataWithAsyncTask() {
        new FetchCoinDataTask().execute();
    }

    private class FetchCoinDataTask extends AsyncTask<Void, Void, List<CoinData>> {

        @Override
        protected List<CoinData> doInBackground(Void... voids) {
            List<CoinData> coinDataList = new ArrayList<>();

            // Your API call and data retrieval logic here...
            // Populating coinDataList with CoinData objects

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=BTC,ETH,LTC")
                    .addHeader("X-CMC_PRO_API_KEY", "27ab17d1-215f-49e5-9ca4-afd48810c149")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);

                    if (jsonObject.has("data")) {
                        JSONObject data = jsonObject.getJSONObject("data");

                        String[] symbols = new String[]{"BTC", "ETH", "LTC"};
                        int index = 0;
                        while (index < symbols.length) {
                            String symbol = symbols[index];
                            JSONObject coinDataJson = data.getJSONObject(symbol);
                            double change24h = coinDataJson.getJSONObject("quote").getJSONObject("USD").getDouble("percent_change_24h");
                            int rank = coinDataJson.getInt("cmc_rank");
                            double priceUSD = coinDataJson.getJSONObject("quote").getJSONObject("USD").getDouble("price");

                            CoinData coinData = new CoinData(symbol, "24h Change: " + change24h + "%, Rank: " + rank + ", Price (USD): " + priceUSD, change24h);
                            coinDataList.add(coinData);

                            index++;
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return coinDataList;
        }

        @Override
        protected void onPostExecute(List<CoinData> coinDataList) {
            super.onPostExecute(coinDataList);
            coinListAdapter.setData(coinDataList);

            // Update the item count TextView after setting the data
            itemCountTextView.setText("Count: " + coinDataList.size());
        }
    }

    private static class CoinListAdapter extends RecyclerView.Adapter<CoinListAdapter.CoinViewHolder> {

        private List<CoinData> coinDataList = new ArrayList<>();

        public void setData(List<CoinData> coinData) {
            coinDataList.clear();
            coinDataList.addAll(coinData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coin, parent, false);
            return new CoinViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
            CoinData coinData = coinDataList.get(position);
            holder.bind(coinData);
        }

        @Override
        public int getItemCount() {
            return coinDataList.size();
        }

        static class CoinViewHolder extends RecyclerView.ViewHolder {
            private TextView symbolTextView, changeTextView;
            private ImageView arrowImageView;

            public CoinViewHolder(@NonNull View itemView) {
                super(itemView);
                symbolTextView = itemView.findViewById(R.id.symbolTextView);
                changeTextView = itemView.findViewById(R.id.changeTextView);
                arrowImageView = itemView.findViewById(R.id.arrowImageView);
            }

            public void bind(CoinData coinData) {
                symbolTextView.setText(coinData.getSymbol());
                changeTextView.setText(coinData.getChange());

                // Set the arrow icon based on positive or negative change
                if (coinData.getChangeDirection() > 0) {
                    arrowImageView.setImageResource(R.drawable.ic_arrow_up);
                    arrowImageView.setColorFilter(Color.parseColor("#00FF00"));
                } else {
                    arrowImageView.setImageResource(R.drawable.ic_arrow_down);
                    arrowImageView.setColorFilter(Color.parseColor("#FF0000"));
                }
            }
        }
    }

    private static class CoinData {
        private String symbol;
        private String change;
        private double changeDirection; // Positive or negative change

        public CoinData(String symbol, String change, double changeDirection) {
            this.symbol = symbol;
            this.change = change;
            this.changeDirection = changeDirection;
        }

        // Getter methods for fields
        public String getSymbol() {
            return symbol;
        }

        public String getChange() {
            return change;
        }

        public double getChangeDirection() {
            return changeDirection;
        }
    }
}
