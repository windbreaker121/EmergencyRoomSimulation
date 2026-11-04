class Semaphore {
    private int permits;

    Semaphore(int permits) {
        this.permits = permits;
    }

    synchronized void acquire() throws InterruptedException {
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    synchronized void release() {
        permits++;
        notifyAll();
    }

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
}