package br.com.ettec.siga.domain;

/**
 * Created by Tom on 22/08/2015.
 */
public class Coordenadas {

    private int id;
    private String latitude;
    private String longitude;
    private String imei;
    private String data;



    public Coordenadas() {
    }



    public Coordenadas(int id, String latitude, String longitude, String imei, String data) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imei = imei;
        this.data = data;

    }

    public void setId(int id) {

        this.id = id;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getId() {

        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getImei() {
        return imei;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Coordenadas{" +
                "id=" + id +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", imei='" + imei + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
