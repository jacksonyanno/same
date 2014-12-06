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

package uy.gub.imm.sae.common.exception;

import java.util.List;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class ValidacionPorCampoException extends ValidacionException {


	private static final long serialVersionUID = 1526751593174792441L;

	/**
	 * Las listas deben tener el mismo tamaño y el mismo debe ser > 0.
	 * @param codigoError
	 * @param nombreCampos es el nombre de los campos para los que hay algun mensaje de error en la validacion
	 * @param mensajes     son los mensajes de error conrespondientes a cada campo.
	 */
	public ValidacionPorCampoException(String codigoError, List<String> nombreCampos, List<String> mensajes) {
		super(codigoError,null, nombreCampos, mensajes);
		
		if (nombreCampos == null ||
			mensajes ==  null ||
			nombreCampos.size() != mensajes.size() ||
			nombreCampos.size() == 0) {
			throw new RuntimeException("La lista de mensajes y nombre de campos deben tener el mismo tamaño y a lo menos un elemento");
		}
	}

}
