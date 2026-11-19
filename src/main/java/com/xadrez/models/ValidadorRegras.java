package com.xadrez.models;

import com.xadrez.models.enums.CorPecaEnum.CorPecas;
import com.xadrez.models.enums.PecaEnum.TipoPeca;

//confere se o movimento de uma peça é permitido pelas regras dela. Só responde sim ou não: quem move é o Tabuleiro, e a jogada só é feita depois de aprovada aqui
public class ValidadorRegras {
	//ponto de entrada: olha a peça que está na origem e chama a regra do tipo dela. Origem vazia = false. A rainha vale pela torre ou pelo bispo
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

	//peão: anda reto (1 casa, ou 2 na primeira jogada) só para casa vazia; captura 1 casa na diagonal para a frente, só se houver peça no destino. As brancas sobem (linha +) e as pretas descem (linha -)
	private boolean movimentoPeao(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		Peca peca = tabuleiro.getPecaNaCasa(posicaoOrigem);
		
		//brancas: avançam uma casa, na mesma coluna, para casa vazia
		if(peca.getCor().equals(CorPecas.BRANCAS) && posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == 1 && posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 0 && tabuleiro.getPecaNaCasa(posicaoDestino) == null) return true;
		
		//brancas: na primeira jogada podem avançar duas casas, com o caminho livre e o destino vazio
		if(peca.getCor().equals(CorPecas.BRANCAS) && !peca.getJaMexeu() && posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == 2 && posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 0 && tabuleiro.getPecaNaCasa(posicaoDestino) == null && tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		//brancas: capturam uma casa na diagonal para a frente, se houver peça no destino
		if(peca.getCor().equals(CorPecas.BRANCAS) && (posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == 1 && (posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == -1 || posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 1)) && tabuleiro.getPecaNaCasa(posicaoDestino) != null) return true;
		
		//pretas: avançam uma casa (descendo), na mesma coluna, para casa vazia
		if(peca.getCor().equals(CorPecas.PRETAS) && posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == -1 && posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 0 && tabuleiro.getPecaNaCasa(posicaoDestino) == null) return true;
		
		//pretas: na primeira jogada podem avançar duas casas, com o caminho livre e o destino vazio
		if(peca.getCor().equals(CorPecas.PRETAS) && !peca.getJaMexeu() && posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == -2 && posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 0 && tabuleiro.getPecaNaCasa(posicaoDestino) == null && tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		//pretas: capturam uma casa na diagonal para a frente, se houver peça no destino
		if(peca.getCor().equals(CorPecas.PRETAS) && (posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == -1 && (posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == -1 || posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 1)) && tabuleiro.getPecaNaCasa(posicaoDestino) != null) return true;
		
		return false;
	}

	//torre: anda em linha reta (só a linha ou só a coluna muda), para outra casa, com o caminho livre
	private boolean movimentoTorre(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//reta: um dos deslocamentos é 0, e a origem não pode ser igual ao destino; depois confere o caminho
		if((posicaoOrigem.getDeslocamentoLinha(posicaoDestino) == 0 || posicaoOrigem.getDeslocamentoColuna(posicaoDestino) == 0) && !posicaoOrigem.getCasa().equals(posicaoDestino.getCasa())) 
			if(tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		return false;
	}

	//bispo: anda na diagonal (os dois deslocamentos têm o mesmo tamanho e nenhum é 0), com o caminho livre
	private boolean movimentoBispo(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		//diagonal: os dois deslocamentos diferentes de 0 e com o mesmo tamanho (valor absoluto); depois confere o caminho
		if((posicaoOrigem.getDeslocamentoLinha(posicaoDestino) != 0 && posicaoOrigem.getDeslocamentoColuna(posicaoDestino) != 0) && (Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino)) == Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)))) 
			if(tabuleiro.verificarCasasEntreOrigemDestino(posicaoOrigem, posicaoDestino)) return true;
		
		return false;
	}

	//cavalo: anda em L (um deslocamento de 2 casas e o outro de 1) e pula as peças do meio, por isso não confere o caminho (o parâmetro tabuleiro não é usado aqui)
	private boolean movimentoCavalo(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		if((Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino)) == 1 && Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)) == 2) || (Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino)) == 2 && Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)) == 1)) return true;
		
		return false;
	}

	//rei: anda uma casa em qualquer direção (reta ou diagonal). O roque ainda não existe
	private boolean movimentoRei(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		if((Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino)) == 1 && Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)) == 0) || (Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino)) == 1 && Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)) == 1) || (Math.abs(posicaoOrigem.getDeslocamentoLinha(posicaoDestino)) == 0 && Math.abs(posicaoOrigem.getDeslocamentoColuna(posicaoDestino)) == 1)) return true;
		
		return false;
	}

	private boolean verificaXeque(Tabuleiro tabuleiro, CorPecas corDoRei) {
		Posicao posicaoDoRei = new Posicao(tabuleiro.procuraRei(corDoRei));
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Posicao posicaoDaPeca = new Posicao(tabuleiro.getCasa(i, j));
				Peca peca = tabuleiro.getPecaNaCasa(posicaoDaPeca);
				if(peca != null) {
					if(!peca.getCor().equals(corDoRei)) {
						if(validaMovimento(tabuleiro, posicaoDaPeca, posicaoDoRei)) return true;
					}
				}
			}
		}
		return false;
	}

	public boolean verificaLanceLegal(Tabuleiro tabuleiro, Posicao posicaoOrigem, Posicao posicaoDestino) {
		CorPecas corDeQuemJogou = tabuleiro.getPecaNaCasa(posicaoOrigem).getCor();
		
		if(!validaMovimento(tabuleiro, posicaoOrigem, posicaoDestino)) return false;
			
		if(!tabuleiro.simulacaoMovimento(posicaoOrigem, posicaoDestino)) return false;
		
		if(verificaXeque(tabuleiro, corDeQuemJogou)) {
			tabuleiro.desfazerMovimento();
			return false;
		}
		tabuleiro.desfazerMovimento();
		return true;
	}
	
	public boolean verificaXequeMate(Tabuleiro tabuleiro, CorPecas corDoRei) {
		if(!verificaXeque(tabuleiro, corDoRei)) return false;
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Posicao posicaoDaPeca = new Posicao(tabuleiro.getCasa(i, j));
				Peca peca = tabuleiro.getPecaNaCasa(posicaoDaPeca);
				if(peca != null) {
					if(peca.getCor().equals(corDoRei)) {
						for(int k = 0; k < 8; k++) {
							for(int l = 0; l < 8; l++) {
								Posicao posicaoDestino = new Posicao(tabuleiro.getCasa(k, l));
								if(verificaLanceLegal(tabuleiro, posicaoDaPeca, posicaoDestino)) return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
}
