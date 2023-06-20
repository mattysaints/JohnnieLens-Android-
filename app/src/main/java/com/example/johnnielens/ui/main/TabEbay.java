package com.example.johnnielens.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.johnnielens.Product;
import com.example.johnnielens.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class TabEbay extends Fragment {
    private PageViewModel pageViewModel;
    private RecyclerView recyclerView;
    private List<Product> products = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_ebay, container, false);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        recyclerView = view.findViewById(R.id.recyclerViewEbay);

        //ottenere lista dei prodotti e settare la recyclerView
        String keys = getArguments().getString("Keys");
        try {
            products = pageViewModel.getListProductEbay(keys).getValue();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerView.Adapter adapter = new RecyclerViewAdapter(products,getContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.setHasFixedSize(true); //le cardView sono tutte delle stesse dimensioni

        return view;
    }
}
