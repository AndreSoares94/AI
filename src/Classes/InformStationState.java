package Classes;

public class InformStationState implements java.io.Serializable{
    String name;
    String coords;
    int nBikes;
    int capacity;
    int acceptedDisc;
    int deniedDisc;
    int totalUsers;

    public InformStationState() {
        this.name = "";
        this.coords = "";
        this.nBikes = 0;
        this.capacity = 0;
        this.acceptedDisc = 0;
        this.deniedDisc = 0;
        this.totalUsers = 0;
    }

    public InformStationState(String nome, String c, int nB, int cap, int tu, int d1, int d2) {
        this.name = nome;
        this.coords = c;
        this.nBikes = nB;
        this.capacity = cap;
        this.totalUsers = tu;
        this.acceptedDisc = d1;
        this.deniedDisc = d2;
    }

    public InformStationState(InformStationState e) {
        this.name = e.getName();
        this.coords = e.getCoords();
        this.nBikes = e.getnBikes();
        this.capacity = e.getCap();
        this.totalUsers = e.getTotalUsers();
        this.acceptedDisc = e.getAcceptedDisc();
        this.deniedDisc = e.getDeniedDisc();
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getName() {
        return this.name;
    }

    public String getCoords() {
        return this.coords;
    }

    public int getnBikes() {
        return this.nBikes;
    }

    public int getCap() {
        return this.capacity;
    }

    public int getAcceptedDisc() {
        return this.acceptedDisc;
    }

    public int getDeniedDisc() {
        return this.deniedDisc;
    }

    public InformStationState clone() {
        return new InformStationState(this);
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setCoords(String coord) {
        this.coords = coord;
    }

    public void setnBikes(int n) {
        this.nBikes = n;
    }

    public void setCap(int c) {
        this.capacity = c;
    }

    public void setAcceptedDisc(int c) {
        this.acceptedDisc = c;
    }

    public void setDeniedDisc(int c) {
        this.deniedDisc = c;
    }
}