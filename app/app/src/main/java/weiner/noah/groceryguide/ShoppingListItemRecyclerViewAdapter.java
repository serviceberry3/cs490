package weiner.noah.groceryguide;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import weiner.noah.groceryguide.databinding.FragmentShoppingListItemBinding;

import java.util.List;

/**
 * RecyclerView.adapter that can display a Product item
 */
public class ShoppingListItemRecyclerViewAdapter extends RecyclerView.Adapter<ShoppingListItemRecyclerViewAdapter.ViewHolder> {
    private View.OnClickListener onClickListener;
    private final List<Product> mValues;
    private final String TAG = "ShoppingListItemRecyclerViewAdapter";

    private ShoppingListFragment shoppingListFragment;

    public ShoppingListItemRecyclerViewAdapter(List<Product> items, ShoppingListFragment shoppingListFragment) {
        mValues = items;
        this.shoppingListFragment = shoppingListFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentShoppingListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mSubcatNameView.setText(mValues.get(position).getSubCatName());
        holder.mProdNameView.setText(mValues.get(position).getName());
        holder.mProdIdTextView.setText(String.valueOf(mValues.get(position).getProdId()));

        //set the onclicklistener for ea item of recyclerview
        //do this when items in the list are tapped
        holder.itemView.setOnClickListener(view -> {
            //Log.i(TAG, "ITEM CLICKED!");
            shoppingListFragment.showMenu(view);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSubcatNameView;
        public final TextView mProdNameView;
        public final TextView mProdIdTextView;
        public Product mItem;

        public ViewHolder(FragmentShoppingListItemBinding binding) {
            super(binding.getRoot());

            mSubcatNameView = binding.subcatName;
            mProdNameView = binding.productName;
            mProdIdTextView = binding.productId;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mProdNameView.getText() + "'";
        }
    }
}