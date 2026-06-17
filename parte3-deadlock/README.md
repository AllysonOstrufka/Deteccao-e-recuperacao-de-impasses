# Parte 3 — Deadlock

Exercício de concorrência em Java com duas versões: uma que trava o sistema (deadlock) e outra que corrige o problema usando hierarquia de recursos.

**Parâmetros:** 2 threads concorrentes, 2 locks (`LOCK_A` e `LOCK_B`), com atraso de 50 ms (`Thread.sleep`) para garantir o cruzamento das requisições.

---

## 1. Descrição do problema

O cenário envolve **duas threads** disputando **dois recursos** representados pelos objetos `LOCK_A` e `LOCK_B`.

Na versão problemática, cada thread adquire os locks em ordem oposta:

- A **Thread 1** adquire `LOCK_A` e, em seguida, tenta adquirir `LOCK_B`.
- A **Thread 2** adquire `LOCK_B` e, em seguida, tenta adquirir `LOCK_A`.

O `Thread.sleep(50)` colocado entre a primeira e a segunda aquisição força que ambas as threads consigam o primeiro lock antes de qualquer uma tentar o segundo. Isso resulta em uma **espera circular** — cada thread segura o recurso que a outra precisa, e nenhuma das duas avança.

---

## 2. Como executar

```bash
# Compilar a versão que trava
cd Deteccao-e-recuperacao-de-impasses/parte3-deadlock/codigoComDeadlock/src/
javac Deadlock.java

# Executar a versão que trava
java Deadlock

# (Pressione Ctrl+C para interromper o travamento do terminal)

# Compilar a versão corrigida
cd Deteccao-e-recuperacao-de-impasses/parte3-deadlock/codigoSemDeadlock/src/
javac SemDeadlock.java

# Executar a versão corrigida
java SemDeadlock
```

---

## 3. Versão que trava (reprodução do deadlock)

### 3.1 Lógica da implementação

Ordem **invertida** de aquisição entre as threads:

```text
Globais:
  LOCK_A, LOCK_B = objetos de sincronizacao distintos

Thread 1:
  adquirir(LOCK_A)
  dormir(50ms)      // aumenta a chance do cruzamento
  adquirir(LOCK_B)  // BLOQUEIA AQUI aguardando T2
  imprimir "T1 concluiu"
  liberar(LOCK_B)
  liberar(LOCK_A)

Thread 2:
  adquirir(LOCK_B)
  dormir(50ms)
  adquirir(LOCK_A)  // BLOQUEIA AQUI aguardando T1
  imprimir "T2 concluiu"
  liberar(LOCK_A)
  liberar(LOCK_B)
```

### 3.2 Logs de execução

A saída evidencia que cada thread adquire seu primeiro lock e fica bloqueada ao tentar o segundo:

```text
T1: Adquiriu LOCK_A
T2: Adquiriu LOCK_B
T1: Aguardando LOCK_B...
T2: Aguardando LOCK_A...

```

> O resultado é **determinístico** por causa do `Thread.sleep(50)`. A Thread 1 segura A e espera B; a Thread 2 segura B e espera A. O programa congela e precisa ser forçado a parar (Ctrl+C).
>
> **Último log de saída:** `T2: Aguardando LOCK_A...` — depois disso, nada mais é impresso.

#### Tabela de resultados — Versão 1 (sem hierarquia)

| Execução | Comportamento Observado | Finalizou? | Último Log de Saída        |
|----------|-------------------------|------------|----------------------------|
| 1        | Travamento total        | Não        | T2: Aguardando LOCK_A...   |
| 2        | Travamento total        | Não        | T2: Aguardando LOCK_A...   |

### 3.3 Diagnóstico

Com o processo travado, é possível confirmar o deadlock pelo número do processo (`jps`) e inspecionar as threads bloqueadas com `jstack`:

```bash
#Comando 
jps                 # descobre o PID do processo "Deadlock"

#Output
203235 Deadlock
204135 Jps
174202 Main
189721 Deadlock
175613 org.eclipse.equinox.launcher_1.7.100.v20251111-0406.jar

#Comando 
jstack <PID>        # imprime o dump de threads

#Output
Found one Java-level deadlock:
=============================
"Thread-0":
  waiting to lock monitor 0x00007c16a4001d40 (object 0x000000063101a698, a java.lang.Object),
  which is held by "Thread-1"

"Thread-1":
  waiting to lock monitor 0x00007c16a80013e0 (object 0x000000063101a688, a java.lang.Object),
  which is held by "Thread-0"

Java stack information for the threads listed above:
===================================================
"Thread-0":
        at Deadlock.lambda$main$0(Deadlock.java:12)
        - waiting to lock <0x000000063101a698> (a java.lang.Object)
        - locked <0x000000063101a688> (a java.lang.Object)
        at Deadlock$$Lambda/0x0000000032040210.run(Unknown Source)
        at java.lang.Thread.runWith(java.base@25.0.2/Thread.java:1487)
        at java.lang.Thread.run(java.base@25.0.2/Thread.java:1474)
"Thread-1":
        at Deadlock.lambda$main$1(Deadlock.java:24)
        - waiting to lock <0x000000063101a688> (a java.lang.Object)
        - locked <0x000000063101a698> (a java.lang.Object)
        at Deadlock$$Lambda/0x0000000032040438.run(Unknown Source)
        at java.lang.Thread.runWith(java.base@25.0.2/Thread.java:1487)
        at java.lang.Thread.run(java.base@25.0.2/Thread.java:1474)

Found 1 deadlock.
```

O dump confirma exatamente a espera circular: `Thread-1` segura `LOCK_A` e espera `LOCK_B`, enquanto `Thread-2` segura `LOCK_B` e espera `LOCK_A`.

---

## 4. Mapeamento das condições de Coffman

Para que um deadlock ocorra, as **quatro condições de Coffman** precisam se manifestar simultaneamente. A versão sem hierarquia satisfaz todas:

### 4.1 Exclusão mútua

Os recursos `LOCK_A` e `LOCK_B` são monitorados pelo bloco `synchronized` do Java. Apenas **uma** thread pode possuir cada lock por vez.

### 4.2 Manter e esperar

A Thread 1 mantém a posse do `LOCK_A` e fica bloqueada aguardando a liberação do `LOCK_B`. Simultaneamente, a Thread 2 mantém o `LOCK_B` e aguarda o `LOCK_A`.

### 4.3 Não preempção

A JVM **não** permite que um lock associado a um bloco `synchronized` seja retirado à força de uma thread. Ele só é liberado voluntariamente quando a execução sai do respectivo bloco.

### 4.4 Espera circular

Formou-se um ciclo fechado de dependências: T1 aguarda um recurso de T2, que por sua vez aguarda um recurso de T1.


---

## 5. Versão corrigida (ordem global de aquisição)

### 5.1 Estratégia adotada

A estratégia é de **prevenção de deadlock** por meio de uma **ordem global de aquisição de recursos (hierarquia)**. A regra é única e vale para todas as threads:

> Independentemente do fluxo lógico, se a thread precisar de ambos os recursos, ela **sempre** deve solicitar o `LOCK_A` antes do `LOCK_B`.

```text
Regra global: TODAS as threads adquirem LOCK_A antes de LOCK_B

Thread 1 e Thread 2:
  adquirir(LOCK_A)
  dormir(50ms)
  adquirir(LOCK_B)
  liberar(LOCK_B)
  liberar(LOCK_A)
```

O fluxo passa a ser:

1. A Thread 1 adquire o `LOCK_A`.
2. A Thread 2 tenta adquirir o `LOCK_A`, mas ele já está ocupado. Ela fica bloqueada **logo no início** e não chega a adquirir o `LOCK_B`.
3. A Thread 1 adquire o `LOCK_B` (livre, pois T2 foi barrada antes), executa a seção crítica e libera ambos os locks.
4. A Thread 2, agora destravada, adquire o `LOCK_A`, depois o `LOCK_B`, e finaliza.

### 5.2 Qual condição de Coffman foi quebrada

A imposição da hierarquia quebra ativamente a **Espera Circular (4.4)**. Como nenhuma thread pode segurar `LOCK_B` enquanto espera por `LOCK_A`, torna-se impossível formar o ciclo de dependências mútuas. Sem espera circular, as demais três condições deixam de ser suficientes para travar o sistema.

### 5.3 Logs de execução (sem travamento)

```text
T1: Tentando adquirir LOCK_A...
T2: Tentando adquirir LOCK_A...
T1: LOCK_A adquirido. Dormindo 50ms...
T1: Acordou. Tentando adquirir LOCK_B...
T1: LOCK_B adquirido. Seção crítica.
T1 concluiu. Liberou LOCK_B e LOCK_A.
T2: LOCK_A adquirido. Dormindo 50ms...
T2: Acordou. Tentando adquirir LOCK_B...
T2: LOCK_B adquirido. Seção crítica.
T2 concluiu. Liberou LOCK_B e LOCK_A.
```

#### Tabela de resultados — Versão 2 (com hierarquia)

| Execução | Comportamento Observado | Finalizou? |
|----------|-------------------------|------------|
| 1        | Execução sequencial     | Sim        |
| 2        | Execução sequencial     | Sim        |

> A Thread 2 é forçada a aguardar a Thread 1 liberar o `LOCK_A` antes de continuar, impossibilitando o cruzamento mortal de requisições.

---

## 6. Conclusão

O deadlock da Versão 1 é a condição simultânea das quatro condições de Coffman, em especial da **espera circular** provocada pela ordem inconsistente de aquisição dos locks. O `Thread.sleep(50)` torna o problema fácil de reproduzir, o cruzamento poderia acontecer mesmo sem ele.

A correção exige **padronizar a ordem de aquisição dos recursos** (sempre `LOCK_A` antes de `LOCK_B`). Essa única regra remove a possibilidade de ciclo e garante que o programa continue rodando, transformando uma disputa que travava indefinidamente em uma execução funcional.