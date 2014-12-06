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

import java.util.Date;
import java.util.List;

import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;

public interface Consultas {

	
	public Reserva consultarReservaId(Integer id, Integer recId) throws ApplicationException, BusinessException;
	public Reserva consultarReservaPorNumero(Recurso r, Date fechaHoraInicio, Integer numero) throws BusinessException, UserException;

	public List<ReservaDTO> consultarReservasPorPeriodoEstado(Recurso recurso, VentanaDeTiempo periodo, Estado estado) throws BusinessException;
	public List<ReservaDTO> consultarReservasPorPeriodoEstado(Recurso recurso, VentanaDeTiempo periodo, List<Estado> estados) throws BusinessException;
	public List<ReservaDTO> consultarReservasEnEspera(Recurso recurso) throws BusinessException;
	public List<ReservaDTO> consultarReservasEnEsperaUtilizadas(Recurso recurso) throws BusinessException;
	public List<Reserva> consultarReservaDatos(List<DatoReserva> datos ,Recurso recurso);
	public List<Reserva> consultarReservasParaCancelar(List<DatoReserva> datos ,Recurso recurso);
	public List<Reserva> consultarReservaDatosHora(List<DatoReserva> datos ,Recurso recurso,Date fecha);	
	public List<ReservaDTO> consultarReservasUsadasPeriodo(Recurso recurso, VentanaDeTiempo periodo) throws BusinessException;
}
