package br.com.ettec.siga.business;

import br.com.ettec.siga.business.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import br.com.ettec.siga.R;
import br.com.ettec.siga.util.DetectaStatusConexao;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Sincronizar extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private SQLiteDatabase db = null;

    DetectaStatusConexao dsc =  null;

    private EnviarAtendiAsyncTask enviarAtendiAsyncTask = null;

    private InformarAtendiAsyncTask informarAtendiAsyncTask = null;

    private IniciarAtendiAsyncTask iniciarAtendiAsyncTask = null;

    private boolean sucessoEnviar = false;

    NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sincronizar);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        iniciarAtendiAsyncTask = new IniciarAtendiAsyncTask();

        informarAtendiAsyncTask = new InformarAtendiAsyncTask();

        enviarAtendiAsyncTask = new EnviarAtendiAsyncTask();

    }

    @Override
    protected void onStart(){
        super.onStart();

        dsc = new DetectaStatusConexao(this);

        if (dsc.existeConexao()) {

            boolean resultadoEnviar = false;

            try {

                resultadoEnviar = enviarAtendiAsyncTask.doInBackground();

                informarAtendiAsyncTask.doInBackground();

                enviarAtendiAsyncTask.doInBackground();


            } catch (Exception e) {
                e.printStackTrace();
            }

            if(resultadoEnviar==true){

                mBuilder =   new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.iconesiga)
                        .setContentTitle("Status Sincronização")
                        .setContentText("A sincronização foi feita com sucesso.");

                Intent resultIntent = new Intent(this, Inicio.class);

                // Because clicking the notification opens a new ("special") activity, there's
                // no need to create an artificial back stack.
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                mBuilder.setContentIntent(resultPendingIntent);

                // Sets an ID for the notification
                int mNotificationId = 001;
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            }else{

                mBuilder =   new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.iconesiga)
                        .setContentTitle("Status Sincronização")
                        .setContentText("Não há dados para serem sincronizados.");

                Intent resultIntent = new Intent(this, Inicio.class);

                // Because clicking the notification opens a new ("special") activity, there's
                // no need to create an artificial back stack.
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                mBuilder.setContentIntent(resultPendingIntent);

                // Sets an ID for the notification
                int mNotificationId = 001;
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());


            }

        }else{

            mBuilder =   new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.iconesiga)
                    .setContentTitle("Status Sincronização")
                    .setContentText("Não há conexão com a internet.");

            Intent resultIntent = new Intent(this, Inicio.class);

            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);

            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());



        }

        Intent it  = new Intent(getBaseContext(), Inicio.class);

        startActivity(it);

        finish();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public class IniciarAtendiAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentosiniciartemp", null);
            int totalDBInf = cursor.getCount();

            if(!cursor.equals(null)){

                int totalReplicadoInf = 0;

                while (cursor.moveToNext()) {

                    StringBuilder strURL = new StringBuilder();

                    try {
                        strURL.append("http://ettec.com.br/iniciaratendimentosiganovo.php?cod=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("cod")), "UTF-8"));
                        strURL.append("&obscolaborador=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("obscolaborador")), "UTF-8"));
                        strURL.append("&latitude=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("latitude")), "UTF-8"));
                        strURL.append("&longitude=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("longitude")), "UTF-8"));
                        strURL.append("&dataatendimento=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("dataAtendimento")), "UTF-8"));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();

                    }

                    try {
                        URL url = new URL(strURL.toString());
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();
                        InputStreamReader ips = new InputStreamReader(http.getInputStream());
                        BufferedReader line = new BufferedReader(ips);

                        String linhaRetorno = line.readLine();

                        if (linhaRetorno.equals("Y")) {
                            db.delete("atendimentosiniciartemp", "_id=?", new String[]{String.valueOf(cursor.getInt(0))});

                            totalReplicadoInf++;
                        }


                    } catch (IOException e) {
                        e.printStackTrace();

                    }


                }

                if(totalDBInf != totalReplicadoInf){

                    exibirMensagem();

                }


            }

            cursor.close();

            return null;
        }
    }


    public class InformarAtendiAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentosinformartemp", null);
            int totalDBInf = cursor.getCount();

            if(!cursor.equals(null)){

                int totalReplicadoInf = 0;

                while (cursor.moveToNext()) {

                    StringBuilder strURL = new StringBuilder();

                    try {
                        strURL.append("http://ettec.com.br/informaratendimentosiganovo.php?cod=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("cod")), "UTF-8"));
                        strURL.append("&obscolaborador=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("obscolaborador")), "UTF-8"));
                        strURL.append("&latitude=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("latitude")), "UTF-8"));
                        strURL.append("&longitude=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("longitude")), "UTF-8"));
                        strURL.append("&dataatendimento=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("dataAtendimento")), "UTF-8"));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();

                    }

                    try {
                        URL url = new URL(strURL.toString());
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();
                        InputStreamReader ips = new InputStreamReader(http.getInputStream());
                        BufferedReader line = new BufferedReader(ips);

                        String linhaRetorno = line.readLine();

                        if (linhaRetorno.equals("Y")) {
                            db.delete("atendimentosinformartemp", "_id=?", new String[]{String.valueOf(cursor.getInt(0))});

                            totalReplicadoInf++;
                        }


                    } catch (IOException e) {
                        e.printStackTrace();

                    }


                }

                if(totalDBInf != totalReplicadoInf){

                    exibirMensagem();

                }


            }

            cursor.close();

            return null;
        }
    }


    public class EnviarAtendiAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return null;
            }


            db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentostemp", null);
            int totalDBEnv = cursor.getCount();

            if(!cursor.equals(null)){

                int totalReplicadoEnv = 0;

                while (cursor.moveToNext()) {

                    StringBuilder strURL = new StringBuilder();

                    try {
                        strURL.append("http://ettec.com.br/finalizaratendimentosiganovo.php?cod=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("cod")), "UTF-8"));
                        strURL.append("&obscolaborador=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("obscolaborador")), "UTF-8"));
                        strURL.append("&latitude=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("latitude")), "UTF-8"));
                        strURL.append("&longitude=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("longitude")), "UTF-8"));
                        strURL.append("&dataatendimento=");
                        strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("dataAtendimento")), "UTF-8"));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();

                    }

                    try {
                        URL url = new URL(strURL.toString());
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();
                        InputStreamReader ips = new InputStreamReader(http.getInputStream());
                        BufferedReader line = new BufferedReader(ips);

                        String linhaRetorno = line.readLine();

                        if (linhaRetorno.equals("Y")) {
                            db.delete("atendimentostemp", "_id=?", new String[]{String.valueOf(cursor.getInt(0))});

                            totalReplicadoEnv++;
                        }


                    } catch (IOException e) {
                        e.printStackTrace();

                    }


                }

                if((totalDBEnv == totalReplicadoEnv)&&(totalDBEnv>0)){

                    sucessoEnviar = true;

                }if(totalDBEnv==0){

                    sucessoEnviar = false;

                }if(totalDBEnv != totalReplicadoEnv){

                    exibirMensagem();

                }

            }

            cursor.close();

            return  sucessoEnviar;
        }
    }

    public  void exibirMensagem(){

        mBuilder =   new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.iconesiga)
                .setContentTitle("Status Sincronização")
                .setContentText("Houve erro na sincronização.");

        Intent resultIntent = new Intent(this, Inicio.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

}
