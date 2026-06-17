import java.util.concurrent.Semaphore;

public class ComSemaforo {

    static int count = 0;
    static Semaphore sem = new Semaphore(1, true);
    static final int T = 10;
    static final int M = 100000; 

    static class Tarefa implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < M; i++) {
                try {
                    sem.acquire();
                    try {
                        count = count + 1;
                    } finally {
                        sem.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
    }

    
    public static void main(String[] args) throws InterruptedException {
        count = 0;
        Thread[] threads = new Thread[T];

        long inicio = System.currentTimeMillis();

        for (int i = 0; i < T; i++) {
            threads[i] = new Thread(new Tarefa());
            threads[i].start();
        }

        for (int i = 0; i < T; i++) {
            threads[i].join();
        }

        long fim = System.currentTimeMillis();

        int esperado = T * M;
        int obtido = count;
        double tempo = (fim - inicio) / 1000.0;

        System.out.println("Esperado : " + esperado);
        System.out.println("Obtido   : " + obtido);
        System.out.printf("Tempo    : %.4fs%n", tempo);
    }
}
