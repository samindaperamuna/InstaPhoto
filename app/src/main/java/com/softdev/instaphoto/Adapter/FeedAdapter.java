package com.softdev.instaphoto.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.softdev.instaphoto.Configuration.AppHandler;
import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import static com.softdev.instaphoto.Configuration.Config.ACTION_LIKE_BUTTON_CLICKED;
import static com.softdev.instaphoto.Configuration.Config.ACTION_LIKE_IMAGE_CLICKED;
import static com.softdev.instaphoto.Configuration.Config.FEED_TYPE_DEFAULT;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Feed> feedItems = new ArrayList<>();
    private Context context;
    private OnFeedItemClickListener onFeedItemClickListener;
    int totalItems;
    //private GestureDetector gestureDetector;
    HashTagHelper hashTagHelper;

    public FeedAdapter(Context context, ArrayList<Feed> feedItems) {
        this.context = context;
        this.feedItems = feedItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FEED_TYPE_DEFAULT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
            CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
            setupViews(view, cellFeedViewHolder);
            return cellFeedViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((CellFeedViewHolder) holder).bindView(feedItems.get(position));
        ((CellFeedViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
        hashTagHelper = HashTagHelper.Creator.create(context.getResources().getColor(R.color.colorPrimary), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {
                onFeedItemClickListener.onHashTagPressed(hashTag);
            }
        });
        hashTagHelper.handle(((CellFeedViewHolder) holder).txtDescription);
        Picasso.with(context)
                .load(feedItems.get(position).getIcon())
                .error(R.drawable.ic_people)
                .placeholder(R.drawable.ic_people)
                .into(((CellFeedViewHolder) holder).photo);
        Picasso.with(context)
                .load(feedItems.get(position).getContent())
                .into(((CellFeedViewHolder) holder).imgContent, new Callback() {
                    @Override
                    public void onSuccess() {
                        ((CellFeedViewHolder) holder).progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        if (feedItems.get(position).getUser_id().equals(String.valueOf(AppHandler.getInstance().getDataManager().getString("id", "")))) {
            ((CellFeedViewHolder) holder).btnMore.setVisibility(View.VISIBLE);
        } else {
            ((CellFeedViewHolder) holder).btnMore.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return FEED_TYPE_DEFAULT;
    }

    @Override
    public int getItemCount() {
        return totalItems;
    }

    public void updateItems(boolean animated) {
        if (animated) {
            notifyItemRangeInserted(getItemCount(), feedItems.size());
            totalItems = feedItems.size();
        } else {
            notifyDataSetChanged();
            totalItems = feedItems.size();
        }
    }
    private void setupViews(final View view, final CellFeedViewHolder cellFeedViewHolder) {
//         gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
//            public boolean onDoubleTap(MotionEvent e) {
//                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
//                boolean isLiked = feedItems.get(adapterPosition).isLiked();
//                feedItems.get(adapterPosition).setLiked(!isLiked);
//                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
//                return true;
//            }
//        });
        cellFeedViewHolder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.imgContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                boolean isLiked = feedItems.get(adapterPosition).isLiked();
                feedItems.get(adapterPosition).setLiked(true);
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                onFeedItemClickListener.onLikeClick(view, cellFeedViewHolder.getAdapterPosition(), 1);
            }
        });
        cellFeedViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                boolean isLiked = feedItems.get(adapterPosition).isLiked();
                if (!isLiked) {
                    feedItems.get(adapterPosition).likes++;
                } else {
                    feedItems.get(adapterPosition).likes--;
                }
                feedItems.get(adapterPosition).setLiked(!isLiked);
                notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);
                onFeedItemClickListener.onLikeClick(view, cellFeedViewHolder.getAdapterPosition(), isLiked ? 0 : 1);
            }
        });
        cellFeedViewHolder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onProfileClick(cellFeedViewHolder.txtName, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onProfileClick(cellFeedViewHolder.photo,cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.txtLikesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onLikesClick(v, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.txtViewComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(v, cellFeedViewHolder.getAdapterPosition());
            }
        });
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position);
        void onMoreClick(View v, int position);
        void onProfileClick(View v, int position);
        void onLikeClick(View v, int position, int action);
        void onLikesClick(View v, int position);
        void onHashTagPressed(String hashTag);
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {

        CircleImageView photo;
        ImageView imgContent;
        ImageView imgLike;
        ImageButton btnComments;
        ImageButton btnLike;
        ImageButton btnMore;
        TextView txtDescription;
        TextView txtDate;
        TextView txtName;
        TextView txtViewComments;
        TextSwitcher txtLikesCounter;
        Feed feedItem;
        ProgressBar progressBar;

        public CellFeedViewHolder(View view) {
            super(view);
            this.photo = (CircleImageView) view.findViewById(R.id.photo);
            this.btnLike = (ImageButton) view.findViewById(R.id.btnLike);
            this.btnComments = (ImageButton) view.findViewById(R.id.btnComment);
            this.btnMore = (ImageButton) view.findViewById(R.id.btnMore);
            this.txtLikesCounter = (TextSwitcher) view.findViewById(R.id.txtLikesCounter);
            this.txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            this.txtDate = (TextView) view.findViewById(R.id.txtDate);
            this.imgContent = (ImageView) view.findViewById(R.id.content);
            this.txtName = (TextView) view.findViewById(R.id.txtName);
            this.imgLike = (ImageView) view.findViewById(R.id.imgLike);
            this.txtViewComments = (TextView) view.findViewById(R.id.txtViewComments);
            this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }

        public void bindView(Feed feedItem) {
            this.feedItem = feedItem;
            btnLike.setImageResource(feedItem.isLiked() ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
            txtDescription.setText(feedItem.getDescription());
            txtName.setText(feedItem.getName());
            txtLikesCounter.setCurrentText(photo.getResources().getQuantityString(
                    R.plurals.likes_count, feedItem.likes, feedItem.likes
            ));
            txtDate.setText(AppHandler.getTimestamp(feedItem.getCreation()));
            txtViewComments.setVisibility(Integer.parseInt(feedItem.getComments()) > 1 ? View.VISIBLE : View.GONE);
            txtViewComments.setText("view all $c comments".replace("$c", feedItem.getComments()));
        }

        public Feed getFeedItem() {
            return feedItem;
        }
    }
}
