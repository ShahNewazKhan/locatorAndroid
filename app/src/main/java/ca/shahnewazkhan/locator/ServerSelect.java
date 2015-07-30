package ca.shahnewazkhan.locator;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;


public class ServerSelect extends AppCompatActivity {

    EditText et_serverUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_select);

        et_serverUrl = (EditText) findViewById(R.id.et_server);
        et_serverUrl.getBackground().setColorFilter(getResources()
                .getColor(R.color.wallet_highlighted_text_holo_dark), PorterDuff.Mode.SRC_ATOP);
    }

    public void testServerUrl(final View v){

        String url = et_serverUrl.getText().toString() + "/api/users";

        if( url != null && !url.isEmpty()){
            testUrl(url,v);
        }else{
            Snackbar.make(v, "Please enter a url", Snackbar.LENGTH_SHORT).show();
        }

    }

    private void testUrl(String url, final View v){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Snackbar.make(v, "Ping succeeded", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int sc, Header[] h, byte[] errorRes, Throwable e) {
                Snackbar.make(v, "Ping failed", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void submitServerUrl(View v){

        String url = et_serverUrl.getText().toString();

        Intent mainActivity = new Intent(this, MainActivity.class);
        mainActivity.putExtra("url", url);
        startActivity(mainActivity);
    }

}
