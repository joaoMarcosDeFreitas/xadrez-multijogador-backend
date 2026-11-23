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

### B. Comentários e ordem dos métodos (revisados em 2026-11-23, no fim da limpeza)
Todos os arquivos do backend foram relidos: comentários desatualizados corrigidos, comentários dos métodos novos escritos e a ordem dos métodos conferida. Só comentário e ordem, sem mudar lógica.
- Fica de fora, porque é código e não comentário: o texto "carácteres" (sem acento em "caracteres") dentro da mensagem de nome inválido do serviço.

### C. Limpeza do código (feita em 2026-11-23, testada jogando: mate, afogamento, roque, peões e lances recusados)
1. `Utils` e a dependência do Gson: apagados.
2. `Partida.verificaSeJogoEstaCheio`: sem `? true : false`, retorna `boolean`.
3. `Tabuleiro.desfazerMovimento`: sem o ternário inútil.
4. `Posicao.hashCode`: sem `Integer.valueOf`.
5. `Posicao.getCasa()` removido (o `Tabuleiro.getCasa(linha, coluna)` é o único); `movimentoTorre` usa `equals`.
6. `Jogador.verificaNome`, `Partida.verificaSeJogoEstaCheio` e `Partida.verificaReconexao` devolvem `boolean`. (O `Boolean jaMexeuMomento` do `Tabuleiro` fica: lá o `null` significa "não há simulação".)
7. `Partida.chaveDaPosicao()`: a chave da repetição num método só.
8. `movimentoPeao`, `movimentoTorre`, `movimentoBispo`, `movimentoCavalo`, `movimentoRei` e `verificaRoque`: deslocamentos calculados uma vez, em variáveis locais.
9. Torre e bispo: mantidos. A checagem "origem diferente do destino" precisa ficar, porque o mate e o afogamento testam todas as casas, inclusive a da própria peça (a classe `Jogada` não participa disso: o serviço não a usa).
10. `ValidadorRegras.temLanceLegal`: o laço "alguma peça da cor tem lance legal?" num método só, usado pelo mate e pelo afogamento.
11. `XadrezServiceImpl`: `responderJogadaRecusada` (4 usos) e `responderErro` (substituiu os `onError` espalhados, os três `INTERNAL` e o antigo `responderServidorErroConectarJogador` com os códigos de texto "3" e "9"; o sucesso da conexão continua no `responderServidorSucessoConectarJogador`). Testado jogando depois do refactor: lances recusados, erro "Não é sua vez", roque, mate do louco (`XEQUE_MATE_PRETAS`) e jogada depois do fim (`FAILED_PRECONDITION`). **Backend fechado em 2026-11-23.**
12. Classe `Jogada` removida (o serviço nunca a usou; saiu também o `import` dela no serviço). Compila.

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
