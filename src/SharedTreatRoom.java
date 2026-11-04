public class SharedTreatRoom {

    private final Semaphore roomsAvailable;
    private final TreatmentRoom[] rooms;

    public SharedTreatRoom(int roomCount);

    public TreatmentRoom requestRoom(String doctorName)
            throws InterruptedException;

    public void returnRoom(TreatmentRoom room);
}