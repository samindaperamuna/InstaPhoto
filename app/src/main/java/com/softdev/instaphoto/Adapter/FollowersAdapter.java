package com.softdev.instaphoto.Adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;
    private boolean delayEnterAnimation = true;
    private ArrayList<User> followersList;
    private ArrayList<User> followingList;
    private boolean isFollowingView;
    private OnFollowersItemClickListener OnFollowersItemClickListener;

    public FollowersAdapter(Context context, ArrayList<User> followersList, ArrayList<User> followingList, boolean isFollowingView) {
        this.context = context;
        this.followersList = followersList;
        this.followingList = followingList;
        this.isFollowingView = isFollowingView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = viewType == 1 ? LayoutInflater.from(context).inflate(R.layout.item_following, parent, false) : LayoutInflater.from(context).inflate(R.layout.item_following, parent, false);
        return new FollowersViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return isFollowingView ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        final FollowersViewHolder holder = (FollowersViewHolder) viewHolder;
        holder.txtName.setText(getCurrentList().get(position).getName());
        Picasso.with(context)
                .load(getCurrentList().get(position).getIcon())
                .placeholder(R.drawable.ic_people)
                .error(R.drawable.ic_people)
                .into(holder.icon);
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnFollowersItemClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnFollowersItemClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
    }

    private ArrayList<User> getCurrentList() {
        return isFollowingView ? followingList : followersList;
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(100);
            view.setAlpha(0.f);
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(delayEnterAnimation ? 20 * (position) : 0)
                    .setInterpolator(new DecelerateInterpolator(2.f))
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true;
                        }
                    })
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return isFollowingView ? followingList.size() : followersList.size();
    }
    public void updateItems() {
        notifyDataSetChanged();
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setOnFollowersItemClickListener(OnFollowersItemClickListener OnFollowersItemClickListener) {
        this.OnFollowersItemClickListener = OnFollowersItemClickListener;
    }

    public interface OnFollowersItemClickListener {
        void onItemClick(View v, int position);
    }

    public static class FollowersViewHolder extends RecyclerView.ViewHolder {
        CircleImageView icon;
        TextView txtName;
        View view;

        public FollowersViewHolder(View view) {
            super(view);
            icon = (CircleImageView) view.findViewById(R.id.photo);
            txtName = (TextView) view.findViewById(R.id.txtName);
            this.view = view;
        }
    }
}
