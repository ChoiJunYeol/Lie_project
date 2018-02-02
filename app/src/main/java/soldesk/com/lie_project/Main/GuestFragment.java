package soldesk.com.lie_project.Main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import soldesk.com.lie_project.R;

/**
 * Created by Soldesk on 2017-12-29.
 */

public class GuestFragment extends Fragment {

    EditText Name_ed;
    Button btnEdit;
    private InputMethodManager imm;
    boolean edit = true;

    public GuestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guest, container, false);

        ((MainActivity)getActivity()).mcToolbar.setTitle("회원 정보");

        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

        String Email = pref.getString("email", "");
        String Name = pref.getString("nickname", "");
        String Sex = pref.getString("sex", "");
        String Alarm = pref.getString("alarm", "");
        String Max = pref.getString("max", "");
        String Min = pref.getString("min", "");

        EditText Email_ed = (EditText)view.findViewById(R.id.email_ed);
        EditText Sex_ed = (EditText)view.findViewById(R.id.sex_ed);
        EditText Alarm_ed = (EditText)view.findViewById(R.id.alarm_ed);
        EditText Avg_heart = (EditText)view.findViewById(R.id.avg_heart);
        EditText Max_heart = (EditText)view.findViewById(R.id.max_heart);
        EditText Min_heart = (EditText)view.findViewById(R.id.min_heart);
        Name_ed = (EditText)view.findViewById(R.id.name_ed);

        Email_ed.setText(Email);
        Sex_ed.setText(Sex);
        Name_ed.setText(Name);
        Alarm_ed.setText(Alarm);
        Avg_heart.setText("미확인!");
        Max_heart.setText(Max);
        Min_heart.setText(Min);

        Email_ed.setClickable(false);
        Email_ed.setFocusable(false);
        Sex_ed.setClickable(false);
        Sex_ed.setFocusable(false);
        Alarm_ed.setClickable(false);
        Alarm_ed.setFocusable(false);
        Avg_heart.setClickable(false);
        Avg_heart.setFocusable(false);
        Max_heart.setClickable(false);
        Max_heart.setFocusable(false);
        Min_heart.setClickable(false);
        Min_heart.setFocusable(false);
        Name_ed.setClickable(false);
        Name_ed.setFocusable(false);

        Button btnOK = (Button)view.findViewById(R.id.btn1);
        btnEdit = (Button)view.findViewById(R.id.btn_edit);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit) {
                    Name_ed.setFocusableInTouchMode(true);
                    Name_ed.setClickable(true);
                    Name_ed.setFocusable(true);
                    imm.showSoftInput(Name_ed, 0);
                    btnEdit.setText("완료");
                    edit = false;
                }else{
                    Name_ed.setClickable(false);
                    Name_ed.setFocusable(false);
                    btnEdit.setText("수정");
                    imm.hideSoftInputFromWindow(Name_ed.getWindowToken(), 0);
                    edit = true;
                }
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = null;

                fragment = new HomeFragment();

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, fragment);
                ft.commit();
            }
        });
        return view;
    }
}
