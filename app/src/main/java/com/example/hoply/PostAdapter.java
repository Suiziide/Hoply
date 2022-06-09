package com.example.hoply;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.RecyclerViewHolder>{

    private List<HoplyPost> postList = new ArrayList<>();
    private Application application;
    private Repo repo;
    private final ExecutorCompletionService<Boolean> completionService = new
            ExecutorCompletionService<>(Executors.newSingleThreadExecutor());

    /**
     * Constructor for the adapter which provides a binding from a data set to a recyclerview
     * @param application
     */
    public PostAdapter(Application application){
        this.application = application;
        repo = new Repo(application);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
    }

    // Defines what to do with each element in the viewmodel lists
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        HoplyPost hoplyPost = postList.get(position);
        HoplyUser userOnPost = repo.returnUserFromId(hoplyPost.getUserId());
        HoplyUser currentUser = LoginPage.currentUser;
        Integer likes = repo.returnReactionsFromTypeAndID(hoplyPost.getPostId(), 1);
        Integer dislikes = repo.returnReactionsFromTypeAndID(hoplyPost.getPostId(), 2);
        Integer neutrals = repo.returnReactionsFromTypeAndID(hoplyPost.getPostId(), 3);
        holder.user.setText(userOnPost.getUserName());
        holder.content.setText(hoplyPost.getContent());
        holder.postLikeReactions.setText(likes.toString());
        holder.postDislikeReactions.setText(dislikes.toString());
        holder.postNeutralReactions.setText(neutrals.toString());

        holder.postLikeReactionsIMG.setOnClickListener(view -> {
            if(hasReacted(currentUser, hoplyPost.getPostId(), 1)) {
                completionService.submit(() -> {
                    repo.insertLocalReaction(new HoplyReaction(currentUser.getUserId(), hoplyPost.getPostId(), 1, System.currentTimeMillis()));
                    return true;
                });
                try {
                    completionService.take().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        });
        holder.postDislikeReactionsIMG.setOnClickListener(view -> {
            if (hasReacted(currentUser, hoplyPost.getPostId(), 2)) {
                completionService.submit(() -> {
                    repo.insertLocalReaction(new HoplyReaction(currentUser.getUserId(), hoplyPost.getPostId(), 2, System.currentTimeMillis()));
                    return true;
                });
                try {
                    completionService.take().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        });
        holder.postNeutralReactionsIMG.setOnClickListener(view -> {
            if(hasReacted(currentUser, hoplyPost.getPostId(), 3)) {
                completionService.submit(() -> {
                    repo.insertLocalReaction(new HoplyReaction(currentUser.getUserId(), hoplyPost.getPostId(), 3, System.currentTimeMillis()));
                    return true;
                });

                try {
                    completionService.take().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        });


        holder.viewComments.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, ViewPostPage.class);
            intent.putExtra("POSTID", hoplyPost.getPostId().intValue());
            context.startActivity(intent);
        });
    }

    /**
     * Returns true if a user has not reacted or if a user has reacted to something else than the
     * one being clicked on. Returns false if the user has reacted on the one being clicked on.
     * This is responsible for deleting and updating the status of comments on a post
     * @param user the user reacting to the post
     * @param postId the postID of the post being reacted to
     * @param reaction the reaction being made
     * @return
     */
    private boolean hasReacted(HoplyUser user, Integer postId, int reaction) {
        Integer reactionType = repo.returnUserReactionToPost(user.getUserId(), postId);
        String request = "https://caracal.imada.sdu.dk/app2022/reactions?user_id=eq." + user.getUserId() + "&post_id=eq." + postId;
        if (reactionType == null) {
            return true;
        } else if (reactionType == reaction) {
            repo.deleteDataFromRemoteDB(request);
            repo.removeUserReactionFromPost(user.getUserId(), postId);
            return false;
        } else {
            repo.deleteDataFromRemoteDB(request);
            repo.removeUserReactionFromPost(user.getUserId(), postId);
            return true;
        }
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
        private final TextView viewComments;

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
            viewComments = view.findViewById(R.id.clickable_text_view_View_comments);
        }
    }
}