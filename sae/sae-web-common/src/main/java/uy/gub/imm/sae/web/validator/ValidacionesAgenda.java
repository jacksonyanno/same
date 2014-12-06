/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2009  IMM - Intendencia Municipal de Montevideo
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.

 * SAME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uy.gub.imm.sae.web.validator;

import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.exceptions.NombreInvalidoException;

public class ValidacionesAgenda extends BaseMBean {

	private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTVUWXYZ0123456789_";
	private static final String NUMEROS="1234567890";

	
	public boolean validarNombre(String nombre) throws NombreInvalidoException{
		
		nombre = nombre.toUpperCase();
		String caracValidos = VALID_CHARS;
		boolean nombreValido = true;

		
		for (int i = 0; (i < nombre.length() && nombreValido); i++) {
			char caracter = nombre.charAt(i);

			// Se chequea que el primer caracter no sea un numero
			if (i==0 && NUMEROS.indexOf(caracter) != -1){
				nombreValido = false;
				throw new NombreInvalidoException(getI18N().getText("validation.name.character.first.invalid"));
			}
			
			// Se chequea si los caracterss son validos
			if (caracValidos.indexOf(caracter) == -1) {
				nombreValido = false;
				throw new NombreInvalidoException(getI18N().getText("validation.name.character.invalid"));
			}
		}

		return nombreValido;
		
	}
}
