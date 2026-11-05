import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class Log {
    static synchronized void log(String s) {
        System.out.println(s);
    }
}

class treatmentRoom {
    String name;
    boolean isAvailable = true;

    treatmentRoom(String name) {
        this.name = name;
    }
}

class TreatmentRooms {
    private Semaphore RoomsAvailable;
    int roomsCount;
    treatmentRoom[] rooms;

    TreatmentRooms(int p) {
        rooms = new treatmentRoom[p];
        this.roomsCount = p;
        RoomsAvailable = new Semaphore(p);

        for (int i = 0; i < p; i++) {
            rooms[i] = new treatmentRoom("Room Number: " + (i+1));
        }
    }

    public treatmentRoom requestRoom(String doctorName) throws InterruptedException {
        if (!RoomsAvailable.tryAcquire(3, TimeUnit.SECONDS)) {
            return null;
        } 
        return aquireRoom();
    }
    
    public synchronized void returnRoom(String doctorName, treatmentRoom room) {
        room.isAvailable = true; // return state of this room object to available
        RoomsAvailable.release(); // release semaphore permit
        Log.log(doctorName + " released " + room.name);
    }
    
    private synchronized treatmentRoom aquireRoom() { // so no two doctors try to aquire a room in the same time
        for (int i = 0; i < roomsCount; i++) {
            if (rooms[i].isAvailable) {
                rooms[i].isAvailable = false;
                return rooms[i];
            }
        }
        throw new IllegalStateException("aquired room but no rooms available");
    }
}

class WaitingRoom {
    private final int capacity;
    private final int totalPatients;
    private final int doctorCount;
    private final Queue<Patients> queue = new LinkedList<>();
    private final AtomicInteger treatedCount = new AtomicInteger(0);

    private final Semaphore seats; // free seats (init = capacity), also our "occupied seats" counter
    private final Semaphore items; // patients waiting to be taken (init = 0)
    private final Semaphore mutex; // protects queue (init = 1)

    WaitingRoom(int capacity, int totalPatients, int doctorCount) {
        this.capacity = capacity;
        this.totalPatients = totalPatients;
        this.doctorCount = doctorCount;
        this.seats = new Semaphore(capacity, true); // fair: patients outside get seats in arrival order
        this.items = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

    int occupiedSeats() {
        return capacity - seats.availablePermits();
    }

    boolean tryEnter(Patients p) throws InterruptedException {
        if (!seats.tryAcquire()) {
            return false;
        }
        addToQueue(p);
        return true;
    }

    void enter(Patients p) throws InterruptedException {
        seats.acquire();
        addToQueue(p);
    }

    private void addToQueue(Patients p) throws InterruptedException {
        mutex.acquire();
        queue.add(p);
        Log.log(p.getName() + " entered the waiting room.");
        mutex.release();
        items.release();
    }

    Patients takePatient() throws InterruptedException {
        items.acquire();
        mutex.acquire();
        Patients p = queue.poll();
        mutex.release();
        if (p != null) {
            seats.release(); // the seat is free again as soon as the doctor takes the patient
        }
        return p;
    }

    void patientTreated() throws InterruptedException {
        if (treatedCount.incrementAndGet() == totalPatients) {
            close();
        }
    }

    void close() throws InterruptedException {
        for (int i = 0; i < doctorCount; i++) {
            mutex.acquire();
            queue.add(null);
            mutex.release();
            items.release();
        }
    }
}

class Patients implements Runnable {
    private String name;
    private WaitingRoom waitingRoom;

    Patients(String name, WaitingRoom waitingRoom) {
        this.name = name;
        this.waitingRoom = waitingRoom;
    }

    String getName() {
        return name;
    }

    public void run() {
        try {
            Log.log(name + " arrived.");

            if (!waitingRoom.tryEnter(this)) {
                Log.log("The waiting room is full. " + name + " is waiting outside for an available seat.");
                waitingRoom.enter(this);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Doctors implements Runnable {
    private String doctorName;
    private WaitingRoom waitingRoom;
    private TreatmentRooms tRooms;

    Doctors(String doctorName, WaitingRoom wRoom, TreatmentRooms tRoom) {
        this.doctorName = doctorName;
        this.waitingRoom = wRoom;
        this.tRooms = tRoom;
    }

    public void run() {
        try{
            while (true) {
                Patients patient = waitingRoom.takePatient();
                if (patient == null) {
                    break;
                }

                treatmentRoom room;
                while ((room = tRooms.requestRoom(doctorName)) == null) {
                    Log.log(doctorName + " waiting for a free room for " + patient.getName());
                }

                try {
                    treating(doctorName, patient.getName(), room);
                } finally {
                    tRooms.returnRoom(doctorName, room);
                }
                Log.log(patient.getName() + " left.");
                waitingRoom.patientTreated();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void treating(String doctorName, String patientName, treatmentRoom room) {
        try {
            Log.log(doctorName + " aquired room " + room.name);
            Log.log(doctorName + " is treating " + patientName + " in " + room.name);
            Thread.sleep(2000); // sleep for 3 seconds
            Log.log(doctorName + " finished treating " + patientName);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted while sleeping");
        }
    }
}

public class Hospital {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Waiting room capacity: ");
        int waitingRoomCapacity = scanner.nextInt();

        System.out.println("Number of doctors: ");
        int doctorsCount = scanner.nextInt();

        System.out.println("Number of patients: ");
        int patientsCount = scanner.nextInt();
        scanner.close();

        System.out.println("Starting Simulation..");
        
        // doctors, patients and treatment rooms array of threads
        Thread[] doctors = new Thread[patientsCount];
        Thread[] patients = new Thread[patientsCount];
        TreatmentRooms tRoom= new TreatmentRooms(2);
        WaitingRoom wRoom = new WaitingRoom(waitingRoomCapacity, patientsCount, doctorsCount);

        // add and start doctors
        for (int i = 0; i < doctorsCount; i++) {
            Doctors doctor = new Doctors("Doctor-Number-" + (i+1), wRoom, tRoom);
            doctors[i] = new Thread(doctor);
            doctors[i].start();
        }
        
        for (int i = 0; i < patientsCount; i++) {
            Patients patient = new Patients("Patient-Number-" + (i+1), wRoom);
            patients[i] = new Thread(patient);
            patients[i].start();
        }

        try{
        if (patientsCount == 0) {
            wRoom.close(); // nobody will ever be treated, so nobody will trigger the close
        }
    } catch (InterruptedException e) {
        System.out.println("Thread was interrupted while sleeping");
        }

    }
}
