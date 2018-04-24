package com.example.android.bibdiscovery.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bibdiscovery.R;
import com.example.android.bibdiscovery.models.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lottejespers.
 */
public class ScoreAdapter extends ArrayAdapter<Score> {

    private List<Score> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView rang;
        TextView txtName;
        TextView txtScore;
    }

    public ScoreAdapter(List<Score> data, Context context) {
        super(context, R.layout.score_row_item, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Score score = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.score_row_item, parent, false);
            viewHolder.rang = (TextView) convertView.findViewById(R.id.rang);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtScore = (TextView) convertView.findViewById(R.id.score);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.rang.setText(String.valueOf(position + 1));
        viewHolder.rang.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/Oswald-Regular.ttf"));
        viewHolder.txtName.setText(score.getName());
        viewHolder.txtName.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/Oswald-Regular.ttf"));
        viewHolder.txtScore.setText(score.getScore());
        viewHolder.txtScore.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/Oswald-Regular.ttf"));

        // Return the completed view to render on screen
        return convertView;
    }
}
