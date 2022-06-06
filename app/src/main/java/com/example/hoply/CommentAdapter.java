package com.example.hoply;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hoply.db.HoplyComment;
import com.example.hoply.db.HoplyUser;
import com.example.hoply.db.Repo;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter  extends RecyclerView.Adapter<CommentAdapter.RecyclerViewHolder>{

    private List<HoplyComment> commentList = new ArrayList<>();
    private Application application;
    private Repo repo;

    public CommentAdapter(Application application){
        this.application = application;
        repo = new Repo(application);
    }

    @NonNull
    @Override
    public CommentAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentAdapter.RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.RecyclerViewHolder holder, int position) {
        HoplyComment hoplyComment = commentList.get(position);
        HoplyUser user = repo.returnUserFromId(hoplyComment.getUserId());
        holder.user.setText(user.getUserName());
        holder.content.setText(hoplyComment.getContent());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void addItems(List<HoplyComment> commentList){
        this.commentList = commentList;
        notifyDataSetChanged();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        private TextView user;
        private TextView content;

        RecyclerViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.comment_user);
            content = view.findViewById(R.id.comment_content);
        }
    }
}