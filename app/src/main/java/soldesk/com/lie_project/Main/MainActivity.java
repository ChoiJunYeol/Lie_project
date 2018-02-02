package soldesk.com.lie_project.Main;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import soldesk.com.lie_project.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static String fragmentTag = "lie";

    CollapsingToolbarLayout mcToolbar;

    String profile;
    Bitmap bitmap;
    String login_service,data="0";

    private static final int REQUEST_ENABLE_BT = 10;
    BluetoothAdapter mBluetoothAdapter;

    Set<BluetoothDevice> mDevices;
    int mPariedDeviceCount = 0;
    //Adapter
    SimpleAdapter adapterDevice;

    BluetoothDevice mRemoteDevie;
    // 스마트폰과 페어링 된 디바이스간 통신 채널에 대응 하는 BluetoothSocket
    BluetoothSocket mSocket = null;
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;
    String mStrDelimiter = "\n";
    char mCharDelimiter =  '\n';

    boolean BLE_state = false;
    String BLE_String_on = "Connected";
    String BLE_String_off = "Disconnected";

    Thread mWorkerThread = null;
    byte[] readBuffer;
    int readBufferPosition;

    ProgressDialog mdialog = null,mmdialog = null,mmmdialog = null;

    //list - Device 목록 저장
    List<Map<String,String>> dataDevice;

    private static String address = "20:16:05:06:06:12";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Lie EA");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mcToolbar = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        mcToolbar.setTitle("LIE_EA");
        mcToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mcToolbar.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);
        mcToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);


        dataDevice = new ArrayList<>();
        adapterDevice = new SimpleAdapter(this, dataDevice, android.R.layout.simple_list_item_2, new String[]{"name","address"}, new int[]{android.R.id.text1, android.R.id.text2});

        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBluetoothSearchReceiver, searchFilter);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);

        profile = pref.getString("profile","");
        String nickname = pref.getString("nickname","");
        String email = pref.getString("email","");
        login_service = pref.getString("type","");

        Fragment fragment = null;

        fragment = new HomeFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main, fragment);
        ft.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View nav_header_view = navigationView.getHeaderView(0);

        ImageView imgView = (ImageView)nav_header_view.findViewById(R.id.imageView);

        TextView nick = (TextView)nav_header_view.findViewById(R.id.nickname);
        nick.setText(nickname);

        TextView service = (TextView)nav_header_view.findViewById(R.id.service);
        service.setText(login_service);

        TextView text_email = (TextView)nav_header_view.findViewById(R.id.email);
        text_email.setText(email);

        Thread mThread = new Thread(){
            @Override
            public void run(){
                try{
                    URL url = new URL(profile);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);

                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };

        mThread.start();

        try{
            mThread.join();
            imgView.setImageBitmap(bitmap);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    //블루투스 검색결과 BroadcastReceiver
    BroadcastReceiver mBluetoothSearchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            BluetoothDevice device;
            //장치가 연결이 되었으면

            switch(action){
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    dataDevice.clear();
                    Toast.makeText(MainActivity.this, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
                    break;
                //블루투스 디바이스 찾음
                case BluetoothDevice.ACTION_FOUND:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //데이터 저장
//                    Map map = new HashMap();
//                    map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
//                    map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
//                    dataDevice.add(map);
                    Toast.makeText(getApplicationContext(),"연결 완료",Toast.LENGTH_LONG).show();
                    //리스트 목록갱신
                    adapterDevice.notifyDataSetChanged();
                    break;
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(MainActivity.this, "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
                    //btnSearch.setEnabled(true);
                    Log.d("에러 2","에러 2");
                    connectToSelectedDevice();
                    mdialog.dismiss();
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    BLE_state = true;
                    BLE_String_on = "Connected : "+device.getName().toString();
                    Toast.makeText(getApplicationContext(), "기기가 연결되었습니다.", Toast.LENGTH_LONG).show();
                    invalidateOptionsMenu();
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    BLE_state = false;
                    BLE_String_off = "DisConnected";
                    invalidateOptionsMenu();
                    Toast.makeText(getApplicationContext(), "기기 연결이 해제되었습니다.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getSupportFragmentManager().getBackStackEntryCount();
            Log.d("11","count : " + count);
            if (count == 0) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
                alert_confirm.setMessage("종료하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(BLE_state){
            menu.findItem(R.id.action_BLE).setIcon(R.drawable.ble);
            menu.findItem(R.id.action_connect).setTitle("연결 해제");
            menu.findItem(R.id.action_state).setTitle(BLE_String_on);
        }else{
            menu.findItem(R.id.action_BLE).setIcon(R.drawable.ble_off);
            menu.findItem(R.id.action_connect).setTitle("연결하기");
            menu.findItem(R.id.action_state).setTitle(BLE_String_off);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_connect) {
            if(BLE_state){
                disconnect();
            }else{
                checkBluetooth();
            }
        }else if(id == R.id.action_connect){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if(id == R.id.Home) {
            fragment = new HomeFragment();
            title = "LIE EA";
        }else if (id == R.id.GuestInfo) {
            fragment = new GuestFragment();
            title = "회원 정보";
        } else if (id == R.id.LieCheck) {
            fragment = new LieFragment();
            title = "거짓말 탐지기";
        } else if (id == R.id.HeartCheck) {
            fragment = new CheckFragment();
            title = "심박수 확인";
        } else if (id == R.id.Alarm) {
            fragment = new AlarmFragment();
            title = "알람";
        }else if (id == R.id.Logout) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
            alert_confirm.setMessage("로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserManagement.requestLogout(new LogoutResponseCallback() {
                                @Override
                                public void onCompleteLogout() {
                                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            LoginManager.getInstance().logOut();
                        }
                    }).setNegativeButton("취소",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'No'
                            return;
                        }
                    });
            AlertDialog alert = alert_confirm.create();
            alert.show();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment).addToBackStack(title);
            ft.commit();

            int count = getSupportFragmentManager().getBackStackEntryCount();
            Log.d("222","count : " + count);
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // 문자열 전송하는 함수(쓰레드 사용 x)
    void sendData(String msg) {
        msg += mStrDelimiter;  // 문자열 종료표시 (\n)
        try{
            // getBytes() : String을 byte로 변환
            // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
            mOutputStream.write(msg.getBytes());  // 문자열 전송.
        }catch(Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
            Toast.makeText(getApplicationContext(), "데이터 전송중 오류가 발생", Toast.LENGTH_LONG).show();
            return;
        }
    }

    //  connectToSelectedDevice() : 원격 장치와 연결하는 과정을 나타냄.
    //        실제 데이터 송수신을 위해서는 소켓으로부터 입출력 스트림을 얻고 입출력 스트림을 이용하여 이루어 진다.
    void connectToSelectedDevice() {
        // BluetoothDevice 원격 블루투스 기기를 나타냄.
        mmdialog = ProgressDialog.show(MainActivity.this, "","기기 연결 중...", true);

        mRemoteDevie = mBluetoothAdapter.getRemoteDevice(address);
        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성, RFCOMM 채널을 통한 연결.
            // createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와 통신할 수 있는 소켓을 생성함.
            // 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는 BluetoothSocket 오브젝트를 리턴함.
            mSocket = mRemoteDevie.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.

            // 데이터 송수신을 위한 스트림 얻기.
            // BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
            // 1. 데이터를 보내기 위한 OutputStrem
            // 2. 데이터를 받기 위한 InputStream
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            // 데이터 수신 준비.
            beginListenForData();
        }catch(Exception e) { // 블루투스 연결 중 오류 발생
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            mBluetoothAdapter.cancelDiscovery();
            return;
        }
        mmdialog.dismiss();
        mBluetoothAdapter.cancelDiscovery();
    }

    // 데이터 수신(쓰레드 사용 수신된 메시지를 계속 검사함)
    void beginListenForData() {
        final Handler handler = new Handler();

        readBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
        readBuffer = new byte[1024];            // 수신 버퍼.

        Log.d("스레드 접근 ","11111");
        // 문자열 수신 쓰레드.
        mWorkerThread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                // interrupt() 메소드를 이용 스레드를 종료시키는 예제이다.
                // interrupt() 메소드는 하던 일을 멈추는 메소드이다.
                // isInterrupted() 메소드를 사용하여 멈추었을 경우 반복문을 나가서 스레드가 종료하게 된다.
                Log.d("스레드 접근 ","22222");
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        // InputStream.available() : 다른 스레드에서 blocking 하기 전까지 읽은 수 있는 문자열 개수를 반환함.
//                            int bytes;
//
//                            while (true) {
//                                try {
//                                    // 입력 스트림에서 데이터를 읽는다
//                                    bytes = mInputStream.read(readBuffer);
//                                    String strBuf = new String(readBuffer, 0, bytes);
//                                    Log.d("Receive: ",strBuf);
//                                    SystemClock.sleep(1);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                    break;
//                                }
//                            }

                        int byteAvailable = mInputStream.available();   // 수신 데이터 확인
                        if(byteAvailable > 0) {// 데이터가 수신된 경우.

                            byte[] packetBytes = new byte[byteAvailable];
                             //read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.

                            Log.d("mCharDelimiter ",""+mCharDelimiter);

                            mInputStream.read(packetBytes);
                            for(int i=0; i<byteAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == mCharDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                    //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    Log.d("data : ",data);

                                    handler.post(new Runnable(){
                                        @Override
                                        public void run() {
                                            //문자열 수신 처리작업
                                            // data + mStrDelimiter
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }

                    } catch (Exception e) {    // 데이터 수신 중 오류 발생.
                        Toast.makeText(getApplicationContext(), "데이터 수신 중 오류가 발생 했습니다.", Toast.LENGTH_LONG).show();
                        Log.d("에러 11111","11111111");
                        return;
                    }
                }
            }
        });
        //mWorkerThread.start();
    }

    void selectDevice() {
        // 블루투스 디바이스는 연결해서 사용하기 전에 먼저 페어링 되어야만 한다
        // getBondedDevices() : 페어링된 장치 목록 얻어오는 함수.
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPariedDeviceCount = mDevices.size();

        if(mPariedDeviceCount > 0 ) { // 페어링된 장치가 있는 경우
            for(BluetoothDevice device : mDevices) { //페어링된 장치 이름과, MAC주소를 가져올 수 있다. Log.d("TEST", device.getName().toString() +" Device Is Connected!"); Log.d("TEST", device.getAddress().toString() +" Device Is Connected!"); }
                Log.d("getAddress","/"+device.getAddress()+"/");
                Log.d("getAddress","/"+device.getAddress().equals(address)+"/");
                if(device.getAddress().equals(address)){
                    Log.d("에러 1","에러 1");
                    connectToSelectedDevice();
                    break;
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error!");
                    builder.setMessage("해당 장치(HC-06)가 없습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(),"서비스 이용이 제한됩니다.",Toast.LENGTH_LONG).show();
                                }
                            });
                    builder.setNegativeButton("다시 검색",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(),"기기를 다시 검색합니다.",Toast.LENGTH_LONG).show();
                                    mdialog = ProgressDialog.show(MainActivity.this, "","기기 검색 중...", true);
                                    new Thread(new Runnable() {
                                        public void run() {
                                            mBluetoothAdapter.startDiscovery();
                                        }
                                    }).start();
                                }
                            });
                    builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }else {
            //페어링 없는 경우
            //장치 검색(discovering 수행 후 연결)
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    void checkBluetooth() {
        /**
         * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
         이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
         */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null ) {  // 블루투스 미지원
            Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
        }
        else { // 블루투스 지원
            if(!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {// 블루투스 지원하며 활성 상태인 경우
                Log.d("에러 3","에러 3");
                selectDevice();
            }
        }
    }



    // onDestroy() : 어플이 종료될때 호출 되는 함수.
    //               블루투스 연결이 필요하지 않는 경우 입출력 스트림 소켓을 닫아줌.
    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothSearchReceiver);
        disconnect();
        super.onDestroy();
    }

    public void disconnect(){
        mmmdialog = ProgressDialog.show(MainActivity.this, "","기기 연결 해제 중...", true);

        try{
            if(!Thread.currentThread().isInterrupted()){
                mWorkerThread.interrupt(); // 데이터 수신 쓰레드 종료
            }
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        mmmdialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // startActivityForResult 를 여러번 사용할 땐 이런 식으로 switch 문을 사용하여 어떤 요청인지 구분하여 사용함.
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) { // 블루투스 활성화 상태
                    Log.d("에러 4","에러 4");
                    selectDevice();
                }
                else if(resultCode == RESULT_CANCELED) { // 블루투스 비활성화 상태 (종료)
                    Toast.makeText(getApplicationContext(), "블루투수를 미사용시 서비스가 제한됩니다.", Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
