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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import uy.gub.imm.sae.business.api.ConsultasLocal;
import uy.gub.imm.sae.business.api.ConsultasRemote;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.entity.ValorPosible;

@Stateless
@RolesAllowed({"RA_AE_FCALL_CENTER","RA_AE_PLANIFICADOR", "RA_AE_ADMINISTRADOR","RA_AE_ANONIMO","RA_AE_FATENCION"})
public class ConsultasBean implements ConsultasLocal, ConsultasRemote{

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;
	
	
	public Reserva consultarReservaId(Integer idReserva, Integer idRecurso) throws ApplicationException, BusinessException {
		
		Reserva reserva = null;
		
		if (idReserva == null)
			throw new BusinessException("-1", "El Nro. de la reserva no puede ser nulo.");
		
		try {
		
			 reserva = entityManager.find(Reserva.class, idReserva);
			 if (reserva != null) {
				 reserva.getDisponibilidades().size();
				 reserva.getDatosReserva().size();
			 }
		
		}catch (Exception e) {
			throw new ApplicationException(e);
		}
		if (reserva != null) {
			if (reserva.getDisponibilidades().iterator().next().getRecurso().getId().intValue() != idRecurso){
				throw new BusinessException("-1","La reserva no corresponde al recurso.");
			}
		}
		return reserva;
	}

	
	public Reserva consultarReservaPorNumero(Recurso r, Date fechaHoraInicio, Integer numero) throws BusinessException, UserException {
		
		Reserva reserva = null;
		
		r = entityManager.find(Recurso.class, r.getId());
		if (r == null || r.getFechaBaja() != null)
			throw new BusinessException("-1", "No se encuentra el recurso.");

		if (fechaHoraInicio == null)
			throw new BusinessException("-1", "Debe indicar la fecha y hora de la reserva.");

		if (numero == null)
			throw new BusinessException("-1", "El número de la reserva no puede ser nulo.");
		
		try {
		reserva = (Reserva) entityManager.createQuery(
				 "select res " +
				 "from  Reserva res join res.disponibilidades d " +
				 "where d.recurso = :recurso and " +
				 "      d.fecha = :fecha and" +
				 "      d.horaInicio = :horaInicio and " +
				 "      res.numero = :numero ")
				 .setParameter("recurso", r)
				 .setParameter("fecha", fechaHoraInicio, TemporalType.DATE)
				 .setParameter("horaInicio", fechaHoraInicio, TemporalType.TIMESTAMP)
				 .setParameter("numero", numero)
				 .getSingleResult();
		} 
		catch ( NoResultException e) { 
			throw new UserException("-1", "No se encuentra reserva para los datos indicados.");
		}

		reserva.getDisponibilidades().size();
		reserva.getDatosReserva().size();

		return reserva;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Reserva> consultarReservaDatosHora(List<DatoReserva> datos ,Recurso recurso, Date fecha){
		List<Reserva> resultados = new ArrayList<Reserva>();
		
		if (! datos.isEmpty()){
			String selectStr = " SELECT distinct(reserva) " ;
			String fromStr =   " FROM Reserva reserva " +
							   "	   join  reserva.disponibilidades disp " +
		  				       "       join  reserva.datosReserva datoReserva " +
		  				       "	   join datoReserva.datoASolicitar datoSolicitar	" ;
		
			String whereStr = " WHERE disp.recurso = :recurso " +
							  " and disp.fechaBaja is null " + 
							  " and disp.fecha >= :hoy " +
						      " and (disp.fecha <> :hoy or disp.horaInicio >= :ahora) " +
						      " and reserva.estado <> 'U' " +
						      " and reserva.estado <> 'C' and (" ;
		 
		
			boolean primerRegistro = true;
			boolean hayCamposClaveNulos = false;
			int i = 0;
			for (DatoReserva datoR : datos){
				if (datoR != null){
					if (primerRegistro){
						whereStr = whereStr + 
						" 	   (upper(datoReserva.valor) = '" + datoR.getValor().toUpperCase() + "' and " +
						" 	   datoSolicitar.id = " + datoR.getDatoASolicitar().getId() + ") ";
						primerRegistro = false;
					} else {
				
					String joinFromAux =  " join  reserva.datosReserva datoReserva" + i +
										  " join datoReserva" + i + ".datoASolicitar datoSolicitar" + i;
					fromStr = fromStr + joinFromAux;
				
					whereStr = whereStr + " AND " +
						" 	 ( upper(datoReserva" + i + ".valor) = '" + datoR.getValor().toUpperCase() + "' and " +
						" 	   datoSolicitar" + i + ".id = " + datoR.getDatoASolicitar().getId() + ") ";
				
					}
				} else {
					hayCamposClaveNulos = true;
				}
				i++;
			}
	
			String consulta = selectStr + fromStr + whereStr ;
		
			// Agrego el ORDER BY parentesis final de la consulta
			consulta = consulta + ") ORDER BY reserva.id ";
		
			try {
				resultados = (List<Reserva>)entityManager.createQuery(consulta)
									.setParameter("recurso", recurso).setParameter("hoy", fecha, TemporalType.DATE)
									.setParameter("ahora", fecha, TemporalType.TIMESTAMP)
									
									.getResultList();
				
				/* 12/03/2010 - Corrige que al traer reservas con la misma clave que se ingreso, 
				 * se filtren las que tengan algun dato clave mas ingresado (por ejemplo cuando 
				 * un gestor reservo para otras personas y luego reserva para si mismo, 
				 * sin ingresar los datos clave opcional) 
				 */
				if(hayCamposClaveNulos)
					resultados = filtrarReservasConMasDatos(resultados, datos);
				
			} catch (Exception e){}
		
				// 	recorro las reservas para obtener las listas disponibilidades y datos reservas 
				for (Reserva r : resultados){
					r.getDisponibilidades().size();
					r.getDatosReserva().size();
				}
		}
				return resultados;
	}
	
	private List<Reserva> filtrarReservasConMasDatos(List<Reserva> reservasIni, List<DatoReserva> datosIngresadosClave){
		List<Reserva> reservasResult = new ArrayList<Reserva>();
		
		boolean hayAlgunoQueNoEsta = false;
		for (Iterator<Reserva> iterator1 = reservasIni.iterator(); iterator1.hasNext();) {
			Reserva reserva = iterator1.next();
			
			hayAlgunoQueNoEsta = false;
			for (Iterator<DatoReserva> iterator = reserva.getDatosReserva().iterator(); iterator.hasNext() && !hayAlgunoQueNoEsta;) {
				DatoReserva datoI = iterator.next();
				if(datoI.getDatoASolicitar().getEsClave() && !existeDatoReservaEnDatosIngresados(datosIngresadosClave, datoI.getDatoASolicitar())){
					hayAlgunoQueNoEsta = true;
				}
			}
			
			if(!hayAlgunoQueNoEsta){
				reservasResult.add(reserva);
			}
		}
		
		return reservasResult;
	}

	private boolean existeDatoReservaEnDatosIngresados(List<DatoReserva> datosIngresadosClave, DatoASolicitar datoSolicReservaFiltrar){
	
		boolean esta = false; 
		
		for (Iterator<DatoReserva> iterator = datosIngresadosClave.iterator(); iterator.hasNext() && !esta;) {
			DatoReserva datoClave = iterator.next();
			if(datoClave!=null && datoClave.getDatoASolicitar().getId().equals(datoSolicReservaFiltrar.getId())){
				esta = true;
			}			
		}
		
		return esta;
	}
	

	@SuppressWarnings("unchecked")
	public List<Reserva> consultarReservaDatos(List<DatoReserva> datos ,Recurso recurso){
		List<Reserva> resultados = new ArrayList<Reserva>();
		
		if (! datos.isEmpty()){

			String selectStr = " SELECT distinct(reserva) " ;
			String fromStr =   " FROM Reserva reserva " +
							   "	   join  reserva.disponibilidades disp " +
		  				       "       join  reserva.datosReserva datoReserva " +
		  				       "	   join datoReserva.datoASolicitar datoSolicitar	" ;
	
			String whereStr = " WHERE disp.recurso = :recurso " +
						  "		  and reserva.estado <> 'P' " ;
		 
	
			/*	String consulta = " SELECT distinct(reserva) " +
						  " FROM Reserva reserva " +
						  "	   join  reserva.disponibilidades disp " +
						  "    join  reserva.datosReserva datoReserva " +
						  "	  join datoReserva.datoASolicitar datoSolicitar	" +
						  " WHERE disp.recurso = :recurso " +
						  "		  and reserva.estado <> 'P' and (" ;
			 */
	
			boolean primerRegistro = true;
			int i = 0;
			for (DatoReserva datoR : datos){
				if((datoR.getValor() != null) && (!datoR.getValor().equalsIgnoreCase("NoSeleccion"))){
					if ((primerRegistro)){
						whereStr = whereStr + 
						" 	   and ((upper(datoReserva.valor) = '" + datoR.getValor().toUpperCase() + "' and " +
						" 	   datoSolicitar.id = " + datoR.getDatoASolicitar().getId() + ") ";
						primerRegistro = false;
					} else {
				
						String joinFromAux =  " join  reserva.datosReserva datoReserva" + i +
											  " join datoReserva" + i + ".datoASolicitar datoSolicitar" + i;
						fromStr = fromStr + joinFromAux;
				
						whereStr = whereStr + " AND " +
							" 	 ( upper(datoReserva" + i + ".valor) = '" + datoR.getValor().toUpperCase() + "' and " +
							" 	   datoSolicitar" + i + ".id = " + datoR.getDatoASolicitar().getId() + ") ";
				
					}
				}
				i++;
			}

			String consulta = selectStr + fromStr + whereStr ;
	
			// Agrego el ORDER BY parentesis final de la consulta
			consulta = consulta + ") ORDER BY reserva.fechaCreacion DESC ";
	
			try {
				resultados = (List<Reserva>)entityManager.createQuery(consulta)
									.setParameter("recurso", recurso)
									.getResultList();
			} catch (Exception e){}
	
			// 	recorro las reservas para obtener las listas disponibilidades y datos reservas 
			for (Reserva r : resultados){
				r.getDisponibilidades().size();
				r.getDatosReserva().size();
			}
		}
		return resultados;
	}

	@SuppressWarnings("unchecked")
	public List<Reserva> consultarReservasParaCancelar(List<DatoReserva> datos ,Recurso recurso){
		List<Reserva> resultados = new ArrayList<Reserva>();
		
		String selectStr = " SELECT distinct(reserva) " ;
		String fromStr =   " FROM Reserva reserva " +
						   "	   join  reserva.disponibilidades disp " +
	  				       "       join  reserva.datosReserva datoReserva " +
	  				       "	   join datoReserva.datoASolicitar datoSolicitar	" ;
	
		String whereStr = " WHERE disp.recurso = :recurso " +
					  "		  and reserva.estado = 'R' " +
					  "		  and disp.horaInicio >= :fecha " ;
	 
	
		/*	String consulta = " SELECT distinct(reserva) " +
					  " FROM Reserva reserva " +
					  "	   join  reserva.disponibilidades disp " +
					  "    join  reserva.datosReserva datoReserva " +
					  "	  join datoReserva.datoASolicitar datoSolicitar	" +
					  " WHERE disp.recurso = :recurso " +
					  "		  and reserva.estado <> 'P' and (" ;
		 */
	
		boolean primerRegistro = true;
		int i = 0;
		for (DatoReserva datoR : datos){
			if((datoR.getValor() != null) && (!datoR.getValor().equalsIgnoreCase("NoSeleccion"))){
				if ((primerRegistro)){
					whereStr = whereStr + 
					" 	   and ((upper(datoReserva.valor) = '" + datoR.getValor().toUpperCase() + "' and " +
					" 	   datoSolicitar.id = " + datoR.getDatoASolicitar().getId() + ") ";
					primerRegistro = false;
				} else {
				
					String joinFromAux =  " join  reserva.datosReserva datoReserva" + i +
										  " join datoReserva" + i + ".datoASolicitar datoSolicitar" + i;
					fromStr = fromStr + joinFromAux;
				
					whereStr = whereStr + " AND " +
						" 	 ( upper(datoReserva" + i + ".valor) = '" + datoR.getValor().toUpperCase() + "' and " +
						" 	   datoSolicitar" + i + ".id = " + datoR.getDatoASolicitar().getId() + ") ";
				
				}
			}
			i++;
		}

		String consulta = selectStr + fromStr + whereStr ;
	
		// Agrego el ORDER BY parentesis final de la consulta
		consulta = consulta + ") ORDER BY reserva.fechaCreacion DESC ";
	
		try {
			resultados = (List<Reserva>)entityManager.createQuery(consulta)
								.setParameter("recurso", recurso)
								.setParameter("fecha", new Date(), TemporalType.TIMESTAMP)
								.getResultList();
		} catch (Exception e){}
	
		// 	recorro las reservas para obtener las listas disponibilidades y datos reservas 
		for (Reserva r : resultados){
			r.getDisponibilidades().size();
			r.getDatosReserva().size();
		}
		return resultados;
	}
	
	public List<ReservaDTO> consultarReservasPorPeriodoEstado(Recurso recurso, VentanaDeTiempo periodo, Estado estado) throws BusinessException {
		List<Estado> estados = new ArrayList<Estado>();
		estados.add(estado);
		return consultarReservasPorPeriodoEstado(recurso, periodo, estados);
	}

	public List<ReservaDTO> consultarReservasPorPeriodoEstado(Recurso recurso, VentanaDeTiempo periodo, List<Estado> estados) throws BusinessException {
		
	
		if (recurso == null || periodo == null || estados == null || estados.size() == 0 || estados.get(0) == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		if (periodo.getFechaInicial() == null || periodo.getFechaFinal() == null) {
			throw new BusinessException("-1", "El periodo debe tener inicio y fin");
		}
		
		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
		return consultarReservasPorPeriodoEstadosDisponibilidades(recurso, periodo, estados, new ArrayList<Integer>());
		
	}

	public List<ReservaDTO> consultarReservasEnEspera(Recurso recurso) throws BusinessException {
		
		Date hoy = new Date();
		VentanaDeTiempo periodo = new VentanaDeTiempo();
		periodo.setFechaInicial(Utiles.time2FinDelDia(hoy));
		periodo.setFechaFinal(Utiles.time2FinDelDia(hoy));
		List<Estado> estados = new ArrayList<Estado>();
		estados.add(Estado.R);

		List<Integer> disponibilidadesIds = consultarDisponibilidadesReservadasYUtilizadas(recurso, periodo);
		
		return consultarReservasPorPeriodoEstadosDisponibilidades(recurso, periodo, estados, disponibilidadesIds);
	}

	public List<ReservaDTO> consultarReservasEnEsperaUtilizadas(Recurso recurso) throws BusinessException {
		
		Date hoy = new Date();
		VentanaDeTiempo periodo = new VentanaDeTiempo();
		periodo.setFechaInicial(Utiles.time2FinDelDia(hoy));
		periodo.setFechaFinal(Utiles.time2FinDelDia(hoy));
		List<Estado> estados = new ArrayList<Estado>();
		estados.add(Estado.R);
		estados.add(Estado.U);

		List<Integer> disponibilidadesIds = consultarDisponibilidadesReservadasYUtilizadas(recurso, periodo);
		
		return consultarReservasPorPeriodoEstadosDisponibilidades(recurso, periodo, estados, disponibilidadesIds);
	}

	
	@SuppressWarnings("unchecked")	
	private List<Integer> consultarDisponibilidadesReservadasYUtilizadas(Recurso recurso, VentanaDeTiempo periodo) throws BusinessException {


		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}
		
		
		//Busco las primeras 2 disponibilidades con reservas en estado R
		Query queryR = entityManager.createQuery(
				"select distinct d.id, d.fecha, d.horaInicio " +
				"from   Reserva r " +
				"       join r.disponibilidades d " +
				"where   " +
				"       d.recurso.id = :recurso and " +
				"       d.fecha between :fi and :ff  and " +
				"       r.estado = :estado " +
				"order by d.fecha, d.horaInicio " 
		);

		queryR.setParameter("recurso", recurso.getId());
		queryR.setParameter("estado", Estado.R);
		queryR.setParameter("fi", periodo.getFechaInicial(), TemporalType.DATE);
		queryR.setParameter("ff", periodo.getFechaFinal(), TemporalType.DATE);
		
		if (recurso.getLargoListaEspera() != null && recurso.getLargoListaEspera() > 0){
			queryR.setMaxResults(recurso.getLargoListaEspera());
		}
		
		List<Object[]> dispsR = queryR.getResultList();

		List<Object[]> dispsU = null;
		
		if (dispsR.size()>0) {
			//Si hay disponibilidades en estado R, busco las 2 inmediatamente anteriores en estado U
			
			Query queryU = entityManager.createQuery(
				"select distinct d.id, d.fecha, d.horaInicio " +
				"from   Reserva r " +
				"       join r.disponibilidades d " +
				"where   " +
				"       d.recurso.id = :recurso and " +
				"       d.fecha between :fi and :ff  and " +
				"       r.estado = :estado and " +
				"		d.horaInicio < :hora " +
				"order by d.fecha desc, d.horaInicio desc " 
			);

			queryU.setParameter("recurso", recurso.getId());
			queryU.setParameter("estado", Estado.U);
			queryU.setParameter("fi", periodo.getFechaInicial(), TemporalType.DATE);
			queryU.setParameter("ff", periodo.getFechaFinal(), TemporalType.DATE);
			queryU.setParameter("hora", (Date)dispsR.get(0)[2], TemporalType.TIMESTAMP);
						
			if (recurso.getLargoListaEspera() != null && recurso.getLargoListaEspera() > 0){
				queryU.setMaxResults(recurso.getLargoListaEspera());
			}
			
			dispsU = queryU.getResultList();
			
		}
		else {
			//Busco las 2 ultimas en estado U
			Query queryU = entityManager.createQuery(
					"select distinct d.id, d.fecha, d.horaInicio " +
					"from   Reserva r " +
					"       join r.disponibilidades d " +
					"where   " +
					"       d.recurso.id = :recurso and " +
					"       d.fecha between :fi and :ff  and " +
					"       r.estado = :estado " +
					"order by d.fecha desc, d.horaInicio desc " 
				);

				queryU.setParameter("recurso", recurso.getId());
				queryU.setParameter("estado", Estado.U);
				queryU.setParameter("fi", periodo.getFechaInicial(), TemporalType.DATE);
				queryU.setParameter("ff", periodo.getFechaFinal(), TemporalType.DATE);
							
				if (recurso.getLargoListaEspera() != null && recurso.getLargoListaEspera() > 0){
					queryU.setMaxResults(recurso.getLargoListaEspera());
				}
				
				dispsU = queryU.getResultList();
		}
		
		
		List<Integer> dispIds = new ArrayList<Integer>();
		
//		if (dispsU != null) {
//			
//			if (dispsU.size() == 2) {
//				dispIds.add((Integer)dispsU.get(1)[0]);
//			}
//			
//			if (dispsU.size() >= 1 && (dispsR == null || dispsR.size() == 0 || !dispsR.get(0)[0].equals(dispsU.get(0)[0])) ) {
//				dispIds.add((Integer)dispsU.get(0)[0]);
//			}
//		}
//		
//		if (dispsR != null) {
//
//			if (dispsR.size() >= 1) {
//				dispIds.add((Integer)dispsR.get(0)[0]);
//			}
//			
//			if (dispsR.size() == 2) {
//				dispIds.add((Integer)dispsR.get(1)[0]);
//			}
//			
//		}
		
		if (dispsU != null) {
			
			for (int i= dispsU.size() - 1; i > 0; i--){
				dispIds.add((Integer)dispsU.get(i)[0]);
			}
			
			if (dispsU.size() >= 1 && (dispsR == null || dispsR.size() == 0 || !dispsR.get(0)[0].equals(dispsU.get(0)[0])) ) {
				dispIds.add((Integer)dispsU.get(0)[0]);
			}
		}
		
		if (dispsR != null) {
			
			for (int i= dispsR.size() - 1; i >= 0; i--){
				dispIds.add((Integer)dispsR.get(i)[0]);
			}
		}
		
		return dispIds;
	}
	
	
	
	@SuppressWarnings("unchecked")	
	private List<ReservaDTO> consultarReservasPorPeriodoEstadosDisponibilidades(Recurso recurso, VentanaDeTiempo periodo, List<Estado> estados, List<Integer> disponibilidadesIds ) throws BusinessException {

		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}
		
		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		

		Map<Integer, Map<String,String>> valoresPosiblesPorEtiqueta = armoMapaCampoValorEtiqueta(recurso);
		
		int i;
		String whereEstados = null;
		for (i=1; i <= estados.size(); i++) {
			if (whereEstados != null) {
				whereEstados += " or ";
			}
			else {
				whereEstados = "";
			}
			
			whereEstados += "r.estado = :estado" + i;
		}

		String whereDispIds = null;
		for (i=1; i <= disponibilidadesIds.size(); i++) {
			if (whereDispIds != null) {
				whereDispIds += " or ";
			}
			else {
				whereDispIds = "";
			}
			
			whereDispIds += "d.id = :dispId" + i;
		}
		String where = "";
		if (whereEstados != null) {
			where += "and ( " + whereEstados + " ) ";
		}
		if (whereDispIds != null) {
			where += "and ( " + whereDispIds + " ) ";
		}
		

		//Esta consulta no funciona con reserva multiples.
		//Asumo que no existen reservas multiples
		String queryString = 
			"select r.id, r.numero, r.estado, d.id, d.fecha, d.horaInicio, das.id, das.nombre, das.tipo, dr.valor, ll.puesto " +
			"from   Reserva r " +
			"       join r.disponibilidades d " +
			"       left join r.datosReserva dr " +
			"		left join dr.datoASolicitar das " +
			"       left join r.llamada ll " +
			"where   " +
			"       d.recurso.id = :recurso and " +
			"       d.fecha between :fi and :ff " +
					where +
			"order by d.fecha, d.horaInicio, r.numero "; 

		Query query = entityManager.createQuery(queryString);
		query.setParameter("recurso", recurso.getId());
		query.setParameter("fi", periodo.getFechaInicial(), TemporalType.DATE);
		query.setParameter("ff", periodo.getFechaFinal(), TemporalType.DATE);

		i = 1;
		for (Estado estado : estados) {
			query.setParameter("estado"+i, estado);
			i++;
		}

		i = 1;
		for (Integer dispId : disponibilidadesIds) {
			query.setParameter("dispId"+i, dispId);
			i++;
		}

		
		
		List<Object[]> resultados = query.getResultList();
		
		List<ReservaDTO> reservas = new ArrayList<ReservaDTO>();

		Integer idReservaActual = null;
		ReservaDTO reservaDTO = null;
		
		Iterator<Object[]> iterator = resultados.iterator();
		while (iterator.hasNext()) {

			Object[] rowReserva = iterator.next();
		
			Integer reservaId        = (Integer)rowReserva[0];
			Integer reservaNumero    = (Integer)rowReserva[1];
			String  reservaEstado    = ((Estado) rowReserva[2]).toString();
//			Integer dispId           = (Integer)rowReserva[3];
			Date    dispFecha        = (Date) rowReserva[4];
			Date    dispHoraInicio   = (Date) rowReserva[5];
			Integer datoASolicitarId = (Integer) rowReserva[6];
			String  nombreDatoReserva= (String) rowReserva[7];
			Tipo    tipoDatoReserva  = (Tipo) rowReserva[8];
			Object  valorDatoReserva = (Object) rowReserva[9];
			Integer puesto           = (Integer)rowReserva[10];


			if (idReservaActual == null || ! idReservaActual.equals(reservaId)) {
				//recien arranco o cambio de reserva: 
				
				idReservaActual = reservaId;
				
				if (reservaDTO != null) {
					//Cambio de reserva, guardo la actual como resultado
					reservas.add(reservaDTO);
				}
				
				reservaDTO = new ReservaDTO();
				
				reservaDTO.setId(reservaId);
				reservaDTO.setNumero(reservaNumero);
				reservaDTO.setEstado(reservaEstado);
				reservaDTO.setFecha(dispFecha);
				reservaDTO.setHoraInicio(dispHoraInicio);
				if (puesto != null) {
					reservaDTO.setPuestoLlamada(puesto);
				}
				
			}
			//El else indica que estoy iterando sobre los datos de la reserva
			
			if (nombreDatoReserva != null) {
				
				
				if (tipoDatoReserva == Tipo.LIST) {
					//Lista de valores: etiqueta del valor
					String valor = valoresPosiblesPorEtiqueta.get(datoASolicitarId).get(valorDatoReserva);
					reservaDTO.getDatos().put(nombreDatoReserva, valor);			
				}
				// 02/06/2014 - ANGARCIA
				else if (tipoDatoReserva == Tipo.DATE) {

					Date dFecha;
					String sFecha = "";

					if (valorDatoReserva != null) {

						try {
							SimpleDateFormat parserFecha = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
							dFecha = parserFecha.parse((String) valorDatoReserva);
							parserFecha = new SimpleDateFormat("dd/MM/yyyy");
							sFecha = parserFecha.format(dFecha);

						} catch (ParseException ex) {
							sFecha = (String) valorDatoReserva;
						}
					}
					reservaDTO.getDatos().put(nombreDatoReserva, sFecha);

				}
				// 02/06/2014 - FIN ANGARCIA
				else {
					//Tipo simple: valor
					reservaDTO.getDatos().put(nombreDatoReserva, valorDatoReserva);			
				}
				
			}
		}

		if (reservaDTO != null) {
			//Al salir del loop, siempre me queda la ultima reserva para agregar al resultado
			reservas.add(reservaDTO);
		}
		
		
		return reservas;
	}
	
	
	private Map<Integer, Map<String,String>> armoMapaCampoValorEtiqueta(Recurso recurso) {
		
		Map<Integer, Map<String,String>> valoresPosibles = new HashMap<Integer, Map<String,String>>();
		for (DatoASolicitar ds: recurso.getDatoASolicitar()) {
			if (ds.getTipo() == Tipo.LIST) {
				Map<String, String> valores = new HashMap<String, String>();
				valoresPosibles.put(ds.getId(), valores);
				
				for(ValorPosible vp: ds.getValoresPosibles()) {
					valores.put(vp.getValor(), vp.getEtiqueta());
				}
			}
		}
		
		return valoresPosibles;
	}
	
	
	public List<ReservaDTO> consultarReservasUsadasPeriodo(Recurso recurso, VentanaDeTiempo periodo) throws BusinessException {
		
	
		if (recurso == null || periodo == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		if (periodo.getFechaInicial() == null || periodo.getFechaFinal() == null) {
			throw new BusinessException("-1", "El periodo debe tener inicio y fin");
		}
		
		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
		return consultarReservasUsadasPorPeriodoDisponibilidades(recurso, periodo, new ArrayList<Integer>());
		
	}

	@SuppressWarnings("unchecked")	
	private List<ReservaDTO> consultarReservasUsadasPorPeriodoDisponibilidades(Recurso recurso, VentanaDeTiempo periodo, List<Integer> disponibilidadesIds ) throws BusinessException {

		if (recurso == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		

		Map<Integer, Map<String,String>> valoresPosiblesPorEtiqueta = armoMapaCampoValorEtiqueta(recurso);
		
		int i;
		String whereDispIds = null;
		for (i=1; i <= disponibilidadesIds.size(); i++) {
			if (whereDispIds != null) {
				whereDispIds += " or ";
			}
			else {
				whereDispIds = "";
			}
			
			whereDispIds += "d.id = :dispId" + i;
		}
		String where = "";
		where += "and ( r.estado = 'U' ) ";
		if (whereDispIds != null) {
			where += "and ( " + whereDispIds + " ) ";
		}
		

		//Esta consulta no funciona con reserva multiples.
		//Asumo que no existen reservas multiples
		String queryString = 
			"select r.id, r.numero, r.estado, d.id, d.fecha, d.horaInicio, das.id, das.nombre, das.tipo, dr.valor, ll.puesto, at.asistio " +
			"from   Reserva r " +
			"       join r.disponibilidades d " +
			"       left join r.datosReserva dr " +
			"		left join dr.datoASolicitar das " +
			"       left join r.llamada ll " +
			"       left join r.atenciones at "+
			"where   " +
			"       d.recurso.id = :recurso and " +
			"       d.fecha between :fi and :ff " +
					where +
			"order by d.fecha, d.horaInicio, r.numero "; 

		Query query = entityManager.createQuery(queryString);
		query.setParameter("recurso", recurso.getId());
		query.setParameter("fi", periodo.getFechaInicial(), TemporalType.DATE);
		query.setParameter("ff", periodo.getFechaFinal(), TemporalType.DATE);

		i = 1;
		for (Integer dispId : disponibilidadesIds) {
			query.setParameter("dispId"+i, dispId);
			i++;
		}

		
		
		List<Object[]> resultados = query.getResultList();
		
		List<ReservaDTO> reservas = new ArrayList<ReservaDTO>();

		Integer idReservaActual = null;
		ReservaDTO reservaDTO = null;
		
		Iterator<Object[]> iterator = resultados.iterator();
		while (iterator.hasNext()) {

			Object[] rowReserva = iterator.next();
		
			Integer reservaId        = (Integer)rowReserva[0];
			Integer reservaNumero    = (Integer)rowReserva[1];
			String  reservaEstado    = ((Estado) rowReserva[2]).toString();
//			Integer dispId           = (Integer)rowReserva[3];
			Date    dispFecha        = (Date) rowReserva[4];
			Date    dispHoraInicio   = (Date) rowReserva[5];
			Integer datoASolicitarId = (Integer) rowReserva[6];
			String  nombreDatoReserva= (String) rowReserva[7];
			Tipo    tipoDatoReserva  = (Tipo) rowReserva[8];
			Object  valorDatoReserva = (Object) rowReserva[9];
			Integer puesto           = (Integer)rowReserva[10];
			Boolean vino             = (Boolean)rowReserva[11];


			if (idReservaActual == null || ! idReservaActual.equals(reservaId)) {
				//recien arranco o cambio de reserva: 
				
				idReservaActual = reservaId;
				
				if (reservaDTO != null) {
					//Cambio de reserva, guardo la actual como resultado

					reservas.add(reservaDTO);
				}
				
				reservaDTO = new ReservaDTO();
				
				reservaDTO.setId(reservaId);
				reservaDTO.setNumero(reservaNumero);
				reservaDTO.setEstado(reservaEstado);
				reservaDTO.setFecha(dispFecha);
				reservaDTO.setHoraInicio(dispHoraInicio);
				if (puesto != null) {
					reservaDTO.setPuestoLlamada(puesto);
				}
				
				if (vino != null){
					reservaDTO.setAsistio(vino);
				}
				
			}
			//El else indica que estoy iterando sobre los datos de la reserva
			
			if (nombreDatoReserva != null) {
				
				
				if (tipoDatoReserva == Tipo.LIST) {
					//Lista de valores: etiqueta del valor
					String valor = valoresPosiblesPorEtiqueta.get(datoASolicitarId).get(valorDatoReserva);
					reservaDTO.getDatos().put(nombreDatoReserva, valor);			
				}
				else {
					//Tipo simple: valor
					reservaDTO.getDatos().put(nombreDatoReserva, valorDatoReserva);			
				}
				
			}


		}

		if (reservaDTO != null) {
			//Al salir del loop, siempre me queda la ultima reserva para agregar al resultado

			reservas.add(reservaDTO);
		}
		
		
		return reservas;
	}
	

	
}
