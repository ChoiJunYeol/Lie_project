package soldesk.com.lie_project.Main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;
import soldesk.com.lie_project.R;

public class LoginActivity extends AppCompatActivity {

    SessionCallback callback;

    String Result;
    EditText id_text,pw_text;
    ProgressDialog dialog = null;

    String myJSON;

    private static final String TAG_TYPE= "type";
    private static final String TAG_ID = "email";
    private static final String TAG_NAME = "name";
    private static final String TAG_SEX ="sex";
    private static final String TAG_ALARM ="alarm";
    private static final String TAG_HEARTMAX ="heart_max";
    private static final String TAG_HEARTMIN ="heart_min";

    JSONArray peoples = null;

    private CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);

        id_text = (EditText)findViewById(R.id.id_text);
        pw_text = (EditText)findViewById(R.id.pw_text);
        Button btn_login = (Button)findViewById(R.id.btn_login);
        TextView join = (TextView)findViewById(R.id.join);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(LoginActivity.this);
                alert_confirm.setMessage("SNS 계정으로 간편하게 이용 가능합니다 \n회원가입을 진행하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getBaseContext(), Membership1.class);
                                startActivity(intent);
                                finish();
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
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id_text.getText().toString().replace(" ", "").equals("") || pw_text.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(getApplicationContext(),"빈칸이 존재합니다.",Toast.LENGTH_LONG).show();
                }else if(id_text.getText().toString().contains(" ") || pw_text.getText().toString().contains(" ")){
                    Toast.makeText(getApplicationContext(),"띄어쓰기를 지워주세요.",Toast.LENGTH_LONG).show();
                }else {
                    if (!isFinishing()) {
                        dialog = ProgressDialog.show(LoginActivity.this, "", "로그인 중...", true);
                        new Thread(new Runnable() {
                            public void run() {
                                HttpPost();
                                dialog.dismiss();
                            }
                        }).start();
                    }
                }
            }
        });

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logOut();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        SharedPreferences pref = getSharedPreferences( "pref" , MODE_PRIVATE);
                        SharedPreferences.Editor ed = pref.edit();

                        Profile profile = Profile.getCurrentProfile();
                        String link = profile.getProfilePictureUri(200, 200).toString();

                        Log.v("result",object.toString());

                        ed.putString("profile",link);
                        ed.commit();
                        try {
                            ed.putString("nickname",object.getString("name"));
                            ed.putString("email",object.getString("email"));
                            ed.putString("sex",object.getString("gender"));
                            ed.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ed.putString("type","Facebook");
                        ed.commit();

                        if (!isFinishing()) {
                            dialog = ProgressDialog.show(LoginActivity.this, "", "회원 정보 조회중...", true);
                            new Thread(new Runnable() {
                                public void run() {
                                    HttpSNS();
                                    dialog.dismiss();
                                }
                            }).start();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,link");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginErr",error.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        //에러로 인한 로그인 실패
//                        finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {

                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.

                    Log.e("UserProfile : ", ""+userProfile);

                    Toast.makeText(getApplicationContext(),"카카오톡 로그인",Toast.LENGTH_LONG);

                    SharedPreferences pref = getSharedPreferences( "pref" , MODE_PRIVATE);
                    SharedPreferences.Editor ed = pref.edit();


                    ed.putString("profile",userProfile.getProfileImagePath());
                    ed.putString("nickname",userProfile.getNickname());
                    ed.putString("email",userProfile.getEmail());
                    ed.putString("sex","No");
                    ed.putString("type","Kakao");
                    ed.commit();

                    if (!isFinishing()) {
                        dialog = ProgressDialog.show(LoginActivity.this, "","회원 정보 조회중...", true);
                        new Thread(new Runnable() {
                            public void run() {
                                HttpSNS();
                                dialog.dismiss();
                            }
                        }).start();
                    }
                }
            });

        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            // 세션 연결이 실패했을때
            // 어쩔때 실패되는지는 테스트를 안해보았음 ㅜㅜ

        }
    }

    private void HttpPost() {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://ccit.cafe24.com/LieEA/Member_Check.php");
        ArrayList<NameValuePair> nameValues = new ArrayList<NameValuePair>();

        try {
            //Post방식으로 넘길 값들을 각각 지정을 해주어야 한다.
            nameValues.add(new BasicNameValuePair("email", id_text.getText().toString()));
            nameValues.add(new BasicNameValuePair("pw", pw_text.getText().toString()));
            nameValues.add(new BasicNameValuePair("type", "User"));

            //HttpPost에 넘길 값을들 Set해주기
            post.setEntity(new UrlEncodedFormEntity(nameValues, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Log.e("Insert Log", ex.toString());
        }
        try {
            //설정한 URL을 실행시키기
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            //통신 값을 받은 Log 생성. (200이 나오는지 확인할 것~) 200이 나오면 통신이 잘 되었다는 뜻!
            Result = EntityUtils.toString(resEntity);

            if(Result.equals("true")){
                SharedPreferences pref = getSharedPreferences( "pref" , MODE_PRIVATE);
                SharedPreferences.Editor ed = pref.edit();

                ed.putString("profile","http://ccit.cafe24.com/LieEA/profile.png");
                ed.commit();

                getData(id_text.getText().toString(),pw_text.getText().toString(),"User");
            }else {
                handler.sendEmptyMessage(0);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void SaveInfo(){
        try {
            peoples = new JSONArray(myJSON);

            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                String type = c.getString(TAG_TYPE);
                String sex = c.getString(TAG_SEX);
                String alarm = c.getString(TAG_ALARM);
                String max = c.getString(TAG_HEARTMAX);
                String min = c.getString(TAG_HEARTMIN);


                SharedPreferences pref = getSharedPreferences( "pref" , MODE_PRIVATE);
                SharedPreferences.Editor ed = pref.edit();

                ed.putString("nickname",name);
                ed.putString("email",id);
                ed.putString("sex",sex);
                ed.putString("type",type);
                ed.putString("alarm",alarm);
                ed.putString("max",max);
                ed.putString("min",min);
                ed.commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getData(String email,String pw,String type){
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String paramEmail = params[0];
                String paramPw = params[1];
                String paramType = params[2];

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://ccit.cafe24.com/LieEA/Member_Info.php");
                ArrayList<NameValuePair> nameValues = new ArrayList<NameValuePair>();

                try {
                    nameValues.add(new BasicNameValuePair("email",paramEmail));
                    nameValues.add(new BasicNameValuePair("pw", paramPw));
                    nameValues.add(new BasicNameValuePair("type", paramType));

                    post.setEntity(new UrlEncodedFormEntity(nameValues, "UTF-8"));

                    HttpResponse response = client.execute(post);

                    InputStream inputStream = response.getEntity().getContent();

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }
                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                Log.d("save",myJSON);
                SaveInfo();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(email,pw,type);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){   // Message id 가 0 이면
                Toast.makeText(getApplicationContext(),"회원 정보가 없습니다. 가입 후 이용해주세요.",Toast.LENGTH_LONG).show();
            }
        }
    };

    private void HttpSNS() {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://ccit.cafe24.com/LieEA/Member_Check.php");
        ArrayList<NameValuePair> nameValues = new ArrayList<NameValuePair>();

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);

        Log.d("이메일 , 타입 : ",pref.getString("email", "")+" / "+pref.getString("type", ""));
        try {
            //Post방식으로 넘길 값들을 각각 지정을 해주어야 한다.
            nameValues.add(new BasicNameValuePair("email", pref.getString("email", "")));
            nameValues.add(new BasicNameValuePair("type", pref.getString("type", "")));

            //HttpPost에 넘길 값을들 Set해주기
            post.setEntity(new UrlEncodedFormEntity(nameValues, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Log.e("Insert Log", ex.toString());
        }

        try {
            //설정한 URL을 실행시키기
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            //통신 값을 받은 Log 생성. (200이 나오는지 확인할 것~) 200이 나오면 통신이 잘 되었다는 뜻!
            Result = EntityUtils.toString(resEntity);

            Log.d("결과값 : ",Result);

            if(Result.equals("true")){
                getData(pref.getString("email", ""),"",pref.getString("type", ""));
            }else {
                handler.sendEmptyMessage(0);
                Intent intent = new Intent(getApplicationContext(), Membership_check.class);
                startActivity(intent);
                finish();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(LoginActivity.this);
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
                        // 'No'
                        return;
                    }
                });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }
}
