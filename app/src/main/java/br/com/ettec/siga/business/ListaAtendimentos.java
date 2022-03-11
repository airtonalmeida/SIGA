package br.com.ettec.siga.business;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ListView;

import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Date;


import br.com.ettec.siga.R;
import br.com.ettec.siga.domain.Atendimento;
import br.com.ettec.siga.persistence.AtendimentoDAO;

import br.com.ettec.siga.util.DetectaStatusConexao;


public class ListaAtendimentos extends Activity {

    private SQLiteDatabase db = null;

    DetectaStatusConexao dsc;

    private EnviarAtendimentoAsyncTask enviarAtendimentoAsyncTask = null;

    private InformarAtendimentoAsyncTask informarAtendimentoAsyncTask = null;

    private IniciarAtendimentoAsyncTask iniciarAtendimentoAsyncTask = null;

    private int idColaborador;

    private static final String PREF_NANME = "LoginPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_atendimentos_activity);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

       SharedPreferences sp1 = getSharedPreferences(PREF_NANME, MODE_PRIVATE );

        dsc = new DetectaStatusConexao(this);

       idColaborador = sp1.getInt("idCol",0);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_atendimentos, menu);
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

    @Override
    protected  void  onStart(){
        super.onStart();

        /* Se houver conexão, o sistema enviará os atendimentos que estão nas tabelas
         "atendimentosinformartemp" e "atendimentostemp"*/
        if (dsc.existeConexao()) {

            /*método que envia os atendimentos inseridos no banco de dados do dispositivo,
             que foram iniciados, mas não finalizados.*/
            iniciarAtendimentoAsyncTask = new IniciarAtendimentoAsyncTask();
            iniciarAtendimentoAsyncTask.execute((Void) null);

            /*método que envia os atendimentos inseridos no banco de dados do dispositivo,
             mas que ainda não foram iniciados, nem finalizados.*/
            informarAtendimentoAsyncTask = new InformarAtendimentoAsyncTask();
            informarAtendimentoAsyncTask.execute((Void) null);

            /*método que envia os atendimentos inseridos no banco de dados do dispositivo,
             mas que foram iniciados e finalizados.*/
            enviarAtendimentoAsyncTask = new EnviarAtendimentoAsyncTask();
            enviarAtendimentoAsyncTask.execute((Void) null);

        }

    }

    @Override
    protected  void onStop(){
        super.onStop();

        finish();

    }

    public class IniciarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentosiniciartemp", null);

            if(!cursor.equals(null)){

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


    public class InformarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentosinformartemp", null);

            if(!cursor.equals(null)){

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


    public class EnviarAtendimentoAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return null;
            }

            db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

            Cursor cursor = db.rawQuery("SELECT * FROM atendimentostemp", null);

            if(!cursor.equals(null)){

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


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onResume(){
        super.onResume();


        ListView lista = (ListView)findViewById(R.id.listView2);

        db = openOrCreateDatabase("siga.db", Context.MODE_PRIVATE, null);

        if (dsc.existeConexao()) {

            ObterAtendimentosRemotoAsyncTask atendimentosRemotosAsyncTask = new ObterAtendimentosRemotoAsyncTask();

            ArrayList<Atendimento> retorno = atendimentosRemotosAsyncTask.doInBackground();

            AtendimentoDAO atendimentoDAO = new AtendimentoDAO(this);

            /**
             * Deleta a tabela atendimentos e recria.             *
             */
            atendimentoDAO.onUpgrade(db, 1, 2);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (int i = 0; i < retorno.size(); i++) {

                Atendimento atendimento = new Atendimento();

                atendimento.setCod(retorno.get(i).getCod());
                atendimento.setCliente(retorno.get(i).getCliente());

                try {
                    Date date = df.parse(retorno.get(i).getDataPrevista());

                    String dataFormatada = dateFormat.format(date);

                    atendimento.setDataPrevista(dataFormatada);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                atendimento.setTelefone(retorno.get(i).getTelefone());
                atendimento.setCelular(retorno.get(i).getCelular());
                atendimento.setEndereco(retorno.get(i).getEndereco());
                atendimento.setNumero(retorno.get(i).getNumero());
                atendimento.setComplemento(retorno.get(i).getComplemento());
                atendimento.setPontoReferencia(retorno.get(i).getPontoReferencia());
                atendimento.setBairro(retorno.get(i).getBairro());
                atendimento.setCidade(retorno.get(i).getCidade());
                atendimento.setObservacao(retorno.get(i).getObservacao());
                atendimento.setObscolaborador(retorno.get(i).getObscolaborador());
                atendimento.setAtivo(retorno.get(i).getAtivo());
                atendimento.setIniciado(retorno.get(i).getIniciado());
                atendimento.setContato(retorno.get(i).getContato());

                int inic =  atendimento.getIniciado();

                if(inic == 0){

                    atendimento.setIniciadoString("Não iniciado");

                }else if(inic == 1){

                    atendimento.setIniciadoString("Iniciado");

                }

                atendimentoDAO.inserir(db, atendimento);

            }

        }

        Cursor cursor = db.rawQuery("SELECT * FROM atendimentos WHERE ativo = 1", null);

        String [] from = {"cliente", "dataPrevista", "iniciadoString"};

        int [] to = {R.id.cliente, R.id.data, R.id.status};

        android.widget.SimpleCursorAdapter ad = new android.widget.SimpleCursorAdapter(getBaseContext(),
                R.layout.listar_atendimentos_model, cursor, from, to, 2);

        lista.setAdapter(ad);



        // obtem o total de registros dos atendimentos não finalizados no banco de dados do celular
        int total = cursor.getCount();

        TextView resultado =  (TextView) findViewById(R.id.textViewValorTotal);

        resultado.setText("Total de atendimentos em aberto: " + total);



        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(@SuppressWarnings("rawtypes") AdapterView adapter, View view, int position, long id){

                SQLiteCursor c = (SQLiteCursor) adapter.getAdapter().getItem(position);

                Intent it = new Intent(getBaseContext(), FinalizarAtendimento.class);

                it.putExtra("id", c.getInt(0) );

                startActivity(it);

                finish();


            }
        });



    }

    /**
     *
     */

    public class ObterAtendimentosRemotoAsyncTask extends AsyncTask<Void, Void, ArrayList<Atendimento>> {

        @Override
        protected ArrayList<Atendimento> doInBackground(Void... params) {


            try {
                // Simulate network access.
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                return null;
            }

            StringBuilder strURL = new StringBuilder();

            ArrayList<Atendimento> retorno= new ArrayList<Atendimento>();


            try {

               strURL.append("http://ettec.com.br/obteratendimentossiganovo.php?id=");
               strURL.append(URLEncoder.encode(Integer.toString(idColaborador), "UTF-8"));

                URL url = new URL(strURL.toString());
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                InputStreamReader ips = new InputStreamReader(http.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(ips);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null){

                    sb.append(line + "\n");
                    retorno = tratarJson(sb.toString());

                }

                bufferedReader.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return retorno;
        }
    }



    private ArrayList<Atendimento> tratarJson (String data){

        ArrayList<Atendimento> atendimentos = new ArrayList<Atendimento>();

        try {
            JSONArray jArray = new JSONArray(data);

            for(int i=0; i<jArray.length();i++){

                Atendimento atendi = new Atendimento();

                JSONObject jo = jArray.getJSONObject(i);
                atendi.setCod(jo.getInt("id"));
                atendi.setCliente(jo.getString("nomeOUrazao"));
                atendi.setDataPrevista(jo.getString("dataPrevista"));
                atendi.setTelefone(jo.getString("telefone"));
                atendi.setCelular(jo.getString("celular"));
                atendi.setEndereco(jo.getString("logradouro"));
                atendi.setNumero(jo.getString("numero"));
                atendi.setComplemento(jo.getString("complemento"));
                atendi.setPontoReferencia(jo.getString("pontoReferencia"));
                atendi.setBairro(jo.getString("bairro"));
                atendi.setCidade(jo.getString("municipio"));
                atendi.setObscolaborador(jo.getString("obsDoColaborador"));
                atendi.setObservacao(jo.getString("observacao"));
                atendi.setAtivo(jo.getInt("ativo"));
                atendi.setIniciado(jo.getInt("iniciado"));
                atendi.setContato(jo.getString("contato"));

                atendimentos.add(atendi);

            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return atendimentos;


    }



    public void onBackPressed()  {

        Intent it  = new Intent(getBaseContext(), Inicio.class);

        startActivity(it);

        finish();


    }




}
