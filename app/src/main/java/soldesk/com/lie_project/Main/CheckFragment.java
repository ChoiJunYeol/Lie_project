package soldesk.com.lie_project.Main;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import soldesk.com.lie_project.R;

public class CheckFragment extends Fragment {

    TextView heart;
    String data;
    int count = 0;
    private LineGraphSeries mSeries1;
    boolean start = false,stop_state = false;
    BackThread thread;

    public CheckFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check, container, false);

        ((MainActivity)getActivity()).mcToolbar.setTitle("심박수 확인");


        heart = (TextView)view.findViewById(R.id.heart);
        final Button play = (Button)view.findViewById(R.id.btn_play);
//        Button stop = (Button)view.findViewById(R.id.btn_stop);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)getActivity()).BLE_state){
                    ((MainActivity)getActivity()).mWorkerThread.start();

                    if(start){
                        thread.interrupt();
                        start=false;
                        play.setBackgroundResource(R.drawable.play);
                    }else{
                        thread = new BackThread();
                        thread.setDaemon(true);
                        thread.start();
                        start = true;
                        play.setBackgroundResource(R.drawable.pause);
                    }
                }else{
                    Toast.makeText(getContext(),"블루투스 연결이 되어 있지 않습니다.",Toast.LENGTH_LONG).show();
                }
            }
        });

//        stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(start){
//                    thread.interrupt();
//                    count=0;
//                    start=false;
//                    stop_state = true;
//                    play.setBackgroundResource(R.drawable.play);
//                }
//                if(((MainActivity)getActivity()).BLE_state){
//                    ((MainActivity)getActivity()).mWorkerThread.interrupt();
//                }else{
//                    Toast.makeText(getContext(),"블루투스 연결이 되어 있지 않습니다.",Toast.LENGTH_LONG).show();
//                }
//            }
//        });



        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);

        mSeries1.setTitle("심박수 실시간 그래프");
        mSeries1.setColor(Color.RED);
        mSeries1.setDrawDataPoints(true);
        mSeries1.setDataPointsRadius(10);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(8);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(250);

        return view;
    }

    class BackThread extends Thread{
        @Override
        public void run() {
            while(start){
                // 메인에서 생성된 Handler 객체의 sendEmpryMessage 를 통해 Message 전달
                data = ((MainActivity)getActivity()).data.trim();
                handler.sendEmptyMessage(0);
                count++;
                try {
                    Thread.sleep(1000);
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
                heart.setText(data); // 메인스레드의 UI 내용 변경
                Log.d("data2 : ",data);
                mSeries1.appendData(new DataPoint(count,Integer.parseInt(data)),true,10);
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