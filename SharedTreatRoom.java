public class SharedTreatRoom {

    private static final long ACQUISITION_TIMEOUT_MS = 3000;

    private final Semaphore roomsAvailable;
    private final TreatmentRoom[] rooms;
    private final Object roomLock = new Object();

    public SharedTreatRoom(int roomCount) {
        if (roomCount <= 0) {
            throw new IllegalArgumentException("Room count must be greater than zero.");
        }
        roomsAvailable = new Semaphore(roomCount);
        rooms = new TreatmentRoom[roomCount];
        for (int i = 0; i < roomCount; i++) {
            rooms[i] = new TreatmentRoom("Room " + (i + 1));
        }
    }

    // try to get a room within 3 seconds. Returns null on timeout
    public TreatmentRoom requestRoom(String doctorName) throws InterruptedException {
        if (!roomsAvailable.tryAcquire(ACQUISITION_TIMEOUT_MS)) {
            return null;
        }

        synchronized (roomLock) {
            for (TreatmentRoom room : rooms) {
                if (room.isAvailable()) {
                    room.setAvailable(false);
                    Log.log(doctorName + " acquired a treatment room (" + room.getName() + ").");
                    return room;
                }
            }
            //a permit was granted but no room is free
            roomsAvailable.release();
            throw new IllegalStateException("Semaphore and treatment room states are inconsistent.");
        }
    }

    public void returnRoom(String doctorName, TreatmentRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("Cannot return a null treatment room.");
        }
        synchronized (roomLock) {
            boolean belongsToManager = false;
            for (TreatmentRoom r : rooms) {
                if (r == room) {
                    belongsToManager = true;
                    break;
                }
            }
            if (!belongsToManager) {
                throw new IllegalArgumentException("This room does not belong to this hospital.");
            }
            if (room.isAvailable()) {
                throw new IllegalStateException("Treatment room has already been released.");
            }
            room.setAvailable(true);
            Log.log(doctorName + " released the treatment room (" + room.getName() + ").");
        }
        // release the permit after the room is marked free
        roomsAvailable.release();
    }
}
