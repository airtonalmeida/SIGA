package br.com.ettec.siga.servico;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import android.widget.Toast;

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
import br.com.ettec.siga.business.Inicio;
import br.com.ettec.siga.util.DetectaStatusConexao;



/**
 * Created by Tom on 25/08/2015.
 */
public class ServicoEnviarAtendimentos extends Service implements Runnable{

    private SQLiteDatabase db = null;

    DetectaStatusConexao dsc =  null;

    private EnviarAtendiAsyncTask enviarAtendiAsyncTask = null;

    private InformarAtendiAsyncTask informarAtendiAsyncTask = null;

    private boolean sucessoEnviar = false;

    NotificationCompat.Builder mBuilder;

    boolean ativo = false;

    String patternHorario;

    DateFormat dfHorario;

    Date horarioInicio;

    Date horarioFim;

    public void onCreate() {
        new Thread(ServicoEnviarAtendimentos.this).start();

        informarAtendiAsyncTask = new InformarAtendiAsyncTask();

        enviarAtendiAsyncTask = new EnviarAtendiAsyncTask();

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
    public int onStartCommand(Intent intent, int flags, int startId){

        return(START_STICKY);

    }


    @Override
    public void run() {

        //try {

           //int diaDaSemana = this.obterDia();

            //if(diaDaSemana!=1) {

                //Date horarioAtual = this.obterHorario();

                //if ((horarioAtual.after(horarioInicio)) && (horarioAtual.before(horarioFim))) {

                    dsc = new DetectaStatusConexao(this);

                    if (dsc.existeConexao()) {

                        boolean resultadoEnviar = false;

                        try {

                            resultadoEnviar = enviarAtendiAsyncTask.doInBackground();

                            informarAtendiAsyncTask.doInBackground();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (resultadoEnviar == true) {

                            mBuilder = new NotificationCompat.Builder(this)
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

                        } /*else {

                            mBuilder = new NotificationCompat.Builder(this)
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
*/
                    } /*else {

                        mBuilder = new NotificationCompat.Builder(this)
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
*/

                //}

            //}

       /* } catch (ParseException e) {
            e.printStackTrace();
        }*/

        stopSelf();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
