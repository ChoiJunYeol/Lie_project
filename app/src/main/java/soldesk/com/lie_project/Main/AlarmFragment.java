package soldesk.com.lie_project.Main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import soldesk.com.lie_project.R;

/**
 * Created by Soldesk on 2017-12-29.
 */

public class AlarmFragment extends Fragment {

    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        ((MainActivity)getActivity()).mcToolbar.setTitle("알람");

        return view;
    }
}
