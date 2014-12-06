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

package uy.gub.imm.sae.business.api;

import java.util.List;

import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.entity.Llamada;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

public interface Llamadas {
	
	public Reserva siguienteEnEspera(Recurso recurso, Integer puesto) throws BusinessException;
	public Reserva volverALlamar(Recurso recurso, Integer puesto, Reserva reserva) throws BusinessException;

	public List<ReservaDTO> obtenerReservasEnEspera(Recurso recurso, List<Estado> estados) throws BusinessException;
	public List<Llamada> obtenerLlamadas(List<Recurso> recursos, Integer cantLlamadas) throws BusinessException;
	public void marcarAsistencia(Recurso recursoMarcado, Reserva r) throws BusinessException;
	public void marcarInasistencia(Recurso recursoMarcado, Reserva r) throws BusinessException;
}
