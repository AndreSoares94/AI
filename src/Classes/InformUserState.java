package Classes;

public class InformUserState implements java.io.Serializable{
    private Position actual;
    private String initStation, finalStation;

    public InformUserState(Position actual, String initStation, String finalStation){
        super();
        this.initStation = initStation;
        this.actual = actual;
        this.finalStation = finalStation;
    }

    public Position getActual() {
        return actual;
    }

    public void setActual(Position actual) {
        this.actual = actual;
    }

    public String getInitStation() {
        return initStation;
    }

    public void setInitStation(String initStation) {
        this.initStation = initStation;
    }

    public String getFinalStation() {
        return finalStation;
    }

    public void setFinalStation(String finalStation) {
        this.finalStation = finalStation;
    }
}
