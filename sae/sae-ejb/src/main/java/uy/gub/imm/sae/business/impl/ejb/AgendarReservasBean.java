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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.AgendarReservasLocal;
import uy.gub.imm.sae.business.api.AgendarReservasRemote;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.enumerados.Evento;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.AutocompletarException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorAccionException;
import uy.gub.imm.sae.common.exception.RolException;
import uy.gub.imm.sae.common.exception.UserCommitException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.exception.ValidacionClaveUnicaException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;
import uy.gub.imm.sae.entity.Enumerados.SAERol;
import uy.gub.imm.sae.entity.Enumerados.SAERolPrefijo;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_PLANIFICADOR","RA_AE_FCALL_CENTER","RA_AE_FATENCION", "RA_AE_ANONIMO", "RA_AE_LLAMADOR"})
public class AgendarReservasBean implements AgendarReservasLocal, AgendarReservasRemote{
	
	
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;

	@Resource
	private SessionContext ctx;
	
	@EJB
	private AgendarReservasHelperLocal helper;
	
	@EJB
	private AccionesHelperLocal helperAccion;
	
	static Logger logger = Logger.getLogger(AgendarReservasBean.class);
	
	
	/**
	 * Retorna la agenda cuyo nombre sea <b>nombre</b> siempre y cuando esta viva (fechaBaja == null).
	 * Controla que el usuario tenga rol FuncionarioAtencion sobre la agenda a retornar
	 * Roles permitidos: FuncionarioAtencion
	 * @throws ApplicationException 
	 * @throws EntidadNoEncontradaException 
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 * @throws ParametroIncorrectoException 
	 */
	public Agenda consultarAgendaPorNombre(String nombre) throws ApplicationException, BusinessException {
		
		if (nombre == null) {
			throw new BusinessException("-1","Parametro nulo");
		}
		
		Agenda agenda = null;
		try {
			agenda = (Agenda) em
					.createQuery("from Agenda a where upper(a.nombre) = :n and a.fechaBaja is null")
					.setParameter("n", nombre.toUpperCase())
					.getSingleResult();
			
		} catch (NoResultException e) {
			throw new BusinessException("-1", "No se encuentra la agenda de nombre "+nombre);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		chequearPermiso(agenda, new SAERolPrefijo[] {SAERolPrefijo.RA_AE_PLANI, SAERolPrefijo.RA_AE_FCALL,SAERolPrefijo.RA_AE_FATEN });
		
		return agenda;
	}

	/**
	 * Retorna el recurso cuyo nombre sea <b>nombre</b> siempre y cuando esta vivo (fechaBaja == null).
	 * Controla que el usuario tenga rol FuncionarioAtencion sobre la agenda <b>a</b>
	 * Roles permitidos: FuncionarioAtencion
	 * @throws BusinessException 
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 * @throws ParametroIncorrectoException 
	 */
	public Recurso consultarRecursoPorNombre(Agenda a, String nombre) throws ApplicationException, BusinessException  {

		if (a == null || nombre == null) {
			throw new BusinessException("-1","Parametros nulos");
		}
		a = consultarAgendaPorNombre(a.getNombre());
		if (a == null) {
			throw new BusinessException("-1","No se encuentra la agenda por nombre");
		}

		Recurso recurso = null;
		
		try {
			recurso = (Recurso) em.createQuery("from Recurso r where r.nombre = :n "+
											   "and r.fechaBaja is null "+
											   "and r.agenda = :a")
								  .setParameter("n", nombre)
								  .setParameter("a", a)
								  .getSingleResult();
			
		} catch (NoResultException e) {
			throw new BusinessException("-1", "No se encuentra el recurso de nombre "+nombre);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		
		return recurso;		
	}

	/**
	 * Retorna el recurso cuyo id sea <b>id</b> siempre y cuando esta vivo (fechaBaja == null).
	 * Controla que el usuario tenga rol FuncionarioAtencion sobre la agenda <b>a</b>
	 * Roles permitidos: FuncionarioAtencion
	 * @throws BusinessException 
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 * @throws ParametroIncorrectoException 
	 */
	public Recurso consultarRecursoPorId(Agenda a, Integer id) throws ApplicationException, BusinessException  {

		if (a == null || id == null) {
			throw new BusinessException("-1","Parametros nulos");
		}
		a = consultarAgendaPorNombre(a.getNombre());
		if (a == null) {
			throw new BusinessException("-1","No se encuentra la agenda por nombre");
		}

		Recurso recurso = null;
		
		try {
			recurso = (Recurso) em.createQuery("from Recurso r where r.id = :id "+
											   "and r.fechaBaja is null "+
											   "and r.agenda = :a")
								  .setParameter("id", id)
								  .setParameter("a", a)
								  .getSingleResult();
			
		} catch (NoResultException e) {
			throw new BusinessException("-1", "No se encuentra el recurso de id "+id);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		
		return recurso;		
	}
	
	/**
	 * Retorna una lista de agendas vivas (fechaBaja == null) ordenada por nombre
	 * Solo retorna las agendas para las que el usuario tenga rol FuncionarioAtencion.
	 * Solo retorna las agendas tales que una posterior llamada a consultarRecursos retorne
	 * por lo menos un recurso.
	 * Roles permitidos: FuncionarioAtencion
	 * @throws ApplicationException 
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	public List<Agenda> consultarAgendas() throws ApplicationException, BusinessException {

		//TODO chequear permisos
		
		List<Agenda> agendas = null;
		try {
			agendas = (List<Agenda>) em
					.createQuery("from Agenda a where a.fechaBaja is null order by a.nombre")
					.getResultList();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		List<Agenda> agendasValidas = new ArrayList<Agenda>();
		for (Agenda agenda : agendas) {
			if (consultarRecursos(agenda).size() != 0) {
				agendasValidas.add(agenda);
			}
		}

		return agendasValidas;
	}


	/**
	 * Retorna una lista de recursos vivos (fechaBaja == null) ordenados por nombre
	 * Solo retorna aquellos recursos tales que la fecha actual este comprendida entre
	 * la fechaInicio y fechaFin del recurso. 
	 * Controla que el usuario tenga rol FuncionarioAtencion sobre la agenda <b>a</b>
	 * Roles permitidos: FuncionarioAtencion
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	public List<Recurso> consultarRecursos(Agenda a) throws ApplicationException, BusinessException {

		if (a == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		a = em.find(Agenda.class, a.getId());
		if (a == null) {
			throw new BusinessException("-1", "No se encuentra la agenda indicada");
		}
		
		//chequearPermiso(a);

		List<Recurso> recursos = null;
		Date ahora = new Date();
		try {
			recursos = (List<Recurso>)em.createQuery(
						"from Recurso r where " +
						"r.agenda = :a and " +
						"r.fechaBaja is null and  " +
						"r.fechaInicio <= :ahora and " +
						"(r.fechaFin is null or r.fechaFin >= :ahora)" +
						"order by r.descripcion")
						.setParameter("a", a)
						.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
						.getResultList();
		}catch (Exception e) {
			throw new ApplicationException(e);
		}
		return recursos;
	}

	public Boolean agendaActiva(Agenda a) {
		// TODO Auto-generated method stub
		return null;
	}

	public void cancelarReserva(Recurso recurso, Reserva reserva) throws BusinessException, ApplicationException {
		
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}

		if (reserva == null) {
			throw new BusinessException("AE20085", "La reserva no puede ser nula");
		}

		
		Reserva r = em.find(Reserva.class, reserva.getId());
		
		if (r == null) {
			throw new BusinessException("AE10086", "No se encontro la reserva indicada");
		}
		if (r.getEstado() != Estado.R) {
			throw new BusinessException("AE10087", "Solo se pueden cancelar reservas en estado Reservada");
		}
		
		Integer id = reserva.getDisponibilidades().get(0).getRecurso().getId();
		if (recurso.getId().intValue() != id.intValue()) {
			throw new BusinessException("AE10088", "El recurso asociado a la reserva no se corresponde con el recurso indicado");			
		}
		
		//chequearPermiso(recurso.getAgenda());
		
		ReservaDTO reservaDTO = new ReservaDTO();
		reservaDTO.setEstado(r.getEstado().toString());
		reservaDTO.setFecha(r.getDisponibilidades().get(0).getFecha());
		reservaDTO.setHoraInicio(r.getDisponibilidades().get(0).getHoraInicio());
		reservaDTO.setId(r.getId());
		reservaDTO.setOrigen(r.getOrigen());
		reservaDTO.setNumero(r.getDisponibilidades().get(0).getNumerador() + 1);
		reservaDTO.setUcancela(ctx.getCallerPrincipal().getName().toLowerCase()); //el usuario de sesion es el que esta cancelando la reserva anterior y ese dato lo enviamos hacia GAP
		if (r.getLlamada() != null) {
			reservaDTO.setPuestoLlamada(r.getLlamada().getPuesto());
		}
		
		
		//Necesito recuperar los datos ingresados por el usuario para pasarlos a las acciones
		//Los elementos DatoASolicitar son managed, los elementos DatoReservas son unmanaged pues son nuevos
		Map<String, DatoReserva> valores = new HashMap<String, DatoReserva>();
		for (DatoReserva valor : reserva.getDatosReserva()) {
			valores.put(valor.getDatoASolicitar().getNombre(), valor);
		}
		
		//Ejecuto acciones asociadas al evento cancelar
		try{
			helperAccion.ejecutarAccionesPorEvento(valores, reservaDTO, recurso, Evento.C);
		}catch (ErrorAccionException e){
			throw new BusinessException(e.getCodigoError(), e.getMessage());
		}
		
		//Cancelo la reserva, paso el estado a Cancelada.
		r.setEstado(Estado.C);
		r.setObservaciones(reserva.getObservaciones());
		r.setUcancela(ctx.getCallerPrincipal().getName().toLowerCase()); //se guarda en SAE el usuario que cancela la reserva anterior 

	}

	/**
	 * Consulta una reserva por numero y para un recurso.
	 * Si se pasa el recurso, se valida que la reserva con <b>numero</b> corresponda al recurso indicado.
	 * En caso contrario simplemente se busca la reserva por id y se la retorna (sin validar nada)
	 */
	public Reserva consultarReservaPorNumero(Recurso r, Integer numero) throws BusinessException {
		
		Reserva reserva = em.find(Reserva.class, numero);
		if (reserva == null) {
			throw new BusinessException("-1", "No se encuentra la reserva indicada");
		}
		
		if(r!=null){
			boolean esReservaDeRecurso = false;
			
			for (Iterator<Disponibilidad> iterator = reserva.getDisponibilidades().iterator(); iterator.hasNext() && !esReservaDeRecurso;) {
				Disponibilidad disp = iterator.next();
				
				if(disp.getRecurso().getId()==r.getId()){
					esReservaDeRecurso = true;
				}
			}
			
			if(esReservaDeRecurso){
				throw new BusinessException("-1", "El numero de reserva no se corresponde con el recurso indicado");
			}
		}
		
		return reserva;
	}

	public List<Reserva> consultarReservasEnPeriodo(Recurso r, VentanaDeTiempo v) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Crea una nueva reserva en estado pendiente, controla que aun exista cupo.
	 * 
	 * Para controlar la existencia de cupo sin necesidad de utilizar bloqueo
	 * persiste la reserva y luego chequea que el cupo real no sea negativo, si esto
	 * sucede, elimina fisicamente la reserva y cancela la operacion.
	 * @throws BusinessException 
	 * @throws UserException 
	 */
	public Reserva marcarReserva(Disponibilidad d) throws BusinessException, UserException {
		
		if (d == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		d = em.find(Disponibilidad.class, d.getId());
		if (d == null) {
			throw new BusinessException("-1", "No se encuentra la disponibilidad indicada");
		}		
		
//		chequearPermiso(d.getRecurso().getAgenda());
		
		//Se crea la reserva en una transaccion independiente
		Reserva reserva = helper.crearReservaPendiente(d);
		
		//Chequeo que el cupo real no de negativo
		//Si el cupo real da negativo, elimino la reserva pendiente y cancelo la operacion
		//De lo contrario la reserva se ha marcado con exito
		if (helper.chequeoCupoNegativo(d)) {
			reserva = em.find(Reserva.class, reserva.getId());
			em.remove(reserva);
			em.flush();
			throw new UserCommitException("AE10069");
		}
		return reserva;
	}

	
	/**
	 * Elimina fisicamente una reserva marcada como pendiente.
	 */
	public void desmarcarReserva(Reserva r) throws BusinessException {

		if (r == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		r = em.find(Reserva.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra la reserva indicada");
		}		
		
		if (r.getEstado() != Estado.P) {
			throw new BusinessException("-1", "Solo se puede desmarcar reservas en estado pendiente");
		}
	//	chequearPermiso(r.getDisponibilidades().get(0).getRecurso().getAgenda());

		em.remove(r);
	}
	
	
	/**
	 * Realiza todas las validaciones configuradas para el recurso menos la validacion de clave unica. 
	 * @param recurso
	 * @param datos
	 * @throws UserException 
	 * @throws BusinessException 
	 * @throws ApplicationException 
	 */
	public void validarDatosReserva(Recurso recurso, List<DatoReserva> datos) 
	throws BusinessException, UserException, ApplicationException {

	
		//Armo las estructuras de Map necesarias para poder ejecutar las validaciones sobre los datos de la reserva
		List<DatoASolicitar> campos = helper.obtenerDatosASolicitar(recurso);

		Map<String, DatoASolicitar> camposMap = new HashMap<String, DatoASolicitar>();
		for (DatoASolicitar datoASolicitar : campos) {
			camposMap.put(datoASolicitar.getNombre(), datoASolicitar);
		}
		
		//Los elementos DatoASolicitar son managed, los elementos DatoReservas son unmanaged pues son nuevos
		Map<String, DatoReserva> valores = new HashMap<String, DatoReserva>();
		for (DatoReserva valor : datos) {
			valores.put(valor.getDatoASolicitar().getNombre(), valor);
		}

		//Validacion basica: campos obligatorios y formato
		helper.validarDatosReservaBasico(campos, valores);

		List<ValidacionPorRecurso> validaciones = helper.obtenerValidacionesPorRecurso(recurso);

		//Ejecucion de los validadores personalizados.
		helper.validarDatosReservaExtendido(validaciones, campos, valores, false, null);
	
	}
	
	
	/**
	 * Confirma la reserva.
	 * Si la reserva debe tener datos, estos son exigidos y validados en este metodo, incluyendo la verificacion de clave unica
	 * Si cancelarReservasPrevias es true: cancela reservas previas para la clave.
	 * Si cancelarReservasPrevias es false: valida que la clave sea unica.
	 * Si confirmarConWarning es true: confirma aunque exista warnings.
	 * Si confirmarConWarning es flase: solo confirma si no hay warnings.  
	 * @throws ApplicationException 
	 * @throws BusinessException
	 * @return Reserva: Devuelve la reserva pues se le ha asignado un numero unico de reserva dentro de la hora a la que pertenece. 
	 * @throws UserException
	 * @throws ValidacionException 
	 */
	public Reserva confirmarReserva(Reserva r, Boolean cancelarReservasPrevias, Boolean confirmarConWarning) 
		throws ApplicationException, BusinessException, ValidacionException, AccesoMultipleException, UserException {
		
		if (r == null || cancelarReservasPrevias == null || confirmarConWarning == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		Set<DatoReserva> datosNuevos = r.getDatosReserva();
		if (datosNuevos == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
				
		r = em.find(Reserva.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra la reserva indicada");
		}
		
		if (r.getEstado() == Estado.U) {
			throw new UserException("-1","No es posible confirmar su reserva, intente con otra fecha u hora");
		}
		
		if (r.getEstado() != Estado.P) {
			throw new BusinessException("-1", "Solo se puede confirmar reservas en estado pendiente");
		}
	//	chequearPermiso(r.getDisponibilidades().get(0).getRecurso().getAgenda());

		//Armo las estructuras de Map necesarias para poder ejecutar las validaciones sobre los datos de la reserva
		Recurso recurso = r.getDisponibilidades().get(0).getRecurso();
		List<DatoASolicitar> campos = helper.obtenerDatosASolicitar(recurso);

		Map<String, DatoASolicitar> camposMap = new HashMap<String, DatoASolicitar>();
		for (DatoASolicitar datoASolicitar : campos) {
			camposMap.put(datoASolicitar.getNombre(), datoASolicitar);
		}
		
		//Los elementos DatoASolicitar son managed, los elementos DatoReservas son unmanaged pues son nuevos
		Map<String, DatoReserva> valores = new HashMap<String, DatoReserva>();
		for (DatoReserva valor : datosNuevos) {
			valores.put(valor.getDatoASolicitar().getNombre(), valor);
		}

		//Validacion basica: campos obligatorios y formato
		helper.validarDatosReservaBasico(campos, valores);

		//Validacion basica: campos clave
		List<Reserva> reservasPrevias = helper.validarDatosReservaPorClave(recurso, r, campos, valores);

		// la validacion nunca deberia devolver mas de una reserva previa. en caso de que esto pase, se retorna un error al usuario
		if (reservasPrevias.size() > 1) {
			String strDato = "";
			for (DatoReserva dato: valores.values()) {
				strDato+=dato.toString();
			}				
			logger.warn("Se encontraron mas de una reservas previas a cancelar, lo que no deberia pasar NUNCA. Reserva:" + r.toString() + "Datos: " + strDato);
			throw new UserException("-1","No es posible cancelar sus reservas anteriores, solicite ayuda telefonica");
		}
		
		//Si Hay reservas repetidas, y la bandera esta en false lanzo una excepcion.
		if (!reservasPrevias.isEmpty() && !cancelarReservasPrevias) {
			//Se carga lista de camposClave
			List<String> nombreCamposClave = new ArrayList<String>();
			for (DatoASolicitar campo : campos) {
				if (campo.getEsClave() ) {
					nombreCamposClave.add(campo.getNombre());
				}
			}
			
			Reserva rPrevia = reservasPrevias.get(0);
			Calendar dia = Calendar.getInstance();
			dia.setTime(rPrevia.getDisponibilidades().get(0).getFecha());
			Calendar hora = Calendar.getInstance();
			hora.setTime(rPrevia.getDisponibilidades().get(0).getHoraInicio());

			String horaInicio = Integer.toString(hora.get(Calendar.HOUR_OF_DAY));
			String minutosInicio = Integer.toString(hora.get(Calendar.MINUTE));
			
			if (horaInicio.length() < 2){
				horaInicio = "0" + horaInicio;
			}				
			
			if (minutosInicio.length() < 2){
				minutosInicio = "0" + minutosInicio;
			}				
	
			throw new ValidacionClaveUnicaException(
					"AE10100", 
					"Ya existe una reserva para el día: " +
					dia.get(Calendar.DAY_OF_MONTH) + " / " +
					(dia.get(Calendar.MONTH)+1) + " / " +
					dia.get(Calendar.YEAR) + " - " +
					horaInicio + ":" + 	minutosInicio +
					" y los datos ingresados ¿Desea modificarla?", 
					nombreCamposClave);			
		}

		List<ValidacionPorRecurso> validaciones = helper.obtenerValidacionesPorRecurso(recurso);

		ReservaDTO reservaDTO = new ReservaDTO();
		reservaDTO.setEstado(r.getEstado().toString());
		reservaDTO.setFecha(r.getDisponibilidades().get(0).getFecha());
		reservaDTO.setHoraInicio(r.getDisponibilidades().get(0).getHoraInicio());
		reservaDTO.setId(r.getId());
		
		//Marcamos el origen de la reserva
		//"C" si tiene el rol de call center
		//"W" si tiene rol anonimo
		//"I" si tiene cualquier otro rol (FATENCION,PLANIFICADOR,ADMINISTRADOR)
		String origen;
		if (ctx.isCallerInRole(SAERol.RA_AE_FCALL_CENTER.toString())){
			origen = "C";
		}else if (ctx.isCallerInRole(SAERol.RA_AE_ANONIMO.toString())){
			origen = "W";
		}else{
			origen = "I";
		}
		reservaDTO.setOrigen(origen);
		reservaDTO.setUcrea(ctx.getCallerPrincipal().getName().toLowerCase());
		reservaDTO.setNumero(r.getDisponibilidades().get(0).getNumerador() + 1);
		if (r.getLlamada() != null) {
			reservaDTO.setPuestoLlamada(r.getLlamada().getPuesto());
		}
		
		//Ejecucion de los validadores personalizados.
		helper.validarDatosReservaExtendido(validaciones, campos, valores, confirmarConWarning, reservaDTO);
		
		// Si Hay reservas repetidas, y la bandera esta en true cancelo la reserva 
		// previa antes de confirmar la reserva nueva.
		// En este punto solo puede haber una reserva previa
		if (!reservasPrevias.isEmpty() && cancelarReservasPrevias) {
			this.cancelarReserva(recurso, reservasPrevias.get(0));
		}

		//Pase las validaciones, procedo a persistir los DatoReserva
		for (DatoReserva dato: valores.values()) {
			DatoASolicitar campo = camposMap.get(dato.getDatoASolicitar().getNombre());
			
			DatoReserva datoNuevo = new DatoReserva();
			datoNuevo.setValor(dato.getValor());
			datoNuevo.setReserva(r);
			datoNuevo.setDatoASolicitar(campo);
			em.persist(datoNuevo);
		}

		
		//Confirmo la reserva, paso el estado a Reservada y le asigno el numero de reserva dentro de la disponibilidad.
		//Con mutua exclusion en el acceso al numerador de la disponibilidad
		Disponibilidad disponibilidad = r.getDisponibilidades().get(0);
		try {
			disponibilidad.setNumerador(disponibilidad.getNumerador()+1);
			em.flush();
		} catch(OptimisticLockException e){
			logger.warn("ACCESO MULTIPLE A DISPONIBILIDAD EN CONFIRMAR RESERVA (id = "+r.getId()+") "+e.getMessage()+" "+e.getCause().getClass().toString());
			throw new AccesoMultipleException("-1","ACCESO MULTIPLE A DISPONIBILIDAD EN CONFIRMAR RESERVA (id = "+r.getId()+")");
		}
		
		//Ejecuto acciones asociadas al evento reservar
		try{
			helperAccion.ejecutarAccionesPorEvento(valores, reservaDTO, recurso, Evento.R);
		}catch (ErrorAccionException e){
			throw new BusinessException(e.getCodigoError(), e.getMensajes().toString());
		}
		
		r.setEstado(Estado.R);
		r.setNumero(disponibilidad.getNumerador());
		r.setOrigen(origen);
		r.setUcrea(ctx.getCallerPrincipal().getName().toLowerCase());
		return r;
	}



	
	@SuppressWarnings("unchecked")
	public List<Disponibilidad> obtenerDisponibilidades(Recurso r, VentanaDeTiempo v) throws BusinessException {
		
		
		if (r == null || v == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		r = em.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
	//	chequearPermiso(r.getAgenda());
		
		Date ahora = new Date();
		
		//Elimino el PASADO
		if (v.getFechaInicial().before(ahora)) {
			v.setFechaInicial(ahora);
		}
		if (v.getFechaInicial().before(r.getFechaInicioDisp())) {
			v.setFechaInicial(r.getFechaInicioDisp());
		}

		List<Object[]> cantReservasVivas =  em
		.createQuery(
		"select d.id, d.fecha, d.horaInicio, count(reserva) " +
		"from  Disponibilidad d JOIN d.reservas reserva " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff and " +
    	"      (d.fecha <> :fi or d.horaInicio >= :fiCompleta) and " +
		"      (reserva.estado <> :cancelado) " +
		"group by d.id, d.fecha, d.horaInicio " +
		"order by d.fecha asc, d.horaInicio asc ")
		.setParameter("r", r)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
		.setParameter("fiCompleta", v.getFechaInicial(), TemporalType.TIMESTAMP)
		.setParameter("cancelado", Estado.C)
		.getResultList();
		
		List<Disponibilidad> disponibilidades =  em
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff and " +
    	"      (d.fecha <> :fi or d.horaInicio >= :fiCompleta) " +
		"order by d.fecha asc, d.horaInicio ")
		.setParameter("r", r)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
		.setParameter("fiCompleta", v.getFechaInicial(), TemporalType.TIMESTAMP)
		.getResultList();

		
		List<Disponibilidad> disp = new ArrayList<Disponibilidad>();
		Iterator<Object[]> cantReservasVivasIter = cantReservasVivas.iterator();
		Object row [] = null;
		if (cantReservasVivasIter.hasNext()) {
			row = cantReservasVivasIter.next();
		}
		
		for (Disponibilidad d : disponibilidades) {
			
			//Busco la cantidad de resevas hechas para esta disponibiliad
			int cant = 0;
			if (row != null && row[0].equals(d.getId())) {
				cant = ((Long)row[3]).intValue();
				if (cantReservasVivasIter.hasNext()) {
					row = cantReservasVivasIter.next();
				}
			}
			
			Disponibilidad dto = new Disponibilidad();
			dto.setId(d.getId());
			dto.setFecha(d.getFecha());
			dto.setHoraInicio(d.getHoraInicio());
			dto.setHoraFin(dto.getHoraFin());
			dto.setRecurso(null);
			dto.setCupo(d.getCupo() - cant);
			
			disp.add(dto);
		}
		
		return disp;
	}
	
	
	
	/**
	 * Devuelve las fechas limites para las cuales se tienen disponibilidades, 
	 * cumpliendo con los periodos para realizar reservas indicado en el recurso, es decir:
	 * El inicio del calendario es el primer dia que tenga asigando disponibilidades, 
	 * que este dentro del periodo de vigencia de las disponibilidades indicado en el recurso 
	 * y sea mayor o igual al dia actual.
	 * El fin del calendario es el ultimo dia que tenga asignado disponibilidades,
	 * que este dentro del periodo de vigencia de las disponibilidades indicado en el recurso 
	 * y tratando de satisfacer que:
	 *      	La diferencia entre fin - inicio sea mayor o igual a ventanaDiasMinimos.
	 *          Los cupos disponibles en la ventana sea mayor o igual a ventanaCuposMinimos. Si 
	 *          esto no fuera posible se debe agrandar la ventana hasta llegar
	 *          a la ultima disponibilidad sin pasarse del periodo de fin de disponibilidades.
	 *          
	 * En este caso la ventana de tiempo retornada representa dias, por lo cual, la hora del dia
	 * esta asignada a las 00:00:00.
	 *  
	 * @param recurso
	 * @return
	 * @throws BusinessException 
	 * @throws RolException 
	 */
	public VentanaDeTiempo obtenerVentanaCalendarioIntranet(Recurso r) throws BusinessException {

		if (r == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		r = em.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}
	//	chequearPermiso(r.getAgenda());	

		//Ajusto el tamaño segun la cantidad de cupos minimos y la cantidad de cupos existentes.
		//Dentro de la ventana obtenida busco la primer y ultima disponibilidad para achicar aun mas la ventana.
		//Luego la agrando hasta cumplir con el cupo minimo.

		VentanaDeTiempo ventanaResultado;
		
		//1- Tamaño estetico: Es una ventana futura o comienza hoy.
		VentanaDeTiempo ventanaEstatica = helper.obtenerVentanaCalendarioEstaticaIntranet(r);
		
		//2- Obtengo una ventana mas chica ajustada segun los cupos que realmente estan disponibles, posiblemente vacia.
		VentanaDeTiempo ventanaAjustada = helper.obtenerVentanaCalendarioAjustadaIntranet(r, ventanaEstatica);
			
		//Si ventanCuposMinimos = 0, la ventana no se corre.
		if (r.getVentanaCuposMinimos() > 0){ 
			//3- Agrando la ventana hasta cumplir con los cupos minimos si es que existe disponibilidad suficiente.
			VentanaDeTiempo ventanaExtendida = helper.obtenerVentanaCalendarioExtendida(r, ventanaAjustada);
			ventanaResultado = ventanaExtendida;
		}
		else {
			ventanaResultado = ventanaAjustada;
		}

		return ventanaResultado;
	}

	/**
	 * Devuelve las fechas limites para las cuales se tienen disponibilidades, 
	 * cumpliendo con los periodos para realizar reservas indicado en el recurso, es decir:
	 * El inicio del calendario es el primer dia que tenga asigando disponibilidades, 
	 * que este dentro del periodo de vigencia de las disponibilidades indicado en el recurso 
	 * y sea mayor o igual al dia actual.
	 * El fin del calendario es el ultimo dia que tenga asignado disponibilidades,
	 * que este dentro del periodo de vigencia de las disponibilidades indicado en el recurso 
	 * y tratando de satisfacer que:
	 *      	La diferencia entre fin - inicio sea mayor o igual a ventanaDiasMinimos.
	 *          Los cupos disponibles en la ventana sea mayor o igual a ventanaCuposMinimos. Si 
	 *          esto no fuera posible se debe agrandar la ventana hasta llegar
	 *          a la ultima disponibilidad sin pasarse del periodo de fin de disponibilidades.
	 *          
	 * En este caso la ventana de tiempo retornada representa dias, por lo cual, la hora del dia
	 * esta asignada a las 00:00:00.
	 *  
	 * @param recurso
	 * @return
	 * @throws BusinessException 
	 * @throws RolException 
	 */
	public VentanaDeTiempo obtenerVentanaCalendarioInternet(Recurso r) throws BusinessException {

		if (r == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		r = em.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}
	//	chequearPermiso(r.getAgenda());	

		//Ajusto el tamaño segun la cantidad de cupos minimos y la cantidad de cupos existentes.
		//Dentro de la ventana obtenida busco la primer y ultima disponibilidad para achicar aun mas la ventana.
		//Luego la agrando hasta cumplir con el cupo minimo.

		VentanaDeTiempo ventanaResultado;
		
		//1- Tamaño estetico: Es una ventana futura o comienza hoy.
		VentanaDeTiempo ventanaEstatica = helper.obtenerVentanaCalendarioEstaticaInternet(r);
		
		//2- Obtengo una ventana mas chica ajustada segun los cupos que realmente estan disponibles, posiblemente vacia.
		VentanaDeTiempo ventanaAjustada = helper.obtenerVentanaCalendarioAjustadaInternet(r, ventanaEstatica);
			
		//Si ventanCuposMinimos = 0, la ventana no se corre.
		if (r.getVentanaCuposMinimos() > 0){ 
			//3- Agrando la ventana hasta cumplir con los cupos minimos si es que existe disponibilidad suficiente.
			VentanaDeTiempo ventanaExtendida = helper.obtenerVentanaCalendarioExtendida(r, ventanaAjustada);
			ventanaResultado = ventanaExtendida;
		}
		else {
			ventanaResultado = ventanaAjustada;
		}

		return ventanaResultado;
	}

	/**
	 * Devuelve una lista de enteros indicando los cupos disponibles, por dia, asignados al recurso. 
	 * Es decir, para cada dia de la ventana de tiempo, devuelve la cantidad de cupos 
	 * disponibles en ese dia para el recurso dado.
	 * 
	 * Si el dia de inicio de la ventana es "hoy", solo se toman en cuenta los cupos a partir de "ahora",
	 * es decir, las disponibilidades cuya horaInicio sea mayor al tiempo actual.
	 * Para los dias de la ventana que no tengan disponibilidad o sean dias del pasado ( < "hoy"), 
	 * se devolvera un valor negativo indicando que ese dia es no-agendable.
	 * Para los dias de la ventana que no les queden cupos libres, se devolvera 0. 
	 * @param recurso
	 * @param ventana
	 * @return 
	 */
	@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_FCALL_CENTER", "RA_AE_PLANIFICADOR","RA_AE_FATENCION", "RA_AE_ANONIMO"})
	public List<Integer> obtenerCuposPorDia(Recurso r, VentanaDeTiempo v) throws BusinessException {
		
		if (r == null || v == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		r = em.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}
	//	chequearPermiso(r.getAgenda());
		
		//Obtengo la suma de cupos asignados por dia
		List<Object[]> cuposAsignados  = helper.obtenerCuposAsignados(r,v);
		//Obtengo la suma de cupos consumidos (reservas) por dia
		List<Object[]> cuposConsumidos = helper.obtenerCuposConsumidos(r,v);
		//Armo la lista de resultados, indicando los cupos para todos los dias solicitados en la ventana
		List<Integer> cuposXdia = helper.obtenerCuposXDia(v, cuposAsignados, cuposConsumidos);

		return cuposXdia;
	}
	
	
	
	public List<Reserva> consultarReservaPorDatos(Recurso r,
			Map<DatoASolicitar, DatoReserva> datos) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public Reserva consultarReservaPorDatosClave(Recurso r,
			Map<DatoASolicitar, DatoReserva> datos) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public Reserva marcarReserva(List<Disponibilidad> disps) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public void reagendarReservas(List<Reserva> reservas, Date fechaHora) {
		// TODO Auto-generated method stub
		
	}
	


	
	/**
	 * Asegura que el usuario logueado tenga los permisos necesarios sobre la agenda indicada en el parametro
	 * Controles:
	 *  Salvo que el usuario tenga el rol RA_AE_ADMINISTRADOR, exige que el usuario tenga por lo menos 
	 *  uno de los roles dinamicos indicados en la lista de prefijos de roles. Recordar que el rol dinamico es el resultado
	 *  del prefijo + nombre de la agenda.
	 * 
	 * @param agenda
	 * @param rolesGenerales
	 * @throws RolException
	 */
	private void chequearPermiso(Agenda agenda, SAERolPrefijo roles []) {
		
		if (!ctx.isCallerInRole(SAERol.RA_AE_ANONIMO.toString())) {
			if (!ctx.isCallerInRole(SAERol.RA_AE_ADMINISTRADOR.toString()) && roles != null) {
				boolean accesoPermitido = false;
				for (SAERolPrefijo prefijo : roles) {
					if (ctx.isCallerInRole(prefijo + "_" + agenda.getNombre())) {
						accesoPermitido = true;
					}
				}
				if (! accesoPermitido) {
					throw new RolException("No tiene los privilegios suficientes"); 
				}
			}
		}
	}

	/**
	 * Completa campos de la reserva.
	 * Los datos necesarios para el servicio son exigidos en el mismo
	 * @throws ApplicationException 
	 * @throws BusinessException
	 * @throws UserException
	 * @throws AutocompletarException 
	 * @return Map<String, Object>: devuelve un Map con los valores que se desea asignar a cada campo. 
	 */
	public Map<String, Object> autocompletarCampo(ServicioPorRecurso s, Map<String, Object> datosParam) 
		throws ApplicationException, BusinessException, AutocompletarException, UserException {
		
		if (s == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		s = em.find(ServicioPorRecurso.class, s.getId());
		
		if (s == null) {
			throw new BusinessException("-1", "No se encuentra el servicio indicado");
		}
		
		s.getAutocompletadosPorDato().size();
		s.getAutocompletado().getParametrosAutocompletados().size();

		Map<String, Object> campos = helper.autocompletarCampo(s,datosParam);

		return campos;
	}
}
