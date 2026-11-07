// Owner: Person 2
// Counting semaphore built with monitors (synchronized / wait / notifyAll).
class Semaphore {
    private int permits;

    Semaphore(int permits) {
        this.permits = permits;
    }

    // Blocks until a permit is available.
    synchronized void acquire() throws InterruptedException {
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    // Non-blocking: takes a permit only if one is available right now.
    synchronized boolean tryAcquire() {
        if (permits == 0) {
            return false;
        }
        permits--;
        return true;
    }

    // Timed acquire: returns false if no permit became available within timeoutMs.
    synchronized boolean tryAcquire(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (permits == 0) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return false;
            }
            wait(remaining);
        }
        permits--;
        return true;
    }

    synchronized void release() {
        permits++;
        notifyAll();
    }
}
