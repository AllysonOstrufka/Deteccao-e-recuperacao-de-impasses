public class Filosofo implements Runnable {
    private final Object garfoEsquerdo;
    private final Object garfoDireito;

    public Filosofo(Object garfoEsquerdo, Object garfoDireito) {
        this.garfoEsquerdo = garfoEsquerdo;
        this.garfoDireito = garfoDireito;
    }

    private void pensar() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " está pensando...");
        Thread.sleep((long) (Math.random() * 10));
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

                synchronized (garfoEsquerdo) {
                    System.out.println(Thread.currentThread().getName() + " tentou pegar o garfo ESQUERDO.");

                    // Garantia de que todos filosofos pegarão garfo esquerdo e que irá travar
                    Thread.sleep(50);

                    synchronized (garfoDireito) {
                        System.out.println(Thread.currentThread().getName() + " tenotu pegar o garfo DIREITO.");
                        comer();
                    }

                    System.out.println(Thread.currentThread().getName() + " soltou o garfo direito.");
                }

                System.out.println(Thread.currentThread().getName() + " soltou o garfo esquerdo.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(Thread.currentThread().getName() + " foi interrompido.");
        }
    }
}