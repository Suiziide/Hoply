package com.example.hoply;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hoply.db.HoplyPost;
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
        while (user == null){
            user = repo.returnUserFromId(hoplyPost.getUserId());
        }
        while(!(user.getUserId().equalsIgnoreCase(hoplyPost.getUserId()))){
            user = repo.returnUserFromId(hoplyPost.getUserId());
        }
        holder.user.setText(user.getUserName());
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
        private FrameLayout frameLayout;

        RecyclerViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.text_view_user);
            content = view.findViewById(R.id.text_view_content);
            frameLayout = view.findViewById(R.id.frame_layout);

        }
    }
}
