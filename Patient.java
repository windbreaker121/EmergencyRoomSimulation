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

    void markTreated() {
        treated = true;
        outcome.release();
    }

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

                outcome.acquire();       // wait for the doctor's permit
                if (treated) {
                    break;
                }
            }

            Log.log(name + " left.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
