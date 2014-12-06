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

package uy.gub.imm.sae.business.api.acciones.ejb;

import java.util.Map;

import uy.gub.imm.sae.business.api.acciones.ejb.exception.InvalidParametersException;
import uy.gub.imm.sae.business.api.acciones.ejb.exception.UnexpectedAccionException;

public interface EjecutorAccion {

	/**
	 * Ejectua la accion de nombre @param nombreAccion
	 *  
	 * @param nombreAccion
	 * @param params es un hash que tiene en forma de <key,value> los datos ingresados por el usuario que realizó la reserva
	 * de forma tal que key = nombre del parametro y value = el valor ingresado. Habrán tantos parámetros como los defindos por la accion concreta.
	 * Adicionalmente se espera que este hash tenga otra serie de parametros extras: 
	 * <"RECURSO", uy.gub.sae.acciones.business.dto.RecursoDTO> Viene dado por programacion
	 * <"RESERVA, uy.gub.imm.sae.business.dto.ReservaDTO> Viene dado por programacion
	 * 
	 * @return
	 * @throws UnexpectedAccionException
	 * @throws InvalidParametersException
	 */
	public ResultadoAccion ejecutar(String nombreAccion, Map<String, Object> params) 
		throws UnexpectedAccionException, InvalidParametersException;
	
}
