package com.example.rougelikegame.android.screens.menu;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.adapters.ItemCodexAdapter;
import com.example.rougelikegame.android.models.items.ItemRegistry;
import com.example.rougelikegame.android.models.items.PassiveItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        RecyclerView itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        itemsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        itemsRecyclerView.addItemDecoration(new GridSpacingDecoration(dpToPx(10), dpToPx(8)));
        itemsRecyclerView.setHasFixedSize(true);

        List<ItemCodexAdapter.CodexItem> codexItems = buildCodexItems();
        itemsRecyclerView.setAdapter(new ItemCodexAdapter(this, codexItems));
    }

    private List<ItemCodexAdapter.CodexItem> buildCodexItems() {
        List<Integer> itemIds = new ArrayList<>(ItemRegistry.getAllItemIds());
        Collections.sort(itemIds);

        List<ItemCodexAdapter.CodexItem> codexItems = new ArrayList<>();
        for (int itemId : itemIds) {
            PassiveItem item = ItemRegistry.create(itemId);
            codexItems.add(new ItemCodexAdapter.CodexItem(itemId, item));
        }

        return codexItems;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static class GridSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int horizontalSpacing;
        private final int verticalSpacing;

        GridSpacingDecoration(int horizontalSpacing, int verticalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
            this.verticalSpacing = verticalSpacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % 2;

            outRect.left = column == 0 ? 0 : horizontalSpacing / 2;
            outRect.right = column == 0 ? horizontalSpacing / 2 : 0;
            outRect.bottom = verticalSpacing;

            if (position < 2) {
                outRect.top = 0;
            }
        }
    }
}
