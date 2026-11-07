// Owner: Person 3
// A patient never makes a doctor wait: if the doctor sends the patient back,
// the PATIENT thread waits outside for a seat on its own.
class Patient implements Runnable {
    private final String name;
    private final SharedWaitingRoom waitingRoom;
    private final Semaphore outcome = new Semaphore(0); // doctor signals: treated / sent back
    private volatile boolean treated = false;

    Patient(String name, SharedWaitingRoom waitingRoom) {
        this.name = name;
        this.waitingRoom = waitingRoom;
    }

    String getName() {
        return name;
    }

    // Called by the doctor after treatment is finished.
    void markTreated() {
        treated = true;
        outcome.release();
    }

    // Called by the doctor when no treatment room was acquired in time.
    void sendBack() {
        treated = false;
        outcome.release();
    }

    @Override
    public void run() {
        try {
            Log.log(name + " arrived.");

            while (true) {
                if (!waitingRoom.tryEnter(this)) {
                    Log.log("The waiting room is full.\n"
                            + name + " is waiting outside for an available seat.");
                    waitingRoom.enter(this);
                }

                outcome.acquire();       // wait for the doctor's verdict
                if (treated) {
                    break;
                }
                // Sent back: loop and try to re-enter the waiting room.
            }

            Log.log(name + " left.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
