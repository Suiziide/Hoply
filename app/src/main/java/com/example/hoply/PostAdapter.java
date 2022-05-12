package com.example.hoply;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hoply.db.HoplyPost;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.RecyclerViewHolder>{

    private List<HoplyPost> postList = new ArrayList<>();

    public PostAdapter(){
    }


    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        HoplyPost hoplyPost = postList.get(position);
        holder.user.setText(hoplyPost.getUserId());
        holder.content.setText(hoplyPost.getContent());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void addItems(List<HoplyPost> postList){
        this.postList = postList;
        notifyDataSetChanged();
    }


    static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        private TextView user;
        private TextView content;

        RecyclerViewHolder(View view) {
            super(view);
            user = (TextView) view.findViewById(R.id.text_view_user);
            content = (TextView) view.findViewById(R.id.text_view_content);
        }
    }
}
