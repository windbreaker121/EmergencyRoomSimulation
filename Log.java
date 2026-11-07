// Owner: Person 1
// Thread-safe console printer so lines from different threads never interleave.
class Log {
    static synchronized void log(String s) {
        System.out.println(s);
    }
}
