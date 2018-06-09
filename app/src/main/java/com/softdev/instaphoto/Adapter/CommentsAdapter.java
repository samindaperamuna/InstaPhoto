package com.softdev.instaphoto.Adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Dataset.Comment;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;
    private boolean delayEnterAnimation = true;
    private ArrayList<Comment> comments;
    private OnItemClickListener onItemClickListener;

    public CommentsAdapter(Context context, ArrayList<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        runEnterAnimation(viewHolder.itemView, position);
        CommentViewHolder holder = (CommentViewHolder) viewHolder;
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onProfileClick(v, viewHolder.getAdapterPosition());
            }
        });
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onMoreClick(v, viewHolder.getAdapterPosition());
            }
        });
        holder.txtComment.setText(Html.fromHtml(comments.get(position).getContent()));
        holder.txtDate.setText(AppHandler.getTimestamp(comments.get(position).getCreation()));
        if (comments.get(position).getUsername().equals(AppHandler.getInstance().getDataManager().getString("username", "")) && comments.get(position).getCommentId() != null) {
            holder.btnMore.setVisibility(View.VISIBLE);
        } else {
            holder.btnMore.setVisibility(View.INVISIBLE);
        }
        Picasso.with(context)
                .load(comments.get(position).getIcon())
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
        return comments.size();
    }

    public void updateItems() {
        notifyDataSetChanged();
    }

    public void addItem() {
        notifyItemInserted(comments.size() - 1);
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    public void setDelayEnterAnimation(boolean delayEnterAnimation) {
        this.delayEnterAnimation = delayEnterAnimation;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onProfileClick(View v, int position);

        void onMoreClick(View v, int position);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView icon;
        TextView txtComment;
        TextView txtDate;
        ImageButton btnMore;

        public CommentViewHolder(View view) {
            super(view);
            icon = (CircleImageView) view.findViewById(R.id.icon);
            txtComment = (TextView) view.findViewById(R.id.txtComment);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            btnMore = (ImageButton) view.findViewById(R.id.btnMore);
        }
    }
}
