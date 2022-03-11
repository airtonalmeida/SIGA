package br.com.ettec.siga.domain;

import org.json.JSONException;
import org.json.JSONStringer;

/**
 * Created by Tom on 27/08/2015.
 */
public class Atendimento {



    private int id;//primary key da tabela do sqlite
    private int cod;
    private String cliente;
    private String dataPrevista;
    private String telefone;
    private String celular;
    private String endereco;
    private String numero;
    private String complemento;
    private String pontoReferencia;
    private String bairro;
    private String cidade;
    private String obscolaborador; //observaçao do colaborador
    private String observacao;
    private String latitude;
    private String longitude;
    private Integer ativo;
    private String dataAtendimento;
    private Integer iniciado;
    private String contato;
    private String iniciadoString;



    public Atendimento() {
    }


    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(String dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getPontoReferencia() {
        return pontoReferencia;
    }

    public void setPontoReferencia(String pontoReferencia) {
        this.pontoReferencia = pontoReferencia;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getAtivo() {
        return ativo;
    }

    public void setAtivo(Integer ativo) {
        this.ativo = ativo;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getDataAtendimento() {
        return dataAtendimento;
    }

    public void setDataAtendimento(String dataAtendimento) {
        this.dataAtendimento = dataAtendimento;
    }

    public String getObscolaborador() {
        return obscolaborador;
    }

    public void setObscolaborador(String obscolaborador) {
        this.obscolaborador = obscolaborador;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Integer getIniciado() {
        return iniciado;
    }

    public void setIniciado(Integer iniciado) {
        this.iniciado = iniciado;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getIniciadoString() {
        return iniciadoString;
    }

    public void setIniciadoString(String iniciadoString) {
        this.iniciadoString = iniciadoString;
    }

    @Override
    public String toString() {
        return "Cliente= " + cliente +
               ", Data= " + dataPrevista;
    }




}
