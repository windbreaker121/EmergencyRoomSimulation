import java.util.LinkedList;
import java.util.Queue;

class SharedWaitingRoom {
    private final int capacity;
    private final Queue<Patient> queue = new LinkedList<>();

    private final Semaphore seats;   // free seats       (init = capacity)
    private final Semaphore items;   // waiting patients (init = 0)
    private final Semaphore mutex;   // protects queue   (init = 1)

    SharedWaitingRoom(int capacity) {
        this.capacity = capacity;
        this.seats = new Semaphore(capacity);
        this.items = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

    void enter(Patient p) throws InterruptedException {
        seats.acquire();
        mutex.acquire();
        queue.add(p);
        mutex.release();
        items.release();
    }

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

    void returnPatient(Patient p) throws InterruptedException {
        enter(p);
    }

    int size() {
        synchronized (queue) {
            return queue.size();
        }
    }

    boolean isFull() {
        return size() >= capacity;
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