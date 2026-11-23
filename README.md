# Xadrez multijogador: servidor gRPC

Servidor de xadrez para dois jogadores, em Java 21 com gRPC. Ele guarda a partida, valida cada jogada pelas regras do xadrez e responde aos clientes. Trabalho de Sistemas Distribuídos (5º semestre, UCSal).

## Como funciona
- **Uma partida por servidor.** A partida é criada quando o servidor liga. O primeiro jogador que entra fica com as brancas e o segundo, com as pretas; a partida começa quando o segundo entra.
- **Tudo é decidido no servidor.** O cliente só envia a origem e o destino (por exemplo `e2` e `e4`). O servidor confere de quem é a vez, se a jogada é legal (inclusive se deixa o próprio rei em xeque) e devolve o resultado.
- **Concorrência.** Os três métodos do serviço são `synchronized` sobre a mesma instância: só uma chamada por vez mexe no estado da partida, o que evita condição de corrida.
- **Reconexão.** O servidor não percebe quando um cliente cai; o estado fica guardado nele. Quem volta chama `ConectarJogador` com o mesmo id e recebe de novo cor e partida, mesmo com a sala cheia.

## Contrato (`src/main/proto/xadrez.proto`)
| Método | O que faz |
|---|---|
| `ConectarJogador` | Entra na partida (ou reconecta, se o id já é conhecido). Devolve id, partida, nome e cor. |
| `EnviarJogada` | Envia origem, destino e, se houver promoção, a peça escolhida. Jogada ilegal volta como resposta normal com `jogadaValida = false` e o motivo. |
| `ObterEstadoDoJogo` | Devolve as peças (`casa,TIPO,COR;...`), de quem é a vez, o estado da partida e o resultado. |

Erros de contexto viram status gRPC: `NOT_FOUND` (partida inexistente), `PERMISSION_DENIED` (jogador que não é da partida), `FAILED_PRECONDITION` (partida fora de andamento, sala cheia ou fora da vez), `INVALID_ARGUMENT` (nome inválido) e `INTERNAL` (erro inesperado).

## Regras implementadas
Movimento de todas as peças, captura, xeque, xeque-mate, afogamento, roque, promoção do peão, empate por material insuficiente, empate pela regra dos 50 lances e empate por repetição de posição. **Não há en passant.**

Resultados possíveis no campo `resultado`: `XEQUE_MATE_BRANCAS` (as brancas venceram), `XEQUE_MATE_PRETAS` (as pretas venceram), `EMPATE_AFOGAMENTO`, `EMPATE_MATERIAL_INSUFICIENTE`, `EMPATE_50_LANCES` e `EMPATE_REPETICAO`.

## Estrutura do código (`src/main/java/com/xadrez`)
| Pacote | Conteúdo |
|---|---|
| `program` | `Main`: liga o servidor (porta 50040, ou a do primeiro argumento) e a reflexão gRPC. |
| `grpc` | `XadrezServiceImpl`: os três métodos remotos. |
| `models` | `Partida` (estado e regras de fim de jogo), `Tabuleiro` (as 64 casas), `ValidadorRegras` (movimentos, xeque, mate, afogamento, roque, promoção), `Posicao`, `Peca` e `Jogador`. |
| `models/enums` | Cor, tipo de peça, estado da partida e resultado. |

## Como rodar
Requisitos: JDK 21 e Maven (ou Eclipse com suporte a Maven).
1. Importe como projeto Maven (a pasta que tem o `pom.xml`) e faça Project > Clean, que gera as classes do `.proto`.
2. Rode a classe `com.xadrez.program.Main`. Deve aparecer "Servidor rodando na porta 50040".
3. Para outra porta, passe-a como primeiro argumento do programa (no Eclipse: Run Configurations > Arguments).

O servidor fala **gRPC nativo**. Para testar sem o front, use um cliente gRPC com reflexão (por exemplo, o Postman, em `localhost:50040`, sem TLS). O navegador não fala gRPC nativo: o front usa gRPC-Web por um proxy Envoy (porta 8080), que repassa ao servidor. Detalhes no repositório do front.

## Observação
O servidor guarda uma única partida em memória: depois que ela termina, é preciso reiniciar o servidor para jogar de novo.
