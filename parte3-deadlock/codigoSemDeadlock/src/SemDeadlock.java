public class SemDeadlock {
    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            System.out.println("T1: Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                System.out.println("T1: LOCK_A adquirido. Dormindo 50ms...");
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                System.out.println("T1: Acordou. Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    System.out.println("T1: LOCK_B adquirido. Seção crítica.");
                }
            }
            System.out.println("T1 concluiu. Liberou LOCK_B e LOCK_A.");
        });

        Thread t2 = new Thread(() -> {
            System.out.println("T2: Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                System.out.println("T2: LOCK_A adquirido. Dormindo 50ms...");
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                System.out.println("T2: Acordou. Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    System.out.println("T2: LOCK_B adquirido. Seção crítica.");
                }
            }
            System.out.println("T2 concluiu. Liberou LOCK_B e LOCK_A.");
        });

        t1.start();
        t2.start();
    }
}