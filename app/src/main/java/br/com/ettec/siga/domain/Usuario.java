package br.com.ettec.siga.domain;

/**
 * Created by Tom on 26/10/2015.
 */
public class Usuario {

    private int idColaborador;

    private String login;

    private int ativo;


    public int getIdColaborador() {
        return idColaborador;
    }

    public void setIdColaborador(int idColaborador) {
        this.idColaborador = idColaborador;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getAtivo() {
        return ativo;
    }

    public void setAtivo(int ativo) {
        this.ativo = ativo;
    }




}
