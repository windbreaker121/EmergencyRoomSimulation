import java.util.Scanner;
import java.util.Queue;

class Patient implements Runnable{
    private String name;
    private SharedWaitingRoom waitingRoom;
    

    patient(String name, SharedWaitingRoom, SharedTreatRoom) {
        this.name = name;
    }

    // evaluate waitingroom
    private boolean enterWaitingRoom()
    {
        // check the shared resource, SharedWaitingRoom
    }

    public void run() {
        // decision
        // if there a seat in the room, enter the queue of waiting room
        // if not, wait until a seat if available (block or sleep)

        
    }
}

class Doctors implements Runnable{
    private String name;
    private SharedWaitingRoom waitingRoom;
    private SharedTreatRoom treatmentRoom;

    doctors(String name, SharedWaitingRoom waitingRoom, SharedTreatRoom treatmentRoom) {
        this.name = name;
        this.waitingRoom = waitingRoom;
        this.treatmentRoom = treatmentRoom;
    }

    public void run() {
        
    }
}

class SharedTreatRoom {
    Integer number;
    boolean free = true;
    
    treatment_rooms(Integer number) {
        this.number = number;
    }
}

class SharedWaitingRoom {
    Integer capacity;
    Integer currentlyOcuupied = 0;
    boolean lock = false;

    waiting_room(Integer capacity, Integer currentlyOcuupied) {
        this.capacity = capacity;
        this.currentlyOcuupied = currentlyOcuupied;
    }

    // askForSeat()
    synchronized public boolean askForSeat() {
        if (currentlyOcuupied < capacity) {
            return true;
        }
        else {
            return false;
        }
    }

    // isLocked()
}

public class Hospital {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Waiting room capacity: ");
        int waitingRoomCapacity = scanner.nextInt();

        System.out.println("Number of doctors: ");
        int doctorsCount = scanner.nextInt();

        System.out.println("Number of patient: ");
        int patientcount = scanner.nextInt();

        System.out.println("Starting Simulation..");

        Queue<patient> waitingQueue = new LinkedList<>();
        WaitingRoom sharedWaitingRoom = new WaitingRoom(waitingRoomCapacity);
        
        // 
        Thread[] doctors = new Thread(doctorsCount);
        Thread[] patient = new Thread(patientcount);

        // add and start doctors
        for (int i = 0; i < doctorsCount; i++) {
            Doctors doctor = new Doctors("Doctor-Number-" + i+1, SharedWaitingRoom, SharedTreatRoom);
            doctors[i] = new Thread(doctor);
            doctors[i].start();
        }
        
        for (int i = 0; i < patientcount; i++){
            Patient patient = new Patient("Patient-Number-" + i+1, SharedWaitingRoom, SharedTreatRoom);
            patient[i] = new Thread(patient);
            patient[i].start();
        }

    }
}
