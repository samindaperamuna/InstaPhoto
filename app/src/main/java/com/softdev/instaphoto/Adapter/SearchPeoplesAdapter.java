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
import android.widget.Toast;

import com.softdev.instaphoto.Dataset.User;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchPeoplesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;
    private boolean delayEnterAnimation = true;
    private ArrayList<User> peoples;
    private OnProfileItemClickListener onProfileItemClickListener;

    public SearchPeoplesAdapter(Context context, ArrayList<User> peoples) {
        this.context = context;
        this.peoples = peoples;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new PeoplesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        runEnterAnimation(viewHolder.itemView, position);
        PeoplesViewHolder holder = (PeoplesViewHolder) viewHolder;
        holder.txtName.setText(peoples.get(position).getName());
        holder.txtUsername.setText(peoples.get(position).getUsername());
        Picasso.with(context)
                .load(peoples.get(position).getIcon())
                .placeholder(R.drawable.ic_people)
                .error(R.drawable.ic_people)
                .into(holder.icon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProfileItemClickListener.onProfileClick(v, position);
            }
        });
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
        return peoples.size();
    }

    public void updateItems() {
        notifyDataSetChanged();
    }

    public void addItem() {
        notifyItemInserted(peoples.size() - 1);
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setDelayEnterAnimation(boolean delayEnterAnimation) {
        this.delayEnterAnimation = delayEnterAnimation;
    }
    public void setOnProfileItemClickListener(OnProfileItemClickListener onProfileItemClickListener) {
        this.onProfileItemClickListener = onProfileItemClickListener;
    }

    public interface OnProfileItemClickListener {
        void onProfileClick(View v, int position);
    }
    public static class PeoplesViewHolder extends RecyclerView.ViewHolder {
        CircleImageView icon;
        TextView txtName;
        TextView txtUsername;

        public PeoplesViewHolder(View view) {
            super(view);
            icon = (CircleImageView) view.findViewById(R.id.icon);
            txtName = (TextView) view.findViewById(R.id.txtName);
            txtUsername = (TextView) view.findViewById(R.id.txtUsername);
        }
    }
}
