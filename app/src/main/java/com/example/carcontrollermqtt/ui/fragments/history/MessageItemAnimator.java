package com.example.carcontrollermqtt.ui.fragments.history;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.carcontrollermqtt.ui.fragments.history.MessagesAdapter.MessageViewHolder;

public class MessageItemAnimator extends SimpleItemAnimator {

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        return false;
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.clearAnimation();
        if (viewHolder instanceof MessageViewHolder) {
            dispatchAddStarting(viewHolder);
            MessageViewHolder messageHolder = (MessageViewHolder) viewHolder;
            messageHolder.itemView.setTranslationX(messageHolder.isIncoming ? -1000 : 1000);
            messageHolder.itemView.animate()
                    .translationX(0)
                    .setInterpolator(new DecelerateInterpolator(3.0f))
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            dispatchAddFinished(messageHolder);
                        }
                    })
                    .start();
        }
        return false;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        return false;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                 RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop,
                                 int toLeft, int toTop) {
        return false;
    }

    @Override
    public void runPendingAnimations() {
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
    }

    @Override
    public void endAnimations() {
    }

    @Override
    public boolean isRunning() {
        return false;
    }

}
