import java.util.concurrent.TimeUnit;

public class Threading implements Runnable {

    private final String message;

    public Threading(String message) {
        this.message = message;
    }


    public void run() {
        while (true) {
            System.out.println(Thread.currentThread().getName() + ":  " + message);
            try {

                TimeUnit.MILLISECONDS.sleep(((long) (Math.random() * 100)));
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Threading("Test 1")).start();
        new Thread(new Threading("Test 2")).start();
        new Thread(new Threading("Test 3")).start();
    }
}