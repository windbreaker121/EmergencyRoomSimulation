import java.util.concurrent.CountDownLatch;

class Patient implements Runnable {
    private final String name;
    private final SharedWaitingRoom waitingRoom;
    private final CountDownLatch done = new CountDownLatch(1);

    Patient(String name, SharedWaitingRoom waitingRoom) {
        this.name = name;
        this.waitingRoom = waitingRoom;
    }

    String getName() {
        return name;
    }

    void markTreated() {
        done.countDown();
    }

    @Override
    public void run() {
        try {
            Log.log(name + " arrived.");

            if (waitingRoom.isFull()) {
                Log.log("The waiting room is full. " + name
                        + " is waiting outside for an available seat.");
            }

            waitingRoom.enter(this);
            Log.log(name + " entered the waiting room.");

            done.await();
            Log.log(name + " left.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}