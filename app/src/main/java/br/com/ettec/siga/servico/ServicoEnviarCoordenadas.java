package br.com.ettec.siga.servico;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.com.ettec.siga.R;
import br.com.ettec.siga.persistence.CoordenadasDAO;
import br.com.ettec.siga.domain.Coordenadas;
import br.com.ettec.siga.util.DataFormatada;
import br.com.ettec.siga.util.DetectaStatusConexao;

/**
 * Created by Tom on 25/08/2015.
 */
/*public class ServicoEnviarCoordenadas extends Service implements Runnable, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {*/

public class ServicoEnviarCoordenadas extends Service implements Runnable, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String latitude;
    private String longitude;
    private SQLiteDatabase db = null;
    private String numeroIMEI;

    private LocationRequest mLocationRequest;

    private LocationListener mLocationListener;

    DetectaStatusConexao dsc = null;

    DataFormatada dataFormatada;

    private CoordenadasAsyncTask coordenadasAsyncTask = null;

    Coordenadas coordenadas;

    boolean ativo = false;

    String patternHorario;

    DateFormat dfHorario;

    Date horarioInicio;

    Date horarioFim;

    public void onCreate() {
        new Thread(ServicoEnviarCoordenadas.this).start();

        numeroIMEI = this.numCel();

        db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

        CoordenadasDAO coordenadasDAO = new CoordenadasDAO(this);

        coordenadasDAO.onCreate(db);

        coordenadasDAO.close();

        db.close();

        dsc = new DetectaStatusConexao(this);

        callConnection();

        dataFormatada = new DataFormatada();

        patternHorario = "HH:mm";

        dfHorario = new SimpleDateFormat(patternHorario);

        try {
            horarioInicio = dfHorario.parse("07:59");
            horarioFim = dfHorario.parse("18:01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return (START_STICKY);

    }


    private void initLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000 * 30); //cinco em cinco minutos
        mLocationRequest.setFastestInterval(10000 * 30);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {

        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

    }


    @Override
    public void run() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            startLocationUpdate();

        }

        stopSelf();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void callConnection() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }


    //LISTENER
    @Override
    public void onConnected(Bundle bundle) {
        Log.i("LOG", "onConnected(" + bundle + ")");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location l) {
                mLocationListener.onLocationChanged(l);
            }
        };

        if(l != null){

            latitude = String.valueOf(l.getLatitude());
            longitude = String.valueOf(l.getLongitude());

            try {

                int diaDaSemana = this.obterDia();

                //if(diaDaSemana!=1){

                    Date horarioAtual = this.obterHorario();

                    //if((horarioAtual.after(horarioInicio)) && (horarioAtual.before(horarioFim))){

                    String dataF = dataFormatada.obterFormatarData();

                    coordenadas = new Coordenadas();

                    coordenadas.setLatitude(latitude);
                    Log.i("Latitude", latitude.toString());
                    coordenadas.setLongitude(longitude);
                    Log.i("Longitude", longitude.toString());
                    coordenadas.setImei(numeroIMEI);
                    coordenadas.setData(dataF);

                    CoordenadasDAO coordenadasDAO = new CoordenadasDAO(this);

                    coordenadasAsyncTask =  new CoordenadasAsyncTask(coordenadas,coordenadasDAO);

                    coordenadasAsyncTask.execute((Void) null);

                   //}


                //}


            } catch (ParseException e) {
                e.printStackTrace();
            }


        }


    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i("LOG", "onConnectionSuspended(" + i + ")");
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("LOG", "onConnectionFailed(" + connectionResult + ")");
    }

    public class CoordenadasAsyncTask extends AsyncTask<Void, Void, Void> {

        private Coordenadas mCoordenadas = new Coordenadas();
        private CoordenadasDAO mCoordenadasDAO;

        CoordenadasAsyncTask(Coordenadas coordenadas,CoordenadasDAO coordenadasDAO) {

            mCoordenadas.setLatitude(coordenadas.getLatitude());
            mCoordenadas.setLongitude(coordenadas.getLongitude());
            mCoordenadas.setImei(coordenadas.getImei());
            mCoordenadas.setData(coordenadas.getData());
            mCoordenadasDAO = coordenadasDAO;

        }


        @Override
        protected Void doInBackground(Void... params) {

            if (dsc.existeConexao()) {

                db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

                Cursor cursor = db.rawQuery("SELECT * FROM coordenadas", null);

                if(!cursor.equals(null)){


                    while (cursor.moveToNext()) {

                        StringBuilder strURL = new StringBuilder();

                        try {
                            strURL.append("http://ettec.com.br/inserircoordenadasiganovo.php?latitude=");
                            strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("latitude")), "UTF-8"));
                            strURL.append("&longitude=");
                            strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("longitude")), "UTF-8"));
                            strURL.append("&imei=");
                            strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("imei")), "UTF-8"));
                            strURL.append("&data=");
                            strURL.append(URLEncoder.encode(cursor.getString(cursor.getColumnIndex("data")), "UTF-8"));

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
                                db.delete("coordenadas", "_id=?", new String[]{String.valueOf(cursor.getInt(0))});
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                            db.close();

                        }


                    }


                }

                db.close();

                StringBuilder strURL2 = new StringBuilder();

                try {
                    strURL2.append("http://ettec.com.br/inserircoordenadasiganovo.php?latitude=");
                    strURL2.append(URLEncoder.encode(mCoordenadas.getLatitude(), "UTF-8"));
                    strURL2.append("&longitude=");
                    strURL2.append(URLEncoder.encode(mCoordenadas.getLongitude(), "UTF-8"));
                    strURL2.append("&imei=");
                    strURL2.append(URLEncoder.encode(mCoordenadas.getImei(), "UTF-8"));
                    strURL2.append("&data=");
                    strURL2.append(URLEncoder.encode(mCoordenadas.getData(), "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }

                try {
                    URL url = new URL(strURL2.toString());
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    new InputStreamReader(http.getInputStream());


                } catch (IOException e) {
                    e.printStackTrace();


                }


                mCoordenadasDAO.close();

                return null;

            }else{

                db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

                mCoordenadasDAO.inserir(db, mCoordenadas);

                mCoordenadasDAO.close();

                db.close();


            }

            return null;



        }

    }

    // CAPTURANDO NÚMERO DE SÉRIE DO CELULAR:
    public String numCel(){

        String IMEI = "";

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();

        return IMEI;
    }


    public int obterDia() throws ParseException {

        Date data = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(data);

        int dia = cal.get(Calendar.DAY_OF_WEEK);

        return dia;

    }

    public Date obterHorario() throws ParseException {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat horarioFormat = new SimpleDateFormat(patternHorario);

        Date data = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        Date data_atual = cal.getTime();

        String horarioFormatado = horarioFormat.format(data_atual);

        Date horario = dfHorario.parse(horarioFormatado);

        return horario;

    }




}
