package br.com.ettec.siga.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.ettec.siga.domain.Atendimento;

/**
 * Created by Tom on 27/08/2015.
 */
public class AtendimentoInformarTempDAO extends SQLiteOpenHelper {

    private static final String TABELA = "atendimentosinformartemp";

    private static final int VERSION = 1;

    public AtendimentoInformarTempDAO(Context context) {
        super(context, TABELA, null, VERSION);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA +
                "( _id INTEGER PRIMARY KEY," +
                " cod INTEGER," +
                " obscolaborador VARCHAR(250)," +
                " latitude VARCHAR(20)," +
                " longitude VARCHAR(20)," +
                " dataAtendimento VARCHAR(20)" +
                ");";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + AtendimentoInformarTempDAO.TABELA);
        this.onCreate(db);

    }

    public void inserir(SQLiteDatabase db, Atendimento atendimento){

        ContentValues valores = new ContentValues();
        valores.put("cod", atendimento.getCod());
        valores.put("obscolaborador", atendimento.getObscolaborador());
        valores.put("latitude", atendimento.getLatitude());
        valores.put("longitude", atendimento.getLongitude());
        valores.put("dataAtendimento", atendimento.getDataAtendimento());

        db.insert(TABELA, "_id", valores);
    }



}
