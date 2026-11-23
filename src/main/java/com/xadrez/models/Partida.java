package com.xadrez.models;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.EstadoDaPartidaEnum.EstadoDaPartida;
import com.xadrez.models.enums.PecaEnum.TipoPeca;
import com.xadrez.models.enums.ResultadoEnum.Resultado;

//uma partida de xadrez: guarda os jogadores (por cor), o estado, de quem é a vez, o resultado e o tabuleiro
//é o ponto de entrada do serviço: o tabuleiro e o validador ficam escondidos aqui dentro, e o serviço só fala com a Partida
public class Partida {
	//---- dados da partida ----
	//identificador da partida (gerado quando o servidor liga)
	private String idPartida;
	//jogadores da partida, indexados pela cor (no máximo 2)
	private final Map<CorPecas, Jogador> jogadores = new EnumMap<>(CorPecas.class);
	//aguardando, em andamento ou concluída
	private EstadoDaPartida estadoDaPartida;
	//cor de quem joga agora (null até a partida começar)
	private CorPecas turno;
	//como a partida terminou (null enquanto não terminou)
	private Resultado resultado;
	//tabuleiro da partida: nasce vazio e é montado quando o segundo jogador entra
	private final Tabuleiro tabuleiro;
	//confere as regras do xadrez (movimento, xeque, mate, afogamento, roque, promoção)
	private final ValidadorRegras validador;

	//---- dados das regras de empate ----
	//jogadas seguidas sem captura e sem movimento de peão (regra dos 50 lances)
	private int contadorJogadas;
	//quantas vezes cada posição já apareceu (chave = peças + vez de jogar). Serve para o empate por repetição
	private Map<String, Integer> repeticoesDaMesaJogada = new HashMap<>();

	//---- construtor ----
	//a partida nasce aguardando jogadores, com o tabuleiro vazio
	public Partida(String idPartida) {
		this.idPartida = idPartida;
		this.estadoDaPartida = EstadoDaPartida.AGUARDANDO;
		this.tabuleiro = new Tabuleiro();
		this.validador = new ValidadorRegras();
	}

	//---- consulta dos dados da partida ----
	//id da partida
	public String getIdPartida() {
		return idPartida;
	}

	//estado atual da partida
	public EstadoDaPartida getEstadoDaPartida() {
		return estadoDaPartida;
	}

	//como a partida terminou (null enquanto não terminou)
	public Resultado getResultado() {
		return resultado;
	}

	//cor da vez (null enquanto a partida não começou)
	public CorPecas getTurno() {
		return turno;
	}

	//---- jogadores ----
	//a partida está cheia quando já tem 2 jogadores
	public boolean verificaSeJogoEstaCheio() {
		return jogadores.size() >= 2;
	}

	//lista as cores que ainda não têm jogador (as duas, só uma ou nenhuma)
	public ArrayList<CorPecas> corDisponivel() {
		//lista que vai guardar as cores livres
		ArrayList<CorPecas> listaDeDisponibilidade = new ArrayList<>();
		//percorre as duas cores e guarda as que ainda não têm jogador
		for(CorPecas cor : CorPecas.values()) {
			if(!jogadores.containsKey(cor)) {
				listaDeDisponibilidade.add(cor);
			}
		}
		return listaDeDisponibilidade;
	}

	//coloca o jogador na cor escolhida. Ignora (sem avisar) se a partida já tem 2 jogadores, se o jogador já entrou ou se a cor está ocupada. Quando o segundo jogador entra, a partida começa
	public void adicionarCorPecasJogadores(CorPecas cor, Jogador jogadorParaAdicionar) {
		if(jogadores.size() < 2) {
			for(Map.Entry<CorPecas, Jogador> jogador : jogadores.entrySet()) {
				if(jogador.getValue().getId().equals(jogadorParaAdicionar.getId()) || jogador.getKey().equals(cor)) {
					return;
				}
			}
			//adiciona a cor e o jogador ao mapa de jogadores
			this.jogadores.put(cor, jogadorParaAdicionar);
			//segundo jogador entrou: a partida começa, as peças são montadas e as brancas jogam primeiro
			if(jogadores.size() == 2) {
				this.estadoDaPartida = EstadoDaPartida.EM_ANDAMENTO;
				this.tabuleiro.escreverPecasIniciais();
				this.turno = CorPecas.BRANCAS;
			}
		}
	}

	//diz se o jogador já está na partida (compara pelo id). É assim que o serviço reconhece uma reconexão
	public boolean verificaReconexao(Jogador jogadorBuscado) {
		//percorre os jogadores da partida
		for(Jogador jogador : jogadores.values()) {
			//mesmo id = é o mesmo jogador
			if(jogadorBuscado.getId().equals(jogador.getId())) {
				return true;
			}
		}
		return false;
	}

	//acha o jogador pelo id e devolve a cor e o jogador cadastrado (null se não achar). O Entry dá a cor (getKey) e os dados registrados (getValue)
	public Map.Entry<CorPecas, Jogador> getJogador(Jogador jogadorBuscado) {
		for(Map.Entry<CorPecas, Jogador> jogador : jogadores.entrySet()) {
			if(jogador.getValue().getId().equals(jogadorBuscado.getId())) {
				return jogador;
			}
		}
		return null;
	}

	//devolve o jogador que está na cor pedida (null se a cor não tem jogador)
	public Jogador getJogador(CorPecas corJogador) {
		return jogadores.get(corJogador);
	}

	//devolve a cor do jogador com esse id, ou null se o id não é de nenhum jogador da partida
	public CorPecas getCorJogadorPeloId(String id) {
		for(Map.Entry<CorPecas, Jogador> jogador : jogadores.entrySet()) {
			if(jogador.getValue().getId().equals(id)) return jogador.getKey();
		}
		return null;
	}

	//devolve a lista dos jogadores que estão na partida
	public ArrayList<Jogador> listaJogadores() {
		//inicia o array de jogadores
		ArrayList<Jogador> jogadoresNaLista = new ArrayList<>();
		//percorre a lista de jogadores
		for(Jogador jogador : jogadores.values()) {
			//adiciona no array de jogadores os jogadores na lista
			jogadoresNaLista.add(jogador);
		}
		//retorna os jogadores
		return jogadoresNaLista;
	}

	//---- vez de jogar ----
	//passa a vez para a outra cor. Só deve ser chamado com a partida em andamento (antes disso o turno ainda é null)
	public void trocarTurno() {
		this.turno = this.turno.oposta();
	}

	//---- tabuleiro e regras (a Partida repassa ao Tabuleiro e ao ValidadorRegras) ----
	//texto com todas as peças e as casas delas (formato casa,TIPO,COR separado por ;)
	public String getPecasTabuleiro() {
		return tabuleiro.getPecasTabuleiro();
	}

	//peça que está na casa, ou null se estiver vazia
	public Peca getPecaNaCasa(Posicao posicao) {
		return tabuleiro.getPecaNaCasa(posicao);
	}

	//diz se o lance é permitido: lance legal normal (formato da peça e rei fora de xeque depois dele) ou roque seguro. Não move nada
	public boolean validaMovimento(Posicao posicaoOrigem, Posicao posicaoDestino) {
		return validador.verificaLanceLegal(tabuleiro, posicaoOrigem, posicaoDestino) || validador.verificaRoqueSeguro(tabuleiro, posicaoOrigem, posicaoDestino);
	}

	//faz o movimento no tabuleiro. Devolve false se for recusado (origem vazia ou destino com peça da mesma cor)
	public boolean moverOuCapturar(Posicao posicaoOrigem, Posicao posicaoDestino) {
		return tabuleiro.moverOuCapturar(posicaoOrigem, posicaoDestino);
	}

	//diz se o lance é um roque (formato e peças). Tem que ser perguntado ANTES de mover o rei, porque depois o "já mexeu" dele muda
	public boolean verificaRoque(Posicao posicaoOrigem, Posicao posicaoDestino) {
		return validador.verificaRoque(tabuleiro, posicaoOrigem, posicaoDestino);
	}

	//move a torre do roque, depois que o rei já foi movido
	public void moverTorreRoque(Posicao posicaoOrigemRei, Posicao posicaoDestinoRei) {
		tabuleiro.moverTorreRoque(posicaoOrigemRei, posicaoDestinoRei);
	}

	//diz se há um peão na casa que chegou à última linha e precisa ser promovido
	public boolean verificaPromocao(Posicao posicaoDaPeca) {
		return validador.verificaPromocao(tabuleiro, posicaoDaPeca);
	}

	//troca o peão da casa pela peça escolhida (rainha, torre, bispo ou cavalo, sem diferenciar maiúscula). Qualquer outro texto, inclusive vazio, vira rainha
	public void promocao(Posicao posicaoDaPeca, String pecaEscolhida) {
		//a peça nova tem a mesma cor do peão
		CorPecas corDaPeca = tabuleiro.getPecaNaCasa(posicaoDaPeca).getCor();
		
		//escolhe a peça pelo texto (sem diferenciar maiúscula nem espaços nas pontas); qualquer texto fora da lista cai no default e vira rainha
		switch(pecaEscolhida.toLowerCase().trim()) {
			case "rainha":
				tabuleiro.trocarPeca(posicaoDaPeca, new Peca(TipoPeca.RAINHA, corDaPeca, true));
				break;
			case "torre":
				tabuleiro.trocarPeca(posicaoDaPeca, new Peca(TipoPeca.TORRE, corDaPeca, true));
				break;
			case "bispo":
				tabuleiro.trocarPeca(posicaoDaPeca, new Peca(TipoPeca.BISPO, corDaPeca, true));
				break;
			case "cavalo":
				tabuleiro.trocarPeca(posicaoDaPeca, new Peca(TipoPeca.CAVALO, corDaPeca, true));
				break;
			default:
				tabuleiro.trocarPeca(posicaoDaPeca, new Peca(TipoPeca.RAINHA, corDaPeca, true));
				break;
		}
	}

	//---- fim de jogo e regras de empate ----
	//encerra a partida: o estado vira concluída e guarda como ela terminou
	public void fimDeJogo(Resultado resultado) {
		this.estadoDaPartida = EstadoDaPartida.CONCLUIDA;
		this.resultado = resultado;
	}

	//a cor da vez está em xeque-mate? (chamado depois de passar a vez)
	public boolean verificaXequeMate() {
		return validador.verificaXequeMate(tabuleiro, turno);
	}

	//a cor da vez está afogada? (chamado depois de passar a vez)
	public boolean verificaAfogamento() {
		return validador.verificaAfogamento(tabuleiro, turno);
	}

	//empate: sobraram só os reis, ou os reis e uma única peça menor
	public boolean verificaMaterialInsuficiente() {
		return validador.verificaMaterialInsuficiente(tabuleiro);
	}

	//regra dos 50 lances: captura ou movimento de peão zera a contagem, qualquer outra jogada soma 1
	public void contarJogada(boolean houveCapturaOuEPeao) {
		//captura ou movimento de peão: a contagem recomeça do zero
		if(houveCapturaOuEPeao) {
			this.contadorJogadas = 0;
			return;
		}
		//qualquer outra jogada: soma 1
		contadorJogadas++;
	}

	//empate: chegou a 100 jogadas (50 de cada jogador) sem captura e sem movimento de peão
	public boolean verificaLimiteDeJogadas() {
		return contadorJogadas >= 100;
	}

	//anota que a posição de agora apareceu mais uma vez. Chamado depois de passar a vez, porque a vez faz parte da chave
	public void registrarRepeticao() {
		//a chave da posição é o texto das peças mais a cor da vez (posições iguais geram a mesma chave)
		String chave = chaveDaPosicao();
		
		//quantas vezes essa posição já apareceu (0 se é a primeira)
		int vezes = repeticoesDaMesaJogada.getOrDefault(chave, 0);
		//grava a contagem nova
		repeticoesDaMesaJogada.put(chave, vezes + 1);
	}

	//empate: a posição de agora já apareceu 3 vezes ou mais
	public boolean verificaRepeticao() {
		//a mesma chave usada no registrarRepeticao
		String chave = chaveDaPosicao();
		
		//quantas vezes essa posição já apareceu
		int vezes = repeticoesDaMesaJogada.getOrDefault(chave, 0);
		return vezes >= 3;
	}
	
	private String chaveDaPosicao() {
		return getPecasTabuleiro() + turno;
	}
}