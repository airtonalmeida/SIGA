package br.com.ettec.siga.business;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import br.com.ettec.siga.R;
import br.com.ettec.siga.domain.Atendimento;
import br.com.ettec.siga.domain.Usuario;
import br.com.ettec.siga.persistence.AtendimentoDAO;
import br.com.ettec.siga.persistence.AtendimentoInformarTempDAO;
import br.com.ettec.siga.persistence.AtendimentoIniciarTempDAO;
import br.com.ettec.siga.util.DetectaStatusConexao;
import android.text.Editable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ImageView sigaImagem;
    private String imei;
    private int idColaborador;
    private boolean logado;
    Usuario usuar;
    private boolean usuarioAtivo=true;

    DetectaStatusConexao dsc;

    private SQLiteDatabase db = null;

    private static final String PREF_NANME = "LoginPreferences";

    private SharedPreferences.OnSharedPreferenceChangeListener callback = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i("Cod", key+"update" );
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

        AtendimentoDAO atendimentoDAO = new AtendimentoDAO(this);

        atendimentoDAO.onCreate(db);

        atendimentoDAO.close();

        AtendimentoInformarTempDAO atendimentoInformarTempDAO = new AtendimentoInformarTempDAO(this);

        atendimentoInformarTempDAO.onCreate(db);

        atendimentoInformarTempDAO.close();

        AtendimentoIniciarTempDAO atendimentoIniciarTempDAO = new AtendimentoIniciarTempDAO(this);

        atendimentoIniciarTempDAO.onCreate(db);

        atendimentoIniciarTempDAO.close();

        imei = this.numCel();

        // Set up the login form.
        mLoginView = (EditText) findViewById(R.id.login);
        mLoginView.addTextChangedListener(Mask.insert("###.###.###-##", mLoginView));

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.login_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        sigaImagem =  (ImageView) findViewById(R.id.imageView);

        sigaImagem.setImageResource(R.mipmap.iconesiga);

        dsc = new DetectaStatusConexao(this);

        SharedPreferences sp1 = getSharedPreferences(PREF_NANME, MODE_PRIVATE );
        idColaborador = sp1.getInt("idCol", 0);
        logado = sp1.getBoolean("logado", false);

        sp1.registerOnSharedPreferenceChangeListener(callback);


    }

    @Override
    protected void onStart(){
        super.onStart();

        SharedPreferences sp1 = getSharedPreferences(PREF_NANME, MODE_PRIVATE );

        logado = sp1.getBoolean("logado",false);

        if(logado){

            Intent it  = new Intent(getBaseContext(), Inicio.class);

            startActivity(it);

            finish();

        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        SharedPreferences sp1 = getSharedPreferences(PREF_NANME, MODE_PRIVATE );
        sp1.unregisterOnSharedPreferenceChangeListener(callback);

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        if (dsc.existeConexao()) {

            if (mAuthTask != null) {
                return;
            }

            // Reset errors.
            mLoginView.setError(null);
            mPasswordView.setError(null);

            // Store values at the time of the login attempt.
            String usuario = mLoginView.getText().toString();
            String password = mPasswordView.getText().toString();


            boolean cancel = false;
            View focusView = null;


            // Check for a valid usuario.
            if (TextUtils.isEmpty(usuario) && TextUtils.isEmpty(password)) {
                mLoginView.setError(getString(R.string.error_field_required));
                focusView = mLoginView;
                cancel = true;
            }

            // Check for a valid usuario.
            if (TextUtils.isEmpty(usuario) && !TextUtils.isEmpty(password)) {
                mLoginView.setError(getString(R.string.error_field_required));
                focusView = mLoginView;
                cancel = true;
            }


            if (TextUtils.isEmpty(password) && !TextUtils.isEmpty(usuario)) {
                mPasswordView.setError(getString(R.string.error_field_required));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }


            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);
                mAuthTask = new UserLoginTask(usuario, password, imei);
                mAuthTask.execute((Void) null);
            }
        }else{

            Toast.makeText(getBaseContext(), "Sem conexão com a Internet!", Toast.LENGTH_LONG).show();

        }

    }



    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
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
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mPassword;
        private final String mImei;

        UserLoginTask(String usuario, String password, String pImei) {
            mLogin = usuario;
            mPassword = password;
            mImei = pImei;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

              ArrayList<Usuario> retorno= new ArrayList<Usuario>();

            try {
                StringBuilder strURL = new StringBuilder();
                strURL.append("http://ettec.com.br/loginsiganovo.php?login=");
                strURL.append(URLEncoder.encode(mLogin, "UTF-8"));
                strURL.append("&senha=");
                strURL.append(URLEncoder.encode(mPassword, "UTF-8"));
                strURL.append("&imei=");
                strURL.append(URLEncoder.encode(mImei, "UTF-8"));


                URL url = new URL(strURL.toString());
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                InputStreamReader ips = new InputStreamReader(http.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(ips);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null){

                    sb.append(line + "\n");
                    retorno = tratarJson(sb.toString());

                    usuar = new Usuario();

                    for (int i = 0; i < retorno.size(); i++) {

                        usuar.setIdColaborador(retorno.get(i).getIdColaborador());
                        usuar.setLogin(retorno.get(i).getLogin());
                        usuar.setAtivo(retorno.get(i).getAtivo());

                    }

                    if((mLogin.equals(usuar.getLogin()))&&(usuar.getAtivo()==1)){

                        usuarioAtivo = true;

                        idColaborador = usuar.getIdColaborador();

                        SharedPreferences sp1 = getSharedPreferences(PREF_NANME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp1.edit();
                        editor.putInt("idCol", idColaborador);
                        editor.putBoolean("logado", true);
                        editor.commit();

                        // TODO: register the new account here.
                        return true;

                    }else if((mLogin.equals(usuar.getLogin()))&&(usuar.getAtivo()==0)){

                        usuarioAtivo = false;

                        return false;

                    }else{

                        return false;
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                mLoginView.setText("");
                mPasswordView.setText("");

                Intent it  = new Intent(getBaseContext(), Inicio.class);

			    startActivity(it);

                finish();
            }else if(usuarioAtivo==false){

                mPasswordView.setError("Usuário inativo!");
                mPasswordView.requestFocus();

                usuarioAtivo=true;

            }else {
                mPasswordView.setError(getString(R.string.error_incorrect_dados));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    private ArrayList<Usuario> tratarJson (String data){

        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();

        try {
            JSONArray jArray = new JSONArray(data);

            for(int i=0; i<jArray.length();i++){

                Usuario usuario = new Usuario();

                JSONObject jo = jArray.getJSONObject(i);
                usuario.setIdColaborador(jo.getInt("colaborador_id"));
                usuario.setLogin(jo.getString("login"));
                usuario.setAtivo(jo.getInt("ativo_externo"));
                usuarios.add(usuario);

            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return usuarios;


    }


    // CAPTURANDO NÚMERO DE SÉRIE DO CELULAR:
    public String numCel(){

        String IMEI = "";

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();

        return IMEI;
    }

    public void onBackPressed()  {

        finish();

    }

    public abstract static class Mask {

        public static String unmask(String s) {
            return s.replaceAll("[.]", "").replaceAll("[-]", "")
                    .replaceAll("[/]", "").replaceAll("[(]", "")
                    .replaceAll("[)]", "");
        }

        public static TextWatcher insert(final String mask, final EditText ediTxt) {
            return new TextWatcher() {
                boolean isUpdating;
                String old = "";

                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    String str = Mask.unmask(s.toString());
                    String mascara = "";
                    if (isUpdating) {
                        old = str;
                        isUpdating = false;
                        return;
                    }
                    int i = 0;
                    for (char m : mask.toCharArray()) {
                        if (m != '#' && str.length() > old.length()) {
                            mascara += m;
                            continue;
                        }
                        try {
                            mascara += str.charAt(i);
                        } catch (Exception e) {
                            break;
                        }
                        i++;
                    }
                    isUpdating = true;
                    ediTxt.setText(mascara);
                    ediTxt.setSelection(mascara.length());
                }

                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                public void afterTextChanged(Editable s) {
                }
            };
        }

    }


}

