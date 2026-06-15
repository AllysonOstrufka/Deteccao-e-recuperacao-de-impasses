public class Filosofo implements Runnable {
    private Object garfo1;
    private Object garfo2;

    public Filosofo(Object garfo1, Object garfo2) {
        this.garfo1 = garfo1;
        this.garfo2 = garfo2;
    }

    private void pensar() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " está pensando...");
        Thread.sleep((long) (Math.random() * 100));
    }

    private void comer() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " ESTÁ COMENDO!");
        Thread.sleep((long) (Math.random() * 100));
    }

    @Override
    public void run() {
        try {
            while (true) {
                pensar();
                // Tenta pegar o primeiro garfo da sua ordem
                synchronized (garfo1) {
                    System.out.println(Thread.currentThread().getName() + " pegou o primeiro garfo.");
                    // Tenta pegar o segundo garfo da sua ordem
                    synchronized (garfo2) {
                        System.out.println(Thread.currentThread().getName() + " pegou o segundo garfo.");
                        comer();
                    } // Libera o garfo 2

                    System.out.println(Thread.currentThread().getName() + " soltou o segundo garfo.");
                } // Libera o garfo 1

                System.out.println(Thread.currentThread().getName() + " soltou o primeiro garfo e voltou a pensar.");
            }
        } catch (InterruptedException e) {
            // Flag dee interrupção
            Thread.currentThread().interrupt();
            System.out.println(Thread.currentThread().getName() + " foi interrompido.");
        }
    }
}
