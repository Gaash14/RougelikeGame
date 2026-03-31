package com.example.rougelikegame.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.ImageSourceOption;

import java.util.List;

/**
 * Adapter for displaying image source options in a list.
 */
public class ImageSourceAdapter extends ArrayAdapter<ImageSourceOption> {

    /**
     * Interface for handling selection of an image source.
     */
    public interface OnImageSourceSelectedListener {
        void onImageSourceSelected(ImageSourceOption option);
    }

    private final LayoutInflater inflater;
    private final List<ImageSourceOption> objects;
    private final OnImageSourceSelectedListener listener;

    /**
     * Constructs a new ImageSourceAdapter.
     *
     * @param context the context to use for layout inflation
     * @param objects the list of image source options to display
     * @param listener the listener for selection events
     */
    public ImageSourceAdapter(@NonNull Context context, @NonNull List<ImageSourceOption> objects,
                              @NonNull OnImageSourceSelectedListener listener) {
        super(context, R.layout.item_image_source, objects);
        this.inflater = LayoutInflater.from(context);
        this.objects = objects;
        this.listener = listener;
    }


    @Override
    public int getCount() {
        return objects.size();
    }

    @Nullable
    @Override
    public ImageSourceOption getItem(int position) {
        return objects.get(position);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.item_image_source, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.icon_dialog_item);
        TextView title = convertView.findViewById(R.id.text_dialog_item);
        TextView description = convertView.findViewById(R.id.text_dialog_item_description);

        ImageSourceOption item = getItem(position);

        if (item != null) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            icon.setImageResource(item.getIconResource());
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageSourceSelected(item);
            }
        });

        return convertView;
    }
}
