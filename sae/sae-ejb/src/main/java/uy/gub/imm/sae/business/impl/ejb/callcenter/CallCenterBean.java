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

package uy.gub.imm.sae.business.impl.ejb.callcenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.AgendarReservasLocal;
import uy.gub.imm.sae.business.api.callcenter.CallCenterLocal;
import uy.gub.imm.sae.business.api.callcenter.CallCenterRemote;
import uy.gub.imm.sae.business.api.callcenter.ReservaDTO;
import uy.gub.imm.sae.business.api.callcenter.ResultadoValidacion;
import uy.gub.imm.sae.business.impl.ejb.AgendarReservasHelperLocal;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorValidacionCommitException;
import uy.gub.imm.sae.common.exception.ErrorValidacionException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionPorCampoException;
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.Validacion;
import uy.gub.imm.sae.entity.ValidacionPorDato;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;

@Stateless
@RolesAllowed({"RA_AE_FCALL_CENTER","RA_AE_FATENCION"})
public class CallCenterBean implements CallCenterLocal, CallCenterRemote {

	// TODO: REVISAR MANEJO DE ROLES (VER QUE ROLES PRECISAMOS PARA QUE ESTO FUNCIONE) -> PUEDE SER UTIL USAR RUNAS
	
	static Logger logger = Logger.getLogger(CallCenterBean.class);
	
	@EJB
	private AgendarReservasHelperLocal helperBean;

	@EJB
	private AgendarReservasLocal agendarReservasBean;

	private Map<String, DatoReserva> construirDatosReserva(List<DatoASolicitar> campos, Map<String, String> datos){
		
		Map<String, DatoReserva> valores = new HashMap<String, DatoReserva>();
		
		for (Iterator<DatoASolicitar> iterator = campos.iterator(); iterator.hasNext();) {
			DatoASolicitar defDato = iterator.next();
			
			String valorDato = datos.get(defDato.getNombre());
			
			if(valorDato!=null){
				// el dato a solicitar esta dentro de los parametros en datos
				DatoReserva dr = new DatoReserva();
				dr.setDatoASolicitar(defDato);
				dr.setValor(valorDato);
				
				valores.put(defDato.getNombre(), dr);
			}
		}

		return valores;
	}
	
	private Map<String, String> desarmarDatosReserva(Set<DatoReserva> datosReserva){
		
		Map<String, String> datos = new HashMap<String, String>();
		
		for (Iterator<DatoReserva> iterator = datosReserva.iterator(); iterator.hasNext();) {
			DatoReserva dato = iterator.next();
			datos.put(dato.getDatoASolicitar().getNombre(), dato.getValor());
		}
		
		return datos;		
	}
	
	private boolean estanTodosCamposClave(List<DatoASolicitar> campos, Map<String, String> datos){
		
		boolean estanTodos = true;
		
		for (Iterator<DatoASolicitar> iterator = campos.iterator(); iterator.hasNext();) {
			DatoASolicitar campoI = iterator.next();

			if(campoI.getEsClave()){
				// el campo I es clave -> revisamos que este como parametro en datos
				if(datos.get(campoI.getNombre())==null){
					// no esta, por lo que no estan todos los campos clave
					estanTodos = false;
				}
			}			
		}
		
		return estanTodos;
	}
	
	private ReservaDTO construirReservaDTOFromReserva(Reserva reserva){
		ReservaDTO resDTO = new ReservaDTO();
		
		resDTO.setId(reserva.getId());
		
		Disponibilidad disp =  reserva.getDisponibilidades().get(0);		
		Calendar calHora = Calendar.getInstance();
		calHora.setTime(disp.getHoraInicio());		
		Calendar cal = Calendar.getInstance();
		cal.setTime(disp.getFecha());
		cal.set(Calendar.HOUR_OF_DAY, calHora.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, calHora.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, calHora.get(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, calHora.get(Calendar.MILLISECOND));		
		resDTO.setFechaYhora(cal.getTime());
		
		resDTO.setDatos(desarmarDatosReserva(reserva.getDatosReserva()));
		
		return resDTO;
	}
	
	private List<ValidacionPorRecurso> filtrarValidacionesEjecutablesSobreDatos(List<ValidacionPorRecurso> validacionesTodas, Map<String, DatoReserva> valores){
		
		List<ValidacionPorRecurso> validacionesFinal = new ArrayList<ValidacionPorRecurso>();
		boolean tengoTodosDatoParaValidar = true;		
		
		for (Iterator<ValidacionPorRecurso> iterator = validacionesTodas.iterator(); iterator.hasNext();) {
			ValidacionPorRecurso vXr = iterator.next();

			Validacion v = vXr.getValidacion();
			tengoTodosDatoParaValidar = true;

			if (v.getFechaBaja() == null) {			
				List<ValidacionPorDato> camposDeLaValidacion = vXr.getValidacionesPorDato();			
				for (Iterator<ValidacionPorDato> iterator2 = camposDeLaValidacion.iterator(); iterator2.hasNext();) {
					ValidacionPorDato valDatoI = iterator2.next();
					
					if (valDatoI.getFechaDesasociacion() == null) {
						DatoASolicitar campo = valDatoI.getDatoASolicitar();
						DatoReserva dato = valores.get(campo.getNombre());
						if (dato == null) {
							tengoTodosDatoParaValidar = false;
						}
					}
				}			
			}
			
			if(tengoTodosDatoParaValidar){
				validacionesFinal.add(vXr);
			}
		}
		
		return validacionesFinal;
	}

	//TODO cambiar idRecurso por nombreRecurso
	public ReservaDTO marcarReserva(String nombreAgenda, Integer idRecurso, Date fechaHoraDesde) throws ApplicationException, BusinessException {
		
		ReservaDTO reservaDTO = null;
		
		try {
		
			Agenda agenda = agendarReservasBean.consultarAgendaPorNombre(nombreAgenda);
			Recurso recurso = agendarReservasBean.consultarRecursoPorId(agenda, idRecurso);
			
			VentanaDeTiempo ventana = agendarReservasBean.obtenerVentanaCalendarioIntranet(recurso);
			
			if(ventana.getFechaInicial().before(fechaHoraDesde)){
				ventana.setFechaInicial(fechaHoraDesde);
			}
			
			List<Disponibilidad> disponibilidades = agendarReservasBean.obtenerDisponibilidades(recurso, ventana);
			
			boolean tengoReserva = false;
			Reserva nuevaReserva = null;
			
			for (Iterator<Disponibilidad> iterator = disponibilidades.iterator(); iterator.hasNext() && !tengoReserva;) {
				Disponibilidad disp = iterator.next();
				
				// intentamos con la disponibilidad actual
				try {
					nuevaReserva = agendarReservasBean.marcarReserva(disp);
					tengoReserva = true;
				} catch (UserException e) {
					// no se pudo reservar, por lo que se intenta nuevamente
				}
			}
			
			if(tengoReserva){
				reservaDTO = construirReservaDTOFromReserva(nuevaReserva);
			}
		} catch (ApplicationException ae) {
			logger.error("ERROR al marcar reserva para agenda <" + nombreAgenda + "> y recurso <" + idRecurso + "> - " + ae.getMessage(), ae);
			throw ae;
		} catch (BusinessException be) {
			logger.error("ERROR al marcar reserva para agenda <" + nombreAgenda + "> y recurso <" + idRecurso + "> - " + be.getMessage(), be);
			throw be;
		}
			
		return reservaDTO;
	}
	
	public ResultadoValidacion validarDatos(String nombreAgenda, Integer idRecurso, Map<String, String> datos) throws ApplicationException, BusinessException {
		// TODO REVISAR MANEJO DE ERRORES - ESTA HECHO, PERO VER QUE SEA ADECUADO!!!!
		
		ResultadoValidacion resVal = new ResultadoValidacion();
		resVal.setOk(true);
		
		try {
		
			Agenda agenda = agendarReservasBean.consultarAgendaPorNombre(nombreAgenda);
			Recurso recurso = agendarReservasBean.consultarRecursoPorId(agenda, idRecurso);
			
			List<DatoASolicitar> campos = helperBean.obtenerDatosASolicitar(recurso);
			Map<String, DatoReserva> valores = construirDatosReserva(campos, datos);
			
			// 1 - corremos validaciones de formato (tipos de datos)
			helperBean.validarTipoDatosReserva(campos, valores);
			
			// 2 - verificamos si tenemos todos los campos clave. Si estan, corremos validacion de unicidad
			if(estanTodosCamposClave(campos, datos)){
				List<Reserva> reservasPrevias = helperBean.validarDatosReservaPorClave(recurso, null, campos, valores);
				
				if(!reservasPrevias.isEmpty()){
					resVal.setReservaPrevia(construirReservaDTOFromReserva(reservasPrevias.get(0)));
				}
			}
			
			// 3 - verificamos para cada campo que validaciones se pueden correr, y 
			// para cada una, si se puede correr (si tenemos todos los campos necesarios)
			// Si es asi, corremos esa validacion.
			List<ValidacionPorRecurso> validaciones = filtrarValidacionesEjecutablesSobreDatos(helperBean.obtenerValidacionesPorRecurso(recurso), valores);
			helperBean.validarDatosReservaExtendido(validaciones, campos, valores, false, null);
		
		} catch (ValidacionPorCampoException vpce) {
			resVal.setOk(false);
			resVal.setNombreCampos(vpce.getNombresCampos());			
			// TODO: ver de definir codigo para esto
			String codigoError = "0";
			resVal.setCodigoError(codigoError);
		} catch (WarningValidacionException wve) {
			resVal.setOk(false);
			resVal.setNombreCampos(wve.getNombresCampos());
			String codigoError = wve.getNombreValidacion() + "_" + wve.getCodigosErrorMensajes().get(0);
			resVal.setCodigoError(codigoError);
		} catch (ErrorValidacionException eve) {
			resVal.setOk(false);
			resVal.setNombreCampos(eve.getNombresCampos());
			String codigoError = eve.getNombreValidacion() + "_" + eve.getCodigosErrorMensajes().get(0);
			resVal.setCodigoError(codigoError);
		} catch (WarningValidacionCommitException wvce) {
			resVal.setOk(false);
			resVal.setNombreCampos(wvce.getNombresCampos());
			String codigoError = wvce.getNombreValidacion() + "_" + wvce.getCodigosErrorMensajes().get(0);
			resVal.setCodigoError(codigoError);
		} catch (ErrorValidacionCommitException evce) {
			resVal.setOk(false);
			resVal.setNombreCampos(evce.getNombresCampos());
			String codigoError = evce.getNombreValidacion() + "_" + evce.getCodigosErrorMensajes().get(0);
			resVal.setCodigoError(codigoError);
		} catch (ApplicationException ae) {
			logger.error("ERROR al validar datos reserva para agenda <" + nombreAgenda + "> y recurso <" + idRecurso + "> - " + ae.getMessage(), ae);
			throw ae;
		} catch (BusinessException be) {
			logger.error("ERROR al validar datos reserva para agenda <" + nombreAgenda + "> y recurso <" + idRecurso + "> - " + be.getMessage(), be);
			throw be;
		}
		
		return resVal;
	}

	public void desmarcarReserva(Integer idReserva) throws BusinessException {
		
		try {
			Reserva reserva = agendarReservasBean.consultarReservaPorNumero(null, idReserva);		
			agendarReservasBean.desmarcarReserva(reserva);
		} catch (BusinessException be) {
			logger.error("ERROR al desmarcar reserva id <" + idReserva + "> - " + be.getMessage(), be);
			throw be;
		}
	}

	public void cancelarReserva(Integer idReservaPrevia) throws BusinessException, ApplicationException {
		
		try {
			Reserva reserva = agendarReservasBean.consultarReservaPorNumero(null, idReservaPrevia);
			agendarReservasBean.cancelarReserva(reserva.getDisponibilidades().get(0).getRecurso(), reserva);
		} catch (BusinessException be) {
			logger.error("ERROR al cancelar reserva id <" + idReservaPrevia + "> - " + be.getMessage(), be);
			throw be;
		}
	}

	public void confirmarReserva(Integer idReserva, Map<String, String> datos) throws BusinessException, ApplicationException, ValidacionException, AccesoMultipleException, UserException {
		
		try {
			Reserva reserva = agendarReservasBean.consultarReservaPorNumero(null, idReserva);
			
			List<DatoASolicitar> campos = helperBean.obtenerDatosASolicitar(reserva.getDisponibilidades().get(0).getRecurso());
			Map<String, DatoReserva> valores = construirDatosReserva(campos, datos);
	
			reserva.setDatosReserva(new HashSet<DatoReserva>(valores.values()));
			
			agendarReservasBean.confirmarReserva(reserva, true, true);
		} catch (BusinessException be) {
			logger.error("ERROR al confirmar reserva id <" + idReserva+ "> - " + be.getMessage(), be);
			throw be;
		} catch (ApplicationException ae) {
			logger.error("ERROR al confirmar reserva id <" + idReserva+ "> - " + ae.getMessage(), ae);
			throw ae;
		} catch (ValidacionException ve) {
			logger.error("ERROR al confirmar reserva id <" + idReserva+ "> - " + ve.getMessage(), ve);
			throw ve;
		} catch (AccesoMultipleException ame) {
			logger.error("ERROR al confirmar reserva id <" + idReserva+ "> - " + ame.getMessage(), ame);
			throw ame;
		} catch (UserException ue) {
			logger.error("ERROR al confirmar reserva id <" + idReserva+ "> - " + ue.getMessage(), ue);
			throw ue;
		}
	}

}
