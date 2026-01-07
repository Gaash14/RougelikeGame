package com.example.rougelikegame.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.models.Skin;
import com.example.rougelikegame.android.models.User;

import java.util.List;

public class SkinAdapter extends RecyclerView.Adapter<SkinAdapter.SkinViewHolder> {

    public interface SkinActionListener {
        void onBuy(Skin skin);
        void onEquip(Skin skin);
    }

    private final List<Skin> skins;
    private final User user;
    private final SkinActionListener listener;

    public SkinAdapter(List<Skin> skins, User user, SkinActionListener listener) {
        this.skins = skins;
        this.user = user;
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

        boolean owned = user.getOwnedSkins().containsKey(skin.getId());
        boolean equipped = skin.getId().equals(user.getEquippedSkinId());

        if (owned) {
            holder.txtPrice.setText(equipped ? "Equipped" : "Owned");
            holder.btnAction.setText(equipped ? "EQUIPPED" : "EQUIP");
            holder.btnAction.setEnabled(!equipped);
        } else {
            holder.txtPrice.setText("Price: " + skin.getPrice());
            holder.btnAction.setText("BUY");
            holder.btnAction.setEnabled(user.getNumOfCoins() >= skin.getPrice());
        }

        holder.btnAction.setOnClickListener(v -> {
            if (owned) {
                listener.onEquip(skin);
            } else {
                listener.onBuy(skin);
            }
        });
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
