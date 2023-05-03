package weiner.noah.groceryguide;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import weiner.noah.groceryguide.databinding.ElementContentsItemBinding;

public class ElementRecyclerViewAdapter extends RecyclerView.Adapter<ElementRecyclerViewAdapter.ViewHolder> {
    private List<SSWhalley.StoreElement> mValues = null;

    private final MapFragment mapFragment;

    private String[] mContents = null;

    private MainActivity mainActivity;

    private int selectedPos = RecyclerView.NO_POSITION;

    private final String TAG = "ElementRecyclerViewAdapter";

    public ElementRecyclerViewAdapter(List<SSWhalley.StoreElement> items, MapFragment mapFragment) {
        mValues = items;
        this.mapFragment = mapFragment;
        this.mainActivity = (MainActivity) mapFragment.getActivity();
    }

    public ElementRecyclerViewAdapter(String[] contents, MapFragment mapFragment) {
        this.mContents = contents;
        this.mapFragment = mapFragment;
        this.mainActivity = (MainActivity) mapFragment.getActivity();
    }

    @NonNull
    @Override
    public ElementRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ElementRecyclerViewAdapter.ViewHolder(ElementContentsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public void setHighlightPosition(int i) {
        //notify registered observers that the previously selected item has changed
        notifyItemChanged(selectedPos);
        selectedPos = i;

        //notify registered observers that newly selected item has changed
        notifyItemChanged(selectedPos);
    }

    //Called by RecyclerView to display the data at the specified position.
    @Override
    public void onBindViewHolder(@NonNull ElementRecyclerViewAdapter.ViewHolder holder, int position) {
        if (mValues != null) {
            //each slot of list should show text containing name of store element
            holder.mTextView.setText(mValues.get(position).getNiceName());

            //set the onclicklistener for ea item of recyclerview
            //do this when items in the list are tapped
            holder.itemView.setOnClickListener(view -> {
                Log.i(TAG, "clicked " + holder.getLayoutPosition());

                //notify registered observers that the previously selected item has changed
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();

                //notify registered observers that newly selected item has changed
                notifyItemChanged(selectedPos);

                mapFragment.openElementContents(mValues.get(position));
            });
        }
        else {
            holder.mTextView.setText(mContents[position]);
        }

        holder.itemView.setSelected(selectedPos == position);


        if (selectedPos == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.focused_color));
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            return mValues.size();
        }
        else {
            return mContents.length;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTextView;

        public ViewHolder(ElementContentsItemBinding binding) {
            super(binding.getRoot());

            mTextView = binding.content;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText() + "'";
        }

        @Override
        public void onClick(View view) {
            view.setSelected(true);
        }
    }
}
