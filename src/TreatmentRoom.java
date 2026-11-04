public class TreatmentRoom {

    private String name;
    private boolean available;

    public TreatmentRoom(String name) {
        this.name = name;
        this.available = true;
    }

    public String getName() {
        return name;
    }

    public boolean isAvaliable() {
        return available;
    }

    void setAvaliable(boolean avalibale) {
        this.available = avalibale;
    }
}
