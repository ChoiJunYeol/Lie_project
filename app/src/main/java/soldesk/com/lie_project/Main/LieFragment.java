package soldesk.com.lie_project.Main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;

import soldesk.com.lie_project.R;

/**
 * Created by Soldesk on 2017-12-29.
 */

public class LieFragment extends Fragment {

    Button btn_start;
    boolean start = false;
    ImageView mImaveView;
    TextView mTimer;
    int mDegree = 0;
    float mScale = 1;
    int turn = 1;
    BackThread thread;
    String data;
    int result = 1;
    int count = 0;

    public LieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lie, container, false);

        ((MainActivity)getActivity()).mcToolbar.setTitle("거짓말 탐지기");

        btn_start = (Button)view.findViewById(R.id.btn_start);
        mImaveView = (ImageView) view.findViewById(R.id.img_arrow);
        mTimer = (TextView)view.findViewById(R.id.timer);

        btn_start.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((MainActivity)getActivity()).BLE_state) {
                    ((MainActivity) getActivity()).mWorkerThread.start();

                    if(start){
                        start = false;
                        mDegree = 0;

                        thread.isInterrupted();

                        mImaveView.setPivotX(mImaveView.getWidth() / 2);
                        mImaveView.setPivotY(mImaveView.getHeight());

                        mImaveView.setScaleX(0.7f);
                        mImaveView.setScaleY(0.7f);

                        mImaveView.setRotation(mDegree);

                        btn_start.setBackgroundResource(R.drawable.start);
                    }else{
                        start = true;
                        thread = new BackThread();
                        thread.setDaemon(true);
                        thread.start();

                        btn_start.setBackgroundResource(R.drawable.stop1);
                    }
                }else{
                    Toast.makeText(getContext(),"블루투스 연결이 되어 있지 않습니다.",Toast.LENGTH_LONG).show();
                }

//                if(((MainActivity)getActivity()).BLE_state){
//                    ((MainActivity)getActivity()).mWorkerThread.start();
//                    if(start){
//                        //정지 버튼
//                        //거탐 종료(스레드)
//                        ((MainActivity)getActivity()).sendData("0");    // on
//                        btn_start.setBackgroundResource(R.drawable.start);
//                        start=false;
//                    }else{
//                        //시작 버튼
//                        //거탐 진행중...
//                        ((MainActivity)getActivity()).sendData("1");    // on
//                        btn_start.setBackgroundResource(R.drawable.stop1);
//                        start = true;
//                    }
//                }else{
//                    Toast.makeText(getContext(),"블루투스 연결이 되어 있지 않습니다.",Toast.LENGTH_LONG).show();
//                }
            }
        });

        return view;
    }

    class BackThread extends Thread{
        @Override
        public void run() {
            while(start){
                // 메인에서 생성된 Handler 객체의 sendEmpryMessage 를 통해 Message 전달
                data = ((MainActivity)getActivity()).data.trim();

                count += 1;

                if(Integer.parseInt(data)>200){
                    result = 2;
                }

                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } // end while
        } // end run()
    } // end class BackThread

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){   // Message id 가 0 이면
                if(mDegree == 90){
                    turn = 2;
                }
                if(mDegree == -90){
                    turn = 1;
                }

                if(turn == 1){
                    mDegree+=10;
                }else{
                    mDegree-=10;
                }

                mImaveView.setPivotX(mImaveView.getWidth() / 2);
                mImaveView.setPivotY(mImaveView.getHeight());

                mImaveView.setScaleX(0.7f);
                mImaveView.setScaleY(0.7f);

                mImaveView.setRotation(mDegree);

                if(count < 20){
                    mTimer.setText("10");
                }else if(count < 40){
                    mTimer.setText("9");
                }else if(count < 60){
                    mTimer.setText("8");
                }else if(count < 80){
                    mTimer.setText("7");
                }else if(count < 100){
                    mTimer.setText("6");
                }else if(count < 120){
                    mTimer.setText("5");
                }else if(count < 140){
                    mTimer.setText("4");
                }else if(count < 160){
                    mTimer.setText("3");
                }else if(count < 180){
                    mTimer.setText("2");
                }else if(count < 200){
                    mTimer.setText("1");
                }else if(count == 220){
                    start = false;
                    mDegree = 0;
                    count = 0;

                    btn_start.setBackgroundResource(R.drawable.start);

                    mTimer.setText("결과 !!");

                    thread.isInterrupted();

                    if(result == 1){  // 진실
                        mImaveView.setPivotX(mImaveView.getWidth() / 2);
                        mImaveView.setPivotY(mImaveView.getHeight());

                        mImaveView.setScaleX(0.7f);
                        mImaveView.setScaleY(0.7f);

                        mImaveView.setRotation(45);
                    }else {   // 거짓
                        mImaveView.setPivotX(mImaveView.getWidth() / 2);
                        mImaveView.setPivotY(mImaveView.getHeight());

                        mImaveView.setScaleX(0.7f);
                        mImaveView.setScaleY(0.7f);

                        mImaveView.setRotation(-45);
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {

        if(start){
            thread.interrupt();
            start=false;
        }
        if(((MainActivity)getActivity()).BLE_state){
            ((MainActivity)getActivity()).mWorkerThread.interrupt();
        }
        super.onDestroy();
    }
}
