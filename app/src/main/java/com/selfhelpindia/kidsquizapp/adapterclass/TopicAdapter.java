package com.selfhelpindia.kidsquizapp.adapterclass;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.selfhelpindia.kidsquizapp.QuizActivity;
import com.selfhelpindia.kidsquizapp.R;
import com.selfhelpindia.kidsquizapp.modelclass.TopicModelClass;

import java.util.ArrayList;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder>{

    Context context;
    ArrayList<TopicModelClass> topicModelClasses;
    public TopicAdapter(Context context, ArrayList<TopicModelClass> topicModelClasses) {
        this.context = context;
        this.topicModelClasses = topicModelClasses;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.topic_category,null);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        final TopicModelClass modelClass = topicModelClasses.get(position);

        holder.topicName.setText(modelClass.getCategoryName());
        Glide.with(context).load(modelClass.getCategoryImage()).into(holder.topicImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QuizActivity.class);
                intent.putExtra("categoryId",modelClass.getCategoryId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return topicModelClasses.size();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder{
        ImageView topicImage;
        TextView topicName;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            topicImage = itemView.findViewById(R.id.topic_image);
            topicName = itemView.findViewById(R.id.topic_category);
        }
    }
}
