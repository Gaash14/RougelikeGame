package com.example.rougelikegame.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.meta.Skin;
import com.example.rougelikegame.android.models.meta.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SkinAdapter extends RecyclerView.Adapter<SkinAdapter.SkinViewHolder> {

    public interface SkinActionListener {
        void onBuy(Skin skin);
        void onEquip(Skin skin);
    }

    private final List<Skin> skins;
    private final User user;
    private final SkinActionListener listener;
    private final boolean allowBuying;
    private final boolean allowEquipping;

    public SkinAdapter(List<Skin> skins,
                       User user,
                       boolean allowBuying,
                       boolean allowEquipping,
                       SkinActionListener listener) {
        this.skins = skins;
        this.user = user;
        this.allowBuying = allowBuying;
        this.allowEquipping = allowEquipping;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SkinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_skin, parent, false);
        return new SkinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkinViewHolder holder, int position) {
        Skin skin = skins.get(position);

        holder.txtName.setText(skin.getName());
        bindSkinPreview(holder.imgSkin, skin.getTexturePath());

        boolean owned = user.getOwnedSkins().containsKey(skin.getId());
        boolean equipped = skin.getId().equals(user.getEquippedSkinId());

        // DEFAULT skin
        if (skin.getUnlockType() == Skin.UnlockType.DEFAULT) {
            holder.txtPrice.setText(equipped ? "Equipped" : "Default");
            holder.btnAction.setText(equipped ? "EQUIPPED" : "EQUIP");
            holder.btnAction.setEnabled(!equipped);
        }

        // SHOP skin
        else if (skin.getUnlockType() == Skin.UnlockType.SHOP) {
            if (owned) {
                holder.txtPrice.setText(equipped ? "Equipped" : "Owned");
                if (allowEquipping) {
                    holder.btnAction.setText(equipped ? "EQUIPPED" : "EQUIP");
                    holder.btnAction.setEnabled(!equipped);
                } else {
                    holder.btnAction.setText("GO TO SKINS");
                    holder.btnAction.setEnabled(false);
                }
            } else {
                if (allowBuying) {
                    holder.txtPrice.setText("Price: " + skin.getPrice());
                    holder.btnAction.setText("BUY");
                    holder.btnAction.setEnabled(user.getNumOfCoins() >= skin.getPrice());
                } else {
                    holder.txtPrice.setText("Locked (Shop)");
                    holder.btnAction.setText("LOCKED");
                    holder.btnAction.setEnabled(false);
                }
            }
        }

        // ACHIEVEMENT skin
        else {
            if (owned) {
                holder.txtPrice.setText(equipped ? "Equipped" : "Unlocked");
                if (allowEquipping) {
                    holder.btnAction.setText(equipped ? "EQUIPPED" : "EQUIP");
                    holder.btnAction.setEnabled(!equipped);
                } else {
                    holder.btnAction.setText("GO TO SKINS");
                    holder.btnAction.setEnabled(false);
                }
            } else {
                holder.txtPrice.setText("Locked (Achievement)");
                holder.btnAction.setText("LOCKED");
                holder.btnAction.setEnabled(false);
            }
        }

        holder.btnAction.setOnClickListener(v -> {
            if (!owned && allowBuying && skin.getUnlockType() == Skin.UnlockType.SHOP) {
                listener.onBuy(skin);
            } else if (owned && !equipped && allowEquipping) {
                listener.onEquip(skin);
            }
        });
    }

    private void bindSkinPreview(ImageView imageView, String texturePath) {
        try (InputStream stream = imageView.getContext().getAssets().open(texturePath)) {
            imageView.setImageBitmap(BitmapFactory.decodeStream(stream));
        } catch (IOException e) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    @Override
    public int getItemCount() {
        return skins.size();
    }

    static class SkinViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSkin;
        TextView txtName, txtPrice;
        Button btnAction;

        SkinViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSkin = itemView.findViewById(R.id.imgSkin);
            txtName = itemView.findViewById(R.id.txtSkinName);
            txtPrice = itemView.findViewById(R.id.txtSkinPrice);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
