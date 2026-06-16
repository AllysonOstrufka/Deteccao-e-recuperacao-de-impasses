public class SemDeadlock {
    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("T1: Adquiriu LOCK_A");
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                synchronized (LOCK_B) {
                    System.out.println("T1 concluiu");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("T2: Adquiriu LOCK_A");
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                synchronized (LOCK_B) {
                    System.out.println("T2 concluiu");
                }
            }
        });

        t1.start();
        t2.start();
    }
}