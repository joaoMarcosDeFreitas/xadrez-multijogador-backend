package com.xadrez.utils;

import com.google.gson.JsonObject;

//classe para reaproveitar códigos que são usados em vários lugares e não pertencem a um grupo particular
public class Utils {
	//monta um JSON com um código e uma descrição. Não está em uso no momento (as verificações passaram a devolver boolean)
	public static JsonObject montaRespotaJsonObject(String codigo, String descricao) {
		//cria um JSON para montar a resposta (informar ao código principal que está inválido)
		JsonObject resposta = new JsonObject();

		//adiciona no JSON as chaves de código de erro e descrição
		resposta.addProperty("código", codigo);
		resposta.addProperty("descrição", descricao);
				
		//retorna o erro ao código principal
		return resposta;
	}
}
