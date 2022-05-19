package com.example.hoply;

import android.app.Application;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hoply.db.HoplyPost;
import com.example.hoply.db.HoplyReaction;
import com.example.hoply.db.HoplyUser;
import com.example.hoply.db.Repo;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.RecyclerViewHolder>{

    private List<HoplyPost> postList = new ArrayList<>();
    private Application application;
    private Repo repo;

    public PostAdapter(Application application){
        this.application = application;
        repo = new Repo(application);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        HoplyPost hoplyPost = postList.get(position);
        HoplyUser user = repo.returnUserFromId(hoplyPost.getUserId());
        Integer likes = repo.returnReactionsFromTypeAndID(hoplyPost.getPostId(), 1);
        Integer dislikes = repo.returnReactionsFromTypeAndID(hoplyPost.getPostId(), 2);
        Integer neutrals = repo.returnReactionsFromTypeAndID(hoplyPost.getPostId(), 3);
        holder.user.setText(user.getUserName());
        holder.content.setText(hoplyPost.getContent());
        holder.postLikeReactions.setText(likes.toString());
        holder.postDislikeReactions.setText(dislikes.toString());
        holder.postNeutralReactions.setText(neutrals.toString());

        holder.postLikeReactionsIMG.setOnClickListener(view -> {
            if(!hasReacted(user, hoplyPost.getPostId(), 1)) {
                repo.insertReaction(new HoplyReaction(hoplyPost.getUserId(), hoplyPost.getPostId(), 1));
                notifyDataSetChanged();
            }
        });
        holder.postDislikeReactionsIMG.setOnClickListener(view -> {
            if(!hasReacted(user, hoplyPost.getPostId(), 2)) {
                repo.insertReaction(new HoplyReaction(hoplyPost.getUserId(), hoplyPost.getPostId(), 2));
                notifyDataSetChanged();
            }
        });
        holder.postNeutralReactionsIMG.setOnClickListener(view -> {
            if(!hasReacted(user, hoplyPost.getPostId(), 3)) {
                repo.insertReaction(new HoplyReaction(hoplyPost.getUserId(), hoplyPost.getPostId(), 3));
                notifyDataSetChanged();
            }
        });
    }

    private boolean hasReacted(HoplyUser user, Integer postId, int reactionType) {

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
        private final TextView user;
        private final TextView content;
        private final TextView postLikeReactions;
        private final TextView postDislikeReactions;
        private final TextView postNeutralReactions;
        private final ImageView postLikeReactionsIMG;
        private final ImageView postDislikeReactionsIMG;
        private final ImageView postNeutralReactionsIMG;

        RecyclerViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.text_view_user);
            content = view.findViewById(R.id.text_view_content);
            postLikeReactions = view.findViewById(R.id.text_view_postLikeReactions);
            postDislikeReactions = view.findViewById(R.id.text_view_postDislikeReactions);
            postNeutralReactions = view.findViewById(R.id.text_view_postNeutralReactions);
            postLikeReactionsIMG = view.findViewById(R.id.image_view_postLikeReactionsIMG);
            postDislikeReactionsIMG = view.findViewById(R.id.image_view_postDislikeReactionsIMG);
            postNeutralReactionsIMG = view.findViewById(R.id.image_view_postNeutralReactionsIMG);
        }
    }
}
