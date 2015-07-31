package ca.shahnewazkhan.locator;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LOCATOR-APP";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 6000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private String mongoID;

    private CallbackManager mCallbackManager;
    private Context context;
    private ProfilePictureView profilePictureView;
    private RecyclerView recList;
    private Profile profile;
    private String locatorApi = "http://107.170.234.15:3000/api/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        mCallbackManager = CallbackManager.Factory.create();
        context = getApplicationContext();
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        profilePictureView.isInEditMode();

        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.isInEditMode();
        LoginManager.getInstance().registerCallback(mCallbackManager, mCallback);

        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken,
                                                       AccessToken newToken) {
                if(newToken == null){
                    logOut();
                }
            }
        };

        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");

        if( !url.equals("pass") ){
            locatorApi = url + "/api/users/" ;
            Toast.makeText(context, "Updated: " + locatorApi, Toast.LENGTH_SHORT ).show();
        }

        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        logOut();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
       /* Toast.makeText(this, "Location Updated",
                Toast.LENGTH_SHORT).show();*/

        if(profile != null){
            deleteUserFromApi(true);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    //After successful facebook login Store user fb id, name & location to api & load cards with
    //current users logged in from api
    private FacebookCallback<LoginResult> mCallback= new FacebookCallback<LoginResult>(){
        @Override
        public void onSuccess(LoginResult loginResult) {

            profile = Profile.getCurrentProfile();

            if(profile != null){
               preparePostUser();
            }
        }
        @Override public void onCancel() {}
        @Override public void onError(FacebookException e) {}
    };

    private void preparePostUser(){

        //Set fb profile picture
        profilePictureView.setProfileId(profile.getId());

        //Prepare params for api post call
        RequestParams params = new RequestParams();
        params.put("name", profile.getName());
        params.put("fb_id", profile.getId());
        params.put("lat", mCurrentLocation.getLatitude());
        params.put("lon", mCurrentLocation.getLongitude());

        //Post to api
        postNewUser(params);

        //Populate user cards
        populateCards();
    }

    //Post user data (fb id, name & location) to api
    private void postNewUser(RequestParams params){
        LocatorRestClient.post(locatorApi, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Get mongo id from response
                try {
                    mongoID = response.getString("_id");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void populateCards(){

        Log.d("GETTING", "USERS");

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(locatorApi, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    JSONArray res = new JSONArray(new String(response));
                    updateUI(res);

                } catch (Exception e) {
                }

            }

            @Override
            public void onFailure(int sc, Header[] h, byte[] errorRes, Throwable e) {
            }
        });

    }

    private void updateUI(JSONArray res){

        List users = new ArrayList();

        try{
            for (int i = 0; i < res.length(); i++) {

                JSONObject jsonObj = (JSONObject) res.get(i);

                //Current user loged into app
                if( mongoID != null && mongoID.equals(jsonObj.getString("_id")))
                    continue;

                UserCardInfo uci = new UserCardInfo();
                uci.name = (String) jsonObj.get("name");
                uci.fb_id = String.valueOf(jsonObj.get("fb_id"));
                uci.distance = getDistance(jsonObj.getDouble("lat"), jsonObj.getDouble("lon"));

                users.add(uci);
            }

            Collections.sort(users, compDistance());

        }catch(Exception e){ e.printStackTrace();}


        UserAdapter userAdapter = new UserAdapter(users);
        if(recList != null){
            Log.d(TAG, "Updating UI");
            recList.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
            //mSwipeRefreshLayout.setRefreshing(false);

        } else {
            Log.d(TAG, "recList is NULL");
        }
    }

    public static Comparator<UserCardInfo> compDistance()
    {
        Comparator comp = new Comparator<UserCardInfo>(){
            @Override
            public int compare(UserCardInfo u1, UserCardInfo u2)
            {
                return Double.compare(Double.parseDouble(u1.distance), Double.parseDouble(u2.distance));
            }
        };
        return comp;
    }

    private String getDistance( double lat, double lon ){

        Location userLocation = new Location("User location");
        userLocation.setLatitude(lat);
        userLocation.setLongitude(lon);

        DecimalFormat df = new DecimalFormat("0.00");
        String distance = df.format(mCurrentLocation.distanceTo(userLocation));

        return distance;
    }

    //Log out user form facebook, delete user data from API and remove user profile pic
    private void logOut(){

        //Remove user from api
        deleteUserFromApi(false);

        //Remove user profile pic
        profilePictureView.setProfileId("");

        //Clear cards
        UserAdapter userAdapter = new UserAdapter(new ArrayList());
        if(recList != null){
            Log.d(TAG, "Clearing Cards");
            recList.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "recList is NULL");
        }
        //Logout user
        LoginManager.getInstance().logOut();

        profile = null;
    }

    private void deleteUserFromApi(final boolean postUser){
        //Delete user data from api
        AsyncHttpClient client = new AsyncHttpClient();
        client.delete(locatorApi + mongoID, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                if(postUser){
                    preparePostUser();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                  Throwable error) {
                error.printStackTrace(System.out);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
