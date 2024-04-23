package com.example.app.info_card;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;

import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {
    // 设置数据列表主体
    private final List<Info> infoList;
    public InfoAdapter(List<Info> infoList) {
        this.infoList = infoList;
    }

    // 设置监听器主体
    private OnLongClickListener onLongClickListener;
    public void setOnLongClickListener(OnLongClickListener listener) {
        onLongClickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Info info = infoList.get(position);
        holder.bind(info);
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // 位移地点的TextView
        TextView location_TextView;
        // 位移时间的TextView
        TextView time_TextView;
        // 位移距离的TextView
        TextView distance_TextView;
        // 位移等级的TextView
        TextView level_TextView;
        // 位移图像的ImageView
        ImageView map_ImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            location_TextView = itemView.findViewById(R.id.info_location);
            time_TextView = itemView.findViewById(R.id.info_time);
            distance_TextView = itemView.findViewById(R.id.info_distance);
            level_TextView = itemView.findViewById(R.id.info_level);
            map_ImageView = itemView.findViewById(R.id.displacement_location_image);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onLongClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Info info = infoList.get(position);
                            onLongClickListener.onLongClick(position, info);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        public void bind(Info info) {
            location_TextView.setText(info.getLocation());
            time_TextView.setText(info.getTime());
            distance_TextView.setText(info.getDistance());
            level_TextView.setText(info.getLevel());
            map_ImageView.setImageBitmap(info.getMap());
        }
    }
}
