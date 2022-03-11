package br.com.ettec.siga.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Tom on 13/10/2015.
 */
public class AlarmeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        intent = new Intent("servico_siga_atendimentos");
        context.startService(intent);

    }

}
