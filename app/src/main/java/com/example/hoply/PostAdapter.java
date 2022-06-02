package com.example.hoply;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hoply.db.HoplyComment;
import com.example.hoply.db.HoplyDatabase;
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
            ImageView v = (ImageView) view;
            v.setEnabled(false);
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
            v.setEnabled(true);
        });
        holder.postNeutralReactionsIMG.setOnClickListener(view -> {
            ImageView v = (ImageView) view;
            v.setEnabled(false);
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
            v.setEnabled(true);
        });


        holder.viewComments.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, ViewPostPage.class);
            intent.putExtra("POSTID", hoplyPost.getPostId().intValue());
            context.startActivity(intent);
        });
    }

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
