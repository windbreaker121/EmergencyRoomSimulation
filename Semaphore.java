class Semaphore {
    private int permits;

    Semaphore(int permits) {
        this.permits = permits;
    }

    // Blocks until a permit is available
    synchronized void acquire() throws InterruptedException {
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    synchronized boolean tryAcquire() {
        if (permits == 0) {
            return false;
        }
        permits--;
        return true;
    }

    // returns false if no permit became available within timeoutMs
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
