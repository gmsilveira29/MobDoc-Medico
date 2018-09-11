package com.gustavo.mobdoc_v0;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener{

    private static String API_KEY = "45975582";
    private static String SESSION_ID = "2_MX40NTk3NTU4Mn5-MTUwNzQ3ODMwMDAxOX5xZG50ZTZOZXZVUVMzNXh0c3A0bDN2azF-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NTk3NTU4MiZzaWc9Njc2Mjk4NzY5YjBkMTU0NzE1MGJjMmMxZmFmOTZjYWI5NTcyNTcxMzpzZXNzaW9uX2lkPTJfTVg0ME5UazNOVFU0TW41LU1UVXdOelEzT0RNd01EQXhPWDV4Wkc1MFpUWk9aWFpWVVZNek5YaDBjM0EwYkROMmF6Ri1mZyZjcmVhdGVfdGltZT0xNTA3NDc4MzIxJm5vbmNlPTAuNjc1MTY4NzYzNzg2MDcxMyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTEwMDY2NzIwJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;
    private Session mSession;
    private FrameLayout r4;
    private FrameLayout mSubscriberViewContainer;
    private FrameLayout mPublisherViewContainer;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private AlertDialog alerta;


    FirebaseDatabase database;
    DatabaseReference refSessao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        requestPermissions();

        database = FirebaseDatabase.getInstance();
        refSessao = database.getReference("quartos/quarto1/sessao");

        // Read from the database
        refSessao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if(value.equals("n")){
                    mSubscriberViewContainer.removeAllViews();
                    mSession.disconnect();
                    finish();
                    startLogin();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {

            // initialize view objects from your layout
            mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);

            // initialize and connect to the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }
    // SessionListener methods

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }

    // PublisherListener methods

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }
    @Override
    public void onBackPressed() {
        caixaDialogo();
    }

    public void caixaDialogo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Encerrar chamada");
        builder.setMessage("Um médico está te visitando, deseja encerrar mesmo assim?");
        //define um botão como positivo
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                mSubscriberViewContainer.removeAllViews();
                mSession.disconnect();
                refSessao.setValue("n");
                finish();
                startLogin();

            }
        });
        //define um botão como negativo
        builder.setNegativeButton("Não, encerrar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
        alerta.dismiss();
            }
        });

        alerta = builder.create();
        alerta.show();
    }
    private void startLogin(){
        Intent login = new Intent(this,LoginActivity.class);
        startActivity(login);
    }


}
