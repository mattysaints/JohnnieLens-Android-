package com.example.johnnielens.ui.main;

import android.os.AsyncTask;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.johnnielens.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world from section: " + input;
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }

    private MutableLiveData<List<Product>> productAmazon = new MutableLiveData<>();
    private MutableLiveData<List<Product>> productEbay = new MutableLiveData<>();

    public MutableLiveData<List<Product>> getListProductAmazon(String keywords) throws IOException, JSONException, ExecutionException, InterruptedException {
        requestSerpwow("Amazon",keywords);
        return productAmazon;
    }

    public MutableLiveData<List<Product>> getListProductEbay(String keywords) throws IOException, JSONException, ExecutionException, InterruptedException {
        requestSerpwow("Ebay",keywords);
        return productEbay;
    }

    public void setProductAmazon(MutableLiveData<List<Product>> productAmazon) {
        this.productAmazon = productAmazon;
    }

    public void setProductEbay(MutableLiveData<List<Product>> productEbay) {
        this.productEbay = productEbay;
    }

    private void requestSerpwow (String site, String keywords) throws IOException, JSONException, ExecutionException, InterruptedException {
        String res = new getJson().execute(site,keywords).get();
        List<Product> list = parseJSON(new JSONObject(res),site);
        if(site.equals("Amazon")){productAmazon.setValue(list);} else {productEbay.setValue(list);}
    }

    private class getJson extends AsyncTask<String,Void,String> {

        private String json;

        @Override
        protected String doInBackground(String... strings) {
            json = downloadJsonFile(strings[0],strings[1]);
            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        private String downloadJsonFile(String site, String keywords) {
            StringBuilder ris = new StringBuilder();
            URL url;
            try {
                if(site.equals("Amazon")){
                    url = new URL("https://api.serpwow.com/live/search?api_key=FEDDA81A3777462E8DC5C167D453F49E&engine=amazon&q="+convertString(keywords)+"&amazon_domain=amazon.it&output=json");
                } else { url = new URL("https://api.serpwow.com/live/search?api_key=FEDDA81A3777462E8DC5C167D453F49E&engine=ebay&q="+convertString(keywords)+"&ebay_domain=ebay.it&output=json");}
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");

                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();

                while (line!=null){
                    ris.append(line).append("\n");
                    line = reader.readLine();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ris.toString();
        }
    }

    private String convertString (String key){
        String temp = key.replaceAll(",","+");
        temp = temp.replace("[","");
        temp = temp.replace("]","");
        temp = temp.replaceAll(" ","");
        return temp;
    }

    private List<Product> parseJSON(JSONObject jsonObject, String site) throws JSONException, MalformedURLException {
        List<Product> result = new ArrayList<>();
        if (site.equals("Ebay")) { //Parse Ebay
            JSONArray jsonArray = jsonObject.getJSONArray("ebay_results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pro = jsonArray.getJSONObject(i);
                String name = pro.getString("title");
                String urlImage = pro.getString("image");
                URL url = new URL(pro.getString("link"));
                try{
                    Double price = pro.getJSONArray("prices").getJSONObject(0).getDouble("value");
                    result.add(new Product(name, urlImage, url, price));
                } catch (JSONException e){
                    result.add(new Product(name, urlImage, url, null));
                }
            }
        } else { //Parse AMAZON
            JSONArray jsonArray = jsonObject.getJSONArray("amazon_results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pro = jsonArray.getJSONObject(i);
                String name = pro.getString("title");
                String urlImage = pro.getString("image");
                URL url = new URL(pro.getString("link"));
                try{
                    Double price = pro.getJSONArray("prices").getJSONObject(0).getDouble("value");
                    result.add(new Product(name, urlImage, url, price));
                } catch (JSONException e){
                    result.add(new Product(name, urlImage, url, null));
                }
            }
        }
        return result;
    }
}

