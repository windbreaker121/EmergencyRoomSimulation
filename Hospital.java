import java.util.Scanner;

public class Hospital {

    private static final int TREATMENT_ROOMS = 2;

    private static int readPositive(Scanner in, String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextInt()) {
                in.next();
                System.out.println("Please enter a whole number.");
                continue;
            }
            int v = in.nextInt();
            if (v >= min) {
                return v;
            }
            System.out.println("Value must be at least " + min + ".");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        int capacity = readPositive(in, "Waiting room capacity: ", 1);
        int doctorCount = readPositive(in, "Number of doctors: ", 1);
        int patientCount = readPositive(in, "Number of patients: ", 0);
        in.close();

        SharedWaitingRoom waitingRoom = new SharedWaitingRoom(capacity);
        SharedTreatRoom treatRooms = new SharedTreatRoom(TREATMENT_ROOMS);

        // Doctors
        Thread[] doctors = new Thread[doctorCount];
        for (int i = 0; i < doctorCount; i++) {
            doctors[i] = new Thread(new Doctor("Doctor " + (i + 1), waitingRoom, treatRooms));
            doctors[i].start();
        }

        // Patients
        StringBuilder list = new StringBuilder("Patients arriving:\n");
        for (int i = 1; i <= patientCount; i++) {
            list.append("P").append(i).append(i < patientCount ? " " : "");
        }
        Log.log(list.toString());

        Thread[] patients = new Thread[patientCount];
        for (int i = 0; i < patientCount; i++) {
            patients[i] = new Thread(new Patient("P" + (i + 1), waitingRoom));
            patients[i].start();
            Thread.sleep(300);
        }

        // A patient thread ends only after it has been treated and has left
        for (Thread t : patients) {
            t.join();
        }

        // Everyone has been treated: wake the idle doctors so they shut down
        waitingRoom.close(doctorCount);
        for (Thread t : doctors) {
            t.join();
        }

        Log.log("All patients have been treated.");
        Log.log("Simulation completed.");
    }
}
