package br.com.ettec.siga.util;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by usuario on 11/08/2016.
 */
public class Imei extends Activity {


    // CAPTURANDO NÚMERO DE SÉRIE DO CELULAR:
    public String numCel(){

        String IMEI = "";

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();

        return IMEI;
    }
}
