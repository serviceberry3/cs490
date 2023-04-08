package weiner.noah.groceryguide;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import weiner.noah.groceryguide.databinding.FragmentShoppingListItemBinding;
import weiner.noah.groceryguide.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.List;

/**
 * RecyclerView.adapter that can display a Product item
 */
public class ShoppingListItemRecyclerViewAdapter extends RecyclerView.Adapter<ShoppingListItemRecyclerViewAdapter.ViewHolder> {

    private final List<Product> mValues;

    public ShoppingListItemRecyclerViewAdapter(List<Product> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentShoppingListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(mValues.get(position).getProdId()));
        holder.mContentView.setText(mValues.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public Product mItem;

        public ViewHolder(FragmentShoppingListItemBinding binding) {
            super(binding.getRoot());

            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}