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
import java.util.Properties;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

import uy.gub.imm.sae.business.api.ConsultasLocal;
import uy.gub.imm.sae.business.api.autocompletados.ejb.AutocompletadorReserva;
import uy.gub.imm.sae.business.api.autocompletados.ejb.ErrorAutocompletado;
import uy.gub.imm.sae.business.api.autocompletados.ejb.ResultadoAutocompletado;
import uy.gub.imm.sae.business.api.autocompletados.ejb.WarningAutocompletado;
import uy.gub.imm.sae.business.api.autocompletados.ejb.exception.UnexpectedAutocompletadoException;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.business.api.validaciones.dto.RecursoDTO;
import uy.gub.imm.sae.business.api.validaciones.ejb.ErrorValidacion;
import uy.gub.imm.sae.business.api.validaciones.ejb.ResultadoValidacion;
import uy.gub.imm.sae.business.api.validaciones.ejb.ValidadorReserva;
import uy.gub.imm.sae.business.api.validaciones.ejb.WarningValidacion;
import uy.gub.imm.sae.business.api.validaciones.ejb.exception.InvalidParametersException;
import uy.gub.imm.sae.business.api.validaciones.ejb.exception.UnexpectedValidationException;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.enumerados.ModoAutocompletado;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorAutocompletarException;
import uy.gub.imm.sae.common.exception.ErrorValidacionCommitException;
import uy.gub.imm.sae.common.exception.ErrorValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionException;
import uy.gub.imm.sae.common.exception.ValidacionPorCampoException;
import uy.gub.imm.sae.common.exception.WarningAutocompletarException;
import uy.gub.imm.sae.common.exception.WarningValidacionCommitException;
import uy.gub.imm.sae.common.exception.WarningValidacionException;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.ParametrosAutocompletar;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.ServicioAutocompletarPorDato;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.Validacion;
import uy.gub.imm.sae.entity.ValidacionPorDato;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;
import uy.gub.imm.sae.entity.ValorConstanteValidacionRecurso;

@Stateless
@PermitAll
public class AgendarReservasHelperBean implements AgendarReservasHelperLocal{

	//Parametro fijo que se pasa en todas las invocaciones a validaciones.
	private final String PARAMETRO_RECURSO = "RECURSO";
	private final String PARAMETRO_RESERVA = "RESERVA";
	private final String PARAMETRO_DATOS_CLAVE = "DATOS_CLAVE";
	
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;

	@EJB
	private ConsultasLocal consultaEJB;
	
	
	/**
	 * Obtiene la ventana del calendario estatica, es decir sin verificar 
	 * los cupos que realmente existen en la ventana.
	 * @param recurso
	 * @return
	 */
	public VentanaDeTiempo obtenerVentanaCalendarioEstaticaIntranet (Recurso recurso) {
		
		//Me aseguro que sea managed
		recurso = em.find(Recurso.class, recurso.getId());
		
		VentanaDeTiempo ventana = new VentanaDeTiempo();

		//FECHA INICIAL
		Date hoy = Utiles.time2InicioDelDia(new Date());
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(hoy);
		cal.add(Calendar.DAY_OF_MONTH, recurso.getDiasInicioVentanaIntranet());
		
		Date fechaInicial = cal.getTime();
		
		ventana.setFechaInicial(recurso.getFechaInicioDisp());
		if (ventana.getFechaInicial().before(fechaInicial)) {
			ventana.setFechaInicial(fechaInicial);
		}

		//FECHA FINAL
		ventana.setFechaFinal(recurso.getDiasVentanaIntranet()-1);
		if (recurso.getFechaFinDisp() != null && recurso.getFechaFinDisp().before(ventana.getFechaFinal())) {
			ventana.setFechaFinal(recurso.getFechaFinDisp());
		}

		return ventana;
	}
	

	/**
	 * Obtiene una ventana mas chica ajustada a las disponibilidades que realmente estan disponibles
	 * (o sea que hay cupos). Si no hay cupos retorna null.
	 */
	@SuppressWarnings("unchecked")
	public VentanaDeTiempo obtenerVentanaCalendarioAjustadaIntranet(Recurso r, VentanaDeTiempo ventana) {

		VentanaDeTiempo ventanaAjustada = new VentanaDeTiempo();
		
		Date ahora = new Date();
		
		//Calculo la fecha de inicio
		Date min = (Date) em
		.createQuery(
		"select min(d.fecha) " +
		"from Disponibilidad d " +
		"where  d.recurso = :rec and " +
		"      d.fechaBaja is null and " +
		"      d.fecha >= :fi and " +
    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora)" 
		)
		.setParameter("rec", r)
		.setParameter("fi", ventana.getFechaInicial(), TemporalType.DATE)
		.setParameter("hoy", ahora, TemporalType.DATE)
		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
		.getSingleResult();
		
		if (min != null ) {
			//Ajusto la ventana
	
			Date hoy = Utiles.time2InicioDelDia(new Date());
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(hoy);
			cal.add(Calendar.DAY_OF_MONTH, r.getDiasInicioVentanaIntranet());
			
			Date fechaInicial = cal.getTime();
			
			ventanaAjustada.setFechaInicial(min);
			if (ventanaAjustada.getFechaInicial().before(fechaInicial)) {
				ventanaAjustada.setFechaInicial(fechaInicial);
			}
			
			//FECHA FINAL
			List<Date> lstMax = (List<Date>) em
			.createQuery(
			"select distinct(d.fecha) " +
			"from Disponibilidad d " +
			"where  d.recurso = :rec and " +
			"      d.fechaBaja is null and " +
			"      d.fecha >= :fi and " +
	    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) " +
	    	"order by d.fecha" 
			)
			.setParameter("rec", r)
			.setParameter("fi", ventana.getFechaInicial(), TemporalType.DATE)
			.setParameter("hoy", ahora, TemporalType.DATE)
			.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
			.setMaxResults(r.getDiasVentanaIntranet())
			.getResultList();
			
			Date max = lstMax.get(lstMax.size() - 1);
			
			ventanaAjustada.setFechaFinal(max);
			if (r.getFechaFinDisp() != null && r.getFechaFinDisp().before(ventanaAjustada.getFechaFinal())) {
				ventanaAjustada.setFechaFinal(r.getFechaFinDisp());
			}

		}
		else {
			//No hay disponibilidades, por lo tanto anulo la ventana
			ventanaAjustada.setFechaInicial(ventana.getFechaInicial());
			ventanaAjustada.setFechaFinal(-1);
		}		

		return ventanaAjustada;
	}
	
	/**
	 * Obtiene la ventana del calendario estatica, es decir sin verificar 
	 * los cupos que realmente existen en la ventana.
	 * @param recurso
	 * @return
	 */
	public VentanaDeTiempo obtenerVentanaCalendarioEstaticaInternet (Recurso recurso) {
		
		//Me aseguro que sea managed
		recurso = em.find(Recurso.class, recurso.getId());
		
		VentanaDeTiempo ventana = new VentanaDeTiempo();

		//FECHA INICIAL
		Date hoy = Utiles.time2InicioDelDia(new Date());
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(hoy);
		cal.add(Calendar.DAY_OF_MONTH, recurso.getDiasInicioVentanaInternet());
		
		Date fechaInicial = cal.getTime();
		
		ventana.setFechaInicial(recurso.getFechaInicioDisp());
		if (ventana.getFechaInicial().before(fechaInicial)) {
			ventana.setFechaInicial(fechaInicial);
		}

		//FECHA FINAL
		ventana.setFechaFinal(recurso.getDiasVentanaInternet()-1);
		if (recurso.getFechaFinDisp() != null && recurso.getFechaFinDisp().before(ventana.getFechaFinal())) {
			ventana.setFechaFinal(recurso.getFechaFinDisp());
		}

		return ventana;
	}
	

	/**
	 * Obtiene una ventana mas chica ajustada a las disponibilidades que realmente estan disponibles
	 * (o sea que hay cupos). Si no hay cupos retorna null.
	 */
	@SuppressWarnings("unchecked")
	public VentanaDeTiempo obtenerVentanaCalendarioAjustadaInternet(Recurso r, VentanaDeTiempo ventana) {

		VentanaDeTiempo ventanaAjustada = new VentanaDeTiempo();
		
		Date ahora = new Date();
		
		//Calculo la fecha de inicio
		Date min = (Date) em
		.createQuery(
		"select min(d.fecha) " +
		"from Disponibilidad d " +
		"where  d.recurso = :rec and " +
		"      d.fechaBaja is null and " +
		"      d.fecha >= :fi and " +
    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora)" 
		)
		.setParameter("rec", r)
		.setParameter("fi", ventana.getFechaInicial(), TemporalType.DATE)
		.setParameter("hoy", ahora, TemporalType.DATE)
		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
		.getSingleResult();
		
		if (min != null ) {
			//Ajusto la ventana
	
			Date hoy = Utiles.time2InicioDelDia(new Date());
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(hoy);
			cal.add(Calendar.DAY_OF_MONTH, r.getDiasInicioVentanaInternet());
			
			Date fechaInicial = cal.getTime();
			
			ventanaAjustada.setFechaInicial(min);
			if (ventanaAjustada.getFechaInicial().before(fechaInicial)) {
				ventanaAjustada.setFechaInicial(fechaInicial);
			}
			
			//FECHA FINAL
			List<Date> lstMax = (List<Date>) em
			.createQuery(
			"select distinct(d.fecha) " +
			"from Disponibilidad d " +
			"where  d.recurso = :rec and " +
			"      d.fechaBaja is null and " +
			"      d.fecha >= :fi and " +
	    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) " +
	    	"order by d.fecha" 
			)
			.setParameter("rec", r)
			.setParameter("fi", ventana.getFechaInicial(), TemporalType.DATE)
			.setParameter("hoy", ahora, TemporalType.DATE)
			.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
			.setMaxResults(r.getDiasVentanaInternet())
			.getResultList();
			
			Date max = lstMax.get(lstMax.size() - 1);
			
			ventanaAjustada.setFechaFinal(max);
			if (r.getFechaFinDisp() != null && r.getFechaFinDisp().before(ventanaAjustada.getFechaFinal())) {
				ventanaAjustada.setFechaFinal(r.getFechaFinDisp());
			}

		}
		else {
			//No hay disponibilidades, por lo tanto anulo la ventana
			ventanaAjustada.setFechaInicial(ventana.getFechaInicial());
			ventanaAjustada.setFechaFinal(-1);
		}		

		return ventanaAjustada;
	}
	
	/**
	 * Obtiene una ventana posiblemente mas grande que cumpla con la cantidad de cupos minimos indicada en el recurso.
	 * Esto solo sera posible si efectivamente existe disponibilidad suficiente hacia el futuro.
	 */
	@SuppressWarnings("unchecked")
	public VentanaDeTiempo obtenerVentanaCalendarioExtendida(Recurso r, VentanaDeTiempo ventana) {
		
		//TODO Falta implementar
		//Se calcula la cantidad de cupos que hay en la ventana
	
		Date ahora = new Date();
		
		//Se obtiene lista de Cupos
		List<Object[]> listaCupos = (List<Object[]>) em
		.createQuery(
		"select d.fecha, sum(d.cupo) " +
		"from  Disponibilidad d  " +
		"where d.recurso = :rec and " +
		"      d.fechaBaja is null and " +
		"      d.fecha >= :fi and " +
    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) " +
		"group by d.fecha " +
		"order by d.fecha asc "
		)
		.setParameter("rec", r)
		.setParameter("fi", ventana.getFechaInicial(), TemporalType.DATE)
		.setParameter("hoy", ahora, TemporalType.DATE)
		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
		.getResultList();
		

		//Se obtiene lista de Reservas
		List<Object[]> listaReservas = (List<Object[]>) em
		.createQuery(
		"select d.fecha, count(r) " +
		"from  Disponibilidad d join d.reservas r " +
		"where d.recurso = :rec and " +
		"      d.fechaBaja is null and " +
		"      d.fecha >= :fi and " +
    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) and " +
		"      r.estado <> :cancelado " +
		"group by d.fecha " +
		"order by d.fecha asc "
		)
		.setParameter("rec", r)
		.setParameter("fi", ventana.getFechaInicial(), TemporalType.DATE)
		.setParameter("hoy", ahora, TemporalType.DATE)
		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
		.setParameter("cancelado", Estado.C)
		.getResultList();

		//Si la cantidad de cupos es menor que ventanaCuposMinimos del recurso,
		//se aumenta la cantidad de dias hasta llegar a ese valor (mientras no se llegue a fechaFinDisp
		// y sigan existiendo disponibilidades).

		Date fechaHasta = null;
		Long cuposDisp = new Long(0);
		Date fechaIterCupo = null;
		Date fechaIterReserva = null;
		
		Iterator<Object[]> iCupos = listaCupos.iterator();
		Iterator<Object[]> iReservas = listaReservas.iterator();
		
		Object[] reserva = null;
		
		if(iReservas.hasNext()){
			reserva = iReservas.next();
			fechaIterReserva = (Date)reserva[0];
		}
		
		while ( cuposDisp < r.getVentanaCuposMinimos() && iCupos.hasNext()){
			//Se controla si existen Disponibilidades
			
			Object[] cupo= iCupos.next();
			fechaIterCupo = (Date)cupo[0];
			
			if(fechaIterReserva!=null && fechaIterCupo.equals(fechaIterReserva)){
				cuposDisp = cuposDisp + (Long)cupo[1] - (Long)reserva[1];

				if(iReservas.hasNext()){
					reserva = iReservas.next();
					fechaIterReserva = (Date)reserva[0];
				} else {
					fechaIterReserva = null;
				}
			} else {
				cuposDisp = cuposDisp + (Long)cupo[1];
			}
			fechaHasta = fechaIterCupo;
			
		}
	
		if ((fechaHasta != null) && ventana.getFechaFinal().before(fechaHasta)){
			ventana.setFechaFinal(fechaHasta);
		}
		
		return ventana;
	}
	
	/**
	 * Obtiene los cupos asignados, es decir la suma de los cupos de las disponibilidades existentes no nulas
	 * dentro de la ventana indicada. Para el rango de la ventana que caiga en el pasado o fuera 
	 * del inicio de disponibilidad indicado en el recurso, no se devuelve cupos.
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> obtenerCuposAsignados(Recurso r, VentanaDeTiempo v) {
		
		//La clono pues la voy a modificar.
		v = new VentanaDeTiempo(v);
		
		Date ahora = new Date();

		//Elimino el PASADO
		if (v.getFechaInicial().before(ahora)) {
			v.setFechaInicial(ahora);
		}
		if (v.getFechaInicial().before(r.getFechaInicioDisp())) {
			v.setFechaInicial(r.getFechaInicioDisp());
		}
		
		//Cupos asignados por dia.
		List<Object[]> cuposAsignados = em
			.createQuery(
			"select d.fecha, sum(d.cupo) " + 
			"from  Disponibilidad d " +
			"where d.recurso = :rec and " +
			"      d.fechaBaja is null and " +
			"      d.fecha between :fi and :ff and " +
	    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) " +
			"group by d.fecha " +
			"order by d.fecha asc ")
			.setParameter("rec", r)
			.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
			.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
			.setParameter("hoy", ahora, TemporalType.DATE)
			.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
			.getResultList();
		
		return cuposAsignados;
	}

	
	/**
	 * Obtiene los cupos consumidos, es decir la suma de las reservas no canceladas
	 * dentro de la ventana indicada. Para el rango de la ventana que caiga en el pasado o fuera 
	 * del inicio de disponibilidad indicado en el recurso, no se devuelve cupos.
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> obtenerCuposConsumidos(Recurso r, VentanaDeTiempo v) {
		
		//La clono pues la voy a modificar.
		v = new VentanaDeTiempo(v);
		
		Date ahora = new Date();

		//Elimino el PASADO
		if (v.getFechaInicial().before(ahora)) {
			v.setFechaInicial(ahora);
		}
		if (v.getFechaInicial().before(r.getFechaInicioDisp())) {
			v.setFechaInicial(r.getFechaInicioDisp());
		}
		
		//Cupos consumidos, es decir, cantidad de reservas por dia no canceladas.
		List<Object[]> cuposConsumidos = em
			.createQuery(
			"select d.fecha, count(reserva) " + 
			"from  Disponibilidad d " +
			"      left join d.reservas reserva " +
			"where d.recurso = :rec and " +
			"      d.fechaBaja is null and " +
			"      d.fecha between :fi and :ff and " +
	    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) and " +
			"      (reserva is null or reserva.estado <> :cancelado) " +
			"group by d.fecha " +
			"order by d.fecha asc ")
			.setParameter("rec", r)
			.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
			.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
			.setParameter("hoy", ahora, TemporalType.DATE)
			.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
			.setParameter("cancelado", Estado.C)
			.getResultList();

		return cuposConsumidos;
	}
	
	
	//Armo la lista de resultados, indicando los cupos para todos los dias solicitados (parametro ventana)
	public List<Integer> obtenerCuposXDia(VentanaDeTiempo v, List<Object[]> cuposAsignados, List<Object[]> cuposConsumidos) {
	
		Iterator<Object[]> iterCuposAsignados  = cuposAsignados.iterator();
		Iterator<Object[]> iterCuposConsumidos = cuposConsumidos.iterator();

		List<Integer> cuposXdia = new ArrayList<Integer>();
		
		Calendar cont = Calendar.getInstance();
		cont.setTime(Utiles.time2InicioDelDia(v.getFechaInicial()));
		Object[] cupoAsignado  = null;
		Object[] cupoConsumido = null;
		if (iterCuposAsignados.hasNext()) {
			cupoAsignado = iterCuposAsignados.next();
		}
		if (iterCuposConsumidos.hasNext()) {
			cupoConsumido = iterCuposConsumidos.next();
		}

		//Recorro la ventana dia a dia y voy generando la lista completa de cupos x dia con -1, 0, >0 segun corresponda.
		while (!cont.getTime().after(v.getFechaFinal())) {

			Integer cantidadDeCupos = -1;
			
			//avanzo un lugar en la lista de cupos x dia si la fecha del cupo es igual a la fecha del contador.
			if (cupoAsignado != null) {
				Date fechaDelCupoA = (Date)cupoAsignado[0];
				if (fechaDelCupoA.equals(cont.getTime())) {
					//Nunca deberia ser mas grande que un Entero.	
					cantidadDeCupos = ((Long)cupoAsignado[1]).intValue();
					if (iterCuposAsignados.hasNext()) {
						cupoAsignado = iterCuposAsignados.next();
					}
					else {
						cupoAsignado = null;
					}
					
					if (cupoConsumido != null) {
						Date fechaDelCupoC = (Date)cupoConsumido[0];
						if (fechaDelCupoC.equals(cont.getTime())) {
							//Nunca deberia ser mas grande que un Entero.	
							cantidadDeCupos -= ((Long)cupoConsumido[1]).intValue();
							
							if (cantidadDeCupos < -1) {
								//Solo se da en el caso de que mas de uno hallan querido reservar a la vez cuando quedaba solo un cupo
								cantidadDeCupos = -1;
							}
							
							if (iterCuposConsumidos.hasNext()) {
								cupoConsumido = iterCuposConsumidos.next();
							}
							else {
								cupoConsumido = null;
							}
						}
					}
				}
			}
			
			cuposXdia.add(cantidadDeCupos);
			cont.add(Calendar.DAY_OF_MONTH, 1);
		}

		return cuposXdia;
	}
	
	
	/**
	 * Crea la reserva como pendiente, realiza todo en una transaccion independiente
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Reserva crearReservaPendiente(Disponibilidad d) {
		
		d = em.find(Disponibilidad.class, d.getId());
		
		//Creo y seteo atributos
		Reserva reserva = new Reserva();
		reserva.setEstado(Estado.P);
		reserva.getDisponibilidades().add(d);
		reserva.setFechaCreacion(new Date());
		em.persist(reserva);
		
		return reserva;
	}
	
	public boolean chequeoCupoNegativo (Disponibilidad d) {
		
		int cantReservas = ((Long) em
		.createQuery(
		"select count(*) " +
		"from  Disponibilidad d join d.reservas r " +
		"where d = :d and " +
		"      r.estado <> :cancelado "
		)
		.setParameter("d", d)
		.setParameter("cancelado", Estado.C)
		.getSingleResult()).intValue();
			
		if (d.getCupo() - cantReservas < 0) {
			return true;
		}
		else {
			return false;
		}
	}



	/**
	 * Retorna los datos a solicitar vivos del recurso 
	 */
	public List<DatoASolicitar> obtenerDatosASolicitar(Recurso r) {

		@SuppressWarnings("unchecked")
		List<DatoASolicitar> campos = (List<DatoASolicitar>) em.createQuery(
		"select c " +
		"from  DatoASolicitar c " +
		"where c.agrupacionDato.recurso = :r and " +
		"      c.fechaBaja = null " +
		"order by c.agrupacionDato.orden, c.fila, c.columna "
		)
		.setParameter("r", r)
		.getResultList();

		return campos;
	}

	
	public List<ValidacionPorRecurso> obtenerValidacionesPorRecurso(Recurso r) {
		
		@SuppressWarnings("unchecked")
		List<ValidacionPorRecurso> validaciones = (List<ValidacionPorRecurso>) em.createQuery(
		"select vxr " +
		"from  ValidacionPorRecurso vxr " +
		"where vxr.recurso = :r and " +
		"      vxr.fechaBaja = null " +
		"order by vxr.ordenEjecucion asc "
		)
		.setParameter("r", r)
		.getResultList();

		return validaciones;
	}
	
	public void validarDatosRequeridosReserva(List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException, ValidacionException {
		
		Map<String, DatoASolicitar> camposMap = new HashMap<String, DatoASolicitar>();
		for (DatoASolicitar datoASolicitar : campos) {
			camposMap.put(datoASolicitar.getNombre(), datoASolicitar);
		}
		
		//Chequea que todos los valores pertenezcan a campos definidos.
		for (String nombreCampo : valores.keySet()) {
			if (camposMap.get(nombreCampo) == null) {
				throw new BusinessException("-1", "No se puede insertar un valor para un DatoASolicitar que no existe en el sistema");
			}
		}
		
		List<String> camposRequeridos = new ArrayList<String>();
		
		//Chequeo existencia de campos obligatorios
		for (DatoASolicitar campo : campos) {
			
			DatoReserva dato = valores.get(campo.getNombre());

			//Chequeo campo requerido
			if (campo.getRequerido() && dato == null) {
				camposRequeridos.add(campo.getNombre());
			}
		}

		//Si faltan campos requeridos
		if (camposRequeridos.size() > 0) {
			String mensaje = "Hay campos requeridos sin rellenar";
			throw new ValidacionException("-1", mensaje, camposRequeridos, null);
		}

	}
	
	public void validarTipoDatosReserva(List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException, ValidacionPorCampoException {
		
		Map<String, DatoASolicitar> camposMap = new HashMap<String, DatoASolicitar>();
		for (DatoASolicitar datoASolicitar : campos) {
			camposMap.put(datoASolicitar.getNombre(), datoASolicitar);
		}
		
		//Chequea que todos los valores pertenezcan a campos definidos.
		for (String nombreCampo : valores.keySet()) {
			if (camposMap.get(nombreCampo) == null) {
				throw new BusinessException("-1", "No se puede insertar un valor para un DatoASolicitar que no existe en el sistema");
			}
		}

		List<String> camposInvalidos  = new ArrayList<String>();
		List<String> mensajes = new ArrayList<String>();
		
		//Chequeo formato
		for (DatoASolicitar campo : campos) {
			
			DatoReserva dato = valores.get(campo.getNombre());

			if (dato != null) {
				if (dato.getValor() == null || dato.getValor().equals("")) {
					throw new BusinessException("-1", "No se puede insertar un DatoReserva con valor en nulo (DatoASolicitar: "+ campo.getNombre() +")");
				}
				
				//TODO completar el chequeo de formato del DatoReserva
				if (campo.getTipo() == Tipo.NUMBER) {
					try {
						Integer.parseInt(dato.getValor());
					}
					catch (NumberFormatException e) {
						camposInvalidos.add(campo.getNombre());
						mensajes.add("El campo " + campo.getEtiqueta() + " debe contener solo digitos");
					}
				}

			}
		}

		//Si hay campos invalidos
		if (camposInvalidos.size() > 0) {
			throw new ValidacionPorCampoException("-1", camposInvalidos, mensajes);
		}
		
	}
	
	public void validarDatosReservaBasico(List<DatoASolicitar> campos, Map<String, DatoReserva> valores) throws BusinessException, ValidacionException {

		// Chequeo campos requeridos
		validarDatosRequeridosReserva(campos, valores);
		
		// Chequeo formato de los valores segun el tipo de dato
		validarTipoDatosReserva(campos, valores);
		
	}

	// TODO: REVISAR RESERVA NUEVA no estaba mergeado con R001
	//El parametro reservaNueva es opcional, si se pasa null la consulta de reservas existentes se haria partir 
	//de ahora en lugar de tomar la fecha de creacion de la reserva.
	public List<Reserva> validarDatosReservaPorClave(Recurso recurso, Reserva reservaNueva,List<DatoASolicitar> campos, Map<String, DatoReserva> valores) 
	throws BusinessException {

	//Se supone que si un campo es clave tiene que ser requerido.
	//Si cambia este supuesto, se deberá revisar este procedimiento.
	
	List<Reserva> listaReserva = new ArrayList<Reserva>();
	List<DatoReserva> datoReservaLista = new ArrayList<DatoReserva>();
	
	Map<String, DatoASolicitar> camposMap = new HashMap<String, DatoASolicitar>();
	for (DatoASolicitar datoASolicitar : campos) {
		camposMap.put(datoASolicitar.getNombre(), datoASolicitar);
	}

	List<DatoASolicitar> camposClave = new ArrayList<DatoASolicitar>();
	
	//Se carga lista de camposClave
	for (DatoASolicitar campo : campos) {
		//Chequeo si el campo es clave
		if (campo.getEsClave() ) {
			camposClave.add(campo);
		}
	}
	
	//Si controla si existen campos clave
	if (camposClave.size() > 0) {
		//Se controla que no exista en la base otra reserva con la misma clave.
		
		Iterator<DatoASolicitar> iCampo = camposClave.iterator();
		while (iCampo.hasNext()){
				
			DatoASolicitar datoASolicitar = iCampo.next();
			DatoReserva datoReserva = valores.get(datoASolicitar.getNombre());
			datoReservaLista.add(datoReserva);
		}

		//Alvaro 17/08/2009 - Se debe controlar la clave unica desde el momento de creacion de la reserva como pendiente
		//                    y no desde ahora, pues ahora > fechaCreacion
		//                    Cambio: new Date() <--> reservaNueva.getFechaCreacion();
		Date ahora = new Date();
		if (reservaNueva != null) {
			ahora = reservaNueva.getFechaCreacion();
		}
		
		// consulto las reservas por dato de reserva (solo para los campos clave)
		listaReserva = consultaEJB.consultarReservaDatosHora(datoReservaLista, recurso,ahora);
		
	}
		return listaReserva;
		
	}

	public void validarDatosReservaExtendido(
			List<ValidacionPorRecurso> validaciones, List<DatoASolicitar> campos, 
			Map<String, DatoReserva> valores, Boolean noLanzarWarning, ReservaDTO reserva) throws ApplicationException, BusinessException, ErrorValidacionException, WarningValidacionException, ErrorValidacionCommitException, WarningValidacionCommitException {

		 	Map<String, String> datosClave = new HashMap<String, String>();
		 	// Si no hay ninguna validación no necesito armar el Map, de modo que no
		 	// busco una forma alternativa de obtener el recurso.
		    if (validaciones.size()>0){
		    	datosClave = copiarDatos(valores, validaciones.get(0).getRecurso());
		    }
		
		for(ValidacionPorRecurso vXr : validaciones) {

			Validacion v = vXr.getValidacion();

			if (v.getFechaBaja() == null) {
			
				List<ValidacionPorDato> camposDeLaValidacion = vXr.getValidacionesPorDato();
				
				Map<String, Object> parametros = new HashMap<String, Object>();
				List<String> nombreCampos = new ArrayList<String>();
				
				for (ValidacionPorDato validacionPorDato : camposDeLaValidacion) {
					if (validacionPorDato.getFechaDesasociacion() == null) {
						String nombreParametro = validacionPorDato.getNombreParametro();
						DatoASolicitar campo = validacionPorDato.getDatoASolicitar();
						DatoReserva dato = valores.get(campo.getNombre());
						if (dato != null) {
							//TODO parsear el valor de String al tipo que corresponda: Stirng, Integer, Date
							if (campo.getTipo() == Tipo.NUMBER){
								parametros.put(nombreParametro, Integer.valueOf(dato.getValor()));
							}
							else if (campo.getTipo() == Tipo.BOOLEAN) {
								parametros.put(nombreParametro, Boolean.valueOf(dato.getValor()));
							}
							else {
								parametros.put(nombreParametro, dato.getValor());
							}
						}
						else {
							//2013-10-30 - Carlos - Se descomenta para que se ejecuten las validaciones a pesar de que todos los campos sean null.
							parametros.put(nombreParametro, null);
							
							//Este codigo esta en las acciones, pero aca no puedo ponerlo mientras tenga mas adelante
							//el quequeo isEmpty para no llamar a una validacion si todos los campos de la misma son opcionales
							//y estan en null. No se hay que analizarlo mas.
						}
						nombreCampos.add(campo.getNombre());
						
					}
				}
				
				List<ValorConstanteValidacionRecurso> constantesDeLaValidacion = vXr.getConstantesValidacion();
			    
				for (ValorConstanteValidacionRecurso valorConstante: constantesDeLaValidacion){
					if (valorConstante.getFechaDesasociacion() == null){
						parametros.put(valorConstante.getNombreConstante(), valorConstante.getValor());
					}
				}
				
				
// Si parametros.isEmpty no llamo a la validacion, para que funcione bien con campos que
// permiten valores nulos
			if (!parametros.isEmpty()||nombreCampos.isEmpty()){
				parametros.put(PARAMETRO_RECURSO, copiarRecurso(vXr.getRecurso()));
				parametros.put(PARAMETRO_RESERVA, reserva);
				parametros.put(PARAMETRO_DATOS_CLAVE, datosClave);
				
				ValidadorReserva validador;
				validador = this.getValidador(v.getHost(), v.getServicio());
				// PAra que no reviente cuando la validación de reagenda
				// devuelve false
				if (nombreCampos.isEmpty()) {
					nombreCampos.add("DUMMY");
				}
				
				try {
					//Ejecuto la validacion
					ResultadoValidacion resultado =  validador.validarDatosReserva(v.getNombre(), parametros);
					
					//Hay errores
					if (resultado.getErrores().size() > 0) {
						List<String> mensajes = new ArrayList<String>();
						List<String> codigosErrorMensajes = new ArrayList<String>();
						for (ErrorValidacion error : resultado.getErrores()) {
							mensajes.add(error.getMensaje());
							codigosErrorMensajes.add(error.getCodigo());
						}
						throw new ErrorValidacionException("-1", nombreCampos, mensajes, codigosErrorMensajes, v.getNombre());
					}
					
					//Hay warnings y los quiero lanzar
					if (resultado.getWarnings().size() > 0 && ! noLanzarWarning) {
						List<String> mensajes = new ArrayList<String>();
						List<String> codigosErrorMensajes = new ArrayList<String>();
						for (WarningValidacion warning : resultado.getWarnings()) {
							mensajes.add(warning.getMensaje());
							codigosErrorMensajes.add(warning.getCodigo());
						}
						throw new WarningValidacionException("-1", nombreCampos, mensajes, codigosErrorMensajes, v.getNombre());
					}
					
					//Hay errores con commit
					if (resultado.getErroresConCommit().size() > 0) {
						List<String> mensajes = new ArrayList<String>();
						List<String> codigosErrorMensajes = new ArrayList<String>();
						for (ErrorValidacion error : resultado.getErroresConCommit()) {
							mensajes.add(error.getMensaje());
							codigosErrorMensajes.add(error.getCodigo());
						}
						throw new ErrorValidacionCommitException("-1", nombreCampos, mensajes, codigosErrorMensajes, v.getNombre());
					}
					
					//Hay warnings con commit y los quiero lanzar
					if (resultado.getWarningsConCommit().size() > 0 && ! noLanzarWarning) {
						List<String> mensajes = new ArrayList<String>();
						List<String> codigosErrorMensajes = new ArrayList<String>();
						for (WarningValidacion warning : resultado.getWarningsConCommit()) {
							mensajes.add(warning.getMensaje());
							codigosErrorMensajes.add(warning.getCodigo());
						}
						throw new WarningValidacionCommitException("-1", nombreCampos, mensajes, codigosErrorMensajes, v.getNombre());
					}
				} catch (UnexpectedValidationException e) {
					throw new ApplicationException(e);
				} catch (InvalidParametersException e) {
					List<String> mensajes = new ArrayList<String>();
					mensajes.add(e.getMessage());
					throw new ErrorValidacionException("-1", nombreCampos, mensajes);
				}
			}
			} // TODO revisar que sea el lugar correcto para cerrar el if !(parametros.isEmpty)
		}
	}
	
	
	private RecursoDTO copiarRecurso(Recurso recurso) {
		
		RecursoDTO recursoDTO = new RecursoDTO();
		
		recursoDTO.setId(recurso.getId());
		recursoDTO.setNombre(recurso.getNombre());
		recursoDTO.setDescripcion(recurso.getDescripcion());
		recursoDTO.setCantDiasAGenerar(recurso.getCantDiasAGenerar());
		recursoDTO.setFechaBaja(recurso.getFechaBaja());
		recursoDTO.setFechaFin(recurso.getFechaFin());
		recursoDTO.setFechaFinDisp(recurso.getFechaFinDisp());
		recursoDTO.setFechaInicio(recurso.getFechaInicio());
		recursoDTO.setFechaInicioDisp(recurso.getFechaInicioDisp());
		recursoDTO.setMostrarNumeroEnLlamador(recurso.getMostrarNumeroEnLlamador());
		recursoDTO.setReservaMultiple(recurso.getReservaMultiple());
		recursoDTO.setVentanaCuposMinimos(recurso.getVentanaCuposMinimos());
		recursoDTO.setDiasInicioVentanaIntranet(recurso.getDiasInicioVentanaIntranet());
		recursoDTO.setDiasVentanaIntranet(recurso.getDiasVentanaIntranet());
		recursoDTO.setDiasInicioVentanaInternet(recurso.getDiasInicioVentanaInternet());
		recursoDTO.setDiasVentanaInternet(recurso.getDiasVentanaInternet());
		
		return recursoDTO;
	}
	
	private Map<String,String> copiarDatos(Map<String, DatoReserva> valores, Recurso rec){
		Map<String, String> retorno = new HashMap<String, String>();
		List<DatoASolicitar> datosSol = rec.getDatoASolicitar();
		Iterator<DatoASolicitar> it = datosSol.iterator();
		while (it.hasNext()){
			DatoASolicitar dato = (DatoASolicitar)it.next();
			if (dato.getEsClave() && (dato.getFechaBaja()==null)){
				try {
				String valor = (String)valores.get(dato.getNombre()).getValor();
				retorno.put(dato.getNombre(), valor);
				}
				catch (Exception e){
					System.out.println(e.getMessage()+" "+dato.getNombre()+" "+rec.getNombre());;
				}
			}
		}
		return retorno;
	}

	private ValidadorReserva getValidador(String host, String jndiName) throws ApplicationException {

		Object ejb = null;
		try {
			InitialContext ctx; 
			if (host != null) {
				
				//Properties props = new Properties();
				//props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
				//props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
				//props.put("java.naming.provider.url", host);
				//ctx = new InitialContext(props);
				ctx = new InitialContext();
			} else {
				ctx = new InitialContext();
			}
			
			ejb = ctx.lookup(jndiName);
		} catch (NamingException e) {
			throw new ApplicationException("-1", "No se pudo acceder a un EJB de tipo ValidadorReserva (jndiName: "+jndiName+")", e);
	    }
		
		ValidadorReserva validador = null;
		if (ejb instanceof ValidadorReserva) {
			validador = (ValidadorReserva) ejb;
		}
		else {
			throw new ApplicationException("-1", "Se esperaba un EJB de tipo ValidadorReserva y se encontró uno del tipo " + ejb.getClass());
		}
		
		return validador;
	}
	
	public Map<String, Object> autocompletarCampo(ServicioPorRecurso s, Map<String, Object> datosParam) throws ApplicationException, BusinessException, ErrorAutocompletarException, WarningAutocompletarException{
		
		Map<String, Object> campos = new HashMap<String, Object>();
		
		List<ServicioAutocompletarPorDato> lstDatos = s.getAutocompletadosPorDato();
		List<ParametrosAutocompletar> parametros = s.getAutocompletado().getParametrosAutocompletados();
		
		Map<String,Object> paramEntradaMap = new HashMap<String, Object>();
		Map<String,Object> paramSalidaMap = new HashMap<String, Object>();
		List<String> nombreCampos = new ArrayList<String>();
		
		//Se crean las estructuras para los parametros de entrada y salida, mapeando los parametros con los datos a solicitar
		for (ParametrosAutocompletar param : parametros){
			for (ServicioAutocompletarPorDato sDato : lstDatos){
				if ( (sDato.getFechaDesasociacion()) == null && (sDato.getNombreParametro().equals(param.getNombre())) ){
					if (ModoAutocompletado.ENTRADA.equals(param.getModo())){
						nombreCampos.add(sDato.getDatoASolicitar().getNombre());
						paramEntradaMap.put(param.getNombre(), datosParam.get(sDato.getDatoASolicitar().getNombre()));
					}else if (ModoAutocompletado.SALIDA.equals(param.getModo())){
						paramSalidaMap.put(param.getNombre(), sDato.getDatoASolicitar().getNombre());
					}
				}
			}
		}
		
		//Se obtiene el servicio de autocompletar que se va a ejecutar
		AutocompletadorReserva servicioAutocompletador = this.getAutocompletador(s.getAutocompletado().getHost(), s.getAutocompletado().getServicio());
		
		try {
			//Se invoca el servicio de autocompletar
			ResultadoAutocompletado resultado = servicioAutocompletador.autocompletarDatosReserva(s.getAutocompletado().getNombre(), paramEntradaMap);
			
			for (String resultKey : resultado.getResultados().keySet()){
				//Mapeo los valores devueltos para los parametros de salida con los datos a solicitar que se van a completar
				if (paramSalidaMap.containsKey(resultKey)){
					campos.put(paramSalidaMap.get(resultKey).toString(), resultado.getResultados().get(resultKey));
				}
			}
			
			if (resultado.getErrores().size() > 0) {
				List<String> mensajes = new ArrayList<String>();
				List<String> codigosErrorMensajes = new ArrayList<String>();
				for (ErrorAutocompletado error : resultado.getErrores()) {
					mensajes.add(error.getMensaje());
					codigosErrorMensajes.add(error.getCodigo());
				}
				throw new ErrorAutocompletarException("-1", nombreCampos, mensajes, codigosErrorMensajes, s.getAutocompletado().getNombre());
			}
			
			if (resultado.getErrores().size() > 0) {
				List<String> mensajes = new ArrayList<String>();
				List<String> codigosErrorMensajes = new ArrayList<String>();
				for (ErrorAutocompletado error : resultado.getErrores()) {
					mensajes.add(error.getMensaje());
					codigosErrorMensajes.add(error.getCodigo());
				}
				throw new ErrorAutocompletarException("-1", nombreCampos, mensajes, codigosErrorMensajes, s.getAutocompletado().getNombre());
			}
			
			if (resultado.getWarnings().size() > 0) {
				List<String> mensajes = new ArrayList<String>();
				List<String> codigosWarningMensajes = new ArrayList<String>();
				for (WarningAutocompletado warning : resultado.getWarnings()) {
					mensajes.add(warning.getMensaje());
					codigosWarningMensajes.add(warning.getCodigo());
				}
				throw new WarningAutocompletarException("-1", nombreCampos, mensajes, codigosWarningMensajes, s.getAutocompletado().getNombre());
			}
			
		} catch (UnexpectedAutocompletadoException e) {
			throw new ApplicationException(e);
		} catch (uy.gub.imm.sae.business.api.autocompletados.ejb.exception.InvalidParametersException e) {
			List<String> mensajes = new ArrayList<String>();
			mensajes.add(e.getMessage());
			throw new ErrorAutocompletarException("-1", nombreCampos, mensajes);
		}
		
		
		return campos;
	}
	
	private AutocompletadorReserva getAutocompletador(String host, String jndiName) throws ApplicationException {

		Object ejb = null;
		try {
			InitialContext ctx; 
			if (host != null) {
				Properties props = new Properties();
				props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
				props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
				props.put("java.naming.provider.url", host);
				ctx = new InitialContext(props);
			} else {
				ctx = new InitialContext();
			}
			
			ejb = ctx.lookup(jndiName);
		} catch (NamingException e) {
			throw new ApplicationException("-1", "No se pudo acceder a un EJB de tipo AutocompletadorReserva (jndiName: "+jndiName+")", e);
	    }
		
		AutocompletadorReserva autocompletador = null;
		if (ejb instanceof AutocompletadorReserva) {
			autocompletador = (AutocompletadorReserva) ejb;
		}
		else {
			throw new ApplicationException("-1", "Se esperaba un EJB de tipo AutocompletadorReserva y se encontró uno del tipo " + ejb.getClass());
		}
		
		return autocompletador;
	}
}


