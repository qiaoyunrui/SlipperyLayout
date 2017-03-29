package com.juhezi.slipperylayoutsample;

import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.juhezi.slipperylayout.SlipperyLayout;

/**
 * Created by qiao1 on 2017/3/29.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTvMessage.setText("Juhezi - " + position);
        holder.mVDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.itemView.getContext(), "Delete - " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 20;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvMessage;
        private View mVDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvMessage = (TextView) itemView.findViewById(R.id.tv_show);
            mVDelete = ((SlipperyLayout) itemView).getMenuView();
        }
    }

}
