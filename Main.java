import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Scanner;

class Log {
    static synchronized void log(String s) {
        System.out.println(s);
    }
}

class Process {
    enum State {
        NEW, READY, RUNNING, TERMINATED
    }

    int pid;
    String name;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int startTime = -1;
    int finishTime = 0;
    int waitingTime = 0;
    int turnaroundTime = 0;
    State state = State.NEW;

    Process(int pid, String name, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}

class ReadyQueue {
    private final PriorityQueue<Process> queue;

    ReadyQueue() {
        // Shortest remainingTime first; tie-break by arrivalTime, then pid
        this.queue = new PriorityQueue<>(
                Comparator.comparingInt((Process p) -> p.remainingTime)
                        .thenComparingInt(p -> p.arrivalTime)
                        .thenComparingInt(p -> p.pid));
    }

    void add(Process p) {
        p.state = Process.State.READY;
        queue.offer(p);
    }

    Process poll() {
        return queue.poll();
    }

    Process peek() {
        return queue.peek();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }
}

class RunningQueue {
    private Process current;

    boolean isEmpty() {
        return current == null;
    }

    Process peek() {
        return current;
    }

    void dispatch(Process p) {
        if (current != null) {
            throw new IllegalStateException("Process " + current.name + "is already running");
        }
        current = p;
        current.state = Process.State.RUNNING;
    }

    Process preempt() {
        Process previous = current;
        if (previous != null) {
            previous.state = Process.State.READY;
            current = null;
        }
        return previous;
    }
    
    Process finish() {
        Process finished = current;
        if (finished != null) {
            finished.state = Process.State.TERMINATED;
            current = null;
        }
        return finished;
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Log.log("Number of processes: ");
        int processCount = scanner.nextInt();

        List<process> processes = new ArrayList<>();
        for (int i = 0; i < processCount; i++) {
            String name = scanner.next();
            int arrivalTime = scanner.nextInt();
            int burstTime = scanner.nextInt();
            processes.add(new process(i + 1, name, arrivalTime, burstTime, 0));
        }

        ReadyQueue readyQueue = new ReadyQueue();
        RunningQueue runningQueue = new RunningQueue();
        process running = null;
        int currentTime = 0;
        int completed = 0;

        while (completed < processCount) {
            
            for (process p : processes) {
                if (p.state == Process.State.New && p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                }
            }

            // preempting
            Process currentlyRunning = runningQueue.peek();
            Process shortest = readyQueue.peak();
            if (currentlyRunning != null && shortest != null) {
                if (currentlyRunning.remainingTime > shortest.remainingTime) {
                    Process preempted = running.preempt();
                    readyQueue.add(preempted);
                }

            }
            else if (currentlyRunning == null && shortest != null) {
                runningQueue.dispatch(readyQueue.poll());
            }

            // execute
            currentlyRunning = runningQueue.peak();
            if (currentlyRunning != null) {
                if (currentlyRunning.startTime == -1) {
                    currentlyRunning.startTime = currentTime;
                }
                currentTime++;
                currentlyRunning.remainingTime--;

                if (currentlyRunning.remainingTime == 0) {
                    Process finished = runningQueue.finish();
                    finished.finishTime = currentTime;
                    finished.turnaroundTime = finished.finishTime - finished.arrivalTime;
                    finished.waitingTime = finished.turnaroundTime - finished.burstTime;
                    completed++;
                }
                else {
                    current++;
                }
            }
            
        }
        

        Log.log("Process\tArrival\tBurst\tFinish\tWaiting\tTurnaround");
        for (Process p : processes) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d%n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.finishTime, p.waitingTime, p.turnaroundTime);
        }

        scanner.close();
    }
}
