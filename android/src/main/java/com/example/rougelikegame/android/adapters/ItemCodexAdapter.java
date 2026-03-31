package com.example.rougelikegame.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ItemCodexAdapter manages the display of passive items in the game's codex.
 * It handles the loading of item sprites and styling based on item tiers.
 */
public class ItemCodexAdapter extends RecyclerView.Adapter<ItemCodexAdapter.ItemViewHolder> {

    /**
     * Wrapper class for codex items to include their unique ID.
     */
    public static class CodexItem {
        public final int itemId;
        public final PassiveItem item;

        public CodexItem(int itemId, PassiveItem item) {
            this.itemId = itemId;
            this.item = item;
        }
    }

    private final List<CodexItem> items;
    private final LayoutInflater inflater;
    private final Context context;

    /**
     * Constructs a new ItemCodexAdapter.
     *
     * @param context the context to use for asset loading and inflation
     * @param items the list of codex items to display
     */
    public ItemCodexAdapter(Context context, List<CodexItem> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_codex_entry, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CodexItem codexItem = items.get(position);
        PassiveItem item = codexItem.item;

        holder.itemName.setText(item.getDisplayName());
        holder.itemId.setText("ID " + codexItem.itemId);
        holder.itemDescription.setText(item.getDescription());
        holder.itemTier.setText(item.getTier().name());

        bindItemSprite(holder.itemSprite, item);
        styleTierBadge(holder.itemTier, item.getTier());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Loads and binds the item sprite from assets.
     *
     * @param itemSprite the ImageView to bind the sprite to
     * @param item the item whose sprite should be loaded
     */
    private void bindItemSprite(ImageView itemSprite, PassiveItem item) {
        String iconPath = "items/" + item.getKey() + ".png";

        try (InputStream inputStream = context.getAssets().open(iconPath)) {
            Bitmap sprite = BitmapFactory.decodeStream(inputStream);
            if (sprite != null) {
                itemSprite.setImageBitmap(sprite);
                return;
            }
        } catch (IOException ignored) {
            // Try legacy icon path fallback.
        }

        String fallbackPath = item.getIconPath();
        if (fallbackPath != null && !fallbackPath.isEmpty()) {
            try (InputStream inputStream = context.getAssets().open(fallbackPath)) {
                Bitmap fallbackSprite = BitmapFactory.decodeStream(inputStream);
                if (fallbackSprite != null) {
                    itemSprite.setImageBitmap(fallbackSprite);
                    return;
                }
            } catch (IOException ignored) {
                // Fall through to error drawable.
            }
        }

        itemSprite.setImageResource(R.drawable.error);
    }

    /**
     * Styles the tier badge based on the item's tier.
     *
     * @param itemTier the TextView representing the tier badge
     * @param tier the tier of the item
     */
    private void styleTierBadge(TextView itemTier, ItemTier tier) {
        int color;
        switch (tier) {
            case S:
                color = Color.parseColor("#D9A441");
                break;
            case A:
                color = Color.parseColor("#7A4DE0");
                break;
            case B:
                color = Color.parseColor("#2A7BE4");
                break;
            case C:
                color = Color.parseColor("#2F9B63");
                break;
            case D:
            default:
                color = Color.parseColor("#5D6474");
                break;
        }

        GradientDrawable badge = (GradientDrawable) ContextCompat
            .getDrawable(context, R.drawable.item_codex_tier_badge_bg)
            .mutate();
        badge.setColor(color);
        itemTier.setBackground(badge);
    }

    /**
     * ViewHolder class for item codex entries.
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemSprite;
        final TextView itemName;
        final TextView itemId;
        final TextView itemDescription;
        final TextView itemTier;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemSprite = itemView.findViewById(R.id.itemSprite);
            itemName = itemView.findViewById(R.id.itemName);
            itemId = itemView.findViewById(R.id.itemId);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemTier = itemView.findViewById(R.id.itemTier);
        }
    }
}
