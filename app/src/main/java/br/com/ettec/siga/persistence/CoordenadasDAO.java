package br.com.ettec.siga.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import br.com.ettec.siga.domain.Coordenadas;

/**
 * Created by Tom on 22/08/2015.
 */
public class CoordenadasDAO extends SQLiteOpenHelper {

    private static final String TABELA = "coordenadas";
    String [] COLUNAS = {"latitude", "longitude", "imei", "data"};
    private static final int VERSION = 1;

    public CoordenadasDAO(Context context) {
        super(context, TABELA, null, VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA +
           "( _id INTEGER PRIMARY KEY," +
           " latitude VARCHAR(20)," +
           " longitude VARCHAR(20)," +
           " imei VARCHAR(20)," +
           " data VARCHAR(20)" +
           ");";

        db.execSQL(sql);

    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + CoordenadasDAO.TABELA);
        this.onCreate(db);

    }




    public void inserir(SQLiteDatabase db,Coordenadas coordenadas){

        ContentValues valores = new ContentValues();
        valores.put("latitude", coordenadas.getLatitude());
        valores.put("longitude", coordenadas.getLongitude());
        valores.put("imei", coordenadas.getImei());
        valores.put("data", coordenadas.getData());
        db.insert(TABELA, "_id", valores);
    }


    public List<Coordenadas> getLista(SQLiteDatabase db){
        Cursor c = db.rawQuery("SELECT _id, latitude, longitude, data FROM coordenadas", null);
        List<Coordenadas> lista = new ArrayList<>();
        while(c.moveToNext()){
            Coordenadas coordenadas = new Coordenadas();
            coordenadas.setId(c.getInt(0));
            coordenadas.setLatitude(c.getString(1));
            coordenadas.setLongitude(c.getString(2));
            //coordenadas.setImei(c.getString(3));
            coordenadas.setData(c.getString(3));

            lista .add(coordenadas);
        }
        c.close();

        return lista;

    }

    /* public void inserir(Coordenadas coordenadas){

        ContentValues valores = new ContentValues();
        valores.put("latitude", coordenadas.getLatitude());
        valores.put("longitude", coordenadas.getLongitude());
        valores.put("imei", coordenadas.getImei());
        valores.put("data", coordenadas.getData());

        getWritableDatabase().insert(TABELA, "_id", valores);
    }*/

    /*
    public List<Coordenadas> getLista(){
        Cursor c = getWritableDatabase().query(TABELA, COLUNAS , null, null, null, null, null);
        List<Coordenadas> lista = new ArrayList<>();
        while(c.moveToNext()){
            Coordenadas coordenadas = new Coordenadas();
            coordenadas.setId(c.getInt(0));
            coordenadas.setLatitude(c.getString(1));
            coordenadas.setLongitude(c.getString(2));
            coordenadas.setImei(c.getString(3));
            coordenadas.setData(c.getString(4));

            lista .add(coordenadas);
        }
        c.close();

        return lista;

    }
*/

}
