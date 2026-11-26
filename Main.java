import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
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
    int responseTime = 0;
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
            throw new IllegalStateException("Process " + current.name + " is already running");
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

        Log.log("Context switching time: ");
        int contextSwitchTime = scanner.nextInt();

        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < processCount; i++) {
            String name = scanner.next();
            int arrivalTime = scanner.nextInt();
            int burstTime = scanner.nextInt();
            processes.add(new Process(i + 1, name, arrivalTime, burstTime));
        }

        ReadyQueue readyQueue = new ReadyQueue();
        RunningQueue runningQueue = new RunningQueue();

        int currentTime = 0;
        int completed = 0;
        int csRemaining = 0;
        Process lastDispatched = null;

        while (completed < processCount) {
            
            // admit processes
            for (Process p : processes) {
                if (p.state == Process.State.NEW && p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                }
            }

            // context switch
            if (csRemaining > 0) {
                csRemaining--;
                currentTime++;
                continue;
            }

            // 3. Preempt if a ready process has strictly less remaining time
            Process running = runningQueue.peek();
            Process shortestReady = readyQueue.peek();

            if (running != null && shortestReady != null
                    && shortestReady.remainingTime < running.remainingTime) {
                readyQueue.add(runningQueue.preempt());
                running = null;
            }

            // 4. Dispatch next process if CPU is free
            // if ct = 0: switching between processes takes no time
            // so next processes starts immediately
            if (running == null && shortestReady != null) {
                if (lastDispatched != null && lastDispatched != shortestReady && contextSwitchTime > 0) {
                    csRemaining = contextSwitchTime;
                    continue;
                }
                Process next = readyQueue.poll();
                runningQueue.dispatch(next);
                lastDispatched = next;
                running = next;
            }

            // 5. Execute 
            if (running != null) {
                if (running.startTime == -1) {
                    running.startTime = currentTime;
                }
                running.remainingTime--;
                currentTime++;

                if (running.remainingTime == 0) {
                    Process finished = runningQueue.finish();
                    finished.finishTime = currentTime;
                    finished.turnaroundTime = finished.finishTime - finished.arrivalTime;
                    finished.waitingTime = finished.turnaroundTime - finished.burstTime;
                    finished.responseTime = finished.startTime - finished.arrivalTime;
                    completed++;
                }
            } else {
                currentTime++;
            }
        }

        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int totalResponseTime = 0;

        Log.log("\nProcess\tArrival\tBurst\tFinish\tWaiting\tTurnaround\tResponse");
        for (Process p : processes) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t\t%d%n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.finishTime, p.waitingTime, p.turnaroundTime, p.responseTime);

            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
            totalResponseTime += p.responseTime;
        }

        System.out.printf("%nAverage Waiting Time: %.2f%n", (double) totalWaitingTime / processCount);
        System.out.printf("Average Turnaround Time: %.2f%n", (double) totalTurnaroundTime / processCount);
        System.out.printf("Average Response Time: %.2f%n", (double) totalResponseTime / processCount);

        scanner.close();
    }
}
