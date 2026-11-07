import java.util.LinkedList;
import java.util.Queue;

class SharedWaitingRoom {
    private final Queue<Patient> queue = new LinkedList<>();

    private final Semaphore seats;   // free seats       (init = capacity) 
    private final Semaphore items;   // waiting patients (init = 0)
    private final Semaphore mutex;   // protects queue   (init = 1)

    SharedWaitingRoom(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }
        this.seats = new Semaphore(capacity);
        this.items = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

    boolean tryEnter(Patient p) throws InterruptedException {
        if (!seats.tryAcquire()) {
            return false;
        }
        addToQueue(p);
        return true;
    }

    // waits outside until a seat is free
    void enter(Patient p) throws InterruptedException {
        seats.acquire();
        addToQueue(p);
    }

    private void addToQueue(Patient p) throws InterruptedException {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            seats.release(); // give the seat back
            throw e;
        }
        queue.add(p);
        Log.log(p.getName() + " entered the waiting room.");
        mutex.release();
        items.release();
    }

    //returns null when the hospital is closing
    Patient takePatient() throws InterruptedException {
        items.acquire();
        mutex.acquire();
        Patient p = queue.poll();
        mutex.release();
        if (p != null) {
            seats.release();
        }
        return p;
    }

    void close(int doctorCount) throws InterruptedException {
        for (int i = 0; i < doctorCount; i++) {
            mutex.acquire();
            queue.add(null);
            mutex.release();
            items.release();
        }
    }
}
