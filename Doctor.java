import java.util.concurrent.ThreadLocalRandom;

public class Doctor implements Runnable {

    private final String name;
    private final SharedWaitingRoom waitingRoom;
    private final SharedTreatRoom treatmentRooms;

    public Doctor(String name, SharedWaitingRoom waitingRoom, SharedTreatRoom treatmentRooms) {
        this.name = name;
        this.waitingRoom = waitingRoom;
        this.treatmentRooms = treatmentRooms;
    }

    @Override
    public void run() {
        try {
            while (true) {

                Patient patient = waitingRoom.takePatient(); //from SharedWaitingRoom, blocks if empty

                // null = hospital is closing
                if (patient == null) {
                    Log.log(name + " is shutting down.");
                    break;
                }

                Log.log(name + " is assigned to " + patient.getName() + ".");
                Log.log(name + " is trying to acquire a treatment room.");

                TreatmentRoom room = treatmentRooms.requestRoom(name); // from SharedTreatRooom, waits up to 3s

                if (room == null) {
                    Log.log(name + " could not acquire a treatment room.\n"
                            + name + " is sending " + patient.getName()
                            + " back to the waiting room.");
                    patient.sendBack();           // from Patient, sets treated = false 
                    Log.log(name + " is now available.");
                    continue;
                }

                try {
                    Log.log(name + " begins treating " + patient.getName()
                            + " in " + room.getName() + ".");
                    Thread.sleep(ThreadLocalRandom.current().nextLong(2000, 5001));
                    Log.log(name + " finished treating " + patient.getName() + ".");
                } finally {
                    treatmentRooms.returnRoom(name, room); // always release
                }

                patient.markTreated(); // from Patient, sets treated = true
                Log.log(name + " is now available.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.log(name + " was interrupted.");
        }
    }
}
