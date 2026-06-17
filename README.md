# Deteccao e recuperacao de impasses
Este repositório faz parte do trabalho desenvolvido por:
- Allyson P. Ostrufka;
- Gonçalo Henrique;
- Gustavo Bolsoni;
- Henrique de Souza Mello.

LINK DO VÍDEO NO YOUTUBE: 

Linguagem escolhida: Java
Instruções de compilação/execução:
Utilizar os comandos `javac` (compilação) e `java` (execução). Considerando este repositório como raíz, os caminhos são:

```bash
# Parte 1 — Jantar dos Filósofos
cd parte1-filosofos/codigoComDeadlock/src && javac *.java && java JantarDosFilosofos
cd parte1-filosofos/codigoSemDeadlock/src && javac *.java && java JantarDosFilosofos

# Parte 2 — Threads e Semáforos
cd parte2-threads-semaforos && javac SemSincronizacao.java ComSemaforo.java
java SemSincronizacao
java ComSemaforo

# Parte 3 — Deadlock
cd parte3-deadlock/codigoComDeadlock/src && javac Deadlock.java && java Deadlock
cd parte3-deadlock/codigoSemDeadlock/src && javac SemDeadlock.java && java SemDeadlock
```
Relatório técnico parte 1:
Contexto: Cinco filósofos estão sentados em uma mesa circular. Entre eles há cinco garfos, um entre cada par de vizinhos, de forma que cada filósofo só consegue comer ao usar os dois garfos que estão à sua esquerda e à sua direita. Cada filósofo alterna eternamente entre três estados:
1 - Pensando: Não usa nenhum dos garfos, entra em `Thread.sleep` simulando um estado de reflexão;
2 - Com fome: Decidiu que quer comer e vai tentar pegar os dois garfos (da esquerda e da direita). No código, esse estado corresponde ao momento entre o fim de `pensar()` e a entrada nos blocos `synchronized` dos garfos (é o momento em que a thread pode ficar bloqueada esperando um garfo ocupado pelo vizinho).
3 - Comendo: — só ocorre depois de adquirir os dois garfos (o esquerdo e o
   direito), dentro dos blocos `synchronized` aninhados.
   
Como os garfos são objetos compartilhados entre dois filósofos vizinhos, o acesso a cada garfo é protegido por `synchronized`, garantindo exclusão mútua: nunca dois filósofos comem usando o mesmo garfo ao mesmo tempo.

--
O que gera o deadlock?

Já que no (`codigoComDeadlock`) todo filósofo segue sempre a mesma regra: pegar com a mão esquerda primeiro e depois com a direita, a probabilidade de que todos os filósofos consigam pegar o primeiro garfo, mas não consigam pegar o segundo é muito alta. Com isso, é formado uma espécie de "ciclo de espera", onde o filósofo 1 aguarda que o 2 solte, o 2 aguarda que o 3 solte e assim por diante. 

Essa condição gera um travamento no sistema: nenhum filósofo come, todos aguardam infinitamente até que o outro solte o garfo.
Esse é uma condição conhecida como espera circular (*circular wait*), uma das quatro condições de Coffman. As outras três também estão presentes nesse cenário (exclusão mútua pelos `synchronized`, manter-e-esperar porque cada filósofo guarda o garfo já obtido enquanto espera o outro, e não preempção porque a JVM nunca retira um monitor de uma thread à força). Como as quatro condições se manifestam simultaneamente, o sistema trava: nenhum filósofo progride.

--
Como resolvemos o deadlock?

No (`codigoSemDeadlock`) o instanciar um novo filósofo, ele sempre tenta adquirir o garfo que foi passado no construtor. Pensando nisso que, na versão corrigida, implementamos a seguinte condição: todos os filósofos seguem a mesma lógica (garfoEsquerdo, garfoDireito) com a exceção do último: que vem com (garfoDireito, garfoEsquerdo), isso é possível observar nesse trecho do código:

```java
if (i == NUM_FILOSOFOS - 1) {
    filosofos[i] = new Filosofo(garfoDireito, garfoEsquerdo);
} else {
    filosofos[i] = new Filosofo(garfoEsquerdo, garfoDireito);
}
```

Isso impede que a condição de espera circular aconteça: não existe mais um ciclo fechado em que cada filósofo espera o vizinho seguinte indefinidamente, porque pelo menos um par de filósofos disputa o mesmo garfo na mesma ordem, e um deles sempre consegue progredir. As outras três condições de Coffman (exclusão mútua, manter-e-esperar, não preempção) continuam presentes, mas isso não é um problema: o deadlock é eliminado assim que uma das condições de Coffman é negada.

--
Justiça e tempo de inanição:

A simulação evita que um mesmo filósofo coma sempre antes dos demais por dois
motivos combinados:

- Tempos aleatórios: `pensar()` e `comer()` usam `Thread.sleep` com duração
  aleatória (`Math.random()`), então a ordem em que os filósofos tentam pegar os garfos varia a cada ciclo, evitando que um padrão fixo favoreça sempre o mesmo filósofo.
- Monitores do Java: os blocos `synchronized` usam o monitor intrínseco do objeto. A JVM não organiza uma fila (como um FIFO), mas na prática threads bloqueadas têm chance similar de serem despertadas quando o monitor é liberado, então nenhum filósofo fica permanentemente impedido de adquirir os garfos. Ele apenas depende da "sorte" de ser mais rápido.

Tudo isso garante o progresso do código, o sistema nunca trava, pela ausência de espera circular e uma distribuição equilibrada das refeições entre os cinco filósofos ao longo do tempo.

---
Pseudocódigo do protocolo

```
N = 5 filósofos
garfos[0..N-1], onde garfo[i] fica entre o filósofo i e o filósofo (i+1) mod N

Para cada filósofo p (0 a N-1):
    garfoEsquerdo = garfos[p]
    garfoDireito  = garfos[(p+1) mod N]

    se p == N-1:
        primeiro = garfoDireito   
        segundo  = garfoEsquerdo
    senão:
        primeiro = garfoEsquerdo
        segundo  = garfoDireito

Loop (para cada filósofo):
    pensar()                  
    // estado: "com fome" (a partir daqui)
    adquirir(primeiro)         
    adquirir(segundo)          
    // estado: "comendo"
    comer()
    liberar(segundo)
    liberar(primeiro)
    // volta ao estado de "pensando"
```

A inversão da ordem apenas no último filósofo é o que rompe o ciclo: o garfo
compartilhado entre o filósofo `N-1` e o filósofo `0` deixa de ser solicitado em ordens opostas pelos dois lados.

Relatório técnico parte 2:

Contexto: Uma variável inteira compartilhada (`count`) é incrementada por T = 10 threads, cada uma realizando M = 100.000 incrementos, totalizando um valor esperado de 1.000.000. Duas versões foram implementadas: uma sem sincronização, expondo uma condição de corrida, e outra protegida por um semáforo binário.

Parâmetros: T = 10 threads, M = 100.000 incrementos por thread → esperado = 1.000.000

--
O que gera a condição de corrida?

A operação `count = count + 1` parece simples, mas é composta por três etapas distintas no nível do hardware e da JVM:

1. Leitura: o valor atual de `count` é carregado da memória para um registrador;
2. Soma: calcula-se `valor + 1`;
3. Escrita: o resultado é gravado de volta na memória.

O problema ocorre quando duas threads executam essas etapas de forma entrelaçada. Com `count = 5`, por exemplo:

```
Thread A lê count  → 5
Thread B lê count  → 5   (antes de A terminar de escrever)
Thread A escreve   → 6
Thread B escreve   → 6   (deveria ser 7, mas B leu o valor antigo)
```

Dois incrementos foram feitos, mas `count` avançou apenas 1 valor. Esse é o comportamento clássico de uma condição de corrida: o escalonador da JVM pode interromper qualquer thread entre a leitura e a escrita, tornando o resultado final imprevisível — às vezes acerta por sorte, às vezes perde centenas de milhares de incrementos. 

--
Como resolvemos a condição de corrida?

No (`codigoComSemaforo`), envolvemos o incremento em um `Semaphore(1, true)` do Java (`java.util.concurrent.Semaphore`). Antes de entrar na seção crítica, a thread chama `()`. Se a vaga estiver ocupada, ela fica bloqueada esperando. Só depois que a thread atual chamar `release()` uma outra thread poderá entrar.

Isso garante exclusão mútua: em qualquer instante, no máximo uma thread está executando `count = count + 1`. As três etapas (de leitura, soma e escrita) tornam-se efetivamente atômicas do ponto de vista das demais threads.

--
Justiça:

A simulação evita inconsistências e distribui o acesso à seção crítica por dois mecanismos combinados:

Semáforo justo: o argumento true no construtor Semaphore(1, true) garante uma fila FIFO entre as threads bloqueadas, então nenhuma thread fica permanentemente impedida de entrar na seção crítica. Ela apenas aguarda sua vez na fila.
Modelo de Memória do Java (JMM): o release() de um semáforo happens-before o acquire() correspondente, garantindo a visibilidade correta dos valores entre as threads.

Isso garante correção (o contador sempre chega a 1.000.000, pela exclusão mútua) e uma entrada ordenada das threads na seção crítica. O custo é a serialização: as threads formam uma fila e passam pelo incremento uma de cada vez, o que nos resultados gerou uma execução ~200x mais lenta do que a versão sem sincronização.

---
Pseudocódigo do protocolo

```
Globais:
  count = 0
  sem = Semaforo(permissoes=1, justo=true)  // binário com FIFO

Para cada thread t (0 a T-1):
  para i de 1 ate M:
    sem.adquirir()       // bloqueia se outra thread estiver na seção crítica
    try:
      count = count + 1  // seção crítica: leitura, soma e escrita são atomicas
    finally:
      sem.liberar()      // garante liberação mesmo em caso de exceção
```

O `finally` é o que garante que nenhuma exceção dentro da seção crítica trave o semáforo para sempre: independentemente do que aconteça dentro do `try`, o `release()` é sempre executado.

Relatório técnico parte 3:

Contexto: Duas threads concorrem pelo acesso a dois locks distintos (`LOCK_A` e `LOCK_B`). Um atraso proposital de 50ms foi introduzido para garantir que o cruzamento de requisições aconteça de forma determinística. Duas versões foram implementadas: uma que entra em deadlock, e outra que o evita aplicando hierarquia global de aquisição de locks.

Parâmetros: 2 threads concorrentes, 2 locks (`LOCK_A` e `LOCK_B`), atraso de 50ms para forçar o cruzamento.

--
O que gera o deadlock?

No (`codigoComDeadlock`), as duas threads adquirem os locks em ordens opostas: a Thread 1 sempre tenta pegar `LOCK_A` primeiro e depois `LOCK_B`, enquanto a Thread 2 faz o inverso. O `sleep` de 50ms foi introduzido propositalmente para garantir que ambas consigam adquirir seu primeiro lock antes de tentar o segundo, tornando o cruzamento praticamente certo e o deadlock apenas uma questão de tempo.

Com isso, forma-se um ciclo de espera: a Thread 1 segura `LOCK_A` e aguarda `LOCK_B`, enquanto a Thread 2 segura `LOCK_B` e aguarda `LOCK_A`. Nenhuma das duas consegue progredir.

Essa condição gera um travamento no sistema: nenhuma thread conclui, ambas aguardam indefinidamente que a outra libere seu lock. Bem parecido com o que acontece no Jantar dos FIlósofos, esse é um exemplo de espera circular, uma das quatro condições de Coffman. As outras três também estão presentes nesse cenário: exclusão mútua pelos blocos `synchronized`, manter-e-esperar porque cada thread segura o lock já obtido enquanto espera o outro, e não preempção porque a JVM nunca retira um monitor de uma thread à força. Como as quatro condições se manifestam simultaneamente, o sistema trava: nenhuma thread progride.

--
Como resolvemos o deadlock desse caso?

No (`codigoSemDeadlock`), adotamos a estratégia de prevenção por hierarquia de recursos: impusemos uma ordem global de aquisição de locks. A regra é simples: não importa o fluxo lógico da thread, se ela precisar de ambos os locks, deve sempre adquirir `LOCK_A` antes de `LOCK_B`. Isso pode ser observado no comportamento das duas threads na versão corrigida — ambas seguem exatamente a mesma sequência de aquisição.

Isso impede que a condição de espera circular aconteça: como nenhuma thread pode tentar `LOCK_B` sem já ter `LOCK_A` primeiro, o cruzamento fatal de requisições se torna impossível. A Thread 2 é bloqueada logo no início, aguardando `LOCK_A`, enquanto a Thread 1 adquire `LOCK_B` livremente, executa sua seção crítica e libera ambos os locks. Só então a Thread 2 pode progredir. As outras três condições de Coffman (exclusão mútua, manter-e-esperar e não preempção) continuam presentes, mas assim como foi explicado na parte 1: o deadlock é eliminado assim que uma das condições é negada.

--
Sobre o determinismo do deadlock:

O travamento na versão com deadlock é observado de forma determinística por dois motivos combinados:

- Atraso proposital: o `Thread.sleep(50ms)` garante que ambas as threads consigam adquirir seu primeiro lock antes de tentar o segundo, tornando o cruzamento de requisições praticamente inevitável.
- Ordem fixa de aquisição: como a Thread 1 sempre pega `LOCK_A` e a Thread 2 sempre pega `LOCK_B` primeiro, não há variação no padrão de disputa — o ciclo de espera sempre se forma da mesma forma.

Na versão com hierarquia, o comportamento passa a ser sequencial: uma thread sempre precisa esperar a outra liberar `LOCK_A` antes de prosseguir, o que elimina qualquer possibilidade de cruzamento.

---
Pseudocódigo do protocolo

```
Globais:
  LOCK_A, LOCK_B = objetos de sincronizacao distintos

// Regra global: toda thread adquire LOCK_A antes de LOCK_B

Para cada thread t (Thread 1 e Thread 2):
  adquirir(LOCK_A)       // Thread 2 bloqueia aqui se Thread 1 ainda não liberou
  dormir(50ms)
  adquirir(LOCK_B)       // seguro: Thread 2 não chegou aqui ainda
  // seção crítica
  liberar(LOCK_B)
  liberar(LOCK_A)
  // volta ao estado disponível
```

A imposição de uma ordem global de aquisição é o que rompe o ciclo: ao forçar que toda thread dispute `LOCK_A` primeiro, garante-se que nunca dois lados do ciclo segurem locks em ordens opostas, e o cruzamento fatal se torna estruturalmente impossível.
Prints/Logs execução parte 1:
Prints/Logs execução parte 2:
Prints/Logs execução parte 3:
