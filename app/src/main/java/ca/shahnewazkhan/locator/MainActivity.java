package ca.shahnewazkhan.locator;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private Context context;
    private ProfilePictureView profilePictureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        mCallbackManager = CallbackManager.Factory.create();
        context = getApplicationContext();
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        profilePictureView.isInEditMode();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.isInEditMode();
        LoginManager.getInstance().registerCallback(mCallbackManager, mCallback);

        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken,
                                                       AccessToken newToken) {
                if(newToken == null){
                    Toast.makeText(context, "LOGED OUT", Toast.LENGTH_SHORT).show();
                    profilePictureView.setProfileId("");
                }

            }
        };

    }

    private FacebookCallback<LoginResult> mCallback= new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Profile profile = Profile.getCurrentProfile();

            if(profile != null){
                Toast.makeText(context, profile.getId(), Toast.LENGTH_SHORT).show();
                Log.d("FBID", profile.getId());
                profilePictureView.setProfileId(profile.getId());
            }
        }
        @Override public void onCancel() {}

        @Override public void onError(FacebookException e) {}
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
