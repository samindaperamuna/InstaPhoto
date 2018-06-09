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
import com.softdev.instaphoto.Dataset.Notification;
import com.softdev.instaphoto.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;
    private ArrayList<Notification> notificationArrayList;
    private OnNotificationItemClickListener onNotificationItemClickListener;

    public NotificationsAdapter(Context context, ArrayList<Notification> notificationArrayList) {
        this.context = context;
        this.notificationArrayList = notificationArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationsAdapter.NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        runEnterAnimation(viewHolder.itemView, position);
        final NotificationsViewHolder holder = (NotificationsViewHolder) viewHolder;
        Picasso.with(context)
                .load(notificationArrayList.get(position).icon)
                .placeholder(R.drawable.ic_people)
                .error(R.drawable.ic_people)
                .into(holder.icon);
        holder.txtNotification.setText(formatNotification(notificationArrayList.get(position).name, notificationArrayList.get(position).action));
        holder.txtDate.setText(AppHandler.getTimestamp(notificationArrayList.get(position).creation));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotificationItemClickListener.onItemClick(v, holder.getAdapterPosition(), Integer.valueOf(notificationArrayList.get(position).action));
            }
        });
    }

    private String formatNotification(String name, String a) {
        int action = Integer.valueOf(a);
        if (action == 1) {
            return ":user commented on your photo.".replace(":user", name);
        } else if (action == 2) {
            return ":user likes your photo.".replace(":user", name);
        } else if (action == 3) {
            return ":user is now following you.".replace(":user", name);
        } else {
            return "";
        }
    }

    public void setOnNotificationItemClickListener(OnNotificationItemClickListener onNotificationItemClickListener) {
        this.onNotificationItemClickListener = onNotificationItemClickListener;
    }

    public interface OnNotificationItemClickListener {
        void onItemClick(View v, int position, int action);
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
        return notificationArrayList.size();
    }

    public void updateItems() {
        notifyDataSetChanged();
    }

    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    private static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView icon;
        TextView txtNotification;
        TextView txtDate;

        private NotificationsViewHolder(View view) {
            super(view);
            icon = (CircleImageView) view.findViewById(R.id.icon);
            txtNotification = (TextView) view.findViewById(R.id.txtNotification);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
        }
    }

}
