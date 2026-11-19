# O que falta fazer — Back-end do Xadrez (servidor gRPC)

Atualizado em 2026-11-19, depois de comparar a lista com o código atual.

**Como usar este arquivo**
- Trabalhar uma sub-etapa por vez.
- Quando um item estiver pronto e conferido (código lido e, quando der, testado no Postman), ele sai daqui. Quando todos os itens de uma etapa saírem, a etapa inteira sai.
- Cada item tem uma linha **Hoje** (o que o código já tem) e uma linha **Falta** (o que ainda precisa ser feito).
- Teste manual: Postman gRPC em `localhost:<porta do Main> (hoje 50040)`, sem TLS, importando `src/main/proto/xadrez.proto` (o servidor não tem reflexão ligada).

**Decisão do João (2026-11-19): todos os testes ficam juntos na Etapa 10; nenhum teste é feito durante as etapas anteriores.**

**Já concluído e removido:** Etapa 1 (pré-jogo e reconexão), testada no Postman em 2026-11-04. Etapa 2 (peças e coordenadas: texto da peça e `Posicao` com validação, texto, deslocamento e igualdade), conferida no código em 2026-11-17. Etapa 3 (tabuleiro: leitura de casa, posição inicial, caminho livre, mover/capturar, localizar o rei, texto do estado, simular/desfazer, trocar peça e ligação à `Partida`), conferida no código em 2026-11-18. Etapa 4 (`ObterEstadoDoJogo`: `NOT_FOUND` para partida inexistente, texto das peças, turno e estado), conferida no código em 2026-11-19. Etapa 6 (`EnviarJogada`: conferências de partida, jogador, estado e vez; posições; validação pelas regras, movimento, troca de turno e resposta `jogadaValida` falsa com o motivo), conferida no código em 2026-11-19. Etapa 5 (movimentos: `ValidadorRegras` com peão, torre, bispo, rainha, cavalo e rei, e `Jogada` com `Posicao` e resultado da validação), conferida no código em 2026-11-19. O item 2.1 também já tinha saído da lista original.

**Resumo**

| Etapa | Assunto | Situação |
|---|---|---|
| 7 | Fim de jogo | Não iniciada |
| 8 | Regras avançadas | Não iniciada |
| 9 | Robustez | Parcial (9.1 em parte) |
| 10 | Testes | Não iniciada |
| 11 | Entrega | Não iniciada |

---

## Etapa 7 — Fim de jogo

Hoje: o `ValidadorRegras.verificaXeque(tabuleiro, cor)` (privado) já devolve `true` se o rei da cor está em xeque: procura o rei e pergunta ao `validaMovimento` se alguma peça adversária consegue ir até ele (item 7.2 feito). O `verificaLanceLegal(tabuleiro, origem, destino)` (público) simula o lance, vê se o rei da cor de quem jogou ficou em xeque e sempre desfaz; a `Partida.validaMovimento` já usa esse método, então um lance que deixa o próprio rei em xeque é recusado com "Movimento inválido!" (item 7.1 feito). O `verificaXequeMate(tabuleiro, cor)` (público) devolve `true` se o rei está em xeque e nenhuma peça da cor tem lance legal para nenhuma das 64 casas (item 7.3 feito).

- **7.4 Detectar afogamento** — não feito.
- **7.5 Resultado no estado e campo novo no `.proto`** — não feito. O estado `CONCLUIDA` já existe no enum, mas nada o define. O `EstadoResponse` não tem campo de resultado.

---

## Etapa 8 — Regras avançadas

Hoje: `Peca` já guarda "já mexeu", que o roque e o avanço duplo do peão vão usar.

- **8.1 Promoção** — não feito. Campo novo no `JogadaRequest` para a peça escolhida (depende de 3.8).
- **8.2 Roque** — não feito.
- **8.3 Empate por material insuficiente** — não feito.
- **8.4 Empate pela regra dos 50 lances** — não feito.
- **8.5 Empate por repetição de posição** — não feito.

---

## Etapa 9 — Robustez

- **9.1 Status gRPC restantes** — *parcial*. Hoje a conexão usa `INVALID_ARGUMENT` e `FAILED_PRECONDITION` (e `UNKNOWN` como padrão). O `NOT_FOUND` já é usado no `ObterEstadoDoJogo` (id de partida errado). Falta usá-lo no `EnviarJogada` e `PERMISSION_DENIED` (jogador que não pertence à partida).
- **9.2 `INTERNAL` para exceção inesperada** — não feito.
- **9.3 Falha do `Main` sem mensagem nula** — não feito. O `catch` ainda usa a mensagem da exceção direto; se ela for nula, o próprio `catch` quebra.
- **9.4 `.gitignore` e `target/` fora do versionamento** — não feito. Não existe `.gitignore`, e arquivos de `target/` aparecem como modificados no git, ou seja, estão versionados e precisam sair do controle.

---

## Etapa 10 — Testes

Hoje: as pastas `src/test/java` e `src/test/resources` existem, vazias. O `pom.xml` já tem JUnit 4.13.2, Mockito e `grpc-testing`.

- **10.1 Testes das regras** — não feito (posição, tabuleiro, cada peça, xeque, fim de jogo).
- **10.2 Testes de integração com o serviço em memória** — não feito (conexão, jogada válida e inválida, estado).
- A conexão de jogadores já foi testada à mão no Postman (Etapa 1), mas não há teste automatizado dela.
- **10.3 Testes manuais no Postman (todas as etapas feitas até aqui)** — não feito. Conectar dois jogadores; `ObterEstadoDoJogo` (um jogador, dois jogadores, id errado); `EnviarJogada` (e2 para e4 das brancas, jogada fora da vez, e7 para e5 das pretas, jogada ilegal como a1 para a5, texto de posição inválido, id de partida e de jogador errados); conferir o texto das peças e o turno depois de cada jogada.

---

## Etapa 11 — Entrega

- **11.1 Relatório técnico com as 6 seções do enunciado** — em andamento (conteúdo do relatório não conferido). As seis seções, conforme `docs/Orientacao_Trabalho_Pratico_Sistemas_Distribuidos.docx`:
  1. Introdução: o jogo escolhido e o motivo de usar RPC (gRPC).
  2. Arquitetura da solução: diagrama (cliente, servidor, stub/skeleton, middleware) e o fluxo de mensagens a cada jogada. Já existe `docs/figura2-diagrama-sequencia.png` (e `.svg`).
  3. Especificação das interfaces remotas: todos os métodos chamáveis (`ConectarJogador`, `EnviarJogada`, `ObterEstadoDoJogo`), com parâmetros, tipos de retorno e exceções tratadas (os status gRPC da Etapa 9).
  4. Tratamento de concorrência e estado do jogo: como o servidor mantém a consistência com vários clientes ao mesmo tempo (hoje, métodos `synchronized`).
  5. Manual de compilação e execução: como subir o servidor e rodar os clientes.
  6. Conclusão e autoavaliação: dificuldades do backend e o relato do uso de IA no frontend.
  - O rascunho do relatório está em `docs/Relatorio_Tecnico_SD_ABNT-2026.docx.pdf`; ele não foi lido para saber quais seções já estão prontas. O arquivo `instrucoes-da-1-entrega.txt` cobre só as seções 1 e 2.
- **11.2 Documentação dos formatos de texto do `.proto`** — não feito (formato do texto das peças, das posições, do turno e do estado).
- **11.3 Reflexão do servidor e porta configurável** — não feito. Hoje a porta (50040) está fixa no `Main`, e a reflexão gRPC não está ligada (por isso o Postman precisa importar o `.proto`).
- **11.4 Teste em duas máquinas e simulação de desconexão** — não feito. O enunciado pede o teste em processos diferentes no localhost e entre máquinas na rede local, e a simulação de desconexão e de exceção na chamada remota.
- **11.5 Concorrência conferida e explicada** — não feito. O enunciado dá 20% da nota a "gerenciamento de concorrência e estado" (sem inconsistências com acessos simultâneos). Hoje os métodos do serviço são `synchronized`. Falta conferir que isso cobre também a leitura do estado (Etapa 4) e a jogada (Etapa 6), e anotar a explicação para o relatório (seção 4) e para a defesa.

---

## Limpeza final (depois de tudo funcionando)

Combinado com o João: remover redundâncias e limpar o código só no fim. Anotadas até agora:
- `Utils` (e a dependência do Gson no `pom.xml`) não é mais usado em nenhum lugar, porque `Jogador.verificaNome` e `Partida.verificaSeJogoEstaCheio` passaram a devolver `boolean`.
- `ValidadorRegras.movimentoBispo` e `movimentoTorre`: condições repetidas e checagens que se sobrepõem (`!= 0` junto com valor absoluto igual; origem diferente do destino que a `Jogada` já garante).
- `ValidadorRegras.movimentoPeao`: a mesma conta de deslocamento é feita várias vezes; dá para guardar em variáveis locais.
- `Tabuleiro.desfazerMovimento`: `pecaCapturadaMomento != null ? pecaCapturadaMomento : null` equivale a só `pecaCapturadaMomento`; e o `;;` em `moverOuCapturar`.
- `Tabuleiro.getCasa(linha, coluna)` e `Posicao.getCasa()` repetem a mesma conta.
- `Posicao.hashCode`: os `Integer.valueOf` são desnecessários.
- `Partida.verificaSeJogoEstaCheio`: `? true : false` é redundante.
- Os quatro laços "para cada peça da cor, para cada casa de destino, existe lance legal?" do `verificaXequeMate` (e do afogamento, 7.4) são os mesmos: dá para virar um método só.
- Comentários desatualizados (por exemplo, no `Jogada`) e os que o João pediu para refazer.
- Mensagem "A partida ainda não começou." no `EnviarJogada` vale também para partida concluída (ajustar na Etapa 7).

## Ordem sugerida

1. Etapas 5 e 6 (movimentos e `EnviarJogada`): deixam o jogo jogável pelo Postman.
2. Etapas 7 e 8 (fim de jogo e regras avançadas).
3. Etapas 9, 10 e 11 (robustez, testes e entrega). O item 9.4 (`.gitignore`) pode ser feito a qualquer momento.
