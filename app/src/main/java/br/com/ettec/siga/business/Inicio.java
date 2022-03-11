package br.com.ettec.siga.business;

import android.*;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;

import br.com.ettec.siga.*;
import br.com.ettec.siga.R;
import br.com.ettec.siga.business.ListaAtendimentos;
import br.com.ettec.siga.business.Sincronizar;
import br.com.ettec.siga.persistence.AtendimentoTempDAO;

public class Inicio extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private View mProgressView;

    private TextView mensagem;

    private TextView tvAtendimentosFinalizados;

    private static final String PREF_NANME = "LoginPreferences";

    private SQLiteDatabase db = null;

    private static final int REQUEST_LOCATION = 2;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(br.com.ettec.siga.R.layout.activity_inicio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mProgressView = findViewById(R.id.lista_progress);

        mensagem = (TextView) findViewById(R.id.mensagem);

        tvAtendimentosFinalizados = (TextView) findViewById(R.id.textViewatendimentosFinalizados);

        db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

        AtendimentoTempDAO atendimentoTempDAO = new AtendimentoTempDAO(this);

        atendimentoTempDAO.onCreate(db);

        atendimentoTempDAO.close();

        callConnection();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            Location myLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            setAlarm(this);

        } else {

            boolean alarmeAtivoCoordenadas = (PendingIntent.getBroadcast(this, 0, new Intent("ALARME_SIGA_DISPARADO_COORDENADAS"), PendingIntent.FLAG_NO_CREATE) == null);

            if (alarmeAtivoCoordenadas) {

                Intent intentSigaCoordenadas = new Intent("ALARME_SIGA_DISPARADO_COORDENADAS");
                PendingIntent pp = PendingIntent.getBroadcast(this, 0, intentSigaCoordenadas, 0);

                Calendar cc = Calendar.getInstance();
                cc.setTimeInMillis(System.currentTimeMillis());
                cc.add(Calendar.SECOND, 3);

                AlarmManager alarmeCoordenadas = (AlarmManager) getSystemService(ALARM_SERVICE);

                alarmeCoordenadas.setRepeating(AlarmManager.RTC_WAKEUP, cc.getTimeInMillis(), 3 * 300000, pp);// tempo de intervalo para repetição do serviço. Neste caso 15 em 15 minutos


            }

        }


    }

    public static void setAlarm(Context context) {

        boolean alarmeAtivoCoordenadas = (PendingIntent.getBroadcast(context, 0, new Intent("ALARME_SIGA_DISPARADO_COORDENADAS"), PendingIntent.FLAG_NO_CREATE) == null);

        if (alarmeAtivoCoordenadas) {

            Intent intentSigaCoordenadas = new Intent("ALARME_SIGA_DISPARADO_COORDENADAS");
            PendingIntent pp = PendingIntent.getBroadcast(context, 0, intentSigaCoordenadas, 0);

            Calendar cc = Calendar.getInstance();
            cc.setTimeInMillis(System.currentTimeMillis());
            cc.add(Calendar.SECOND, 3);

            AlarmManager alarmeCoordenadas = (AlarmManager) context.getApplicationContext().getSystemService(ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                //alarmeCoordenadas.setWindow(AlarmManager.RTC_WAKEUP, cc.getTimeInMillis(), 300000,pp);
                alarmeCoordenadas.setExact(AlarmManager.RTC_WAKEUP,cc.getTimeInMillis(),pp);

            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mensagem.setText("");

        tvAtendimentosFinalizados.setText("Total de atendimentos não sincronizados: " + "");

        Cursor cursor = db.rawQuery("SELECT * FROM atendimentostemp", null);

        // obtem o total de registros dos atendimentos finalizados no banco de dados do celular
        int totalAF = cursor.getCount();

        tvAtendimentosFinalizados.setText("Total de atendimentos não sincronizados: " + totalAF);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mensagem.setText("");

        Cursor cursor = db.rawQuery("SELECT * FROM atendimentostemp", null);

        // obtem o total de registros dos atendimentos finalizados no banco de dados do celular
        int totalAF = cursor.getCount();

        tvAtendimentosFinalizados.setText("Total de atendimentos não sincronizados: " + totalAF);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inicio, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            showProgress(true);

            mensagem.setText("Carregando lista de atendimentos...");

            Intent it = new Intent(getBaseContext(), ListaAtendimentos.class);

            startActivity(it);

            finish();

        } else if (id == R.id.nav_gallery) {

            showProgressSincronizar(true);

            mensagem.setText("Processo de sincronização em andamento...");

            Intent it = new Intent(getBaseContext(), Sincronizar.class);

            startActivity(it);

            finish();

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgressSincronizar(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

            tvAtendimentosFinalizados.setVisibility(show ? View.GONE : View.VISIBLE);
            tvAtendimentosFinalizados.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvAtendimentosFinalizados.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);

        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

            tvAtendimentosFinalizados.setVisibility(show ? View.GONE : View.VISIBLE);
            tvAtendimentosFinalizados.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvAtendimentosFinalizados.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });


            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);

        }
    }

    private synchronized void callConnection() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
