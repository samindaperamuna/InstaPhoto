package com.softdev.instaphoto.Adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Dataset.Like;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;
    private ArrayList<Like> likes;
    private OnClickListener onClickListener;

    public LikesAdapter(Context context, ArrayList<Like> likes) {
        this.context = context;
        this.likes = likes;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_like, parent, false);
        return new LikesAdapter.LikeViewHolder(view);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        runEnterAnimation(viewHolder.itemView, position);
        LikeViewHolder holder = (LikeViewHolder) viewHolder;
        holder.txtName.setText(likes.get(position).getName());
        holder.txtUsername.setText(likes.get(position).getUsername());
        holder.btnFollow.setVisibility(likes.get(position).getUsername().equals(AppHandler.getInstance().getDataManager().getString("username", "null")) ? View.INVISIBLE : (likes.get(position).isFollowed() ? View.INVISIBLE : View.VISIBLE));
        Picasso.with(context)
                .load(likes.get(position).getIcon())
                .placeholder(R.drawable.ic_people)
                .error(R.drawable.ic_people)
                .into(holder.icon);
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(100);
            view.setAlpha(0.f);
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(0)
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
        return likes.size();
    }

    public void updateItems() {
        notifyDataSetChanged();
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    private static class LikeViewHolder extends RecyclerView.ViewHolder {
        CircleImageView icon;
        TextView txtName;
        TextView txtUsername;
        ImageView btnFollow;

        private LikeViewHolder(View view) {
            super(view);
            icon = (CircleImageView) view.findViewById(R.id.icon);
            txtUsername = (TextView) view.findViewById(R.id.txtUsername);
            txtName = (TextView) view.findViewById(R.id.txtName);
            btnFollow = (ImageView) view.findViewById(R.id.btnFollow);
        }
    }
}
