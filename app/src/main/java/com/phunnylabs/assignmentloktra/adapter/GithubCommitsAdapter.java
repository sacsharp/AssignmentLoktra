package com.phunnylabs.assignmentloktra.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.phunnylabs.assignmentloktra.R;
import com.phunnylabs.assignmentloktra.models.Commit;

import java.util.ArrayList;

/**
 * Created by sachin on 06/06/17.
 */

public class GithubCommitsAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<Commit> mCommits;

    public GithubCommitsAdapter(Context context, ArrayList<Commit> commits) {
        mContext = context;
        mCommits = commits;
    }

    @Override
    public int getCount() {
        return mCommits.size();
    }

    @Override
    public Commit getItem(int i) {
        return mCommits.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (null == convertView) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            convertView = inflater.inflate(R.layout.github_commit_item, viewGroup, false);
            viewHolder = new ViewHolder();

            viewHolder.imageViewAuthorAvatar = (ImageView) convertView.findViewById(R.id.imageViewAuthorAvatar);
            viewHolder.textViewAuthorName = (TextView) convertView.findViewById(R.id.textViewAuthorName);
            viewHolder.textViewCommitId = (TextView) convertView.findViewById(R.id.textViewCommitId);
            viewHolder.textViewCommitMessage = (TextView) convertView.findViewById(R.id.textViewCommitMessage);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mContext).load(getItem(position).getAvatarURL()).asBitmap().into(new BitmapImageViewTarget(viewHolder.imageViewAuthorAvatar) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                viewHolder.imageViewAuthorAvatar.setImageDrawable(circularBitmapDrawable);
            }
        });

        viewHolder.textViewAuthorName.setText(getItem(position).getAuthorName());
        viewHolder.textViewAuthorName.setOnClickListener(view -> {
            if (getItem(position).getAuthorURL() != null && getItem(position).getAuthorURL().length() > 0) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getItem(position).getAuthorURL()));
                mContext.startActivity(i);
            }
        });

        viewHolder.textViewCommitId.setText(String.format("Id: %s", getItem(position).getCommitID()));
        viewHolder.textViewCommitId.setOnClickListener(view -> {
            if (getItem(position).getCommitURL() != null && getItem(position).getCommitURL().length() > 0) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getItem(position).getCommitURL()));
                mContext.startActivity(i);
            }
        });

        viewHolder.textViewCommitMessage.setText(getItem(position).getCommitMessage());
        return convertView;
    }

    private class ViewHolder {
        ImageView imageViewAuthorAvatar;
        TextView textViewAuthorName, textViewCommitId, textViewCommitMessage;
    }
}
