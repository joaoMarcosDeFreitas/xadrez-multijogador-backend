package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

//o tabuleiro: 64 casas numa matriz [linha][coluna]. Cada casa tem uma Peca ou null (vazia). A linha 1 do xadrez é o índice 0 (brancas)
//aqui ficam só as operações sobre as casas (ler, montar, mover). As regras de cada peça ficam no ValidadorRegras
public class Tabuleiro {
	//---- dados do tabuleiro ----
	//matriz privada: só os métodos desta classe alteram as casas, ninguém de fora escreve direto
	private final Peca[][] tabuleiro;

	//---- guardam a última simulação de movimento (só uma por vez) ----
	//peça que estava no destino da simulação (null se estava vazio)
	private Peca pecaCapturadaMomento;
	//"já mexeu" da peça antes da simulação (null = não há simulação para desfazer)
	private Boolean jaMexeuMomento;
	//casa de origem da simulação
	private Posicao posicaoOrigemMomento;
	//casa de destino da simulação
	private Posicao posicaoDestinoMomento;

	//---- construtor ----
	//cria o tabuleiro vazio (todas as casas null). As peças só entram em escreverPecasIniciais
	public Tabuleiro() {
		super();
		this.tabuleiro = new Peca[8][8];
	}

	//---- leitura ----
	//devolve a peça da casa, ou null se estiver vazia. Quem chama precisa conferir null antes de usar a peça
	public Peca getPecaNaCasa(Posicao posicao) {
		return this.tabuleiro[posicao.getLinha()][posicao.getColuna()];
	}

	//converte linha e coluna (índices) no texto da casa (ex.: 0 e 4 viram "e1"). É o caminho inverso do construtor da Posicao
	public String getCasa(int linha, int coluna) {
		return Character.toString((coluna) + 'a') + Character.toString((linha) + '1');
	}

	//devolve o texto das peças no formato casa,TIPO,COR separado por ; (ex.: a1,TORRE,BRANCAS;b1,CAVALO,BRANCAS). Casa vazia não entra. É o texto que vai para o cliente
	public String getPecasTabuleiro() {
		String retornoTodasPecas = "";
		for(int i = 0; i < this.tabuleiro.length; i++) {
			for(int j = 0; j < this.tabuleiro[i].length; j++) {
				Peca pecaAtual = this.tabuleiro[i][j];
				//só casas com peça entram no texto; o ; vai antes de cada peça, menos da primeira
				if(pecaAtual != null) retornoTodasPecas += retornoTodasPecas.isBlank() ? getCasa(i, j) + "," + pecaAtual.getNomePeca().toUpperCase() + "," + pecaAtual.getCorPeca().toUpperCase() : ";" + getCasa(i, j) + "," + pecaAtual.getNomePeca().toUpperCase() + "," + pecaAtual.getCorPeca().toUpperCase();
			}
		}
		return retornoTodasPecas;
	}

	//devolve o texto da casa do rei da cor pedida. O rei sempre existe, então se não achar lança exceção (tabuleiro inconsistente)
	public String procuraRei(CorPecas cor) {
		for(int i = 0; i < this.tabuleiro.length; i++) {
			for(int j = 0; j < this.tabuleiro[i].length; j++) {
				Peca peca = this.tabuleiro[i][j];
				if(peca != null) {
					if(peca.getTipoPeca().equals(TipoPeca.REI) && peca.getCor().equals(cor)) return getCasa(i, j);
				}
			}
		}
		throw new IllegalStateException("O rei não foi encontrado! Um erro ocorreu");
	}

	//---- montagem das peças ----
	//monta as 32 peças da posição inicial. É chamado pela Partida quando o segundo jogador entra
	public void escreverPecasIniciais() {
		//ordem das peças da linha de trás, da coluna a até h (o índice do vetor é a coluna). É a mesma para brancas e pretas
		TipoPeca[] tipoPeca = {
				TipoPeca.TORRE, 
				TipoPeca.CAVALO, 
				TipoPeca.BISPO, 
				TipoPeca.RAINHA, 
				TipoPeca.REI, 
				TipoPeca.BISPO, 
				TipoPeca.CAVALO, 
				TipoPeca.TORRE
				};
		
		//uma volta por coluna: cada volta cria as 4 peças daquela coluna, todas como objetos novos (cada peça tem o seu próprio jaMexeu)
		for(int j = 0; j < this.tabuleiro.length; j++) {
			//cria peças brancas (linha 0 = peça maior, linha 1 = peão)
			escreverPeca(new Peca(tipoPeca[j], CorPecas.BRANCAS, false), 0, j);
			escreverPeca(new Peca(TipoPeca.PEAO, CorPecas.BRANCAS, false), 1, j);
		
			//cria peças pretas (linha 7 = peça maior, linha 6 = peão)
			escreverPeca(new Peca(TipoPeca.PEAO, CorPecas.PRETAS, false), 6, j);
			escreverPeca(new Peca(tipoPeca[j], CorPecas.PRETAS, false), 7, j);
		}
	}

	//coloca uma peça na casa indicada, no lugar da que estava (usado na promoção do peão)
	public void trocarPeca(Posicao posicao, Peca peca) {
		escreverPeca(peca, posicao.getLinha(), posicao.getColuna());
	}

	//---- movimentos ----
	//devolve true se NÃO há peça entre a origem e o destino (as duas pontas não contam). Só vale para reta ou diagonal (torre, bispo, rainha, avanço duplo do peão, roque)
	public boolean verificarCasasEntreOrigemDestino(Posicao posicaoOrigem, Posicao posicaoDestino) {
		//passo de cada volta (-1, 0 ou 1 em cada direção): o signum reduz o deslocamento a uma casa por vez
		int linhasAndadas = Integer.signum(posicaoOrigem.getDeslocamentoLinha(posicaoDestino));
		int colunasAndadas = Integer.signum(posicaoOrigem.getDeslocamentoColuna(posicaoDestino));
		
		//começa na primeira casa depois da origem
		int linhaAtual = posicaoOrigem.getLinha() + linhasAndadas;
		int colunaAtual = posicaoOrigem.getColuna() + colunasAndadas;
		
		//anda casa por casa até chegar no destino (o destino não é conferido)
		while(linhaAtual != posicaoDestino.getLinha() || colunaAtual != posicaoDestino.getColuna()) {
			//achou peça no caminho: o caminho não está livre
			if(this.tabuleiro[linhaAtual][colunaAtual] != null) return false;
			linhaAtual += linhasAndadas;
			colunaAtual += colunasAndadas;
		}
		return true;
	}

	//move a peça da origem para o destino (se o destino tem peça adversária, é captura). Só recusa origem vazia e destino com peça da mesma cor; as regras de cada peça ficam no validador. Devolve false se recusou
	public boolean moverOuCapturar(Posicao posicaoOrigem, Posicao posicaoDestino) {
		//peça que está se movendo e peça do destino (null se o destino estiver vazio)
		Peca peca = this.tabuleiro[posicaoOrigem.getLinha()][posicaoOrigem.getColuna()];
		Peca pecaDestino = this.tabuleiro[posicaoDestino.getLinha()][posicaoDestino.getColuna()];
		
		//recusa: não há peça na origem
		if(peca == null) return false;
		
		//recusa: o destino já tem peça da mesma cor (só compara a cor quando o destino não é null)
		if(pecaDestino != null && peca.getCor().equals(pecaDestino.getCor())) return false;
		
		//coloca a peça no destino (substitui a capturada, se houver) e esvazia a origem
		escreverPeca(peca, posicaoDestino.getLinha(), posicaoDestino.getColuna());
		escreverPeca(null, posicaoOrigem.getLinha(), posicaoOrigem.getColuna());
		//marca que a peça já se moveu (roque e avanço duplo do peão)
		peca.setJaMexeu(true);
		return true;
	}

	//move a torre do roque, depois que o rei já foi movido: ela sai do canto (coluna 7 ou 0) e vai para a casa por onde o rei passou
	public void moverTorreRoque(Posicao posicaoOrigemRei, Posicao posicaoDestinoRei) {
		//+1 = roque para a direita, -1 = roque para a esquerda
		int sentidoDoMovimentoRei = Integer.signum(posicaoOrigemRei.getDeslocamentoColuna(posicaoDestinoRei));
		//a torre termina na casa por onde o rei passou (a coluna do destino do rei menos o sentido)
		int colunaDestinoTorre = posicaoDestinoRei.getColuna() - sentidoDoMovimentoRei;
		
		//casa de onde a torre sai: o canto da linha do rei (coluna 7 para a direita, coluna 0 para a esquerda)
		Posicao posicaoOrigemTorre;
		
		if(sentidoDoMovimentoRei == 1) {
			posicaoOrigemTorre = new Posicao(getCasa(posicaoOrigemRei.getLinha(), 7));
		}else {
			posicaoOrigemTorre = new Posicao(getCasa(posicaoOrigemRei.getLinha(), 0));
		}
		
		//casa para onde a torre vai
		Posicao posicaoDestinoTorre = new Posicao(getCasa(posicaoOrigemRei.getLinha(), colunaDestinoTorre));
		//move a torre (o moverOuCapturar também marca que ela já se moveu)
		moverOuCapturar(posicaoOrigemTorre, posicaoDestinoTorre);
	}

	//faz o movimento de teste, guardando antes o que for preciso para desfazer. Devolve false se o movimento foi recusado (e então não há nada para desfazer). Só há uma simulação por vez
	public boolean simulacaoMovimento(Posicao posicaoOrigem, Posicao posicaoDestino) {
		
		//lê ANTES de mover o "já mexeu" da peça da origem e a peça do destino (depois de mover esses dados se perdem)
		Boolean jaMexeuLocal = this.tabuleiro[posicaoOrigem.getLinha()][posicaoOrigem.getColuna()] != null ? this.tabuleiro[posicaoOrigem.getLinha()][posicaoOrigem.getColuna()].getJaMexeu() : null;
		Peca pecaCapturadaLocal = this.tabuleiro[posicaoDestino.getLinha()][posicaoDestino.getColuna()];
		
		//se o movimento for recusado, sai sem guardar nada
		if(!moverOuCapturar(posicaoOrigem, posicaoDestino)) return false;
		
		//movimento aceito: guarda os dados para o desfazerMovimento
		this.pecaCapturadaMomento = pecaCapturadaLocal;
		this.jaMexeuMomento = jaMexeuLocal;
		this.posicaoOrigemMomento = posicaoOrigem;
		this.posicaoDestinoMomento = posicaoDestino;
		return true;
	}

	//desfaz a última simulação: devolve a peça para a origem, recoloca a peça capturada (ou deixa a casa vazia) e restaura o "já mexeu". Se não há simulação pendente, não faz nada
	public void desfazerMovimento() {		
		//só age se há simulação pendente
		if(this.jaMexeuMomento != null && this.posicaoOrigemMomento != null && this.posicaoDestinoMomento != null) {
			//a peça que está no destino volta para a origem
			this.tabuleiro[this.posicaoOrigemMomento.getLinha()][this.posicaoOrigemMomento.getColuna()] = this.tabuleiro[this.posicaoDestinoMomento.getLinha()][this.posicaoDestinoMomento.getColuna()];
			//o destino volta a ter a peça capturada (ou fica vazio)
			this.tabuleiro[this.posicaoDestinoMomento.getLinha()][this.posicaoDestinoMomento.getColuna()] = this.pecaCapturadaMomento;
			//a peça volta a ter o "já mexeu" que tinha antes
			this.tabuleiro[this.posicaoOrigemMomento.getLinha()][this.posicaoOrigemMomento.getColuna()].setJaMexeu(this.jaMexeuMomento);
			
			//limpa os dados: não há mais simulação pendente
			this.pecaCapturadaMomento = null;
			this.jaMexeuMomento = null;
			this.posicaoOrigemMomento = null;
			this.posicaoDestinoMomento = null;
		}
	}

	//---- apoio ----
	//único ponto que grava na matriz (usado na montagem, no movimento e na troca de peça). Grava sem conferir nada: as conferências ficam em quem chama. Passar null esvazia a casa
	private void escreverPeca(Peca peca, int linha, int coluna) {
		this.tabuleiro[linha][coluna] = peca;
	}
}
