import java.util.concurrent.ThreadLocalRandom;

public class Doctor implements Runnable {

    private final String name;
    private final SharedWaitingRoom waitingRoom;
    private final SharedTreatRoom treatmentRooms;

    public Doctor(
            String name,
            SharedWaitingRoom waitingRoom,
            SharedTreatRoom treatmentRooms) {

        this.name = name;
        this.waitingRoom = waitingRoom;
        this.treatmentRooms = treatmentRooms;
    }

    @Override
    public void run() {

        try {

            while (true) {

                // Wait for and retrieve a patient.
                Patient patient = waitingRoom.takePatient();

                // Null means the hospital is shutting down.
                if (patient == null) {
                    Log.log(name + " is shutting down.");
                    break;
                }

                Log.log(name + " is assigned to "
                        + patient.getName() + ".");

                Log.log(name + " is trying to acquire a treatment room.");

                TreatmentRoom room;

                try {
                    room = treatmentRooms.requestRoom(name);

                } catch (InterruptedException e) {

                    // The patient has not been treated.
                    waitingRoom.returnPatient(patient);

                    throw e;
                }

                // No treatment room acquired within 3 seconds.
                if (room == null) {

                    Log.log(name + " could not acquire a treatment room.");

                    Log.log(name + " is sending "
                            + patient.getName()
                            + " back to the waiting room.");

                    waitingRoom.returnPatient(patient);

                    continue;
                }

                boolean treatmentCompleted = false;

                try {

                    Log.log(name + " begins treating "
                            + patient.getName()
                            + " in " + room.getName() + ".");

                    // Random treatment duration: 2–5 seconds
                    long treatmentTime =
                            ThreadLocalRandom.current()
                                    .nextLong(2000, 5001);

                    Thread.sleep(treatmentTime);

                    Log.log(name + " finished treating "
                            + patient.getName() + ".");

                    treatmentCompleted = true;

                } finally {

                    // Always release the treatment room
                    treatmentRooms.returnRoom(room);
                }

                if (treatmentCompleted) {

                    // Allow the patient thread to leave.
                    patient.markTreated();

                    Log.log(name + " is now available.");
                }

            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            Log.log(name + " was interrupted.");
        }
    }
}