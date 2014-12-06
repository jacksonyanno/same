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

import uy.gub.imm.sae.common.DisponibilidadReserva;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.RolException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Plantilla;
import uy.gub.imm.sae.entity.Recurso;

public interface Disponibilidades {
	public void generarDisponibilidadesNuevas(Recurso r, Date fecha, Date horaDesde, Date horaHasta, Integer frecuencia, Integer cupo) throws UserException, ApplicationException;
	public void generarDisponibilidades(Recurso r, Date f, VentanaDeTiempo periodo) throws UserException, ApplicationException;	
	public void generarPatronSemana(Recurso r, VentanaDeTiempo semana, VentanaDeTiempo periodo) throws BusinessException, UserException, ApplicationException;	
	public List<Disponibilidades> consultarDisponibilidadesSolapadas(Recurso r, Plantilla p, VentanaDeTiempo v);
	public void generarDisponibilidaesAutomaticamente();
	public void eliminarDisponibilidades(Recurso r, VentanaDeTiempo v) throws BusinessException, UserException;
	public List<DisponibilidadReserva> obtenerDisponibilidadesReservas(Recurso r, VentanaDeTiempo v) throws BusinessException, RolException;
	public void modificarCupoDeDisponibilidad(Disponibilidad d) throws UserException, BusinessException;
	public void modificarCupoPeriodo(Disponibilidad d) throws UserException, BusinessException;
	public Integer cantDisponibilidadesDia(Recurso r, Date f) throws UserException, BusinessException;
	public Date ultFechaGenerada(Recurso r) throws UserException, BusinessException;
}
