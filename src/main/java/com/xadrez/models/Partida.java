package com.xadrez.models;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.EstadoDaPartidaEnum.EstadoDaPartida;
import com.xadrez.models.enums.PecaEnum.TipoPeca;
import com.xadrez.models.enums.ResultadoEnum.Resultado;

//uma partida de xadrez: guarda os jogadores (por cor), o estado, de quem é a vez e o tabuleiro. É o ponto de entrada do serviço: o tabuleiro e o validador ficam escondidos aqui dentro
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
    //tabuleiro da partida: nasce vazio e é montado quando o segundo jogador entra
    private final Tabuleiro tabuleiro;
    //confere se o movimento de uma peça é permitido pelas regras dela
    private final ValidadorRegras validador;
    private Resultado resultado;

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
	
	public Resultado getResultado() {
		return resultado;
	}

	//cor da vez (null enquanto a partida não começou)
	public CorPecas getTurno() {
		return turno;
	}

	//---- jogadores ----
	//a partida está cheia quando já tem 2 jogadores
	public Boolean verificaSeJogoEstaCheio() {
		return jogadores.size() >= 2 ? true : false;
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
	public Boolean verificaReconexao(Jogador jogadorBuscado) {
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

	//---- tabuleiro (a Partida repassa ao Tabuleiro e ao ValidadorRegras) ----
	//texto com todas as peças e as casas delas (formato casa,TIPO,COR separado por ;)
	public String getPecasTabuleiro() {
		return tabuleiro.getPecasTabuleiro();
	}

	//peça que está na casa, ou null se estiver vazia
	public Peca getPecaNaCasa(Posicao posicao) {
		return tabuleiro.getPecaNaCasa(posicao);
	}

	//diz se o movimento é permitido pelas regras da peça que está na origem (não move nada)
	public boolean validaMovimento(Posicao posicaoOrigem, Posicao posicaoDestino) {
		return validador.verificaLanceLegal(tabuleiro, posicaoOrigem, posicaoDestino) || validador.verificaRoqueSeguro(tabuleiro, posicaoOrigem, posicaoDestino);
	}

	//faz o movimento no tabuleiro. Devolve false se for recusado (origem vazia ou destino com peça da mesma cor)
	public boolean moverOuCapturar(Posicao posicaoOrigem, Posicao posicaoDestino) {
		return tabuleiro.moverOuCapturar(posicaoOrigem, posicaoDestino);
	}
	
	public void fimDeJogo(Resultado resultado) {
		this.estadoDaPartida = EstadoDaPartida.CONCLUIDA;
		this.resultado = resultado;
	}
	
	public boolean verificaXequeMate() {
		return validador.verificaXequeMate(tabuleiro, turno);
	}
	
	public boolean verificaAfogamento() {
		return validador.verificaAfogamento(tabuleiro, turno);
	}
	
	public boolean verificaPromocao(Posicao posicaoDaPeca) {
		return validador.verificaPromocao(tabuleiro, posicaoDaPeca);
	}
	
	public void promocao(Posicao posicaoDaPeca, String pecaEscolhida) {
		CorPecas corDaPeca = tabuleiro.getPecaNaCasa(posicaoDaPeca).getCor();
		
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
	
	public boolean verificaRoque(Posicao posicaoOrigem, Posicao posicaoDestino) {
		return validador.verificaRoque(tabuleiro, posicaoOrigem, posicaoDestino);
	}
	
	public void moverTorreRoque(Posicao posicaoOrigemRei, Posicao posicaoDestinoRei) {
		tabuleiro.moverTorreRoque(posicaoOrigemRei, posicaoDestinoRei);
	}
}