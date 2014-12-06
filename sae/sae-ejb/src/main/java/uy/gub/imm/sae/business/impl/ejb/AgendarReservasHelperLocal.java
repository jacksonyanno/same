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

package uy.gub.imm.sae.business.impl.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorAutocompletarException;
import uy.gub.imm.sae.common.exception.ErrorValidacionCommitException;
import uy.gub.imm.sae.common.exception.ErrorValidacionException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionPorCampoException;
import uy.gub.imm.sae.common.exception.WarningAutocompletarException;
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;

@Local
public interface AgendarReservasHelperLocal {
	
	public VentanaDeTiempo obtenerVentanaCalendarioEstaticaIntranet (Recurso recurso);
	public VentanaDeTiempo obtenerVentanaCalendarioAjustadaIntranet(Recurso r, VentanaDeTiempo ventana);
	public VentanaDeTiempo obtenerVentanaCalendarioEstaticaInternet (Recurso recurso);
	public VentanaDeTiempo obtenerVentanaCalendarioAjustadaInternet(Recurso r, VentanaDeTiempo ventana);
	public VentanaDeTiempo obtenerVentanaCalendarioExtendida(Recurso r, VentanaDeTiempo ventana);
	public List<Object[]> obtenerCuposAsignados(Recurso r, VentanaDeTiempo ventana);
	public List<Object[]> obtenerCuposConsumidos(Recurso r, VentanaDeTiempo ventana);
	public List<Integer> obtenerCuposXDia(VentanaDeTiempo ventana, List<Object[]> cuposAsignados, List<Object[]> cuposConsumidos);
	public Reserva crearReservaPendiente(Disponibilidad d);
	public boolean chequeoCupoNegativo (Disponibilidad d);
	public List<DatoASolicitar> obtenerDatosASolicitar(Recurso r);
	public List<ValidacionPorRecurso> obtenerValidacionesPorRecurso(Recurso r);
	public void validarDatosReservaBasico(List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException, ValidacionException;
	public void validarDatosReservaExtendido(List<ValidacionPorRecurso> validaciones, List<DatoASolicitar> campos, Map<String, DatoReserva> valores, Boolean noLanzarWarning, ReservaDTO reservaDTO) throws ApplicationException, BusinessException, ErrorValidacionException, WarningValidacionException, ErrorValidacionCommitException, WarningValidacionCommitException;
	public List<Reserva> validarDatosReservaPorClave(Recurso recurso, Reserva reserva, List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException;
	public void validarDatosRequeridosReserva(List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException, UserException;
	public void validarTipoDatosReserva(List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException, ValidacionPorCampoException;
	public Map<String, Object> autocompletarCampo(ServicioPorRecurso s, Map<String, Object> datosParam) throws ApplicationException, BusinessException, ErrorAutocompletarException, WarningAutocompletarException;
}
