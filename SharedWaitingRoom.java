// Owner: Person 3
import java.util.LinkedList;
import java.util.Queue;

// Bounded buffer: patients are producers, doctors are consumers.
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

    // Non-blocking attempt. Returns false (and changes nothing) if the room is full.
    boolean tryEnter(Patient p) throws InterruptedException {
        if (!seats.tryAcquire()) {
            return false;
        }
        addToQueue(p);
        return true;
    }

    // Blocking: waits outside until a seat is free.
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

    // Doctors block here while idle. Returns null when the hospital is closing.
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

    // Wakes every idle doctor with a null "poison pill" so they can shut down.
    void close(int doctorCount) throws InterruptedException {
        for (int i = 0; i < doctorCount; i++) {
            mutex.acquire();
            queue.add(null);
            mutex.release();
            items.release();
        }
    }
}
