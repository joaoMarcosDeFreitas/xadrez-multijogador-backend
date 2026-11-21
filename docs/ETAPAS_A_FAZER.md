# O que falta fazer — Back-end do Xadrez (servidor gRPC)

Atualizado em 2026-11-21, depois de combinar a lista final com o João.

**Como usar este arquivo**
- Trabalhar um item por vez. Quando um item estiver pronto e conferido (código lido), ele sai da lista.
- Backend sem IA: o Claude só orienta, confere e organiza comentários e ordem dos métodos. O código da lógica é escrito pelo João.
- Para conferir à mão: Postman gRPC em `localhost:<porta do Main>` (hoje 50040), sem TLS. Com a reflexão ligada, basta apontar o servidor e usar "server reflection".

## Cancelado (decisão do João em 2026-11-21)
- **Etapa de testes** (testes automáticos, testes manuais no Postman e o teste em duas máquinas): cancelada.
- **En passant:** não será feito.
- **Documentação** (relatório e formatos de texto do `.proto`): fora da lista.

## Já concluído
**Já concluído e removido:** Etapa 1 (pré-jogo e reconexão), testada no Postman em 2026-11-04. Etapa 2 (peças e coordenadas: texto da peça e `Posicao` com validação, texto, deslocamento e igualdade), conferida no código em 2026-11-17. Etapa 3 (tabuleiro: leitura de casa, posição inicial, caminho livre, mover/capturar, localizar o rei, texto do estado, simular/desfazer, trocar peça e ligação à `Partida`), conferida no código em 2026-11-18. Etapa 4 (`ObterEstadoDoJogo`: `NOT_FOUND` para partida inexistente, texto das peças, turno e estado), conferida no código em 2026-11-19. Etapa 9 (robustez: `NOT_FOUND`, `PERMISSION_DENIED` e `INTERNAL` nos métodos do serviço, `catch` do `Main` sem depender da mensagem, `.gitignore` com `target/` e `bin/` e `target/` fora do rastreamento do git), conferida em 2026-11-21; falta commitar o `.gitignore` junto com a remoção do `target/`. Etapa 8 (regras avançadas: promoção do peão, roque, empate por material insuficiente, empate pelos 50 lances e empate por repetição de posição), conferida no código em 2026-11-21; resultados novos no `Resultado`: `EMPATE_MATERIAL_INSUFICIENTE`, `EMPATE_50_LANCES` e `EMPATE_REPETICAO`; limites conhecidos: a repetição não diferencia direito de roque, e o en passant não existe (decidido que não entra). Etapa 7 (fim de jogo: `verificaXeque`, `verificaLanceLegal` aplicado nas jogadas, `verificaXequeMate`, `verificaAfogamento`, `Resultado` no estado e no `.proto`, partida vira `CONCLUIDA`), conferida no código em 2026-11-20. Sentido do resultado: `XEQUE_MATE_BRANCAS` = as brancas venceram; `XEQUE_MATE_PRETAS` = as pretas venceram; `EMPATE_AFOGAMENTO` = empate. Etapa 6 (`EnviarJogada`: conferências de partida, jogador, estado e vez; posições; validação pelas regras, movimento, troca de turno e resposta `jogadaValida` falsa com o motivo), conferida no código em 2026-11-19. Etapa 5 (movimentos: `ValidadorRegras` com peão, torre, bispo, rainha, cavalo e rei, e `Jogada` com `Posicao` e resultado da validação), conferida no código em 2026-11-19. O item 2.1 também já tinha saído da lista original. Etapa 11, partes de código: porta configurável pelo primeiro argumento do programa e reflexão gRPC ligada (o Postman descobre os métodos sozinho), e a concorrência conferida no código (os três métodos do serviço são `synchronized` na mesma instância, só existe uma `Partida`).

## Sugestões opcionais de limpeza (fora da lista combinada)

**Sugestões para o João decidir e fazer por conta própria** (backend sem IA: o Claude não altera a lógica do código do João). Já feito em 2026-11-21, sem mudar lógica: remoção do `;;` do `moverOuCapturar`, remoção do `import` sem uso (`ValidadorRegras`) no serviço, comentários e ordem dos métodos em todos os arquivos. Ainda sugeridas:
- `Utils` (e a dependência do Gson no `pom.xml`) não é mais usado em nenhum lugar, porque `Jogador.verificaNome` e `Partida.verificaSeJogoEstaCheio` passaram a devolver `boolean`.
- `ValidadorRegras.movimentoBispo` e `movimentoTorre`: condições repetidas e checagens que se sobrepõem (`!= 0` junto com valor absoluto igual; origem diferente do destino que a `Jogada` já garante).
- `ValidadorRegras.movimentoPeao`: a mesma conta de deslocamento é feita várias vezes; dá para guardar em variáveis locais.
- `Tabuleiro.desfazerMovimento`: `pecaCapturadaMomento != null ? pecaCapturadaMomento : null` equivale a só `pecaCapturadaMomento`.
- `Tabuleiro.getCasa(linha, coluna)` e `Posicao.getCasa()` repetem a mesma conta.
- `Posicao.hashCode`: os `Integer.valueOf` são desnecessários.
- `Partida.verificaSeJogoEstaCheio`: `? true : false` é redundante.
- `Partida.registrarRepeticao` e `verificaRepeticao` montam a mesma chave (peças + turno): dá para ter um método só para a chave.
- Os quatro laços "para cada peça da cor, para cada casa de destino, existe lance legal?" do `verificaXequeMate` (e do `verificaAfogamento`) são os mesmos: dá para virar um método só.
- Comentários desatualizados (por exemplo, no `Jogada`) e os que o João pediu para refazer.

## O que falta fazer (lista combinada com o João)

Ordem sugerida: 1, 2, 3, 4, 5. Os itens 2 a 5 já usam o padrão do item 1, e os itens 2 e 3 mexem no `.proto` juntos. Sempre que o `.proto` mudar, gerar as classes de novo (Project > Clean no Eclipse) e avisar o colega do front sobre os campos novos.

### 1. Padronizar as respostas do serviço
- **Hoje:** o `conectarJogador` usa `responderServidorErroConectarJogador` (com os códigos de texto "3" e "9" que viram status gRPC) e `responderServidorSucessoConectarJogador`. O `enviarJogada` repete cinco vezes o bloco que monta e envia a jogada recusada, e os três métodos repetem o `catch` com `INTERNAL`.
- **Fazer:** métodos de apoio no serviço, usados pelos três métodos:
  - **erro gRPC:** recebe o observador (pode ser `StreamObserver<?>`, serve para os três), o `Status` e a mensagem, e envia o `onError`. Substitui o método de erro da conexão e dispensa os códigos de texto;
  - **jogada recusada:** recebe o observador de `JogadaResponse` e o motivo, monta a resposta com `jogadaValida` falso, `onNext` e `onCompleted`;
  - **sucesso** (conexão, jogada aceita e estado), onde fizer sentido.
  Em cada ponto, a conferência vira "se falhou, chama o método e `return`".

### 2. Responder o fim de jogo
- **Hoje:** o `JogadaResponse` só tem `jogadaValida` e `mensagemRetorno`. Quando a jogada dá mate ou empate, a resposta ainda diz "Jogada feita!", e quem jogou só descobre que a partida acabou chamando o `ObterEstadoDoJogo`. O adversário também só descobre assim.
- **Fazer:** acrescentar ao `JogadaResponse` campos novos (números 3 e 4), por exemplo o estado da partida e o resultado, preenchidos depois da conferência de fim de jogo (resultado vazio enquanto a partida continua). Pensar também em um texto claro em `mensagemRetorno` quando a jogada encerra a partida.
- **Decidir:** quais campos entram e o texto da mensagem.

### 3. Avisar o xeque
- **Hoje:** ninguém avisa o cliente que um rei está em xeque. O teste de xeque existe só dentro do validador (`verificaXeque`, privado).
- **Fazer:** deixar esse teste acessível (método público no validador e um repasse na `Partida`, no mesmo desenho dos outros), e acrescentar um campo `emXeque` no `EstadoResponse` (número 5) e no `JogadaResponse`, dizendo se a cor que vai jogar agora está em xeque. Só vale com a partida em andamento (antes de começar, o turno é nulo).
- **Decidir:** tipo do campo (booleano é o mais simples) e se vai nos dois lugares.

### 4. Revanche
- **Hoje:** existe uma única partida por servidor, e `CONCLUIDA` é definitivo: para jogar de novo é preciso reiniciar o servidor.
- **Fazer:** uma chamada nova no `.proto` (por exemplo `Revanche`, com a sua requisição e resposta) e, na `Partida`, um método que reinicia o jogo mantendo os dois jogadores: tabuleiro remontado na posição inicial (o campo `tabuleiro` é `final`, então é preciso limpar e remontar o que existe, ou criar um método no `Tabuleiro` para isso), estado em andamento, turno das brancas, resultado nulo, contagem dos 50 lances zerada e o mapa de repetições limpo. Regras da chamada: só vale com a partida `CONCLUIDA` (senão `FAILED_PRECONDITION`), só para jogador da partida (senão `PERMISSION_DENIED`), e o método do serviço é `synchronized` como os outros.
- **Decidir:** se os dois jogadores precisam aceitar (cada um pede e reinicia quando os dois pedirem) ou se basta um pedir; se as cores trocam; o que o estado mostra enquanto há um pedido pendente.

### 5. Desconexão: explicar e dar um futuro
- **Como funciona hoje (para saber explicar na defesa):** as chamadas gRPC são independentes e o servidor não percebe quando um cliente cai. Todo o estado da partida fica guardado no servidor. Quem volta chama `conectarJogador` com o mesmo id e recebe de volta o id, a partida, o nome e a cor, mesmo com a sala cheia (a reconexão vem antes da conferência de sala cheia). O jogo continua de onde parou.
- **O problema:** se o jogador nunca voltar, a partida fica parada para sempre, e o adversário espera sem saber.
- **Futuro proposto:** definir uma política de abandono. Guardar o instante da última atividade da vez (ou de cada jogador); se o jogador da vez passar mais de N segundos sem agir, a partida termina por abandono, com vitória do adversário (novo valor em `Resultado`, por exemplo `ABANDONO_BRANCAS` e `ABANDONO_PRETAS`). A conferência do prazo pode ser feita quando qualquer chamada chega, sem criar thread, para manter a concorrência simples. O `ObterEstadoDoJogo` passa a mostrar o resultado, e a revanche (item 4) vale depois disso. A reconexão dentro do prazo continua restaurando tudo.
- **Decidir:** o valor de N, o que conta como atividade, e o nome do resultado novo.
