package de.tum.in.tumcampus.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MVVObject;

/**
 * Created by enricogiga on 17/06/2015.
 */
public class MvvAdapter extends BaseAdapter{


    private MVVObject data;
    private View.OnClickListener listener;
    public static class ViewHolder{
        public ImageView icon;
        public TextView number;
        public TextView station;
        public TextView minutes;

    }

    public MvvAdapter(MVVObject data, View.OnClickListener listener){
        this.data = data;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        if (data != null)
            return data.getResultList().size();
        Utils.log("Warning! dataList is empty!");
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (data != null)
            return data.getResultList().get(position);
        Utils.log("Warning! dataList is empty!");
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            if (data.isSuggestion()){

            }else {

            }
            convertView.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder)convertView.getTag();



        return null;
    }
}
