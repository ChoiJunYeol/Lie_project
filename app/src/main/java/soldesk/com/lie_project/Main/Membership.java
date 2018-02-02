package soldesk.com.lie_project.Main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import soldesk.com.lie_project.R;

public class Membership extends AppCompatActivity {

    String sex_checked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);

        String Email = pref.getString("email", "");
        String Name = pref.getString("nickname", "");
        String Sex = pref.getString("sex", "");
        final String type = pref.getString("type", "");

        Button btnOK = (Button)findViewById(R.id.btnOK);
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        final EditText Email_Text = (EditText)findViewById(R.id.emailText);
        final EditText Name_Text = (EditText)findViewById(R.id.NameText);
        final RadioGroup rg = (RadioGroup)findViewById(R.id.SexRd);

        Email_Text.setClickable(false);
        Email_Text.setFocusable(false);

        Email_Text.setText(Email);
        Name_Text.setText(Name);

         if(Sex.equals("male")){
             RadioButton male = (RadioButton) findViewById(R.id.male);
             male.setChecked(true);
         }else if(Sex.equals("female")){
             RadioButton female = (RadioButton) findViewById(R.id.female);
             female.setChecked(true);
         }

         btnOK.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //빈칸 있을 경우 토스트 띄우기, 완료 될 경우 Login창으로

                 RadioButton rd = (RadioButton)findViewById(rg.getCheckedRadioButtonId());
                 sex_checked = rd.getText().toString();

                 if (Name_Text.getText().toString().replace(" ", "").equals("")) {

                     Toast.makeText(getApplicationContext(), "빈칸이 존재합니다.", Toast.LENGTH_LONG).show();

                 } else if (Name_Text.getText().toString().contains(" ")) {
                     Toast.makeText(getApplicationContext(), "띄어쓰기를 지워주세요.", Toast.LENGTH_LONG).show();
                 }else if(!rd.isChecked()){
                     Toast.makeText(getApplicationContext(), "성별을 체크해 주세요.", Toast.LENGTH_LONG).show();
                 } else {
                     ProgressDialog dialog = ProgressDialog.show(Membership.this, "", "회원 등록중...", true);
                     new Thread(new Runnable() {
                         public void run() {
                             HttpPost(Email_Text.getText().toString(), Name_Text.getText().toString(), type, sex_checked);
                         }
                     }).start();

                     Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                     startActivity(intent);
                     finish();
                 }
             }
         });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void HttpPost(String email,String name,String type,String sex) {

        Log.d("값 : ",""+email+" / "+name+" / "+type+" / "+sex);

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://ccit.cafe24.com/LieEA/Member_Insert.php");
        ArrayList<NameValuePair> nameValues = new ArrayList<NameValuePair>();

        try {
            //Post방식으로 넘길 값들을 각각 지정을 해주어야 한다.
            nameValues.add(new BasicNameValuePair("name",name));
            nameValues.add(new BasicNameValuePair("email", email));
            nameValues.add(new BasicNameValuePair("type", type));
            nameValues.add(new BasicNameValuePair("sex", sex));

            //HttpPost에 넘길 값을들 Set해주기
            post.setEntity(new UrlEncodedFormEntity(nameValues, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Log.e("Insert Log", ex.toString());
        }

        try {
            //설정한 URL을 실행시키기
            HttpResponse response = client.execute(post);
            //통신 값을 받은 Log 생성. (200이 나오는지 확인할 것~) 200이 나오면 통신이 잘 되었다는 뜻!
            Log.i("Insert Log", "response.getStatusCode:" + response.getStatusLine().getStatusCode());

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
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(Membership.this);
        alert_confirm.setMessage("회원 등록을 취소하시면 서비스를 이용 할 수 없습니다.\n취소하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
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
}
