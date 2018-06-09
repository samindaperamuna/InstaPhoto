package com.softdev.instaphoto.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.R;
import com.softdev.instaphoto.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ExploreGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PHOTO_ANIMATION_DELAY = 100;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    private final Context context;
    private final int cellSize;
    private ArrayList<Feed> exploreList;
    private boolean lockedAnimations = false;
    private int lastAnimatedItem = -1;
    private OnExploreItemClickListener OnExploreItemClickListener;

    public ExploreGridAdapter(Context context, ArrayList<Feed> exploreList) {
        this.context = context;
        this.cellSize = Utils.getScreenWidth(context) / 3;
        this.exploreList = exploreList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo_grid, parent, false);
        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
        layoutParams.height = cellSize;
        layoutParams.width = cellSize;
        layoutParams.setFullSpan(false);
        view.setLayoutParams(layoutParams);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        bindPhoto((PhotoViewHolder) holder, position);
        ((PhotoViewHolder) holder).photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnExploreItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
            }
        });
    }

    private void bindPhoto(final PhotoViewHolder holder, int position) {
        Picasso.with(context)
                .load(exploreList.get(position).getContent())
                .resize(cellSize, cellSize)
                .centerCrop()
                .into(holder.photo, new Callback() {
                    @Override
                    public void onSuccess() {
                        animatePhoto(holder);
                    }

                    @Override
                    public void onError() {

                    }
                });
        if (lastAnimatedItem < position) lastAnimatedItem = position;
    }

    private void animatePhoto(PhotoViewHolder viewHolder) {
        if (!lockedAnimations) {
            if (lastAnimatedItem == viewHolder.getAdapterPosition()) {
                setLockedAnimations(true);
            }

            long animationDelay = PHOTO_ANIMATION_DELAY + viewHolder.getAdapterPosition() * 30;

            viewHolder.layout.setScaleY(0);
            viewHolder.layout.setScaleX(0);

            viewHolder.layout.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .setDuration(200)
                    .setInterpolator(INTERPOLATOR)
                    .setStartDelay(animationDelay)
                    .start();
        }
    }

    public void updateItems(boolean animated) {
        if (animated) {
            notifyItemRangeInserted(0, exploreList.size());
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return exploreList.size();
    }

    public void setOnExploreItemClickListener(OnExploreItemClickListener OnExploreItemClickListener) {
        this.OnExploreItemClickListener = OnExploreItemClickListener;
    }

    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
    }

    public interface OnExploreItemClickListener {
        void onItemClick(View v, int position);
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layout;
        ImageView photo;

        public PhotoViewHolder(View view) {
            super(view);
            this.photo = (ImageView) view.findViewById(R.id.photo);
            this.layout = (FrameLayout) view.findViewById(R.id.mainLayout);
        }
    }
}
