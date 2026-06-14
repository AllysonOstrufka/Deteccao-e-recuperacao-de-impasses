public class JantarDosFilosofos {

    public static void main(String[] args) {
        int NUM_FILOSOFOS = 5;
        Filosofo[] filosofos = new Filosofo[NUM_FILOSOFOS];
        Object[] garfos = new Object[NUM_FILOSOFOS];

        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            garfos[i] = new Object();
        }

        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            Object garfoEsquerdo = garfos[i];
            Object garfoDireito = garfos[(i + 1) % NUM_FILOSOFOS];

            filosofos[i] = new Filosofo(garfoEsquerdo, garfoDireito);

            Thread t = new Thread(filosofos[i], "Filósofo-" + (i + 1));
            t.start();
        }
    }
}