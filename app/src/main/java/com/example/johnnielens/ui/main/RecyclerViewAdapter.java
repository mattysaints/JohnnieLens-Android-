package com.example.johnnielens.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.johnnielens.Product;
import com.example.johnnielens.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Product> products;
    private Context context;

    public RecyclerViewAdapter(List<Product> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.layout_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Product product = products.get(position);
        holder.nameProduct.setText(product.getName());
        if (product.productValid()){
            Picasso.get().load(product.getImage()).into(holder.imageProduct);
            if(product.getPrice()!=null) {
                holder.price.setText(product.getPrice().toString());
            }
            if(product.getUrl()!=null){
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(product.getUrlString()));
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameProduct;
        private ImageView imageProduct;
        private TextView price;
        private ImageButton imageButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameProduct = itemView.findViewById(R.id.nameProduct);
            imageProduct = itemView.findViewById(R.id.imageView);
            price = itemView.findViewById(R.id.price);
            imageButton = itemView.findViewById(R.id.imageButton);
        }
    }
}
