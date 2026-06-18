Imagine 5 filósofos sentados em uma mesa redonda. Entre cada um deles, há um garfo (5 garfos no total). Para comer, um filósofo precisa obrigatoriamente de dois garfos (o da sua esquerda e o da sua direita).

- Eles passam a vida em um ciclo eterno de três passos:

Pensando: Não usam os garfos. Simulado no código por um tempo de pausa (Thread.sleep).
Com fome: O momento crítico onde tentam adquirir os dois garfos da mesa.
Comendo: Acontece apenas quando conseguem os dois garfos. O uso de cada garfo é protegido por blocos synchronized no Java, garantindo a exclusão mútua (duas pessoas nunca usam o mesmo garfo ao mesmo tempo).

- O Problema
Se programarmos todos os filósofos com a exata mesma regra (pegue o garfo da esquerda primeiro, e depois o da direita), criamos um possível travamento.
Se todos sentirem fome ao mesmo tempo, cada um vai pegar o seu respectivo garfo da esquerda. E assim, o filósofo 1 fica esperando o 2 soltar o garfo, o 2 espera o 3, e assim por diante. Forma-se um ciclo onde ninguém come e ninguém solta o garfo que já tem.
Na teoria de sistemas operacionais, isso é chamado de Espera Circular (uma das quatro condições de Coffman para que um deadlock aconteca). Como todas as condições se alinham, o sistema trava de vez.

A Solução: Quebrando o Ciclo
Para resolver isso, nós introduzimos uma pequena exceção na regra.
A lógica de pegar os garfos é a mesma para quase todos, exceto para o último filósofo da mesa. Enquanto os primeiros filósofos pegam primeiro o garfo da esquerda e depois o da direita, o último faz o inverso: pega primeiro o da direita, depois o da esquerda.
Ao inverter a ordem de apenas um deles, nós destruímos a possibilidade da Espera Circular. Em algum momento, dois filósofos vizinhos vão disputar o exato mesmo primeiro garfo. Um deles vai conseguir, comer em paz e depois liberar os recursos para os outros. O ciclo foi quebrado e o sistema nunca trava.

Justiça (Evitando a Inanição)
Como garantimos que um filósofo não seja mais rápido e coma o tempo todo enquanto os outros morrem de fome (starvation)? O nosso sistema se equilibra naturalmente por dois motivos:

Tempos aleatórios: O tempo que eles passam pensando e comendo varia a cada ciclo (usando Math.random), embaralhando a ordem em que tentam pegar os garfos.
Monitores do Java: O synchronized não cria uma fila rígida, mas na prática, dá chances parecidas para que as threads bloqueadas acordem e consigam o recurso quando ele é liberado.

Isso garante que o sistema sempre progrida e que as refeições sejam distribuídas de forma justa com o passar do tempo.

Pseudocódigo da Solução

N = 5 filósofos
garfos[0..N-1] (cada garfo fica entre dois filósofos)

Para cada filósofo (p):
garfo_esquerdo = garfos[p]
garfo_direito = garfos[(p+1) mod N]

se p for o último filósofo (N-1):
primeiro = garfo_direito
segundo = garfo_esquerdo
senão:
primeiro = garfo_esquerdo
segundo = garfo_direito

Loop de vida do filósofo:
pensar()

adquirir(primeiro)
adquirir(segundo)

comer()

liberar(segundo)
liberar(primeiro)