public class Deadlock {
    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("T1: Adquiriu LOCK_A");
                try { Thread.sleep(50); } catch (InterruptedException e) {}

                System.out.println("T1: Aguardando LOCK_B...");
                synchronized (LOCK_B) {
                    System.out.println("T1 concluiu");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (LOCK_B) {
                System.out.println("T2: Adquiriu LOCK_B");
                try { Thread.sleep(50); } catch (InterruptedException e) {}

                System.out.println("T2: Aguardando LOCK_A...");
                synchronized (LOCK_A) {
                    System.out.println("T2 concluiu");
                }
            }
        });

        t1.start();
        t2.start();
    }
}