package rk.android.vision.vision_fb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String GENDER = "gender";
    private static final String EMAIL = "email";
    private static final String BIRTHDAY = "birthday";

    private static final String FIELDS = "fields";

    private static final String REQUEST_FIELDS =
            TextUtils.join(",", new String[]{ID, NAME, PICTURE, GENDER, EMAIL, BIRTHDAY});

    public LoginButton loginButton;
    public TextView textView,log_status;
    public CallbackManager callbackManager;
    private JSONObject user;
    public ProfilePictureView profilePictureView;
    public PackageInfo info;

    public String myJSON;

    private static final String TAG_MAIL = "email_id";
    private static final String TAG_EMAIL = "email";

    public JSONArray result = null;

    public int code;
    public InputStream is=null;
    public String input_result=null;
    public String line=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.content_main,null);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        textView = (TextView) findViewById(R.id.text);
        log_status = (TextView) findViewById(R.id.log_status);
        profilePictureView = (ProfilePictureView)findViewById(R.id.profile_picture);
        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions(Arrays.asList("user_friends", "email", "user_birthday"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
                textView.setText(error.getMessage().toString());
                Log.d("connectedStateLabel", error.getMessage().toString());
            }
        });


        try {
            info = getPackageManager().getPackageInfo("rk.android.vision.vision_fb", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchUserInfo() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            user = me;
                            updateUI();
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString(FIELDS, REQUEST_FIELDS);
            request.setParameters(parameters);
            GraphRequest.executeBatchAsync(request);
        } else {
            user = null;
        }
    }


    private void updateUI() {

        if (AccessToken.getCurrentAccessToken() != null) {
            if (user != null) {
                String id = user.optString("id");
                String name = user.optString("name");
                String gender = user.optString("gender");
                String email = user.optString("email");
                String dob = user.optString("birthday");
                textView.setText("Id:"+id
                        + "\n" + "Name:"+name
                        + "\n" + "Gender:"+gender
                        + "\n" + "Email:"+email
                        + "\n" + "DOB:"+dob);
                Log.d("connectedStateLabel", user.optString("id"));
                Log.d("connectedStateLabel",user.optString("picture"));
                Log.d("connectedStateLabel",user.optString("name"));
                Log.d("connectedStateLabel",user.optString("gender"));
                Log.d("connectedStateLabel",user.optString("email"));
                Log.d("connectedStateLabel",user.optString("birthday"));
                profilePictureView.setProfileId(user.optString("id"));
                getData(id,name,gender,email);
            } else {
                log_status.setText("Logged in");
            }
        } else {
            log_status.setText("Not Logged in");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserInfo();
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchUserInfo();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void getData(final String id,final String name,final String gender,final String rec_email) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://192.168.137.1/vision_web/getdata.php");

                // Depends on your web service
                httppost.setHeader("Content-type", "application/json");

                InputStream inputStream = null;
                String result = null;
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    // Oops
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (Exception squish) {
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                showList(id,name,gender,rec_email);
                Log.d("email_web_db",rec_email);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    protected void showList(final String id,final String name,final String gender,final String rec_email){

        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            result = jsonObj.getJSONArray(TAG_MAIL);

            if(result.length()==0){
                Toast.makeText(getApplicationContext(),"results length "+result.length(),Toast.LENGTH_LONG).show();
                insert(id,name,gender,rec_email);
            }

            for(int i=0;i<result.length();i++){
                JSONObject c = result.getJSONObject(i);
                String email = c.getString(TAG_EMAIL);

               Toast.makeText(getApplicationContext(),"results length "+result.length(),Toast.LENGTH_LONG).show();
                if(!rec_email.equals(email)){
                    insert(id,name,gender,rec_email);
                    Toast.makeText(getApplicationContext(),"email matches",Toast.LENGTH_LONG).show();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void insert(final String id,final String name,final String gender,final String email) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://192.168.137.1/vision_web/insert.php");

                InputStream inputStream = null;
                String result = null;
                try {
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("id",id));
                    nameValuePairs.add(new BasicNameValuePair("name",name));
                    nameValuePairs.add(new BasicNameValuePair("gender",gender));
                    nameValuePairs.add(new BasicNameValuePair("email",email));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    // Oops
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (Exception squish) {
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                try
                {
                    JSONObject json_data = new JSONObject(result);
                    code=(json_data.getInt("code"));

                    if(code==1)
                    {
                        Toast.makeText(getBaseContext(), "Inserted Successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Sorry, Try Again",
                                Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e)
                {
                    Log.e("Fail 1", e.toString());
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

}
