package br.com.ettec.siga.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Tom on 13/10/2015.
 */
public class BootUpReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {

        //Imei imei = new Imei();

        //String nImei = imei.numCel();


        boolean alarmeAtivoCoordenadas = (PendingIntent.getBroadcast(context, 0, new Intent("ALARME_SIGA_DISPARADO_COORDENADAS"), PendingIntent.FLAG_NO_CREATE) == null);

        if(alarmeAtivoCoordenadas){

            Intent intentSigaCoordenadas = new Intent("ALARME_SIGA_DISPARADO_COORDENADAS");
            PendingIntent p = PendingIntent.getBroadcast(context, 0, intentSigaCoordenadas, 0);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.add(Calendar.SECOND, 3);

            AlarmManager alarme = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarme.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 3 * 300000, p);// tempo de intervalo para repetição do serviço. Neste caso 15 em 15 minutos

        }

    }




}
