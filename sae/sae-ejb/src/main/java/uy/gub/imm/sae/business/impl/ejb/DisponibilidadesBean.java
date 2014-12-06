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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.Calendario;
import uy.gub.imm.sae.business.api.Disponibilidades;
import uy.gub.imm.sae.business.api.DisponibilidadesLocal;
import uy.gub.imm.sae.business.api.DisponibilidadesRemote;
import uy.gub.imm.sae.business.impl.factories.CalendarioFactory;
import uy.gub.imm.sae.common.DisponibilidadReserva;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.RolException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Plantilla;
import uy.gub.imm.sae.entity.Recurso;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR"})
public class DisponibilidadesBean implements DisponibilidadesLocal, DisponibilidadesRemote {

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;

	static Logger logger = Logger.getLogger(DisponibilidadesBean.class);
	
	public List<Disponibilidades> consultarDisponibilidadesSolapadas(Recurso r,
			Plantilla p, VentanaDeTiempo v) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public void eliminarDisponibilidades(Recurso r, VentanaDeTiempo v) throws BusinessException, UserException {
		
		r = entityManager.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1","No existe el recurso");
		}
		
		//La fecha final no puede ser nula
		if (v.getFechaFinal() == null){
			throw new BusinessException("-1", "La fecha final no puede ser nula");			
		}
		
		//TODO falta reviar el lockeo por si alguien reserva mientras elimino disponibilidades
		entityManager.lock(r,LockModeType.WRITE);
		entityManager.flush();

		Long cantReservasVivas =  (Long) entityManager
		.createQuery(
		"select count(d.id) " +
		"from  Disponibilidad d join d.reservas reserva " +
		"where d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff and " +
		"      (reserva.estado <> :cancelado) ")
		.setParameter("r", r)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
		.setParameter("cancelado", Estado.C)
		.getSingleResult();		
		
		if ( cantReservasVivas > 0){
			throw new UserException("-1", "No se puede eliminar disponibilidades con reservas vivas.");			
		}
		
		//Se obtienen las disponibilidades a eliminar
		List<Disponibilidad> disponibilidades =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff " +
		"order by d.fecha asc, d.horaInicio ")
		.setParameter("r", r)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
		.getResultList();				
		
		//Si la lista obtenida es vacía no hay nada que eliminar
		if (disponibilidades == null || disponibilidades.isEmpty()){
			throw new UserException("-1", "No existen disponibilidades para el período indicado");			
		}
		else {
			Date ahora = new Date();
			for (Disponibilidad d : disponibilidades) {
				d.setFechaBaja(ahora);
			}
		}
		
	}

	/**
	 * Genera disponibilidades en una fecha para un recurso.
	 * No se controla que los horarios se solapen. 
	 * Controla: 
	 * 1) Que no exista una disponibilidad viva para la misma fecha y la misma hora.
	 * 2) Solo se generen disponibilidades para días que se encuentren marcados
	 *    como hábiles en sp_dias.
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public void generarDisponibilidadesNuevas(Recurso r, Date fecha, Date horaDesde, Date horaHasta, 
			Integer frecuencia, Integer cupo) throws UserException, ApplicationException {
		
		Recurso rManaged = entityManager.find(Recurso.class, r.getId());
		if (rManaged == null) {
			throw new UserException("AE10022","No existe el recurso");
		}
		
		//JPA utiliza lock optimista. En el momento de grabar los cambios
		//si la entidad fue modificada levanta una excepción.
		entityManager.lock(rManaged,LockModeType.WRITE);
		entityManager.flush();
		
		if (fecha == null){
			throw new UserException("AE10130", "La fecha no puede ser nula");
		}

		if (horaDesde == null){
			throw new UserException("AE10131", "La hora Desde no puede ser nula");
		}

		if (horaHasta == null){
			throw new UserException("AE10132", "La hora Hasta no puede ser nula");
		}

		if ( horaDesde.compareTo(horaHasta) >= 0){
			throw new UserException("AE10133", "La hora Desde debe ser menor que la hora Hasta");
		}
		
		if (frecuencia == null || frecuencia.intValue() <= 0){
			throw new UserException("AE10134", "La frecuencia debe ser mayor que cero");			
		}
		
		if (cupo == null || cupo.intValue() <= 0){
			throw new UserException("AE10135", "El cupo debe ser mayor que cero");
		}
		
		if ( (Utiles.time2InicioDelDia(fecha).compareTo(Utiles.time2InicioDelDia(horaDesde)) != 0) ||
				(Utiles.time2InicioDelDia(fecha).compareTo(Utiles.time2InicioDelDia(horaHasta)) != 0 )){
			throw new UserException("AE10136", "Las fechas de Hora Desde y Hora Hasta deben coincidir con la Fecha a generar");
		}
		
		if (fecha.before(rManaged.getFechaInicioDisp())){
			throw new UserException("AE10139","La fecha debe ser mayor a la fecha inicio Disponibilidad del recurso");
		}
		
		if (rManaged.getFechaFinDisp() != null){
			if (fecha.after(rManaged.getFechaFinDisp())){
				throw new UserException("AE10140","La fecha debe ser menor a la fecha fin Disponibilidad del recurso");
			}
		}

		if (!esDiaHabil(fecha, r)){
			throw new UserException("AE10138", "La fecha ingresada no corresponde a un día hábil");			
		} 

		Calendar calHoraInicio = new GregorianCalendar();
		Calendar calHoraFin = new GregorianCalendar();
		
		calHoraInicio.setTime(horaDesde);
		calHoraFin.setTime(horaDesde);

		logger.debug("Fecha: " + fecha.toString());
		logger.debug("Frecuencia: " + frecuencia);
		logger.debug("Hora Inicio: " + calHoraInicio.getTime().toString());
		logger.debug("Hora Fin: " + horaHasta.toString());
		logger.debug("Cupo: " + cupo);

		while (calHoraInicio.getTime().before(horaHasta)) {
			
			if (existeDisponibilidadEnHoraInicio(rManaged, calHoraInicio.getTime())){
				throw new UserException("AE10137", "Ya existe alguna disponibilidad generada para la fecha y horaInicio: " + Utiles.date2string(calHoraInicio.getTime(), Utiles.DIA_HORA));			
			}

			calHoraFin.add(Calendar.MINUTE, frecuencia);
			
			logger.debug("--> Hora Inicio: " + calHoraInicio.getTime().toString());
			logger.debug("--> Hora Fin: " + calHoraFin.getTime().toString());

			//Se crea la disponibilidad 
			generarNuevaDisponibilidad(rManaged, fecha, calHoraInicio.getTime(),calHoraFin.getTime(), cupo);


			//Se actualiza horaInicio
			calHoraInicio.add(Calendar.MINUTE, frecuencia);

		}
			
	}
	

	/**
	 * Genera disponibilidades en una ventana de tiempo para un recurso. Toma como modelo 
	 * las disponibilidades generadas para una fecha.
	 * Controla: 
	 * 1) Los días a generar se encuentren entre fechaInicioDisp y fechaFinDisp 
	 *    del recurso.
	 * 2) La cantidad de días a generar no puede superar cantDiasAGenerar del recurso.
	 * 3) Solo se generen disponibilidades para días que se encuentren marcados
	 *    como hábiles en sp_dias.
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	@SuppressWarnings("unchecked")
	public void generarDisponibilidades(Recurso r, Date fechaModelo, VentanaDeTiempo v) throws UserException, ApplicationException {

		Recurso rManaged = entityManager.find(Recurso.class, r.getId());
		if (rManaged == null) {
			throw new UserException("AE10022","No existe el recurso");
		}
		
		//JPA utiliza lock optimista. En el momento de grabar los cambios
		//si la entidad fue modificada levanta una excepción.
		entityManager.lock(rManaged,LockModeType.WRITE);
		entityManager.flush();
		
		fechaModelo = Utiles.time2InicioDelDia(fechaModelo);
		v.setFechaInicial(Utiles.time2InicioDelDia(v.getFechaInicial()));
		v.setFechaFinal(Utiles.time2FinDelDia(v.getFechaFinal()));

		
        //Se controla fecha inicial con fechaInicioDisp
		if (v.getFechaInicial().compareTo(rManaged.getFechaInicioDisp()) < 0){
			throw new UserException("AE10110", "La fecha inicial no puede ser menor que fecha de Inicio de Disponibilidad");
		}
		
		//La fecha final no puede ser nula
		if (v.getFechaFinal() == null){
			throw new UserException("AE10111", "La fecha final no puede ser nula");			
		}
		
		//La fecha final tiene que ser <= a fechaFinDisp o fechaFinDisp es nula
		if (rManaged.getFechaFinDisp() != null){
			if (v.getFechaFinal().compareTo(rManaged.getFechaFinDisp()) > 0){
				throw new UserException("AE10112", "La fecha final no puede ser mayor que fecha de Fin de Disponibilidad");				
			}
		}

		//La cantidad de dias a generar debe ser menor o igual a cantDiasAGenerar del Recurso. 
		Calendar calendario = new GregorianCalendar();
		int cantDias = 0;

		calendario.setTime(v.getFechaInicial());
		while (!calendario.getTime().after(v.getFechaFinal())) {
			cantDias = cantDias + 1;
			calendario.add(Calendar.DATE, 1);
		}
		
		if (cantDias > rManaged.getCantDiasAGenerar()){
			throw new UserException("AE10113", "La cantidad de dias a generar debe ser menor que la cantida de dias a generar para el recurso");
		}

        //La fecha inicial debe ser menor o igual a la fecha final
		if (v.getFechaInicial().compareTo(v.getFechaFinal()) > 0){
			throw new UserException("AE10116", "La fecha inicial debe ser menor o igual a la fecha final");
		}

				
		logger.debug("Fecha Modelo: " + fechaModelo.toString());

		//Se obtienen las disponibilidades generardas para la fecha a tomar como modelo.
		List<Disponibilidad> disponibilidades =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha = :f " +
		"order by d.horaInicio ")
		.setParameter("r", rManaged)
		.setParameter("f", fechaModelo, TemporalType.DATE)
		.getResultList();		
		
		
		//Si la lista obtenida es vacía no se puede continuar
		if (disponibilidades == null || disponibilidades.size() == 0){
			throw new UserException("AE10114", "No existen disponibilidades generadas para la fecha ingresada");			
		}

		logger.debug("Disponibilidades existentes a tomar de modelo: " + disponibilidades.size());

		//Se controla que no existan disponibilidades generadas en el período dado por la ventana
		// de tiempo v
		List<Disponibilidad> dispCtrl =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff " +
		"order by d.horaInicio ")
		.setParameter("r", rManaged)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)		
		.getResultList();		
		
		
		//Si la lista obtenida es vacía no se puede continuar
		if ( dispCtrl.size() > 0){
			throw new UserException("AE10115", "No pueden existir disponibilidades en el período a generar.");			
		}


		Calendar cal = new GregorianCalendar();

		//Se inicializa en el primer día a generar.
		cal.setTime(v.getFechaInicial());
		while (!cal.getTime().after(v.getFechaFinal())) {

			if (esDiaHabil(cal.getTime(), r))	{
				
				//Se recorren las disponibilidades para la fecha ingresada como modelo.
				for (Disponibilidad d : disponibilidades) {
					generarNuevaDisponibilidad(rManaged, cal.getTime(), 
							d.getHoraInicio(), d.getHoraFin(), d.getCupo());
				}
			}
			cal.add(Calendar.DATE, 1);
		}
	}
	
	/*
	 * Se genera una nueva disponibilidad.
	 * Se debe tener en cuenta que hora inicio y hora fin tienen también la fecha.
	 * Para que la agenda funcione correctamente la fecha para ambos casos
	 * deberá ser igual que la fecha de la disponibilidad.
	 */
	private void generarNuevaDisponibilidad(Recurso r, Date fecha, Date horaI, Date horaF, Integer cupo){
		
		
		Disponibilidad nuevaDisp = new Disponibilidad();

		nuevaDisp.setFecha(Utiles.time2InicioDelDia(fecha));
		nuevaDisp.setCupo(cupo);
		nuevaDisp.setRecurso(r);
		
		Calendar cal = Calendar.getInstance();
		Calendar calAux = Calendar.getInstance();
		cal.setTime(fecha);
		
		//disp.horaInicio tendrá la misma fecha que disp.fecha
		calAux.setTime(horaI);
		cal.set(Calendar.HOUR_OF_DAY, calAux.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, calAux.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, calAux.get(Calendar.SECOND));
		nuevaDisp.setHoraInicio(cal.getTime());

		//disp.horaFin tendrá la misma fecha que disp.fecha
		calAux.setTime(horaF);
		cal.set(Calendar.HOUR_OF_DAY, calAux.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, calAux.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, calAux.get(Calendar.SECOND));
		nuevaDisp.setHoraFin(cal.getTime());
		
		entityManager.persist(nuevaDisp);
	}
	
	
	@SuppressWarnings("unchecked")
	public void generarPatronSemana(Recurso r, VentanaDeTiempo semana,	VentanaDeTiempo periodo) throws BusinessException, UserException, ApplicationException {

		r = entityManager.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1","No existe el recurso");
		}
		
		//JPA utiliza lock optimista. En el momento de grabar los cambios
		//si la entidad fue modificada levanta una excepción.
		entityManager.lock(r,LockModeType.WRITE);
		entityManager.flush();
		
		//Se controla fecha inicial con fechaInicioDisp
		if (periodo.getFechaInicial().before(r.getFechaInicioDisp())){
			throw new UserException("-1", "La fecha inicial es menor que el inicio de disponibilidad del recurso");
		}
		
		//La fecha final no puede ser nula
		if (periodo.getFechaFinal() == null){
			throw new UserException("-1", "La fecha final no puede ser nula");			
		}
		
		//La fecha final tiene que ser <= a fechaFinDisp o fechaFinDisp es nula
		if (r.getFechaFinDisp() != null && periodo.getFechaFinal().after(r.getFechaFinDisp())){
				throw new UserException("-1", "La fecha final es mayor que el fin de disponibilidad del recurso");				
		}
		
		//La cantidad de dias a generar debe ser menor o igual a cantDiasAGenerar del Recurso. 
		Calendar cal = Calendar.getInstance();
		cal.setTime(Utiles.time2FinDelDia(cal.getTime()));
		cal.add(Calendar.DATE, r.getCantDiasAGenerar());
		if (periodo.getFechaFinal().after(cal.getTime())){
			throw new UserException("AE10113", "No se puede generar disponibilidades para una fecha mayor a: hoy + la cantidad de dias a generar indicada en el recurso");
		}

		//Se controla que no existan disponibilidades generadas vivas en el período
		Long cantDispEnPeriodo =  (Long) entityManager
		.createQuery(
		"select count(*) " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff " )
		.setParameter("r", r)
		.setParameter("fi", periodo.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", periodo.getFechaFinal(), TemporalType.DATE)		
		.getSingleResult();		
		
		if ( cantDispEnPeriodo > 0){
			throw new UserException("-1", "No pueden existir disponibilidades en el período a generar.");			
		}
		
		
		//Se obtienen las disponibilidades generardas para la semana tomada como patron.
		List<Disponibilidad> disponibilidadesPatron =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff " +
		"order by d.fecha asc, d.horaInicio ")
		.setParameter("r", r)
		.setParameter("fi", semana.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", semana.getFechaFinal(), TemporalType.DATE)
		.getResultList();				
		
		//Si la lista obtenida es vacía no se puede continuar
		if (disponibilidadesPatron == null || disponibilidadesPatron.size() == 0){
			throw new UserException("-1", "No existen disponibilidades generadas para la fecha ingresada");			
		}

		generarNuevasDisponibilidadesPorSemana(r, disponibilidadesPatron, periodo);
		
	}

	
	private void generarNuevasDisponibilidadesPorSemana(Recurso r, List<Disponibilidad> patron, VentanaDeTiempo periodo) throws ApplicationException {

		//Seudocodigo de generacion de disponibilidades
		//Para cada disponibilidad tomada como patron:
		//		Obtengo horaInicio, horaFin, cupos
		//		Seteo un calendario a la fecha de periodo.fechaInicio
		//		Corro el calendario hasta hacer coincidir el dia de la semana con el de la disponibilidad.fecha
		//		Mientras el calendario no se pase de periodo.fechaFin:
		//			genero disponibilidad
		//			sumo 7 dias al calendario

		Calendar calendario = Calendar.getInstance();
		Calendar calDisp = Calendar.getInstance();
	
		for (Disponibilidad disponibilidad : patron) {
			
			calendario.setTime(periodo.getFechaInicial());
			calDisp.setTime(disponibilidad.getFecha());
			
			//Hago coincidir el dia de la semana del calendario (D1) 
			//con el dia de la semana de la fecha de la disponibilidad patron (D2)
			//Para eso sumo la diferencia D2-D1 y si la misma es negativa ademas le sumo 7 dias.
			int calendarioDia = calendario.get(Calendar.DAY_OF_WEEK);
			int disponibilidadDia = calDisp.get(Calendar.DAY_OF_WEEK);
			int diferencia = disponibilidadDia - calendarioDia;
			if (diferencia >= 0) {
				calendario.add(Calendar.DAY_OF_WEEK, diferencia);
			}
			else {
				calendario.add(Calendar.DAY_OF_WEEK, diferencia + 7);
			}
			
			
			while ( ! calendario.getTime().after(periodo.getFechaFinal()) ) {
				
				if(esDiaHabil(calendario.getTime(), r)) {

					generarNuevaDisponibilidad(r, 
							calendario.getTime(), 
							disponibilidad.getHoraInicio(), 
							disponibilidad.getHoraFin(), 
							disponibilidad.getCupo());
				}
				
				calendario.add(Calendar.DAY_OF_WEEK, 7);
			}
			
		}
	}

	
	public void generarDisponibilidaesAutomaticamente() {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR", "RA_AE_FATENCION", "RA_AE_ANONIMO"})
	public List<DisponibilidadReserva> obtenerDisponibilidadesReservas(Recurso r, VentanaDeTiempo v) throws BusinessException, RolException {
		
		
		if (r == null || v == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		r = entityManager.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
		//chequearPermiso(r.getAgenda());
		

		
		//No se elimina el PASADO para permitir consultas sobre
		//datos anteriores.

		/*
		Date ahora = new Date();
		
		if (v.getFechaInicial().before(ahora)) {
			v.setFechaInicial(ahora);
		}
		*/
		if (v.getFechaInicial().before(r.getFechaInicioDisp())) {
			v.setFechaInicial(r.getFechaInicioDisp());
		}
		logger.debug("Periodo a consultar " + v.getFechaInicial().toString()+ " a " + v.getFechaFinal().toString());
		List<Object[]> cantReservasVivas =  entityManager
		.createQuery(
		"select d.id, d.fecha, d.horaInicio, count(reserva) " +
		"from  Disponibilidad d JOIN d.reservas reserva " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff and " +
//    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) and " +
		"      (reserva.estado <> :cancelado) " +
		" group by d.id, d.fecha, d.horaInicio " +
		" order by d.fecha asc, d.horaInicio asc ")
		.setParameter("r", r)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
//		.setParameter("hoy", ahora, TemporalType.DATE)
//		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
		.setParameter("cancelado", Estado.C)
		.getResultList();		
	
		
		List<Disponibilidad> disponibilidades =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha between :fi and :ff " +
//    	"      and (d.fecha <> :hoy or d.horaInicio >= :ahora) " +
		" order by d.fecha asc, d.horaInicio ")
		.setParameter("r", r)
		.setParameter("fi", v.getFechaInicial(), TemporalType.DATE)
		.setParameter("ff", v.getFechaFinal(), TemporalType.DATE)
//		.setParameter("hoy", ahora, TemporalType.DATE)
//		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
		.getResultList();		
		
		
		List<DisponibilidadReserva> listadreserva = new ArrayList<DisponibilidadReserva>();
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
			
			DisponibilidadReserva dreserva = new DisponibilidadReserva();
			dreserva.setId(d.getId());
			dreserva.setFecha(d.getFecha());
			dreserva.setHoraInicio(d.getHoraInicio());
			dreserva.setHoraFin(d.getHoraFin());
			dreserva.setRecurso(null);
			dreserva.setCupo(d.getCupo());
			dreserva.setCupoDisponible(d.getCupo() - cant);
			dreserva.setCantReservas(cant);
			
			listadreserva.add(dreserva);			
		}		
		
		return listadreserva;
	}


	public void modificarCupoDeDisponibilidad(Disponibilidad d) throws UserException, BusinessException  {

		Disponibilidad dispActual = (Disponibilidad) entityManager.find(Disponibilidad.class, d.getId());
		
		if (dispActual == null) {
			throw new UserException("AE10080","No existe la disponibilidad que se quiere modificar: " + d.getId().toString());
		}
		
		//No se puede modificar una disponibilidad con fecha de baja
		if (dispActual.getFechaBaja() != null) {
			throw new UserException("AE10081","No se puede modificar una disponibilidad con fecha de baja");
		}
		
		//Se controla que la disponibilidad no tenga fecha de baja
		if (d.getFechaBaja() != null) {
			throw new BusinessException("AE20082","No se puede modificar la fecha de baja de una disponibilidad");			
		}
		
		// prueba para ver si se elimina el error
		if (d.getCupo()== null){
			throw new UserException("AE10084","El valor del cupo no puede ser nulo.");
		}
		
		//Si se está disminuyendo la cantidad de cupos, se controla que la cantidad
		//de reservas con estado <> C <= que la cantidad de cupos.
		if (dispActual.getCupo() > d.getCupo()){
			if (cantReservas(dispActual) > d.getCupo()){
				throw new UserException("AE10083","El valor del cupo debe ser mayor o igual a la cantidad de reservas existentes.");				
			}
		}
		dispActual.setCupo(d.getCupo());
	}

	
	@SuppressWarnings("unchecked")
	private Long cantReservas(Disponibilidad d){
		//Se obtiene lista de Reservas
		List<Object[]> listaReservas = (List<Object[]>) entityManager
		.createQuery(
		"select d.fecha, count(r) " +
		"from  Disponibilidad d left join d.reservas r " +
		"where d = :d and " +
		"      (r is null or r.estado <> :cancelado) " +
		"group by d.fecha " +
		"order by d.fecha asc "
		)
		.setParameter("d", d)
		.setParameter("cancelado", Estado.C)
		.getResultList();

		Long cuposDisp = new Long(0);
		Iterator<Object[]> iReservas = listaReservas.iterator();
		while ( iReservas.hasNext()){
			Object[] reserva = iReservas.next();
			cuposDisp = cuposDisp + (Long)reserva[1]; 
			
		}
		logger.debug("Disp " + d.getId()+ " Reservas " + cuposDisp.toString());		
		return cuposDisp;
	}

	@SuppressWarnings("unchecked")
	public void modificarCupoPeriodo(Disponibilidad d) throws UserException, BusinessException  {

		Disponibilidad dispActual = (Disponibilidad) entityManager.find(Disponibilidad.class, d.getId());
		Date ahora = new Date();

		if (dispActual == null) {
			throw new UserException("AE10080","No existe la disponibilidad que se quiere modificar: " + d.getId().toString());
		}

		//No se puede modificar una disponibilidad con fecha de baja
		if (dispActual.getFechaBaja() != null) {
			throw new UserException("AE10081","No se puede modificar una disponibilidad con fecha de baja");
		}

		//Se controla que la disponibilidad no tenga fecha de baja
		if (d.getFechaBaja() != null) {
			throw new BusinessException("AE20082","No se puede modificar la fecha de baja de una disponibilidad");			
		}

		// prueba para ver si se elimina el error
		if (d.getCupo()== null){
			throw new UserException("AE10084","El valor del cupo no puede ser nulo.");
		}
		
		List<Disponibilidad> disponibilidades =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha >= :fi and " +
    	"      (d.fecha <> :hoy or d.horaInicio >= :ahora) " +
//    	"   and    d.horaInicio = :horaInicio " +
		"order by d.fecha asc, d.horaInicio ")
		.setParameter("r", dispActual.getRecurso())
		.setParameter("fi", dispActual.getFecha(), TemporalType.DATE)
		.setParameter("hoy", ahora, TemporalType.DATE)
		.setParameter("ahora", ahora, TemporalType.TIMESTAMP)
//		.setParameter("horaInicio", dispActual.getHoraInicio(), TemporalType.TIME)
		.getResultList();
		Calendar hora = Calendar.getInstance();
		hora.setTime(dispActual.getHoraInicio());

		String horaInicio = Integer.toString(hora.get(Calendar.HOUR_OF_DAY));
		String minutosInicio = Integer.toString(hora.get(Calendar.MINUTE));

		Iterator<Disponibilidad> iDispon = disponibilidades.iterator();
		while ( iDispon.hasNext()){
			Disponibilidad disp = iDispon.next();

			Calendar horaPost = Calendar.getInstance();
			horaPost.setTime(disp.getHoraInicio());

			String horaInicioP = Integer.toString(horaPost.get(Calendar.HOUR_OF_DAY));
			String minutosInicioP = Integer.toString(horaPost.get(Calendar.MINUTE));

			if (horaInicio.equals(horaInicioP) && minutosInicio.equals(minutosInicioP) )
			{
				//Si se está disminuyendo la cantidad de cupos, se controla que la cantidad
				//de reservas con estado <> C <= que la cantidad de cupos.
				if (disp.getCupo() > d.getCupo()){
					if (cantReservas(disp) > d.getCupo()){
						Calendar diaError = Calendar.getInstance();
						diaError.setTime(disp.getFecha());
						throw new UserException("AE10083","El valor del cupo debe ser mayor o igual a la cantidad de reservas existentes ("+diaError.get(Calendar.DAY_OF_MONTH) + " / " +
								(diaError.get(Calendar.MONTH)+1) + " / " +
								diaError.get(Calendar.YEAR)+")" );				
					}
				}
				disp.setCupo(d.getCupo());
				
			}
		}
		

	}

	@SuppressWarnings("unchecked")
	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR", "RA_AE_FATENCION"})
		public Integer cantDisponibilidadesDia(Recurso r, Date f)
			throws UserException, BusinessException {

		List<Disponibilidad> dispo = entityManager
		.createQuery(
				"select d " +
				"from  Disponibilidad d " +
				"where d.recurso is not null and " +
				"      d.recurso = :r and " +
				"      d.fechaBaja is null and " +
				"      d.fecha = :fi ")
				.setParameter("r", r)
				.setParameter("fi", f)
				.getResultList();
		Integer cant = dispo.size();
		return cant;
	}

	/*
	 * Retorna true si existen disponibilidades vivas para un recurso, fecha y
	 * hora inicio.
	 */
	@SuppressWarnings("unchecked")
	private boolean existeDisponibilidadEnHoraInicio(Recurso r, Date horaInicio){
		boolean existeDisp = false;
		//Se obtienen las disponibilidades vivas para un recurso generadas para la fecha y 
		// hora inicio indicadas.
		List<Disponibilidad> disponibilidades =  entityManager
		.createQuery(
		"select d " +
		"from  Disponibilidad d " +
		"where d.recurso is not null and " +
		"      d.recurso = :r and " +
		"      d.fechaBaja is null and " +
		"      d.fecha = :f and" +
		"      d.horaInicio = :hi " +
		"order by d.horaInicio ")
		.setParameter("r", r)
		.setParameter("f", horaInicio, TemporalType.DATE)
		.setParameter("hi", horaInicio, TemporalType.TIMESTAMP)
		.getResultList();		
		
		//Sólo se puede continuar si la lista obtenida es vacía
		if (disponibilidades.size() > 0){
			existeDisp = true;
		}
		
		return existeDisp;
	}

	
	/*
	 * Devuelve true si la fecha es un día habil
	 */
	private boolean esDiaHabil(Date fecha, Recurso r) throws ApplicationException{

		Calendario calendario = CalendarioFactory.getCalendario();
		return calendario.esDiaHabil(fecha, r);
	}

	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR", "RA_AE_FATENCION"})
	public Date ultFechaGenerada(Recurso r) throws UserException,
			BusinessException {

		Object maximo = entityManager.createQuery(
				"select max(d.fecha) " +
				"from Disponibilidad d " +
				"where d.recurso = :r " +
				"and d.fechaBaja is null ").setParameter("r", r).getSingleResult();
		Date ultFecha = (Date)maximo;
		return ultFecha;
	}
}
