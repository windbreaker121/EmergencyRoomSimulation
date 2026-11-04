public class SharedTreatRoom {

    private static final long ACQUISITION_TIMEOUT_MS = 3000;

    private final Semaphore roomsAvailable;
    private final TreatmentRoom[] rooms;
    private final Object roomLock = new Object();

    public SharedTreatRoom(int roomCount) {

        if (roomCount <= 0) {
            throw new IllegalArgumentException(
                    "Room count must be greater than zero."
            );
        }

        roomsAvailable = new Semaphore(roomCount);
        rooms = new TreatmentRoom[roomCount];

        for (int i = 0; i < roomCount; i++) {
            rooms[i] = new TreatmentRoom("Room " + (i + 1));
        }
    }

    public TreatmentRoom requestRoom(String doctorName)
            throws InterruptedException {

        boolean acquired = roomsAvailable.tryAcquire(
                ACQUISITION_TIMEOUT_MS
        );

        if (!acquired) {
            return null;
        }

        synchronized (roomLock) {

            for (TreatmentRoom room : rooms) {

                if (room.isAvailable()) {

                    room.setAvailable(false);

                    Log.log(
                            doctorName + " acquired " + room.getName() + "."
                    );

                    return room;
                }
            }

            // Defensive recovery: a permit was acquired,
            // but no available room was found.
            roomsAvailable.release();

            throw new IllegalStateException(
                    "Semaphore and treatment room states are inconsistent."
            );
        }
    }

    public void returnRoom(TreatmentRoom room) {

        if (room == null) {
            throw new IllegalArgumentException(
                    "Cannot return a null treatment room."
            );
        }

        synchronized (roomLock) {

            boolean belongsToManager = false;

            for (TreatmentRoom availableRoom : rooms) {
                if (availableRoom == room) {
                    belongsToManager = true;
                    break;
                }
            }

            if (!belongsToManager) {
                throw new IllegalArgumentException(
                        "This room does not belong to this hospital."
                );
            }

            if (room.isAvailable()) {
                throw new IllegalStateException(
                        "Treatment room has already been released."
                );
            }

            room.setAvailable(true);

            roomsAvailable.release();

            Log.log(room.getName() + " is now available.");
        }
    }
}