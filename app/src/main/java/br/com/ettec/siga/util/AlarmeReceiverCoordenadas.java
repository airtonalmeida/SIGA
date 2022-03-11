package br.com.ettec.siga.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.Date;
import br.com.ettec.siga.business.Inicio;
import br.com.ettec.siga.servico.ServicoEnviarCoordenadas;

/**
 * Created by Tom on 13/05/2016.
 */
public class AlarmeReceiverCoordenadas extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            Inicio.setAlarm(context);

        }

            intent = new Intent(context, ServicoEnviarCoordenadas.class);
            context.startService(intent);





    }



}
