package br.com.ettec.siga.business;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import br.com.ettec.siga.R;
import br.com.ettec.siga.domain.Atendimento;
import br.com.ettec.siga.domain.Coordenadas;
import br.com.ettec.siga.persistence.AtendimentoInformarTempDAO;
import br.com.ettec.siga.persistence.AtendimentoIniciarTempDAO;
import br.com.ettec.siga.persistence.AtendimentoTempDAO;
import br.com.ettec.siga.util.DataFormatada;
import br.com.ettec.siga.util.DetectaStatusConexao;

public class FinalizarAtendimento extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    Atendimento atendimento;

    EditText editTextObsColaborador;

    DetectaStatusConexao dsc;

    SQLiteDatabase db = null;

    private int id;

    private EnviarAtendimentoAsyncTask enviarAtendimentoAsyncTask = null;

    private FinalizarAtendimentoAsyncTask finalizarAtendimentoAsyncTask = null;

    private InformarAtendimentoAsyncTask informarAtendimentoAsyncTask = null;

    private IniciarAtendimentoAsyncTask iniciarAtendimentoAsyncTask = null;

    private GoogleApiClient mGoogleApiClient;

    private String latitude;

    private String longitude;

    private String obsDoColaborador;

    private int iniciado;

    Coordenadas coordenadas = null;

    DataFormatada dataFormatada;

    AtendimentoTempDAO atendimentoTempDAO;

    AtendimentoInformarTempDAO atendimentoInformarTempDAO;

    AtendimentoIniciarTempDAO atendimentoIniciarTempDAO;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalizar_atendimento_activity);

        editTextObsColaborador = (EditText) findViewById(R.id.editTextObsColaborador);

        callConnection();

        dsc = new DetectaStatusConexao(this);

        atendimentoTempDAO = new AtendimentoTempDAO(this);

        atendimentoInformarTempDAO = new AtendimentoInformarTempDAO(this);

        atendimentoIniciarTempDAO = new AtendimentoIniciarTempDAO(this);

        dataFormatada = new DataFormatada();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

        if (dsc.existeConexao()) {

            enviarAtendimentoAsyncTask = new EnviarAtendimentoAsyncTask();

            enviarAtendimentoAsyncTask.execute((Void) null);

        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            callConnection();

        }

        Intent it = getIntent();

        id = it.getIntExtra("id", 0);


        Cursor cursor = db.rawQuery("SELECT * FROM atendimentos WHERE _id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {

            TextView textViewCliente = (TextView) findViewById(R.id.textViewCliente);
            TextView textViewData = (TextView) findViewById(R.id.textViewData);
            TextView textViewTelefone = (TextView) findViewById(R.id.textViewTelefone);
            TextView textViewEndereco = (TextView) findViewById(R.id.textViewEndereco);
            TextView textViewNumero = (TextView) findViewById(R.id.textViewNumero);
            TextView textViewComplemento = (TextView) findViewById(R.id.textViewComplemento);
            TextView textViewPontoReferencia = (TextView) findViewById(R.id.textViewPontoReferencia);
            TextView textViewBairro = (TextView) findViewById(R.id.textViewBairro);
            TextView textViewCidade = (TextView) findViewById(R.id.textViewCidade);
            TextView textViewObservacao = (TextView) findViewById(R.id.textViewObservacao);
            EditText editTextObsColaborador = (EditText) findViewById(R.id.editTextObsColaborador);
            TextView textViewContato = (TextView) findViewById(R.id.textViewContato);

            textViewCliente.setText(cursor.getString(2));
            textViewData.setText(cursor.getString(3));
            textViewTelefone.setText(cursor.getString(4) + " / " + cursor.getString(5));
            textViewEndereco.setText(cursor.getString(6));
            textViewNumero.setText(cursor.getString(7));
            textViewComplemento.setText(cursor.getString(8));
            textViewPontoReferencia.setText(cursor.getString(9));
            textViewBairro.setText(cursor.getString(10));
            textViewCidade.setText(cursor.getString(11));
            obsDoColaborador = cursor.getString(12);
            if (obsDoColaborador == null) {
                obsDoColaborador = "";
            }

            textViewObservacao.setText(cursor.getString(13));
            int restam = 255 - obsDoColaborador.length();
            editTextObsColaborador.setHint("Restam " + restam + " caracteres.");

            iniciado = cursor.getInt(17);

            if (iniciado == 0) {

                Button viewByIdIniciar = (Button) findViewById(R.id.buttonIniciarAtendimento);

                viewByIdIniciar.setVisibility(View.VISIBLE);

                Button viewByIdFinalizar = (Button) findViewById(R.id.buttonFinalizarAtendimento);

                viewByIdFinalizar.setVisibility(View.GONE);

            }

            if (iniciado == 1) {

                Button viewByIdIniciar = (Button) findViewById(R.id.buttonIniciarAtendimento);

                viewByIdIniciar.setVisibility(View.GONE);

                Button viewByIdFinalizar = (Button) findViewById(R.id.buttonFinalizarAtendimento);

                viewByIdFinalizar.setVisibility(View.VISIBLE);

            }

            textViewContato.setText(cursor.getString(18));


        }

        cursor.close();


    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("FinalizarAtendimento Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        AppIndex.AppIndexApi.start(client2, getIndexApiAction());
    }

    /**
     * Método que tem como finalidade enviar os atendimentos inseridos no banco de dados do
     * dispositivo. Esse método é só executado no carregamento da activity, se houver conexão com a Internet.
     */
    public class EnviarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentostemp", null);

            if (!cursor.equals(null)) {

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
                        }


                    } catch (IOException e) {
                        e.printStackTrace();


                    }


                }


            }

            cursor.close();

            return null;
        }
    }


    @SuppressLint("ShowToast")
    public void iniciarClick(View v) {

        AlertDialog.Builder msg = new AlertDialog.Builder(FinalizarAtendimento.this);

        msg.setMessage("Iniciar atendimento?");
        msg.setNegativeButton("Não", null);
        msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String dataAtendimento = dataFormatada.obterFormatarData2();

                atendimento = new Atendimento();

                Cursor cursor = db.rawQuery("SELECT cod FROM atendimentos WHERE _id = ?", new String[]{String.valueOf(id)});

                if (cursor.moveToFirst()) {

                    atendimento.setCod(Integer.parseInt(cursor.getString(0)));

                    if (obsDoColaborador.equalsIgnoreCase("")) {

                        atendimento.setObscolaborador(editTextObsColaborador.getText().toString());

                    } else {

                        atendimento.setObscolaborador(obsDoColaborador + " + " + editTextObsColaborador.getText().toString());

                    }


                    atendimento.setLatitude(coordenadas.getLatitude());

                    atendimento.setLongitude(coordenadas.getLongitude());

                    atendimento.setDataAtendimento(dataAtendimento);

                    atendimento.setIniciado(1);

                    atendimento.setIniciadoString("Iniciado");

                    ContentValues cv = new ContentValues();

                    cv.put("iniciado", atendimento.getIniciado());

                    cv.put("iniciadoString", atendimento.getIniciadoString());

                    int atendimentos = db.update("atendimentos", cv, "cod = " + String.valueOf(atendimento.getCod()), null);


                }


                iniciarAtendimentoAsyncTask = new IniciarAtendimentoAsyncTask(atendimento, atendimentoIniciarTempDAO);

                iniciarAtendimentoAsyncTask.execute((Void) null);

                Intent it = new Intent(getBaseContext(), Inicio.class);

                startActivity(it);

                finish();

            }
        });

        msg.show();

    }

    public class IniciarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {

        private Atendimento mAtendimentoIniciar = new Atendimento();
        private AtendimentoIniciarTempDAO mAtendimentoIniciarTempDAO;

        IniciarAtendimentoAsyncTask(Atendimento atendimento, AtendimentoIniciarTempDAO atendimentoIniciarTempDAO) {

            mAtendimentoIniciar.setCod(atendimento.getCod());
            mAtendimentoIniciarTempDAO = atendimentoIniciarTempDAO;

        }


        @Override
        protected Void doInBackground(Void... params) {

            if (dsc.existeConexao()) {

                StringBuilder strURL2 = new StringBuilder();

                try {
                    strURL2.append("http://ettec.com.br/iniciaratendimentosiganovo.php?cod=");
                    strURL2.append(URLEncoder.encode(Integer.toString(atendimento.getCod()), "UTF-8"));
                    strURL2.append("&obscolaborador=");
                    strURL2.append(URLEncoder.encode(atendimento.getObscolaborador(), "UTF-8"));
                    strURL2.append("&latitude=");
                    strURL2.append(URLEncoder.encode(atendimento.getLatitude(), "UTF-8"));
                    strURL2.append("&longitude=");
                    strURL2.append(URLEncoder.encode(atendimento.getLongitude(), "UTF-8"));
                    strURL2.append("&dataatendimento=");
                    strURL2.append(URLEncoder.encode(atendimento.getDataAtendimento(), "UTF-8"));

                    URL url = new URL(strURL2.toString());
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    InputStreamReader ips = new InputStreamReader(http.getInputStream());
                    new BufferedReader(ips);

                } catch (IOException e) {
                    e.printStackTrace();

                }

                db.close();


            } else {


                mAtendimentoIniciarTempDAO.inserir(db, atendimento);

                mAtendimentoIniciarTempDAO.close();

                db.close();

            }

            return null;
        }
    }


    @SuppressLint("ShowToast")
    public void finalizarClick(View v) {

        AlertDialog.Builder msg = new AlertDialog.Builder(FinalizarAtendimento.this);

        msg.setMessage("Finalizar atendimento?");
        msg.setNegativeButton("Não", null);
        msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String dataAtendimento = dataFormatada.obterFormatarData2();

                atendimento = new Atendimento();

                Cursor cursor = db.rawQuery("SELECT cod FROM atendimentos WHERE _id = ?", new String[]{String.valueOf(id)});

                if (cursor.moveToFirst()) {

                    atendimento.setCod(Integer.parseInt(cursor.getString(0)));

                    if (obsDoColaborador.equalsIgnoreCase("")) {

                        atendimento.setObscolaborador(editTextObsColaborador.getText().toString());

                    } else {

                        atendimento.setObscolaborador(obsDoColaborador + " + " + editTextObsColaborador.getText().toString());

                    }


                    atendimento.setAtivo(0);

                    atendimento.setLatitude(coordenadas.getLatitude());

                    atendimento.setLongitude(coordenadas.getLongitude());

                    atendimento.setDataAtendimento(dataAtendimento);
                }


                finalizarAtendimentoAsyncTask = new FinalizarAtendimentoAsyncTask(atendimento, atendimentoTempDAO);

                finalizarAtendimentoAsyncTask.execute((Void) null);

                Intent it = new Intent(getBaseContext(), Inicio.class);

                startActivity(it);

                finish();

            }
        });

        msg.show();

    }

    public class FinalizarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {

        private Atendimento mAtendimento = new Atendimento();
        private AtendimentoTempDAO mAtendimentoTempDAO;

        FinalizarAtendimentoAsyncTask(Atendimento atendimento, AtendimentoTempDAO atendimentoTempDAO) {

            mAtendimento.setCod(atendimento.getCod());
            mAtendimentoTempDAO = atendimentoTempDAO;

        }


        @Override
        protected Void doInBackground(Void... params) {

            if (dsc.existeConexao()) {

                StringBuilder strURL2 = new StringBuilder();

                try {
                    strURL2.append("http://ettec.com.br/finalizaratendimentosiganovo.php?cod=");
                    strURL2.append(URLEncoder.encode(Integer.toString(atendimento.getCod()), "UTF-8"));
                    strURL2.append("&obscolaborador=");
                    strURL2.append(URLEncoder.encode(atendimento.getObscolaborador(), "UTF-8"));
                    strURL2.append("&latitude=");
                    strURL2.append(URLEncoder.encode(atendimento.getLatitude(), "UTF-8"));
                    strURL2.append("&longitude=");
                    strURL2.append(URLEncoder.encode(atendimento.getLongitude(), "UTF-8"));
                    strURL2.append("&dataatendimento=");
                    strURL2.append(URLEncoder.encode(atendimento.getDataAtendimento(), "UTF-8"));

                    URL url = new URL(strURL2.toString());
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    InputStreamReader ips = new InputStreamReader(http.getInputStream());
                    BufferedReader line = new BufferedReader(ips);

                    String linhaRetorno = line.readLine();

                    if (linhaRetorno.equals("Y")) {
                        db.delete("atendimentos", "cod=?", new String[]{String.valueOf(atendimento.getCod())});
                    }


                } catch (IOException e) {
                    e.printStackTrace();

                }

                db.close();


            } else {


                mAtendimentoTempDAO.inserir(db, atendimento);

                db.delete("atendimentos", "cod=?", new String[]{String.valueOf(mAtendimento.getCod())});

                mAtendimentoTempDAO.close();

                db.close();

            }

            return null;
        }
    }

    @SuppressLint("ShowToast")
    public void informarClick(View v) {

        AlertDialog.Builder msg = new AlertDialog.Builder(FinalizarAtendimento.this);

        msg.setMessage("Informar atendimento?");
        msg.setNegativeButton("Não", null);
        msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String dataAtendimento = dataFormatada.obterFormatarData2();

                atendimento = new Atendimento();

                Cursor cursor = db.rawQuery("SELECT cod FROM atendimentos WHERE _id = ?", new String[]{String.valueOf(id)});

                if (cursor.moveToFirst()) {

                    atendimento.setCod(Integer.parseInt(cursor.getString(0)));

                    if (obsDoColaborador.equalsIgnoreCase("")) {

                        atendimento.setObscolaborador(editTextObsColaborador.getText().toString());

                    } else {

                        atendimento.setObscolaborador(obsDoColaborador + " + " + editTextObsColaborador.getText().toString());

                    }


                    atendimento.setLatitude(coordenadas.getLatitude());

                    atendimento.setLongitude(coordenadas.getLongitude());

                    atendimento.setDataAtendimento(dataAtendimento);
                }


                informarAtendimentoAsyncTask = new InformarAtendimentoAsyncTask(atendimento, atendimentoInformarTempDAO);

                informarAtendimentoAsyncTask.execute((Void) null);

                Intent it = new Intent(getBaseContext(), Inicio.class);

                startActivity(it);

                finish();

            }
        });

        msg.show();

    }

    public class InformarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {

        private Atendimento mAtendimentoInformar = new Atendimento();
        private AtendimentoInformarTempDAO mAtendimentoInformarTempDAO;

        InformarAtendimentoAsyncTask(Atendimento atendimento, AtendimentoInformarTempDAO atendimentoInformarTempDAO) {

            mAtendimentoInformar.setCod(atendimento.getCod());
            mAtendimentoInformarTempDAO = atendimentoInformarTempDAO;

        }


        @Override
        protected Void doInBackground(Void... params) {

            if (dsc.existeConexao()) {

                StringBuilder strURL2 = new StringBuilder();

                try {
                    strURL2.append("http://ettec.com.br/informaratendimentosiganovo.php?cod=");
                    strURL2.append(URLEncoder.encode(Integer.toString(atendimento.getCod()), "UTF-8"));
                    strURL2.append("&obscolaborador=");
                    strURL2.append(URLEncoder.encode(atendimento.getObscolaborador(), "UTF-8"));
                    strURL2.append("&latitude=");
                    strURL2.append(URLEncoder.encode(atendimento.getLatitude(), "UTF-8"));
                    strURL2.append("&longitude=");
                    strURL2.append(URLEncoder.encode(atendimento.getLongitude(), "UTF-8"));
                    strURL2.append("&dataatendimento=");
                    strURL2.append(URLEncoder.encode(atendimento.getDataAtendimento(), "UTF-8"));

                    URL url = new URL(strURL2.toString());
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    InputStreamReader ips = new InputStreamReader(http.getInputStream());
                    new BufferedReader(ips);

                } catch (IOException e) {
                    e.printStackTrace();

                }

                db.close();


            } else {


                mAtendimentoInformarTempDAO.inserir(db, atendimento);

                mAtendimentoInformarTempDAO.close();

                db.close();

            }

            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client2, getIndexApiAction());

        atendimentoTempDAO.close();

        obsDoColaborador = "";

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.disconnect();
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

        if (l != null) {

            latitude = String.valueOf(l.getLatitude());
            longitude = String.valueOf(l.getLongitude());

            coordenadas = new Coordenadas();

            coordenadas.setLatitude(latitude);
            coordenadas.setLongitude(longitude);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_finalizar_atendimento, menu);
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

    public void onBackPressed() {

        Intent it = new Intent(getBaseContext(), ListaAtendimentos.class);

        startActivity(it);

        finish();


    }

}
