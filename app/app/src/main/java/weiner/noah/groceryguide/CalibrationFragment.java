package weiner.noah.groceryguide;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weiner.noah.groceryguide.databinding.FragmentCalibrationBinding;
import weiner.noah.groceryguide.databinding.FragmentMapBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalibrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String baseDir, fileName, filePath;

    private FragmentCalibrationBinding binding;

    private MainActivity mainActivity;

    CSVWriter writer;

    private final String TAG = "CalibrationFragment";

    private Button saveBtn;

    public CalibrationFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalibrationFragment.
     */
    public static CalibrationFragment newInstance(String param1, String param2) {
        CalibrationFragment fragment = new CalibrationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required empty public constructor
        mainActivity = (MainActivity) requireActivity();

        //baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        baseDir = mainActivity.getExternalFilesDir(null).getAbsolutePath();
        fileName = "calibs" + Utils.getDateTime() + ".csv";
        filePath = baseDir + File.separator + fileName;
        Log.i(TAG, "filepath is " + filePath);

        try {
            writer = new CSVWriter(new FileWriter(filePath));
        } catch (IOException e) {
            Log.i(TAG, "ERROR making CSV writer");
            throw new RuntimeException(e);
        }

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //get the binding for fragment_first and its root view to display
        binding = FragmentCalibrationBinding.inflate(inflater, container, false);

        binding.saveButton.setOnClickListener(view -> {
            Log.i(TAG, "writing entries " + binding.input.getText().toString() + ", " + CurrentUserPosition.getCurrXPos() + ", " +CurrentUserPosition.getCurrYPos());
            String[] entries = {binding.input.getText().toString(), String.valueOf(CurrentUserPosition.getCurrXPos()), String.valueOf(CurrentUserPosition.getCurrYPos())};
            writer.writeNext(entries);

            binding.input.getText().clear();
        });

        //inflate layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}