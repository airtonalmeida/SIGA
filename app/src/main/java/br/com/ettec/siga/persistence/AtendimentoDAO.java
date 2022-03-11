package br.com.ettec.siga.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.ettec.siga.domain.Atendimento;

/**
 * Created by Tom on 27/08/2015.
 */
public class AtendimentoDAO extends SQLiteOpenHelper {

    private static final String TABELA = "atendimentos";
    String [] COLUNAS = {"dataPrevista", "cliente"};
    private static final int VERSION = 1;

    public AtendimentoDAO(Context context) {
        super(context, TABELA, null, VERSION);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA +
                "( _id INTEGER PRIMARY KEY," +
                " cod INTEGER," +
                " cliente VARCHAR(100)," +
                " dataPrevista VARCHAR(20)," +
                " telefone VARCHAR(20)," +
                " celular VARCHAR(20)," +
                " endereco VARCHAR(100)," +
                " numero VARCHAR(10)," +
                " complemento VARCHAR(60)," +
                " pontoReferencia VARCHAR(100)," +
                " bairro VARCHAR(60)," +
                " cidade VARCHAR(60)," +
                " obscolaborador VARCHAR(250)," +
                " observacao VARCHAR(250)," +
                " latitude VARCHAR(20)," +
                " longitude VARCHAR(20)," +
                " ativo INTEGER," +
                " iniciado INTEGER," +
                " contato VARCHAR(100)," +
                " iniciadoString VARCHAR(20)" +
                ");";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + AtendimentoDAO.TABELA);
        this.onCreate(db);

    }

    public void inserir(SQLiteDatabase db, Atendimento atendimento){

        ContentValues valores = new ContentValues();
        valores.put("cod", atendimento.getCod());
        valores.put("cliente", atendimento.getCliente());
        valores.put("dataPrevista", atendimento.getDataPrevista());
        valores.put("telefone", atendimento.getTelefone());
        valores.put("celular", atendimento.getCelular());
        valores.put("endereco", atendimento.getEndereco());
        valores.put("numero", atendimento.getNumero());
        valores.put("complemento", atendimento.getComplemento());
        valores.put("pontoReferencia", atendimento.getPontoReferencia());
        valores.put("bairro", atendimento.getBairro());
        valores.put("cidade", atendimento.getCidade());
        valores.put("obscolaborador", atendimento.getObscolaborador());
        valores.put("observacao", atendimento.getObservacao());
        valores.put("ativo",atendimento.getAtivo());
        valores.put("iniciado",atendimento.getIniciado());
        valores.put("contato", atendimento.getContato());
        valores.put("iniciadoString", atendimento.getIniciadoString());
        db.insert(TABELA, "_id", valores);
    }




}
