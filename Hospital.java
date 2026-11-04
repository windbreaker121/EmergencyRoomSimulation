import java.util.Scanner;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

        for (int i = 0; i <= p; i++) {
            rooms[i] = new treatmentRoom("Room Number: " + (i+1));
        }
    }

    public treatmentRoom requestRoom(String doctorName) throws InterruptedException {
        if (!RoomsAvailable.tryAcquire(3, TimeUnit.SECONDS)) {
            // should return room object
            // not just boolean
            return null;
        } else {
            return aquireRoom();
        }
    }
    
    public synchronized void returnRoom(treatmentRoom room) {
        room.isAvailable = true; // return state of this room object to available
        RoomsAvailable.release(); // release semaphore permit
    }
    
    private synchronized treatmentRoom aquireRoom() { // so no two doctors try to aquire a room in the same time
        for (int i = 0; i < roomsCount; i++) {
            if (rooms[i].isAvailable) {
                //
                rooms[i].isAvailable = false;
                return rooms[i];
            }
        }
        throw new IllegalStateException("aquired room but no rooms available");
    }
}

class WaitingRoom {
    int capacity;
    int currentlyOcuupied = 0;

    WaitingRoom(int capacity, int currentlyOcuupied) {
        this.capacity = capacity;
        this.currentlyOcuupied = currentlyOcuupied;
    }

    // askForSeat()
    synchronized public boolean askForSeat() {
        if (currentlyOcuupied < capacity) {
            return true;
        } else {
            return false;
        }
    }
}

class Patients implements Runnable {
    private String name;
    private WaitingRoom wRoom;

    Patients(String name, WaitingRoom wRoom) {
        this.name = name;
    }

    public void run() {
        // decision
        // if there a seat in the room, enter the queue of waiting room
        // if not, wait until a seat if available (block or sleep)
    }
}

class Doctors implements Runnable {
    private String doctorName;
    private boolean idle = true;
    private treatmentRoom room;
    private String patientName;
    // private WaitingRoom waitingRoom;
    private TreatmentRooms tRooms;

    Doctors(String doctorName, TreatmentRooms tRoom) {
        this.doctorName = doctorName;
        // this.waitingRoom = wRoom;
        this.tRooms = tRoom;
    }

    public void run() {

        while (this.idle == true) {
        }
        // not idle anymore
        try{
            room = tRooms.requestRoom(doctorName);
            if (room != null) {
                try {
                    treating(doctorName, patientName, room);
                } finally {
                    tRooms.returnRoom(room);
                }
            }
            else {
                // drop patient
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void treating(String doctorName, String patientName, treatmentRoom room) {
        try {
            System.out.println(doctorName + " is treating " + patientName + " in " + room.name);
            Thread.sleep(2000); // sleep for 3 seconds
            System.out.println(doctorName + " Finished.");
        } catch (InterruptedException e) {
            // Handle the interruption here
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
        WaitingRoom wRoom = new WaitingRoom(waitingRoomCapacity, 0);

        // add and start doctors
        for (int i = 0; i < doctorsCount; i++) {
            Doctors doctor = new Doctors("Doctor-Number-" + (i+1), tRoom);
            doctors[i] = new Thread(doctor);
            doctors[i].start();
        }
        
        for (int i = 0; i < patientsCount; i++) {
            Patients patient = new Patients("Patient-Number-" + i + 1, wRoom);
            patients[i] = new Thread(patient);
            patients[i].start();
        }
    }
}
