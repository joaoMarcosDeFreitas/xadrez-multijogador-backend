package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

//confere as regras do xadrez. Só responde sim ou não (e, nas simulações, desfaz o que testou): quem move de verdade é o Tabuleiro
//a Partida é quem usa esta classe; o serviço nunca fala direto com ela
public class ValidadorRegras {
	//---- regras de movimento ----
	//ponto de entrada: olha a peça que está na origem e chama a regra do tipo dela. Origem vazia = false. A rainha vale pela torre ou pelo bispo
	//só olha o formato do movimento e o caminho. Não confere xeque nem roque (ver verificaLanceLegal e verificaRoqueSeguro)
	public boolean validaMovimento(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		Peca peca = tabuleiro.getPecaNaCasa(posicaoOrigem);
		if(peca == null) return false;
		
		switch(peca.getTipoPeca()) {
			case TipoPeca.PEAO:
				return movimentoPeao(tabuleiro, posicaoOrigem, posicaoDestino);
			case TipoPeca.TORRE:
				return movimentoTorre(tabuleiro, posicaoOrigem, posicaoDestino);
			case TipoPeca.BISPO:
				return movimentoBispo(tabuleiro, posicaoOrigem, posicaoDestino);
			case TipoPeca.RAINHA:
				return movimentoTorre(tabuleiro, posicaoOrigem, posicaoDestino) || movimentoBispo(tabuleiro, posicaoOrigem, posicaoDestino);
			case TipoPeca.CAVALO:
				return movimentoCavalo(tabuleiro, posicaoOrigem, posicaoDestino);
			case TipoPeca.REI:
				return movimentoRei(tabuleiro, posicaoOrigem, posicaoDestino);
			default: 
				return false;
		}
	}

	//---- xeque e fim de jogo ----
	//diz se o lance é legal: o movimento é permitido pela peça e o rei de quem jogou não fica em xeque depois dele
	//testa fazendo o lance de mentira no tabuleiro (simulação) e desfaz sempre antes de devolver
	public boolean verificaLanceLegal(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//lê a cor ANTES de simular (depois do lance a origem fica vazia)
		CorPecas corDeQuemJogou = tabuleiro.getPecaNaCasa(posicaoOrigem).getCor();
		
		//primeiro o formato do movimento da peça
		if(!validaMovimento(tabuleiro, posicaoOrigem, posicaoDestino)) return false;
			
		//faz o lance de mentira; se o tabuleiro recusar (destino com peça da mesma cor), nada foi movido e não há o que desfazer
		if(!tabuleiro.simulacaoMovimento(posicaoOrigem, posicaoDestino)) return false;
		
		//com o lance feito: se o rei de quem jogou ficou em xeque o lance é ilegal. Nos dois casos o lance é desfeito antes de devolver
		if(verificaXeque(tabuleiro, corDeQuemJogou)) {
			tabuleiro.desfazerMovimento();
			return false;
		}
		tabuleiro.desfazerMovimento();
		return true;
	}

	//xeque-mate: o rei da cor está em xeque e nenhuma peça dela tem lance legal (com o rei em xeque, um lance legal é uma saída)
	public boolean verificaXequeMate(Tabuleiro tabuleiro, CorPecas corDoRei) {
		//sem xeque não é mate
		if(!verificaXeque(tabuleiro, corDoRei)) return false;
		
		//em xeque e sem nenhum lance legal: é mate
		return !temLanceLegal(tabuleiro, corDoRei);
	}

	//afogamento (empate): o rei da cor NÃO está em xeque, mas nenhuma peça dela tem lance legal
	public boolean verificaAfogamento(Tabuleiro tabuleiro, CorPecas corPeca) {
		//com xeque não é afogamento (seria xeque-mate)
		if(verificaXeque(tabuleiro, corPeca)) return false;
		
		//fora de xeque e sem nenhum lance legal: é afogamento
		return !temLanceLegal(tabuleiro, corPeca);	
	}

	//empate por material insuficiente (versão simples): só os reis, ou os reis e uma única peça menor (bispo ou cavalo, de qualquer cor)
	public boolean verificaMaterialInsuficiente(Tabuleiro tabuleiro) {
		//conta bispos e cavalos juntos (das duas cores)
		int contagemCavaloEBispo = 0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Posicao posicaoDaPeca = new Posicao(tabuleiro.getCasa(i, j));
				Peca peca = tabuleiro.getPecaNaCasa(posicaoDaPeca);
				if(peca != null) {
					//com peão, torre ou rainha em jogo ainda dá para dar mate
					if(peca.getTipoPeca().equals(TipoPeca.PEAO) || peca.getTipoPeca().equals(TipoPeca.RAINHA) || peca.getTipoPeca().equals(TipoPeca.TORRE)) return false;
					//bispo ou cavalo: soma na contagem
					if(peca.getTipoPeca().equals(TipoPeca.CAVALO) || peca.getTipoPeca().equals(TipoPeca.BISPO)) contagemCavaloEBispo++;
				}
			}
		}
		//0 ou 1 peça menor = material insuficiente
		if(contagemCavaloEBispo == 0 || contagemCavaloEBispo == 1) return true;
		return false;
	}

	//---- regras especiais (promoção e roque) ----
	//diz se há um peão na casa e ele chegou à última linha da cor dele (brancas na linha 7, pretas na linha 0), então deve ser promovido
	public boolean verificaPromocao(Tabuleiro tabuleiro, Posicao posicaoDaPeca) {
		Peca peca = tabuleiro.getPecaNaCasa(posicaoDaPeca);
		//só interessa se há um peão na casa
		if(peca == null || !peca.getTipoPeca().equals(TipoPeca.PEAO)) return false;
		
		//brancas promovem na linha 7
		if(peca.getCor().equals(CorPecas.BRANCAS) && posicaoDaPeca.getLinha() == 7) return true;
		
		//pretas promovem na linha 0
		if(peca.getCor().equals(CorPecas.PRETAS) && posicaoDaPeca.getLinha() == 0) return true;
		
		return false;
	}

	//diz se o movimento tem o formato do roque e as peças estão em condição: rei e torre sem ter se movido, rei andando duas colunas na mesma linha e o caminho do rei até a torre livre
	//não confere xeque (ver verificaRoqueSeguro). Fica separado do movimentoRei de propósito: o verificaXeque usa o validaMovimento (que chama o movimentoRei), e um rei a duas casas não ataca
	public boolean verificaRoque(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//deslocamento em colunas até o destino (com sinal), calculado uma vez só
		int deslocamentoColuna = posicaoOrigem.getDeslocamentoColuna(posicaoDestino);
		Peca pecaMovida = tabuleiro.getPecaNaCasa(posicaoOrigem);
		//tem que ser um rei que ainda não se moveu
		if(!pecaMovida.getTipoPeca().equals(TipoPeca.REI) || pecaMovida.getJaMexeu()) return false;
		
		//tem que ser na mesma linha
		if(posicaoOrigem.getDeslocamentoLinha(posicaoDestino) != 0) return false;
		
		//e andando exatamente duas colunas (para a direita ou para a esquerda)
		if(deslocamentoColuna != 2 && deslocamentoColuna != -2) return false;

		//para a direita: a torre está na coluna 7. Para a esquerda (else): na coluna 0
		if(Integer.signum(deslocamentoColuna) == 1) {
			//casa do canto direito, na linha do rei
			Posicao posicaoDaTorre = new Posicao(tabuleiro.getCasa(posicaoOrigem.getLinha(), 7));
			Peca peca = tabuleiro.getPecaNaCasa(posicaoDaTorre);
			
			if(peca == null) return false;
			
			if(!peca.getCor().equals(pecaMovida.getCor()) || !peca.getTipoPeca().equals(TipoPeca.TORRE) || peca.getJaMexeu() || !tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDaTorre)) return false;
		}else {
			//casa do canto esquerdo, na linha do rei
			Posicao posicaoDaTorre = new Posicao(tabuleiro.getCasa(posicaoOrigem.getLinha(), 0));
			Peca peca = tabuleiro.getPecaNaCasa(posicaoDaTorre);
			
			if(peca == null) return false;
			
			if(!peca.getCor().equals(pecaMovida.getCor()) || !peca.getTipoPeca().equals(TipoPeca.TORRE) || peca.getJaMexeu() || !tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDaTorre)) return false;
		}
		
		return true;
	}

	//o roque completo: o formato do verificaRoque e, além disso, o rei não está em xeque, não passa por casa atacada e não termina em casa atacada
	public boolean verificaRoqueSeguro(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		Peca pecaMovida = tabuleiro.getPecaNaCasa(posicaoOrigem);

		//primeiro o formato e as peças do roque
		if(!verificaRoque(tabuleiro, posicaoOrigem, posicaoDestino)) return false;
		
		//o rei não pode estar em xeque agora
		if(verificaXeque(tabuleiro, pecaMovida.getCor())) return false;
	
		//casa por onde o rei passa (uma coluna na direção do roque): põe o rei lá de teste, vê o xeque e desfaz (o desfazer vem nos dois casos)
		tabuleiro.simulacaoMovimento(posicaoOrigem, new Posicao(tabuleiro.getCasa(posicaoOrigem.getLinha(), posicaoOrigem.getColuna() + Integer.signum(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)))));
		if(verificaXeque(tabuleiro, pecaMovida.getCor())) {
			tabuleiro.desfazerMovimento();
			return false;
		}
		tabuleiro.desfazerMovimento();
		
		//casa onde o rei termina: mesmo teste
		tabuleiro.simulacaoMovimento(posicaoOrigem, posicaoDestino);
		if(verificaXeque(tabuleiro, pecaMovida.getCor())) {
			tabuleiro.desfazerMovimento();
			return false;
		}
		tabuleiro.desfazerMovimento();
		
		return true;
	}

	//---- apoio (usados só aqui dentro): a regra de cada peça, o teste de xeque e a busca de lance legal ----
	//peão: anda reto (1 casa, ou 2 na primeira jogada) só para casa vazia; captura 1 casa na diagonal para a frente, só se houver peça no destino. As brancas sobem (linha +) e as pretas descem (linha -)
	private boolean movimentoPeao(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		Peca peca = tabuleiro.getPecaNaCasa(posicaoOrigem);
		//deslocamentos até o destino (com sinal: o sentido importa para o peão) e se o destino está vazio, calculados uma vez só
		int deslocamentoLinha = posicaoOrigem.getDeslocamentoLinha(posicaoDestino);
		int deslocamentoColuna = posicaoOrigem.getDeslocamentoColuna(posicaoDestino);
		boolean destinoVazio = tabuleiro.getPecaNaCasa(posicaoDestino) == null;
		
		//brancas: avançam uma casa, na mesma coluna, para casa vazia
		if(peca.getCor().equals(CorPecas.BRANCAS) && deslocamentoLinha == 1 && deslocamentoColuna == 0 && destinoVazio) return true;
		
		//brancas: na primeira jogada podem avançar duas casas, com o caminho livre e o destino vazio
		if(peca.getCor().equals(CorPecas.BRANCAS) && !peca.getJaMexeu() && deslocamentoLinha == 2 && deslocamentoColuna == 0 && destinoVazio && tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		//brancas: capturam uma casa na diagonal para a frente, se houver peça no destino
		if(peca.getCor().equals(CorPecas.BRANCAS) && (deslocamentoLinha == 1 && (deslocamentoColuna == -1 || deslocamentoColuna == 1)) && !destinoVazio) return true;
		
		//pretas: avançam uma casa (descendo), na mesma coluna, para casa vazia
		if(peca.getCor().equals(CorPecas.PRETAS) && deslocamentoLinha == -1 && deslocamentoColuna == 0 && destinoVazio) return true;
		
		//pretas: na primeira jogada podem avançar duas casas, com o caminho livre e o destino vazio
		if(peca.getCor().equals(CorPecas.PRETAS) && !peca.getJaMexeu() && deslocamentoLinha == -2 && deslocamentoColuna == 0 && destinoVazio && tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		//pretas: capturam uma casa na diagonal para a frente, se houver peça no destino
		if(peca.getCor().equals(CorPecas.PRETAS) && (deslocamentoLinha == -1 && (deslocamentoColuna == -1 || deslocamentoColuna == 1)) && !destinoVazio) return true;
		
		return false;
	}

	//torre: anda em linha reta (só a linha ou só a coluna muda), para outra casa, com o caminho livre
	private boolean movimentoTorre(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//deslocamentos até o destino, calculados uma vez só
		int deslocamentoLinha = posicaoOrigem.getDeslocamentoLinha(posicaoDestino);
		int deslocamentoColuna = posicaoOrigem.getDeslocamentoColuna(posicaoDestino);
		//reta: um dos deslocamentos é 0. A origem não pode ser igual ao destino (o mate e o afogamento testam todas as casas, inclusive a da própria peça); depois confere o caminho
		if((deslocamentoLinha == 0 || deslocamentoColuna == 0) && !posicaoOrigem.equals(posicaoDestino)) 
			if(tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		return false;
	}

	//bispo: anda na diagonal (os dois deslocamentos têm o mesmo tamanho e nenhum é 0), com o caminho livre
	private boolean movimentoBispo(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//deslocamentos até o destino, calculados uma vez só
		int deslocamentoLinha = posicaoOrigem.getDeslocamentoLinha(posicaoDestino);
		int deslocamentoColuna = posicaoOrigem.getDeslocamentoColuna(posicaoDestino);
		//diagonal: os dois deslocamentos diferentes de 0 e com o mesmo tamanho (valor absoluto); depois confere o caminho
		if((deslocamentoLinha != 0 && deslocamentoColuna != 0) && (Math.abs(deslocamentoLinha) == Math.abs(deslocamentoColuna))) 
			if(tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		return false;
	}

	//cavalo: anda em L (um deslocamento de 2 casas e o outro de 1) e pula as peças do meio, por isso não confere o caminho (o parâmetro tabuleiro não é usado aqui)
	private boolean movimentoCavalo(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//tamanho do deslocamento em linhas e em colunas (sem o sinal), calculado uma vez só
		int linhas = Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino));
		int colunas = Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino));
		//L: (1 linha e 2 colunas) ou (2 linhas e 1 coluna), em valor absoluto
		if((linhas == 1 && colunas == 2) || (linhas == 2 && colunas == 1)) return true;
		
		return false;
	}

	//rei: anda uma casa em qualquer direção (reta ou diagonal). O roque tem regra própria (verificaRoque). O parâmetro tabuleiro não é usado aqui
	private boolean movimentoRei(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//tamanho do deslocamento em linhas e em colunas (sem o sinal), calculado uma vez só
		int linhas = Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino));
		int colunas = Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino));
		//uma casa: (1 linha e 0 colunas), (1 e 1) ou (0 e 1), em valor absoluto
		if((linhas == 1 && colunas == 0) || (linhas == 1 && colunas == 1) || (linhas == 0 && colunas == 1)) return true;
		
		return false;
	}

	//diz se o rei da cor está em xeque: alguma peça adversária consegue ir até a casa dele (o validaMovimento responde "essa peça chega naquela casa?")
	private boolean verificaXeque(Tabuleiro tabuleiro, CorPecas corDoRei) {
		//casa do rei da cor (o procuraRei devolve o texto da casa)
		Posicao posicaoDoRei = new Posicao(tabuleiro.procuraRei(corDoRei));
		
		//percorre as 64 casas
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Posicao posicaoDaPeca = new Posicao(tabuleiro.getCasa(i, j));
				Peca peca = tabuleiro.getPecaNaCasa(posicaoDaPeca);
				if(peca != null) {
					//só as peças adversárias podem dar xeque
					if(!peca.getCor().equals(corDoRei)) {
						//essa peça consegue ir até a casa do rei? Então o rei está em xeque
						if(validaMovimento(tabuleiro, posicaoDaPeca, posicaoDoRei)) return true;
					}
				}
			}
		}
		return false;
	}
	
	//diz se alguma peça da cor tem pelo menos um lance legal (usado pelo xeque-mate e pelo afogamento). Testa cada peça da cor contra as 64 casas de destino, inclusive a casa dela mesma
	private boolean temLanceLegal(Tabuleiro tabuleiro, CorPecas cor) {
		//percorre as 64 casas procurando as peças da cor
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Posicao posicaoDaPeca = new Posicao(tabuleiro.getCasa(i, j));
				Peca peca = tabuleiro.getPecaNaCasa(posicaoDaPeca);
				if(peca != null) {
					//só as peças da cor pedida
					if(peca.getCor().equals(cor)) {
						//testa levar essa peça para cada uma das 64 casas
						for(int k = 0; k < 8; k++) {
							for(int l = 0; l < 8; l++) {
								Posicao posicaoDestino = new Posicao(tabuleiro.getCasa(k, l));
								//achou um lance legal: não precisa procurar mais
								if(verificaLanceLegal(tabuleiro, posicaoDaPeca, posicaoDestino)) return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
