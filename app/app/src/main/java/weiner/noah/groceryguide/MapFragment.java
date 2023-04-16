package weiner.noah.groceryguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import java.util.ArrayList;

import weiner.noah.groceryguide.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {
    private final String TAG = "MapFragment";

    private FragmentMapBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //listen for results sent to this fragment
        getParentFragmentManager().setFragmentResultListener("drawDot", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                int[] idArr = bundle.getIntArray("idArr");

                //add dot to list of dots for each subcat label for the subcat of interest
                for (int i = 0; i < idArr.length; i++) {
                    binding.storeMap.addDot(idArr[i]);
                }

                binding.storeMap.invalidate();
                binding.storeMap.zoomOnSubcatLabels();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //get the binding for fragment_first and its root view to display
        binding = FragmentMapBinding.inflate(inflater, container, false);

        //should pretty much always return the root (outermost) view here
        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}