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
import java.util.Map;

import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.AutocompletarException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.ServicioPorRecurso;

public interface AgendarReservas {
	public Agenda consultarAgendaPorNombre(String nombre) throws ApplicationException, BusinessException;
	public Recurso consultarRecursoPorNombre(Agenda a, String nombre) throws ApplicationException, BusinessException;
	public Recurso consultarRecursoPorId(Agenda a, Integer id) throws ApplicationException, BusinessException;
	public List<Agenda> consultarAgendas() throws ApplicationException, BusinessException;
	public List<Recurso> consultarRecursos(Agenda a) throws ApplicationException, BusinessException;
	
	public Boolean agendaActiva(Agenda a);
	public VentanaDeTiempo obtenerVentanaCalendarioIntranet(Recurso r) throws BusinessException;
	public VentanaDeTiempo obtenerVentanaCalendarioInternet(Recurso r) throws BusinessException;
	public List<Integer> obtenerCuposPorDia(Recurso r, VentanaDeTiempo v) throws BusinessException;
	public List<Disponibilidad> obtenerDisponibilidades(Recurso r, VentanaDeTiempo v) throws BusinessException;
	public Reserva marcarReserva(Disponibilidad d) throws BusinessException, UserException;
	public void desmarcarReserva(Reserva r) throws BusinessException;
	public Reserva confirmarReserva(Reserva r, Boolean cancelarReservasPrevias, Boolean confirmarConWarning) throws ApplicationException, BusinessException, ValidacionException, AccesoMultipleException, UserException;
	public void validarDatosReserva(Recurso recurso, List<DatoReserva> datos) throws BusinessException, UserException, ApplicationException;
	
	public Reserva consultarReservaPorNumero(Recurso r, Integer numero) throws BusinessException;
	public List<Reserva> consultarReservaPorDatos(Recurso r, Map<DatoASolicitar, DatoReserva> datos);
	public Reserva consultarReservaPorDatosClave(Recurso r, Map<DatoASolicitar, DatoReserva> datos) throws ApplicationException;
	public void cancelarReserva(Recurso recurso, Reserva reserva) throws BusinessException, ApplicationException;
	
	public Reserva marcarReserva(List<Disponibilidad> disps);
	public List<Reserva> consultarReservasEnPeriodo(Recurso r, VentanaDeTiempo v);
	public void reagendarReservas(List<Reserva> reservas, Date fechaHora);
	
	public Map<String, Object> autocompletarCampo(ServicioPorRecurso s, Map<String, Object> datosParam) throws ApplicationException, BusinessException, AutocompletarException, UserException;
}
