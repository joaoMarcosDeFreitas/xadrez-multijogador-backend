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

## Decisão de 2026-11-23: projeto finalizado, só polimento
Nada de funcionalidade nova. Só limpeza de código, clareza de redundâncias e ajustes de leitura, **na ordem abaixo**. Só se mexe no que o João pedir diretamente (backend sem IA: o Claude orienta, confere e ajusta comentários; o código é do João). Exceção: se faltar algo para os requisitos do trabalho ou para jogar de redes diferentes (ver o item A).

## Etapas a seguir (em ordem)

### A. Conferir os requisitos (antes de tudo)
- [ ] **O João testa a rede na faculdade hoje (2026-11-23); fora do trabalho do Claude.** Montagem: o notebook do João é o servidor (Java + Envoy/Docker) e os PCs da faculdade são os clientes (só o front, com o endereço `http://IP-DO-NOTEBOOK:8080` na tela de entrada); plano de segurança: duas abas em `localhost` no notebook + vídeo gravado. Ponto aberto: se o Wi-Fi e o cabo da faculdade são a mesma rede. Jogar em duas máquinas/redes diferentes funciona? O servidor escuta em todas as interfaces (porta 50040, ou a do argumento), mas a internet exige liberar a porta no roteador/firewall ou usar um túnel; o front passa pelo Envoy (porta 8080). Decidir como será na apresentação (mesma rede local ou túnel) e testar uma vez.
- [ ] Os três métodos do enunciado (`ConectarJogador`, `EnviarJogada`, `ObterEstadoDoJogo`), concorrência com `synchronized` e reconexão estão cobertos (conferido no código em 2026-11-23).

### B. Comentários (feito em 2026-11-23)
Revisados todos os arquivos do backend. Corrigidos só comentários, sem tocar em código: `CONCLUIDA` dizia "ainda não é usado"; `Peca.jaMexeu` dizia "no futuro, para o roque"; explicação do `verificaRoque` (o `verificaXeque` usa o `validaMovimento`, não o `movimentoRei` direto); `catch` do serviço com o comentário dentro do bloco; "os três métodos" no cabeçalho do serviço; comentário da conferência da partida no `obterEstadoDoJogo`.
- Fica de fora, porque é código e não comentário: o texto "Nome inválido... carácteres" (acento de "caracteres") dentro da mensagem de erro do serviço.

### C. Limpeza do código (o João faz, o Claude confere depois; jogar uma partida depois de cada item)
Do mais seguro ao que mexe mais:
1. `Utils` (e a dependência do Gson no `pom.xml`) não é mais usado em nenhum lugar: apagar os dois.
2. `Partida.verificaSeJogoEstaCheio`: `? true : false` é redundante (e o retorno pode ser `boolean`).
3. `Tabuleiro.desfazerMovimento`: `pecaCapturadaMomento != null ? pecaCapturadaMomento : null` equivale a só `pecaCapturadaMomento`.
4. `Posicao.hashCode`: os `Integer.valueOf` são desnecessários.
5. `Tabuleiro.getCasa(linha, coluna)` e `Posicao.getCasa()` repetem a mesma conta.
6. `Jogador.verificaNome`, `Partida.verificaSeJogoEstaCheio` e `Partida.verificaReconexao` devolvem `Boolean` (objeto); podem ser `boolean`, como os outros. (Não mexer no `Boolean jaMexeuMomento` do `Tabuleiro`: lá o `null` significa "não há simulação".)
7. `Partida.registrarRepeticao` e `verificaRepeticao` montam a mesma chave (peças + turno): um método só para a chave.
8. `ValidadorRegras.movimentoPeao`: a mesma conta de deslocamento é feita várias vezes; guardar em variáveis locais.
9. `ValidadorRegras.movimentoBispo` e `movimentoTorre`: condições repetidas e checagens que se sobrepõem (`!= 0` junto com valor absoluto igual; origem diferente do destino que a `Jogada` já garante).
10. `ValidadorRegras.verificaXequeMate` e `verificaAfogamento`: os laços "para cada peça da cor, para cada casa de destino, existe lance legal?" são os mesmos: virar um método só.
11. `XadrezServiceImpl.enviarJogada`: o bloco que monta e envia a jogada recusada se repete 4 vezes, e o `catch` com `INTERNAL` se repete nos três métodos: métodos de apoio.
12. `Jogada`: a classe não é usada pelo serviço (ele valida e responde direto). Decidir se fica ou sai.

### C-final. Comentários e ordem dos métodos (o Claude faz, só depois de terminar o C)
Combinado em 2026-11-23: durante a limpeza o João mexe **só no código** (sem escrever comentário e sem mudar a posição de método). Ao terminar o C, o Claude: escreve/ajusta os comentários do que foi alterado (métodos novos, como a chave da posição, e os que ficaram sem sentido), coloca cada método novo no lugar certo da seção e reconfere a ordem dos métodos em todos os arquivos. Só comentário e ordem, sem mudar lógica.

### D. Revisitar o front (depois do backend 100%)
- Conferir se está pronto para a apresentação: rodar servidor + Envoy + `npm start` e jogar uma partida inteira em duas abas (mate, empate, promoção, roque, reconexão).
- Front hoje: build e 10 testes passam; alterações ainda sem commit e o Matheus ainda não foi avisado.
- Não entregar `tools/servidor-teste` como servidor.

### E. Relatório e entrega
- Seções 2 (cliente é Angular + Envoy), 5 e 6 do relatório, referências, Quadro 4 e Apêndice A ainda estão com texto-modelo. Detalhes em `..\docs\PROXIMOS_PASSOS.md`.
- Commitar o `.gitignore` e a remoção de `target/`.

## Fora do escopo (não vai ser feito; citar como trabalhos futuros no relatório)
- **Responder o fim de jogo na resposta da jogada:** hoje `JogadaResponse` só tem `jogadaValida` e `mensagemRetorno`; quem descobre o fim da partida é o `ObterEstadoDoJogo` (campo `resultado`).
- **Avisar o xeque:** nenhum campo de xeque no contrato; o teste existe só dentro do validador.
- **Revanche:** `CONCLUIDA` é definitivo; para jogar de novo é preciso reiniciar o servidor.
- **Abandono/desconexão:** o servidor não percebe quando um cliente cai; todo o estado fica no servidor, e quem volta chama `conectarJogador` com o mesmo id e recebe cor e partida de volta (a reconexão vem antes da conferência de sala cheia). Limite conhecido: se o jogador nunca voltar, a partida fica parada. Futuro: prazo por inatividade e resultado `ABANDONO_*`.
- **Padronizar as respostas** vira só a limpeza C11.
- Já cancelados antes: en passant, etapa de testes, documentação do `.proto`.
