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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import uy.gub.imm.sae.business.api.RecursosLocal;
import uy.gub.imm.sae.business.api.RecursosRemote;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.AccionPorDato;
import uy.gub.imm.sae.entity.AccionPorRecurso;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.ServicioAutocompletarPorDato;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.TextoRecurso;
import uy.gub.imm.sae.entity.ValidacionPorDato;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;
import uy.gub.imm.sae.entity.ValorPosible;

@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO", "RA_AE_LLAMADOR"})
public class RecursosBean implements RecursosLocal, RecursosRemote{

	
	private final static String GROUP_NAME_PREFIX = "GR_";
	private final static String AGENDA_NAME_PREFIX = "AG_";
		
	
	
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager entityManager;

	/**
	 * Crea el recurso <b>r</b> asociándolo a la agenda <b>a</b>.
	 * Controla la unicidad del nombre del recurso entre todos los recursos vivos (fechaBaja == null).
	 * Se controla que la agenda se encuentre viva.
	 * Se permite setear todos sus atributos menos la fechaBaja. 
	 * Controla:
	 *	fechaInicio <> NULL
	 *  fechaInicio <= fechaFin o fechaFin == NULL
	 *	fechaInicioDisp <> NULL 	 
	 *	fechaInicioDisp <= fechaFinDisp o fechaFinDisp == NULL
	 *	fechaInicio <= fechaInicioDisp
	 * 	ventanaDiasMinimos > 0
	 *  ventanaCuposMinimos > 0
	 *	cantDiasAGenerar > 0
	 *	largoListaEspera > 0
	 *  largo del campo Serie menor o igual a 3
	 * 	cantDiasAGenerar >= ventanaDiasMinimos 
	 * Retorna el recurso con su identificador interno.
	 * Controla que el usuario tenga rol Planificador sobre la agenda <b>a</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 */
	public Recurso crearRecurso(Agenda a, Recurso r) throws UserException, ApplicationException, BusinessException {
		
		if (a == null) {
			throw new BusinessException("AEB0001");
		}
		Agenda aManejada = (Agenda) entityManager.find(Agenda.class, a.getId());
		if (aManejada == null) {
			throw new BusinessException("AE10002", "No existe la agenda: "+a.getId()+" - "+a.getNombre());
		}
		//No se puede dar de alta un recurso para una agenda cerrada
		if (aManejada.getFechaBaja() != null) {
			throw new BusinessException("AE10021","No se puede dar de alta un recurso para una agenda cerrada");			
		}
		a = aManejada;
		aManejada = null; //De aquí en mas utilizo "a".


		r.setAgenda(a);
		
		
		
		//Controla la unicidad del nombre del recurso entre todos los recursos vivos (fechaBaja == null)
		//para la misma agenda.
		if (r.getNombre() != null && existeRecursoPorNombre(r) ) {
				throw new UserException("AE10010","Ya existe el recurso de nombre "+ r.getNombre());
		}
		if (r.getDescripcion() != null && existeRecursoPorDescripcion(r) ) {
			throw new UserException("AE10010","Ya existe el recurso con descripción "+ r.getDescripcion());
		}
		

		if (r.getNombre() == null || r.getNombre().isEmpty()) {
			//Consulto los recursos para encontrar un nombre único de agenda dentro del grupo 
			r.setNombre(getNextRecursoName(r.getAgenda().getNombre()));
		}
		if (r.getDescripcion() == null || r.getDescripcion().isEmpty()) {
			r.setDescripcion(r.getNombre());
		}
		
		
		//Se controla que el recurso no tenga fecha de baja
		if (r.getFechaBaja() != null){
			throw new BusinessException("AE20011","No se puede dar de alta un recurso con fecha de baja: "+ r.getNombre());			
		}

		//fechaInicio <> NULL
		if (r.getFechaInicio() == null){
			throw new UserException("AE10012","Fecha de Inicio no puede ser nula");			
		}
		
		//Se setea hora en fecha de inicio 00:00:00
		r.setFechaInicio(Utiles.time2InicioDelDia(r.getFechaInicio()));

		//Si la fecha de Fin no es nula, se setea la hora al final del Día.
		if (r.getFechaFin() != null){
			r.setFechaFin(Utiles.time2FinDelDia(r.getFechaFin()));
		}

		//fechaInicio <= fechaFin o fechaFin == NULL
		if ((r.getFechaFin() != null)&& (r.getFechaInicio().compareTo(r.getFechaFin()) > 0 )){
			throw new UserException("AE10013","Fecha de Inicio debe ser menor o igual a Fecha Fin o Fecha Fin es nula");			
		}

		//fechaInicioDisp <> NULL
		if (r.getFechaInicioDisp() == null){
			throw new UserException("AE10014","Fecha de Inicio de Disponibilidad no puede ser nula");			
		}

		if (r.getSabadoEsHabil() == null){
			throw new UserException("-1","El atributo SabadoEsHabil no puede ser nulo");			
		}

		//Se setea hora en fecha de inicio disp. 00:00:00
		r.setFechaInicioDisp(Utiles.time2InicioDelDia(r.getFechaInicioDisp()));

		//Si la fecha de Fin de disponibilidad no es nula, se setea la hora al final del Día.
		if (r.getFechaFinDisp() != null){
			r.setFechaFinDisp(Utiles.time2FinDelDia(r.getFechaFinDisp()));	
		}
	

		//fechaInicioDisp <= fechaFinDisp o fechaFinDisp == NULL
		if ((r.getFechaFinDisp() != null) && (r.getFechaInicioDisp().compareTo(r.getFechaFinDisp()) > 0 )){
			throw new UserException("AE10015","Fecha de Inicio Disponibilidad debe ser menor o igual a Fecha Fin Disponibilidad o Fecha Fin Disponibilidad es nula");			
		}

		//fechaInicio <= fechaInicioDisp
		if (r.getFechaInicio().compareTo(r.getFechaInicioDisp()) > 0 ){
			throw new UserException("AE10016","Fecha de Inicio debe ser menor o igual a Fecha inicio Disponibilidad");			
		}
		
		//diasInicioVentanaIntranet >= 0
		if (r.getDiasInicioVentanaIntranet() == null){
			throw new UserException("AE10083","Debe ingresar Días Inicio Ventana de Intranet");
		}
		if (r.getDiasInicioVentanaIntranet() < 0){
			throw new UserException("AE10023","Días Inicio Ventana de Intranet debe ser mayor o igual que cero");			
		}
		
		//diasVentanaIntranet > 0
		if (r.getDiasVentanaIntranet() == null){
			throw new UserException("AE10084","Debe ingresar Días Ventana de Intranet");
		}
		if (r.getDiasVentanaIntranet() <= 0){
			throw new UserException("AE10024","Días Ventana de Intranet debe ser mayor que cero");			
		}
		
		//diasInicioVentanaInternet >= 0
		if (r.getDiasInicioVentanaInternet() == null){
			throw new UserException("AE10085","Debe ingresar Días Inicio Ventana de Internet");
		}
		if (r.getDiasInicioVentanaInternet() < 0){
			throw new UserException("AE10025","Días Inicio Ventana de Internet debe ser mayor o igual que cero");			
		}
		
		//diasVentanaInternet > 0
		if (r.getDiasVentanaInternet() == null){
			throw new UserException("AE10086","Debe ingresar Días Ventana de Internet");
		}
		if (r.getDiasVentanaInternet() <= 0){
			throw new UserException("AE10026","Días Ventana de Internet debe ser mayor que cero");			
		}

		//ventanaCuposMinimos >= 0
		if (r.getVentanaCuposMinimos() == null){
			throw new UserException("AE10079","Debe ingresar Ventana de Cupos Mínimos");
		}
		if (r.getVentanaCuposMinimos() < 0 ){
			throw new UserException("AE10018","Ventana de Cupos Mínimos debe ser mayor o igual a cero");			
		}

		//cantDiasAGenerar > 0
		if (r.getCantDiasAGenerar() == null){
			throw new UserException("AE10082","Debe ingresar Cantidad de dias a Generar");
		}
		if (r.getCantDiasAGenerar() <= 0 ){
			throw new UserException("AE10019","Cantidad de Días a generar debe ser mayor que cero");			
		}

		//cantDiasAGenerar >= diasInicioVentanaIntranet + diasVentanaIntranet
		if (r.getCantDiasAGenerar().compareTo(r.getDiasInicioVentanaIntranet() + r.getDiasVentanaIntranet()) < 0 ){
			throw new UserException("AE10022","Cantidad de Días a generar debe ser mayor o igual que la suma de los Días Inicio Ventana en Intranet y los Días Ventana de Intranet");			
		}

		//cantDiasAGenerar >= diasInicioVentanaInternet + diasVentanaInternet
		if (r.getCantDiasAGenerar().compareTo(r.getDiasInicioVentanaInternet() + r.getDiasVentanaInternet()) < 0 ){
			throw new UserException("AE10022","Cantidad de Días a generar debe ser mayor o igual que la suma de los Días Inicio Ventana en Internet y los Días Ventana de Internet");			
		}
		
		//TODO: Permitir configurar null o 0 para que el largo de la lista de espera pueda ser el total
		//largoListaEspera > 0
//		if ((r.getLargoListaEspera() != null) && (r.getLargoListaEspera() <= 0 )){
//			throw new UserException("AE10021","Largo Lista de Espera debe ser mayor que cero");			
//		}
		
		//Por defecto se crea el recurso con textos del recurso con valores nulos.
		if (r.getTextoRecurso() == null){
			TextoRecurso tnuevo = new TextoRecurso();
			r.setTextoRecurso(tnuevo);
			tnuevo.setRecurso(r);
		}
		
		//Se controla que la serie no tenga largo mayor a 3
		if((r.getSerie() != null)&&(r.getSerie().length() > 3)){
			throw new UserException("AE10028","El largo del campo serie no puede ser mayor a 3");
		}
		
		//se controla que el campo "usaLlamador" no sea null
		if(r.getUsarLlamador() == null){
			throw new UserException("AE10029","El campo Usar LLamador no puede ser null.");
		}
		
		entityManager.persist(r);
		return r;

	}
	
	/**
	 * Modifica los datos del recurso <b>r</b>.
	 * Controla la unicidad del nombre del recurso entre todos los recursos vivos (fechaBaja == null).
	 * Se permite setear todos sus atributos menos la fechaBaja. 
	 * Controla:
	 *	fechaInicio <> NULL
	 *  fechaInicio <= fechaFin o fechaFin == NULL
	 *	fechaInicioDisp <> NULL 	 
	 *	fechaInicioDisp <= fechaFinDisp o fechaFinDisp == NULL
	 *	fechaInicio <= fechaInicioDisp
	 * 	ventanaDiasMinimos > 0
	 *  ventanaCuposMinimos > 0
	 *	cantDiasAGenerar > 0
	 *	largoListaEspera > 0
	 *  El largo del campo Serie no puede ser mayor a 3
	 * 	cantDiasAGenerar >= ventanaDiasMinimos
	 *  Si reservaMultiple = True => se podrá cambiar su valor a reservaMultiple = FALSE 
	 *                               solo si no existe reserva viva con más de una disponibilidad para ese recurso.
     *  No pueden quedar disponibilidades vivas fuera del período fechaInicioDisp y fechaFinDisp.
     *	No pueden quedar reservas vivas fuera del período fechaInicioDisp y fechaFinDisp.  
	 * Retorna el recurso con su identificador interno.
	 * Controla que el usuario tenga rol Planificador sobre la agenda <b>a</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws BusinessException 
	 * @throws ApplicationException 
	 */	
	public void modificarRecurso(Recurso r) throws UserException, BusinessException, ApplicationException {

		Recurso recursoActual = (Recurso) entityManager.find(Recurso.class, r.getId());
		
		if (recursoActual == null) {
			throw new UserException("AE10022","No existe el recurso que se quiere modificar: " + r.getId().toString());
		}
		
		//No se puede modificar un recurso con fecha de baja
		if (recursoActual.getFechaBaja() != null) {
			throw new UserException("AE10024","No se puede modificar un recurso con fecha de baja");
		}
		
		//Controla la unicidad del nombre del recurso entre todos los recursos vivos (fechaBaja == null)
		//para la misma agenda.
		if (existeRecursoPorNombre(r) ) {
				throw new UserException("AE10010","Ya existe el recurso de nombre "+ r.getNombre());
		}
		if (existeRecursoPorDescripcion(r) ) {
			throw new UserException("AE10010","Ya existe el recurso con descripcion "+ r.getDescripcion());
		}
		
		//Se controla que el recurso no tenga fecha de baja
		if (r.getFechaBaja() != null){
			throw new BusinessException("AE10023","No se puede modificar la fecha de baja"+ r.getNombre());			
		}

		//fechaInicio <> NULL
		if (r.getFechaInicio() == null){
			throw new UserException("AE10012","Fecha de Inicio no puede ser nula");			
		}

		//Se setea hora en fecha de inicio 00:00:00
		r.setFechaInicio(Utiles.time2InicioDelDia(r.getFechaInicio()));

		//Si la fecha de Fin de disponibilidad no es nula, se setea la hora al final del Día.
		if (r.getFechaFin() != null){
			r.setFechaFin(Utiles.time2FinDelDia(r.getFechaFin()));
		}

		
		//fechaInicio <= fechaFin o fechaFin == NULL
		if ((r.getFechaFin() != null)&& (r.getFechaInicio().compareTo(r.getFechaFin()) > 0 )){
			throw new UserException("AE10013","Fecha de Inicio debe ser menor o igual a Fecha Fin o Fecha Fin es nula");			
		}

		//fechaInicioDisp <> NULL
		if (r.getFechaInicioDisp() == null){
			throw new UserException("AE10014","Fecha de Inicio de Disponibilidad no puede ser nula");			
		}

		//Se setea hora en fecha de inicio disp. 00:00:00
		r.setFechaInicioDisp(Utiles.time2InicioDelDia(r.getFechaInicioDisp()));

		//Si la fecha de Fin de disponibilidad no es nula, se setea la hora al final del Día.
		if (r.getFechaFinDisp() != null){
			r.setFechaFinDisp(Utiles.time2FinDelDia(r.getFechaFinDisp()));
		}

		
		//fechaInicioDisp <= fechaFinDisp o fechaFinDisp == NULL
		if ((r.getFechaFinDisp() != null) && (r.getFechaInicioDisp().compareTo(r.getFechaFinDisp()) > 0 )){
			throw new UserException("AE10015","Fecha de Inicio Disponibilidad debe ser menor o igual a Fecha Fin Disponibilidad o Fecha Fin Disponibilidad es nula");			
		}

		//fechaInicio <= fechaInicioDisp
		if (r.getFechaInicio().compareTo(r.getFechaInicioDisp()) > 0 ){
			throw new UserException("AE10016","Fecha de Inicio debe ser menor o igual a Fecha Fin Disponibilidad");			
		}

		//diasInicioVentanaIntranet >= 0
		if (r.getDiasInicioVentanaIntranet() == null){
			throw new UserException("AE10083","Debe ingresar Días Inicio Ventana de Intranet");
		}
		if (r.getDiasInicioVentanaIntranet() < 0){
			throw new UserException("AE10023","Días Inicio Ventana de Intranet debe ser mayor o igual que cero");			
		}
		
		//diasVentanaIntranet > 0
		if (r.getDiasVentanaIntranet() == null){
			throw new UserException("AE10084","Debe ingresar Días Ventana de Intranet");
		}
		if (r.getDiasVentanaIntranet() <= 0){
			throw new UserException("AE10024","Días Ventana de Intranet debe ser mayor que cero");			
		}
		
		//diasInicioVentanaInternet >= 0
		if (r.getDiasInicioVentanaInternet() == null){
			throw new UserException("AE10085","Debe ingresar Días Inicio Ventana de Internet");
		}
		if (r.getDiasInicioVentanaInternet() < 0){
			throw new UserException("AE10025","Días Inicio Ventana de Internet debe ser mayor o igual que cero");			
		}
		
		//diasVentanaInternet > 0
		if (r.getDiasVentanaInternet() == null){
			throw new UserException("AE10086","Debe ingresar Días Ventana de Internet");
		}
		if (r.getDiasVentanaInternet() <= 0){
			throw new UserException("AE10026","Días Ventana de Internet debe ser mayor que cero");			
		}

		//ventanaCuposMinimos >= 0
		if ((r.getVentanaCuposMinimos() == null)||(r.getVentanaCuposMinimos() < 0 )){
			throw new UserException("AE10018","Ventana de Cupos Mínimos debe ser mayor o igual que cero");			
		}

		//cantDiasAGenerar > 0
		if ((r.getCantDiasAGenerar() == null)||(r.getCantDiasAGenerar() <= 0 )){
			throw new UserException("AE10019","Cantidad de Días a generar debe ser mayor que cero");			
		}

		//cantDiasAGenerar >= diasInicioVentanaIntranet + diasVentanaIntranet
		if (r.getCantDiasAGenerar().compareTo(r.getDiasInicioVentanaIntranet() + r.getDiasVentanaIntranet()) < 0 ){
			throw new UserException("AE10022","Cantidad de Días a generar debe ser mayor o igual que la suma de los Días Inicio Ventana en Intranet y los Días Ventana de Intranet");			
		}

		//cantDiasAGenerar >= diasInicioVentanaInternet + diasVentanaInternet
		if (r.getCantDiasAGenerar().compareTo(r.getDiasInicioVentanaInternet() + r.getDiasVentanaInternet()) < 0 ){
			throw new UserException("AE10022","Cantidad de Días a generar debe ser mayor o igual que la suma de los Días Inicio Ventana en Internet y los Días Ventana de Internet");			
		}
		
		if (r.getReservaMultiple() == null){
			throw new BusinessException("AE20001","Reserva Multiple no puede ser nula");			
		}

		if (r.getSabadoEsHabil() == null){
			throw new BusinessException("AE20002","El atributo SabadoEsHabil no puede ser nulo");			
		}

		//  Si reservaMultiple = True => se podrá cambiar su valor a reservaMultiple = FALSE 
		//  solo si no existe reserva viva con más de una disponibilidad para ese recurso.
		if ((recursoActual.getReservaMultiple()!= r.getReservaMultiple() )
				&& (r.getReservaMultiple() == false) ){
			//no existe reserva viva con más de una disponibilidad para ese recurso.
			if (existeReservaVivaMultiple(r)){
				throw new UserException("AE10025","No se puede desactivar reservaMultiple si existen reservas multiples vivas");
			}
		}

		//No pueden quedar disponibilidades vivas fuera del período fechaInicioDisp y fechaFinDisp.
		//OJO, hay que ver bien que se quiere controlar aca!!!
		if ( hayDispVivasPorFecha(r, r.getFechaInicioDisp(), r.getFechaFinDisp()) ){
			throw new UserException("AE10026","No pueden quedar disponibilidades vivas fuera del período fechaInicioDisp y fechaFinDisp");
		}
		
	    //No pueden quedar reservas vivas fuera del período fechaInicioDisp y fechaFinDisp.
		if ( hayReservasVivasPorFecha(r,r.getFechaInicioDisp(),r.getFechaFinDisp()) ){
			throw new UserException("AE10027","No pueden quedar reservas vivas fuera del período fechaInicioDisp y fechaFinDisp");
		}
		
		//Se controla que el texto del paso 2 no supere el largo esperado
		if ((r.getTextoRecurso() != null) && (r.getTextoRecurso().getTextoPaso2() != null) && (r.getTextoRecurso().getTextoPaso2().length() > 1000)){
			throw new UserException("AE10028","El texto del paso dos y su formato superan el largo esperado");			
		}
		
		//Se controla que el texto del paso 3 no supere el largo esperado
		if ((r.getTextoRecurso() != null) && (r.getTextoRecurso().getTextoPaso3() != null) && (r.getTextoRecurso().getTextoPaso3().length() > 1000)){
			throw new UserException("AE10028","El texto del paso tres y su formato superan el largo esperado");			
		}
		
		//Se controla que la serie no tenga largo mayor a 3
		if((r.getSerie() != null)&&(r.getSerie().length() > 3)){
			throw new UserException("AE10028","El largo del campo serie no puede ser mayor a 3");
		}
		
	
		recursoActual.setNombre(r.getNombre());
		recursoActual.setDescripcion(r.getDescripcion());
		recursoActual.setFechaInicio(r.getFechaInicio());
		recursoActual.setFechaFin(r.getFechaFin());
		recursoActual.setFechaInicioDisp(r.getFechaInicioDisp());
		recursoActual.setFechaFinDisp(r.getFechaFinDisp());
		recursoActual.setDiasInicioVentanaIntranet(r.getDiasInicioVentanaIntranet());
		recursoActual.setDiasVentanaIntranet(r.getDiasVentanaIntranet());
		recursoActual.setDiasInicioVentanaInternet(r.getDiasInicioVentanaInternet());
		recursoActual.setDiasVentanaInternet(r.getDiasVentanaInternet());
		recursoActual.setVentanaCuposMinimos(r.getVentanaCuposMinimos());
		recursoActual.setCantDiasAGenerar(r.getCantDiasAGenerar());
		recursoActual.setLargoListaEspera(r.getLargoListaEspera());
		recursoActual.setSerie(r.getSerie());
		recursoActual.setVisibleInternet(r.getVisibleInternet());
		recursoActual.setReservaMultiple(r.getReservaMultiple());
		recursoActual.setMostrarNumeroEnLlamador(r.getMostrarNumeroEnLlamador());
		recursoActual.setMostrarNumeroEnTicket(r.getMostrarNumeroEnTicket());
		recursoActual.setSabadoEsHabil(r.getSabadoEsHabil());
		
		TextoRecurso texto = recursoActual.getTextoRecurso();
		texto.setTextoPaso2(r.getTextoRecurso().getTextoPaso2());
		texto.setTextoPaso3(r.getTextoRecurso().getTextoPaso3());
		texto.setTituloCiudadanoEnLlamador(r.getTextoRecurso().getTituloCiudadanoEnLlamador());
		texto.setTituloPuestoEnLlamador(r.getTextoRecurso().getTituloPuestoEnLlamador());
		texto.setTicketEtiquetaUno(r.getTextoRecurso().getTicketEtiquetaUno());
		texto.setTicketEtiquetaDos(r.getTextoRecurso().getTicketEtiquetaDos());
		texto.setValorEtiquetaUno(r.getTextoRecurso().getValorEtiquetaUno());
		texto.setValorEtiquetaDos(r.getTextoRecurso().getValorEtiquetaDos());

	}

	/**
	 * Realiza una baja lógica del recurso <b>r</b> (se setea fechaBaja con la fecha actual del sistema).
	 * Controla que no existan disponibilidades vivas (fechaBaja == null) y futuras (fecha > fecha actual del sistema).
	 * Controla que no existan reservas vivas (estado R o P).
	 * Controla que no existan agrupaciones de datos en el recurso
	 * Controla que no existan Validaciones aplicadas en el recurso
	 * Controla que el usuario tenga rol Planificador sobre la agenda asociada. 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */	
	public void eliminarRecurso(Recurso r) throws UserException, ApplicationException {
		// TODO Auto-generated method stub
        Recurso recursoActual = (Recurso) entityManager.find(Recurso.class, r.getId());
		
		//Se controla que no existan disponibilidades vivas para el recurso.
		 if  (hayDisponibilidadesVivas(recursoActual)) {
			throw new UserException("AE10142","Se encontró alguna disponibilidad viva para el recurso" );
		}

		//Se controla que no existan reservas vivas para el recurso. 
		 if (hayReservasVivas(recursoActual)){
			throw new UserException("AE10004","Existen reservas vivas para esa agenda/recurso");
		}
		//Se controla que no existan AgrupacionDeDatos vivas para el recurso. 
		 if (hayAgrupacionDatoEnRecursoVivo(recursoActual)){
			throw new UserException("-1","Se encontró alguna agrupación de datos vivas para ese recurso");
		 }
		//Se controla que no existan ValidacionesEnRecurso vivos para el recurso.
		// Esto se controla porque puede ir una validacion a "ningun dato". 
		// (y eso esta bien pues podria ir contra el nombre del recurso)
		if (hayValidacionesEnRecursoVivos(recursoActual)){
			 throw new UserException("-1","Se encontró alguna validación vinculada al recurso");
		}
		
		//Se controla que no existan AccionesEnRecurso vivos para el recurso.
		// Esto se controla porque puede haber acciones sin "ningun dato". 
		if (hayAccionesEnRecursoVivos(recursoActual)){
			 throw new UserException("-1","Se encontró alguna acción vinculada al recurso");
		}
		
		//Se controla que no existan ServiciosAutocompletarEnRecurso vivos para el recurso.
		// Esto se controla porque puede haber algun servicio autocompletar sin "ningun dato". 
		if (hayAutocompletadosEnRecursoVivos(recursoActual)){
			 throw new UserException("-1","Se encontró algún autocompletado vinculado al recurso");
		}
		
		if (recursoActual == null) {
			throw new UserException("AE10022","No existe el recurso: " + r.getId().toString());
		}
		
		recursoActual.setFechaBaja(new Date());
	}

	public Recurso consultarRecurso(Recurso r) throws UserException{
		// TODO Auto-generated method stub
        Recurso recursoActual = (Recurso) entityManager.find(Recurso.class, r.getId());
		
		if (recursoActual == null) {
			throw new UserException("AE10022","No existe el recurso: " + r.getId().toString());
		}
		return recursoActual;
	
	};
	/**
	 * Agrega un DatoDelRecurso <b>d</b> asociándolo al recurso <b>r</b>.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso <b>r</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */
	public DatoDelRecurso  agregarDatoDelRecurso(Recurso r, DatoDelRecurso d) throws UserException {
		// TODO Auto-generated method stub
		Recurso recursoActual = (Recurso) entityManager.find(Recurso.class, r.getId());
		
		if (recursoActual == null) {
			throw new UserException("AE10022","No existe el recurso que se quiere modificar: " + r.getId().toString());
		}
		
		//No se puede agregar un dato a un recurso con fecha de baja
		if (recursoActual.getFechaBaja() != null) {
			throw new UserException("AE10024","No se puede modificar un recurso con fecha de baja");
		}

		d.setRecurso(r);
		
		if (d.getEtiqueta() == null){
			throw new UserException("AE10034","La etiqueta de la información del recurso no puede ser nula");
		}
		
		if (d.getOrden() == null || d.getOrden().intValue() < 0){
			throw new UserException("AE10035","El orden de la información del recurso debe ser mayor o igual a cero");			
		}
		
		if (d.getValor() == null || d.getValor().compareTo("")==0){
			throw new UserException("AE10036","El valor de la información del recurso no puede ser nulo");
		}

		entityManager.persist(d);
		return d;
	}

	/**
	 * Modifica los datos del DatoDelRecurso <b>d</b>.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso asociado a <b>d</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */	
	public void modificarDatoDelRecurso(DatoDelRecurso d) throws UserException {

		DatoDelRecurso datoActual = (DatoDelRecurso) entityManager.find(DatoDelRecurso.class, d.getId());
		
		if (datoActual == null) {
			throw new UserException("AE10033","No existe la información del recurso que se quiere modificar: " + d.getId().toString());
		}
		
		//No se puede modificar un dato de un recurso con fecha de baja
		if (datoActual.getRecurso().getFechaBaja() != null) {
			throw new UserException("AE10024","No se puede modificar un recurso con fecha de baja");
		}
		
		if (d.getEtiqueta() == null){
			throw new UserException("AE10034","La etiqueta de la información del recurso no puede ser nula");
		}
		
		if (d.getOrden() == null || d.getOrden().intValue() < 0){
			throw new UserException("AE10035","El orden de la información del recurso debe ser mayor o igual a cero");			
		}
		
		if (d.getValor() == null || d.getValor().compareTo("")==0){
			throw new UserException("AE10036","El valor de la información del recurso no puede ser nulo");
		}

		
		datoActual.setEtiqueta(d.getEtiqueta());
		datoActual.setOrden(d.getOrden());
		datoActual.setValor(d.getValor());
	}
	
	/**
	 * Realiza una baja física del DatoDelRecurso <b>d</b>
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso asociado a <b>d</b>. 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */	
	public void eliminarDatoDelRecurso(DatoDelRecurso d) throws UserException {

		DatoDelRecurso datoActual = (DatoDelRecurso) entityManager.find(DatoDelRecurso.class, d.getId());
		
		if (datoActual == null) {
			throw new UserException("AE10033","No existe la información del recurso: " + d.getId().toString());
		}
		entityManager.remove(datoActual);
	}

	/**
	 * Retorna una lista de datos del recurso
	 * Controla que el usuario tenga rol Administrador/Planificador sobre la agenda <b>a</b> del recurso
	 * Roles permitidos: Administrador, Planificador
	 */
	@SuppressWarnings("unchecked")
    @RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO","RA_AE_FCALL_CENTER","RA_AE_FATENCION"})	
	public List<DatoDelRecurso> consultarDatosDelRecurso(Recurso r)throws ApplicationException, BusinessException {
		
		if (r == null) {
			throw new BusinessException("-1", "Parametro nulo");
		}
		
		r = entityManager.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra la agenda indicada");
		}
		
		try{
			List<DatoDelRecurso> datosDelRecurso = (List<DatoDelRecurso>) entityManager
									.createQuery("SELECT d from DatoDelRecurso d " +
											     "WHERE d.recurso = :r " +
											     "ORDER BY d.orden")
									.setParameter("r", r)
									// TODO CONTROLAR ROLES
									.getResultList();
			return datosDelRecurso;
			} catch (Exception e){
				throw new ApplicationException(e);
			}
	}
		
	

	/**
	 * Agrega una AgrupacionDato  <b>d</b>.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso <b>r</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public AgrupacionDato agregarAgrupacionDato(Recurso r, AgrupacionDato a) throws UserException, ApplicationException {

		Recurso recursoActual = (Recurso) entityManager.find(Recurso.class, r.getId());
		
		if (recursoActual == null) {
			throw new UserException("AE10022","No existe el recurso que se quiere modificar: " + r.getId().toString());
		}
		
		//No se puede modificar un recurso con fecha de baja
		if (recursoActual.getFechaBaja() != null) {
			throw new UserException("AE10024","No se puede modificar un recurso con fecha de baja");
		}

		a.setRecurso(r);
		
		if ( existeAgrupacionPorNombre(a) ){
			throw new UserException("AE10053","Ya existe esa agrupación para el recurso");
		}
		
		if (a.getNombre() == null){
			throw new UserException("AE10037","El nombre de una agrupación no puede ser nulo");
		}
		
		if (a.getOrden() == null){
			throw new UserException("AE10038","El orden de una agrupación no puede ser nulo");			
		}
		
		entityManager.persist(a);
		return a;
	}

	/**
	 * Modifica Agrupaciones del dato <b>d</b>.
	 * Si tiene fecha baja no se puede modificar.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso asociado a <b>d</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */	
	public void modificarAgrupacionDato(AgrupacionDato a) throws UserException {
		
		AgrupacionDato agrupacionActual = (AgrupacionDato) entityManager.find(AgrupacionDato.class, a.getId());
		
		if (agrupacionActual == null) {
			throw new UserException("AE10039","No existe la agrupacion del dato: " + a.getId().toString());
		}
		
		if (a.getNombre() == ""){
			throw new UserException("AE10037","El nombre de una agrupación no puede ser nulo");
		}
		
		if (a.getOrden() == null){
			throw new UserException("AE10038","El orden de una agrupación no puede ser nulo");			
		}
		if (agrupacionActual.getFechaBaja() != null){
			throw new UserException("AE10068","No se puede modifcar una agrupacion dada de baja");			
		}
		
		agrupacionActual.setNombre(a.getNombre());
		agrupacionActual.setOrden(a.getOrden());
		
	}
	
	/**
	 * Realiza una baja logica de agrupaciones del dato <b>d</b>.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso asociado a <b>d</b>. 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */	
	public void eliminarAgrupacionDato(AgrupacionDato a) throws UserException, ApplicationException  {
		
		AgrupacionDato agrupacionActual = (AgrupacionDato) entityManager.find(AgrupacionDato.class, a.getId());
		
		if (agrupacionActual == null) {
			throw new UserException("AE10039","No existe la agrupacion del dato: " + a.getId().toString());
		}
		if (agrupacionActual.getFechaBaja() != null){
			throw new UserException("AE10068","La agrupacion ya esta dada de baja");			
		}
		if (existeDatoASolicPorAgrupacion(a.getId())) {
			throw new UserException("AE10077","Se encontró algún dato vivo para esa agrupación");
		}
		agrupacionActual.setFechaBaja(new Date());
	}

	/**
	 * Retorna una lista de agrupaciones de datos del recurso
	 * Controla que el usuario tenga rol Administrador/Planificador sobre la agenda <b>a</b> del recurso
	 * Roles permitidos: Administrador, Planificador
	 * @throws ApplicationException 
	 */
	@SuppressWarnings("unchecked")
	public List<AgrupacionDato> consultarAgrupacionesDatos(Recurso r) throws ApplicationException{
		try{
			List<AgrupacionDato> agrupacionDato = (List<AgrupacionDato>) entityManager
									.createQuery("SELECT a from AgrupacionDato a " +
											     "WHERE a.recurso = :r " +
											     "AND a.fechaBaja is null " +
											     "ORDER BY a.orden")
									.setParameter("r", r)
									// TODO CONTROLAR ROLES
									.getResultList();
			return agrupacionDato;
			} catch (Exception e){
				throw new ApplicationException(e);
			}
	}
		


	/**
	 * Agrega un DatoASolicitar <b>d</b> asociándolo al recurso <b>r</b>.
	 * Controla que no exista otro dato vivo (fechaBaja == null) con el mismo nombre.
	 * Se permite setear todos sus atributos menos la fechaBaja.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso <b>r</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public DatoASolicitar agregarDatoASolicitar(Recurso r,AgrupacionDato a, DatoASolicitar d) 
		throws UserException, ApplicationException, BusinessException {

		
		Recurso recursoActual = (Recurso) entityManager.find(Recurso.class, r.getId());
		
		if (recursoActual == null) {
			throw new BusinessException("AEB0003", "No existe el recurso: " + r.getId());
		}
		
		recursoActual.getAgrupacionDatos().size();
		
		
		if (a == null){
			throw new UserException("AE10043","No existe la agrupacion del dato");
		}
		AgrupacionDato agrupacionActual = (AgrupacionDato) entityManager.find(AgrupacionDato.class, a.getId());
		if (agrupacionActual == null) {
			throw new UserException("AE10043","No existe la agrupacion del dato: " + a.getId());
		}
		
		//System.out.println(recursoActual.getNombre());
		//agrupacionActual.getRecurso().setNombre("aa");
		//entityManager.persist(agrupacionActual);
		//entityManager.persist(agrupacionActual.getRecurso());
		//Recurso recursoActual2 = (Recurso) entityManager.find(Recurso.class, r.getId());
		//System.out.println(recursoActual2.getNombre());
		
		//if (agrupacionActual.getRecurso() != recursoActual) {
			//throw new BusinessException("AEB0002", "El recurso de la agrupacion y el indicado como parámetro no coinciden");
		//}
			
		d.setRecurso(recursoActual);
		d.setAgrupacionDato(agrupacionActual);
		
		//No se puede modificar un dato asociado a una agrupación con fecha de baja
		if (agrupacionActual.getFechaBaja() != null) {
			throw new UserException("AE10044","No se puede modificar una agrupación de datos con fecha de baja");
		}

		if ( existeDatoASolicPorNombre (d.getNombre(), recursoActual.getId(), null) ) {
			throw new UserException("AE10054","Ya existe ese dato a solicitar para el recurso");
		}

		if (d.getNombre() == null || d.getNombre().equals("")){
			throw new UserException("AE10045","El nombre del dato a solicitar no puede ser nulo");
		}

		if (d.getEtiqueta() == null || d.getEtiqueta().equals("")){
			throw new UserException("AE10046","La etiqueta del dato a solicitar no puede ser nula");
		}

		if (d.getTipo() == null || d.getTipo().equals("")){
			throw new UserException("AE10047","El tipo del dato a solicitar no puede ser nulo");
		}
		
		if (d.getRequerido() == null){
			throw new UserException("AE10048","Se debe indicar si el dato a solicitar debe es requerido");
		}

		if (d.getEsClave() == null){
			throw new UserException("AE10049","Se debe indicar si el dato a solicitar debe es clave");
		}

		if (d.getFila() == null){
			throw new UserException("AE10050","La fila no puede ser nula");			
		}
		
		if (d.getColumna() == null){
			throw new UserException("AE10051","La columna no puede ser nula");			
		}

		if (d.getLargo() == null){
			throw new UserException("AE10052","El largo no puede ser nulo");			
		}

		if (d.getIncluirEnReporte() == null){
			throw new UserException("AE10120","Incluir en Reporte no puede ser nulo");			
		}

		if (d.getAnchoDespliegue() == null){
			throw new UserException("AE10121","El ancho de despliegue no puede ser nulo");			
		}
		
		if (d.getIncluirEnReporte()== true && d.getAnchoDespliegue().intValue()<= 0){
			throw new UserException("AE10122","El ancho de despliegue debe ser mayor que cero");			
		}
		
		if (d.getIncluirEnLlamador() == null) {
			throw new UserException("-1","Incluir en llamador no puede ser nulo");
		}
		
		if (d.getOrdenEnLlamador() == null && d.getIncluirEnReporte()) {
			throw new UserException("-1","Debe indicar el orden en el que se mostrará el dato en la pantalla del llamador");
		}
		
		else if (d.getOrdenEnLlamador() == null){
			d.setOrdenEnLlamador(1);
		}
		
		if (d.getLargoEnLlamador() == null){
			d.setLargoEnLlamador(d.getLargo());
		}

		
		entityManager.persist(d);
		return d;
	}

	/**
	 * Modifica los datos de un DatoASolicitar <b>d</b>.
	 * Controla que no exista otro dato vivo (fechaBaja == null) con el mismo nombre.
	 * Controla que no existan datos de reserva asociados al DatoASolicitar.
	 * Se permite setear todos sus atributos menos la fechaBaja.
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso <b>r</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */	
	public void modificarDatoASolicitar(DatoASolicitar d) throws UserException, ApplicationException {

		DatoASolicitar datoActual = (DatoASolicitar) entityManager.find(DatoASolicitar.class, d.getId());
		
		if (datoActual == null) {
			throw new UserException("AE10055","No existe el dato a Solicitar: " + d.getId().toString());
		}

		//No se puede modificar un dato con fecha de baja
		if (datoActual.getFechaBaja() != null) {
			throw new UserException("AE10056","No se puede modificar un dato con fecha de baja");
		}

		if ( existeDatoASolicPorNombre(d.getNombre(), datoActual.getRecurso().getId(), d.getId()) ) {
			throw new UserException("AE10054","Ya existe ese dato a solicitar para el recurso");
		}

		if (d.getNombre() == null){
			throw new UserException("AE10045","El nombre del dato a solicitar no puede ser nulo");
		}

		if (d.getEtiqueta() == null){
			throw new UserException("AE10046","La etiqueta del dato a solicitar no puede ser nula");
		}

		if (d.getTipo() == null){
			throw new UserException("AE10047","El tipo del dato a solicitar no puede ser nulo");
		}
		
		if (d.getRequerido() == null){
			throw new UserException("AE10048","Se debe indicar si el dato a solicitar debe es requerido");
		}

		if (d.getEsClave() == null){
			throw new UserException("AE10049","Se debe indicar si el dato a solicitar debe es clave");
		}

		if (d.getFila() == null){
			throw new UserException("AE10050","La fila no puede ser nula");			
		}
		
		if (d.getColumna() == null){
			throw new UserException("AE10051","La columna no puede ser nula");			
		}

		if (d.getLargo() == null){
			throw new UserException("AE10052","El largo no puede ser nulo");			
		}

		if (d.getIncluirEnReporte() == null){
			throw new UserException("AE10120","Incluir en Reporte no puede ser nulo");			
		}

		if (d.getAnchoDespliegue() == null){
			throw new UserException("AE10121","El ancho de despliegue no puede ser nulo");			
		}

		if (d.getIncluirEnReporte()== true && d.getAnchoDespliegue().intValue()<= 0){
			throw new UserException("AE10122","El ancho de despliegue debe ser mayor que cero");			
		}
		
		if (d.getIncluirEnLlamador() == null) {
			throw new UserException("-1","Incluir en llamador no puede ser nulo");
		}
		
		if (d.getOrdenEnLlamador() == null && d.getIncluirEnReporte()) {
			throw new UserException("-1","Debe indicar el orden en el que se mostrará el dato en la pantalla del llamador");
		}
		
		else if (d.getOrdenEnLlamador() == null){
			d.setOrdenEnLlamador(1);
		}
		
		if (d.getLargoEnLlamador() == null){
			d.setLargoEnLlamador(d.getLargo());
		}
		
		
		datoActual.setNombre(d.getNombre());
		datoActual.setEtiqueta(d.getEtiqueta());
		datoActual.setTipo(d.getTipo());
		datoActual.setRequerido(d.getRequerido());
		datoActual.setEsClave(d.getEsClave());
		datoActual.setFila(d.getFila());
		datoActual.setColumna(d.getColumna());
		datoActual.setLargo(d.getLargo());
		datoActual.setTextoAyuda(d.getTextoAyuda());
 	    datoActual.setAgrupacionDato(d.getAgrupacionDato());
 	    datoActual.setIncluirEnReporte(d.getIncluirEnReporte());
 	    datoActual.setAnchoDespliegue(d.getAnchoDespliegue());
 	    datoActual.setIncluirEnLlamador(d.getIncluirEnLlamador());
 	    datoActual.setLargoEnLlamador(d.getLargoEnLlamador());
 	    datoActual.setOrdenEnLlamador(d.getOrdenEnLlamador());
	}
	

	/**
	 * Realiza una baja lógica del dato a solicitar <b>d</b> (se setea fechaBaja con la fecha actual del sistema).
	 * Controla que el usuario tenga rol Planificador sobre la agenda asociada. 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */	
	public void eliminarDatoASolicitar(DatoASolicitar d) throws UserException {

		DatoASolicitar datoActual = (DatoASolicitar) entityManager.find(DatoASolicitar.class, d.getId());
		if (datoActual == null) {
			throw new UserException("AE10055","No existe el dato a Solicitar: " + d.getId().toString());		
		}
		//
		// Se chequea que no tenga validaciones asociadas.
		List<ValidacionPorDato> validacionesDato = datoActual.getValidacionesPorDato();
		if (validacionesDato.size() > 0) {
			Date date = new Date();
			for (Iterator<ValidacionPorDato> i = validacionesDato.iterator(); i.hasNext();) {
				ValidacionPorDato v = i.next();
				if (v.getFechaDesasociacion() == null){
					if (v.getValidacionPorRecurso().getFechaBaja()== null){
						throw new UserException("AE10141","No es posible eliminar el dato a solicitar, tiene validaciones asociadas.");		
					}
					if (v.getValidacionPorRecurso().getFechaBaja().compareTo(date) > 0 ) {
						throw new UserException("AE10141","No es posible eliminar el dato a solicitar, tiene validaciones asociadas.");		
					}
				}
			}
		}
		
		
		// Se chequea que no tenga acciones asociadas.
		List<AccionPorDato> accionesDato = datoActual.getAccionesPorDato();
		if (accionesDato.size() > 0) {
			Date date = new Date();
			for (Iterator<AccionPorDato> i = accionesDato.iterator(); i.hasNext();) {
				AccionPorDato a = i.next();
				if (a.getFechaDesasociacion() == null){
					if (a.getAccionPorRecurso().getFechaBaja()== null){
						throw new UserException("AE10141","No es posible eliminar el dato a solicitar, tiene acciones asociadas.");		
					}
					if (a.getAccionPorRecurso().getFechaBaja().compareTo(date) > 0 ) {
						throw new UserException("AE10141","No es posible eliminar el dato a solicitar, tiene acciones asociadas.");		
					}
				}
			}
		}
		
		
		// Se chequea que no tenga autocompletados asociadas.
		List<ServicioAutocompletarPorDato> autocompletarDato = datoActual.getAutocompletadosPorDato();
		if (autocompletarDato.size() > 0) {
			Date date = new Date();
			for (Iterator<ServicioAutocompletarPorDato> i = autocompletarDato.iterator(); i.hasNext();) {
				ServicioAutocompletarPorDato a = i.next();
				if (a.getFechaDesasociacion() == null){
					if (a.getServicioPorRecurso().getFechaBaja()== null){
						throw new UserException("AE10141","No es posible eliminar el dato a solicitar, tiene servicios de autocompletar asociados.");		
					}
					if (a.getServicioPorRecurso().getFechaBaja().compareTo(date) > 0 ) {
						throw new UserException("AE10141","No es posible eliminar el dato a solicitar, tiene servicios de autocompletar asociados.");		
					}
				}
			}
		}
			
				
		datoActual.setFechaBaja(new Date());
	}
	
	
	/**
	 * Agrega un ValorPosible asociándolo al DatoASolicitar.
	 * Controla que no exista otro valor posible con la misma etiqueta y/o valor que se solapen en el 
	 * período (fechaDesde, fechaHasta).
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso <b>r</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public ValorPosible agregarValorPosible(DatoASolicitar d, ValorPosible v) throws UserException, ApplicationException {

		DatoASolicitar datoActual = (DatoASolicitar) entityManager.find(DatoASolicitar.class, d.getId());
		
		if (datoActual == null) {
			throw new UserException("AE10055","No existe el dato a Solicitar: " + d.getId().toString());
		}

		//No se puede modificar un dato con fecha de baja
		if (datoActual.getFechaBaja() != null) {
			throw new UserException("AE10056","No se puede modificar un dato con fecha de baja");
		}

		v.setDato(d);
		

		if (v.getEtiqueta() == null){
			throw new UserException("AE10057","La etiqueta no puede ser nula");			
		}
		
		if (v.getValor() == null){
			throw new UserException("AE10058","El valor no puede ser nulo");
		}
		
		if (v.getOrden() == null){
			throw new UserException("AE10059","El orden no puede ser nulo");			
		}

		if (v.getFechaDesde() == null){
			throw new UserException("AE10060","La fecha desde no puede ser nula");			
		}
		
		//fechaDesde <= fechaHasta o fechaHasta == NULL
		if ((v.getFechaHasta() != null) && (v.getFechaDesde().compareTo(v.getFechaHasta()) > 0 )){
			throw new UserException("AE10061","Fecha desde debe ser menor o igual a fecha hasta o fecha hasta es nula");			
		}

        //Controla que no exista otro valor posible con la misma etiqueta y/o valor que se solapen en el 
		// período (fechaDesde, fechaHasta).
		if (existeValorPosiblePeriodo(v)) {
			throw new UserException("AE10069","No puede existir otra etiqueta/valor posible que se solape en el período");
		}

		entityManager.persist(v);
		return v;
	}

	/**
	 * Modifica el ValorPosible.
	 * Controla que no exista otro valor posible con la misma etiqueta y/o valor que se solapen en el 
	 * período (fechaDesde, fechaHasta).
	 * Permite modificar fecha hasta , ya que significa la vigencia del valor
	 * Controla que el usuario tenga rol Planificador sobre la agenda del recurso <b>r</b> 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 * @throws ApplicationException 
	 */
	public void modificarValorPosible(ValorPosible v) throws UserException, ApplicationException {

		ValorPosible valorActual = (ValorPosible) entityManager.find(ValorPosible.class, v.getId());
		
		if (valorActual == null) {
			throw new UserException("AE10062","No existe el valor posible " + v.getId().toString());
		}

		//No se puede modificar un dato con fecha de baja
		if (valorActual.getDato().getFechaBaja() != null) {
			throw new UserException("AE10056","No se puede modificar un dato con fecha de baja");
		}

		v.setDato(valorActual.getDato());

		//No se puede modificar un valor con fecha de baja
		/* Lo comento pues es incorrecto, ver el comentario del metodo mas arriba para entender.
		if (valorActual.getFechaHasta() != null) {
			throw new UserException("AE10063","No se puede modificar un valor con fecha de baja");
		}*/

		if (v.getEtiqueta() == null){
			throw new UserException("AE10057","La etiqueta no puede ser nula");			
		}
		
		if (v.getValor() == null){
			throw new UserException("AE10058","El valor no puede ser nulo");
		}
		
		if (v.getOrden() == null){
			throw new UserException("AE10059","El orden no puede ser nulo");			
		}

		if (v.getFechaDesde() == null){
			throw new UserException("AE10060","La fecha desde no puede ser nula");			
		}
		
		//fechaDesde <= fechaHasta o fechaHasta == NULL
		if ((v.getFechaHasta() != null) && (v.getFechaDesde().compareTo(v.getFechaHasta()) > 0 )){
			throw new UserException("AE10061","Fecha desde debe ser menor o igual a fecha hasta o fecha hasta es nula");			
		}

        //Controla que no exista otro valor posible con la misma etiqueta y/o valor que se solapen en el 
		// período (fechaDesde, fechaHasta).
		if (existeValorPosiblePeriodo(v)) {
			throw new UserException("AE10069","No puede existir otra etiqueta/valor posible que se solape en el período");
		}

		valorActual.setEtiqueta(v.getEtiqueta());
		valorActual.setOrden(v.getOrden());
		valorActual.setValor(v.getValor());
		valorActual.setFechaDesde(v.getFechaDesde());
		valorActual.setFechaHasta(v.getFechaHasta());
		
	}

	/**
	 * Realiza una baja física del ValorPosible
	 * Controla que el usuario tenga rol Planificador sobre la agenda asociada 
	 * Roles permitidos: Planificador
	 * @throws UserException 
	 */	
	public void eliminarValorPosible(ValorPosible v) throws UserException {

		ValorPosible valorActual = (ValorPosible) entityManager.find(ValorPosible.class, v.getId());
		
		if (valorActual == null) {
			throw new UserException("AE10062","No existe el valor posible " + v.getId().toString());
		}
		entityManager.remove(valorActual);
	
	}

	
	
	
	private Boolean existeRecursoPorNombre(Recurso r) throws ApplicationException{
		try{
		Long cant = (Long) entityManager
							.createQuery("SELECT count(r) from Recurso r " +
									"WHERE UPPER(r.nombre) = :nombre " +
									"AND (r.id <> :id OR :id is null)" +
									"AND r.agenda = :agenda " +
									"AND r.fechaBaja IS NULL")
							.setParameter("nombre", r.getNombre().toUpperCase())
							.setParameter("id", r.getId())
							.setParameter("agenda", r.getAgenda())
							.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}

	}

		
	private Boolean existeRecursoPorDescripcion(Recurso r) throws ApplicationException{
		try{
		Long cant = (Long) entityManager
							.createQuery("SELECT count(r) from Recurso r " +
									"WHERE UPPER(r.descripcion) = :descripcion " +
									"AND (r.id <> :id OR :id is null)" +
									"AND r.agenda = :agenda " +
									"AND r.fechaBaja IS NULL")
							.setParameter("descripcion", r.getDescripcion().toUpperCase())
							.setParameter("id", r.getId())
							.setParameter("agenda", r.getAgenda())
							.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}

	}
	

	private Boolean existeReservaVivaMultiple(Recurso r) throws ApplicationException{
		try {
			
			Date ahora = new Date();
			
			List<?> a = (List<?>) entityManager
					.createQuery("SELECT r.id, count(d) FROM Disponibilidad d JOIN d.reservas r " +
							"WHERE d.recurso = :recurso " +
							"  AND d.fecha >= :fecha" +
							"  AND d.horaFin >= :hora_actual" +
							"  AND (r.estado = :reservado OR r.estado = :pendiente) " +
							"GROUP BY r.id " +
							"HAVING COUNT(d) > 1")
					.setParameter("recurso", r)
					.setParameter("fecha",ahora,TemporalType.DATE)
					.setParameter("hora_actual",ahora,TemporalType.TIMESTAMP)
					.setParameter("reservado", Estado.R)
					.setParameter("pendiente", Estado.P)
					.getResultList();

		return ( !a.isEmpty());
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}

	private Boolean hayReservasVivasPorFecha(Recurso r, Date desde, Date hasta) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(r) FROM Disponibilidad d JOIN d.reservas r " +
							"WHERE d.recurso = :recurso " +
							"  AND (d.fecha < :fecha_desde OR d.fecha > :fecha_hasta )" +
							"  AND (r.estado = :reservado OR r.estado = :pendiente) " )
					.setParameter("recurso", r)
					.setParameter("fecha_desde", desde,TemporalType.DATE)
					.setParameter("fecha_hasta", hasta,TemporalType.DATE)
					.setParameter("reservado", Estado.R)
					.setParameter("pendiente", Estado.P)
					.getSingleResult();

		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}

	private Boolean hayDispVivasPorFecha(Recurso r, Date desde, Date hasta) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(d) FROM Disponibilidad d " +
							"WHERE d.recurso = :recurso " +
							"  AND (d.fecha < :fecha_desde OR d.fecha > :fecha_hasta )" +
							"  AND d.fechaBaja IS NULL")
					.setParameter("recurso", r)
					.setParameter("fecha_desde", desde)
					.setParameter("fecha_hasta", hasta)
					.getSingleResult();

		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}

	private Boolean existeAgrupacionPorNombre(AgrupacionDato a) throws ApplicationException{
		try{
	
		Long cant = (Long) entityManager
							.createQuery("SELECT count(a) from AgrupacionDato a " +
									"WHERE a.nombre = :nombre " +
									"AND (a.id <> :id OR :id is null)" +
									"AND  a.recurso = :recurso " +
									"AND  a.fechaBaja IS NULL")
							.setParameter("nombre", a.getNombre())
							.setParameter("id", a.getId())
							.setParameter("recurso", a.getRecurso())
							.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}

	}

	//
	// Controla que no exista un dato a solicitar con el mismo nombre en la base.
	private Boolean existeDatoASolicPorNombre(String n, Integer idRecurso, Integer idDatoSolicitar) throws ApplicationException{
		try{
			Long cant = (Long) entityManager
							.createQuery("SELECT count(d) "+
									"from DatoASolicitar d " +
									"WHERE upper(d.nombre) = upper(:n) " +
									"AND d.recurso.id = :recurso " +
									"AND d.fechaBaja IS NULL " +
									"AND (d.id <> :idDatoSolicitar or :idDatoSolicitar is null)")
							.setParameter("n", n)
							.setParameter("recurso", idRecurso)
							.setParameter("idDatoSolicitar", idDatoSolicitar)
							.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}

	}

	//
	// Controla que no existan datos a solicitar vivos para la agrupacion
	// ya que si existen esta no se puede eliminar
	private Boolean existeDatoASolicPorAgrupacion(Integer a) throws ApplicationException{
		try{
			Long cant = (Long) entityManager
							.createQuery("SELECT count(d) "+
									"from DatoASolicitar d " +
									"WHERE d.agrupacionDato.id = :a " +
									"AND d.fechaBaja IS NULL")
							.setParameter("a", a)
							.getSingleResult();
		
		return (cant > 0);
		} catch (Exception e){
			throw new ApplicationException(e);
		}

	}

	//Controla que no exista otro valor posible con el mismo valor/etiqueta que se solapen en el período (fechaDesde, fechaHasta).
	private Boolean existeValorPosiblePeriodo(ValorPosible v) throws ApplicationException{
		
		String queryString = 
				"SELECT count(v) from ValorPosible v " +
				"WHERE (v.valor = :valor OR v.etiqueta = :etiqueta ) " +
				(v.getId() != null ? " AND v.id <> :id " : "") +	//Si estoy modifiando un valorPosible, no lo comparo consigo mismo.
				"AND  v.dato = :dato " +
				"AND (" +
				(v.getFechaHasta() == null ? " (v.fechaHasta is null) OR " + //Si el  nuevo valorPosible no tiene fechaHasta y en la DB hay otro sin fechaHasta => se solapan
				                             " (v.fechaHasta is not null and :desde <= v.fechaHasta) " //Si el nuevo valorPosible no tiene fechaHasta y comienza antes del fin del que existe en la DB => se solapan
				                             :
				                             " (v.fechaHasta is null and :hasta >= v.fechaDesde) OR " + //Si el  nuevo valorPosible termina dentro del período del que esta en la DB => se solapan
				                             " (v.fechaHasta is not null and :hasta >= v.fechaDesde and :desde <= v.fechaHasta) " ) + //Si el  nuevo valorPosible termina dentro del período del que esta en la DB => se solapan
				")";
		
		
		
		try{
			Query query = entityManager.createQuery(queryString);
			query.setParameter("valor", v.getValor())
 				 .setParameter("etiqueta", v.getEtiqueta())
 				 .setParameter("dato", v.getDato())
				 .setParameter("desde", v.getFechaDesde(), TemporalType.TIMESTAMP);
			
			if (v.getId() != null) {
				query.setParameter("id", v.getId());
			}
			if (v.getFechaHasta() != null) {
				query.setParameter("hasta", v.getFechaHasta(), TemporalType.TIMESTAMP);
			}

			Long cant = (Long) query.getSingleResult();
			
			return (cant > 0);
			
		} catch (Exception e){
			throw new ApplicationException(e);
		}

	}
	
		


	/**
	 * Retorna el arbol de AgrupacionDato/DatoASolicitar/ValorPosible vivos.
	 * La lista de objetos AgrupacionDato ordenada por el atributo orden.
	 * La lista de objetos DatoASolicitar ordenada por (fila,columna)
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO", "RA_AE_FATENCION","RA_AE_FCALL_CENTER"})
	public List<AgrupacionDato> consultarDefinicionDeCampos(Recurso recurso) throws BusinessException {
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}
		
		Recurso recursoDTO = recurso;
		
		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
		List<AgrupacionDato> agrupacionesDTO = new ArrayList<AgrupacionDato>();

		//Obtengo las agrupaciones vivas del recurso
		List<AgrupacionDato> agrupaciones = (List<AgrupacionDato>) entityManager
											.createQuery(
											"from AgrupacionDato agrupacion " +
											"where agrupacion.recurso = :r and agrupacion.fechaBaja is null " +
											"order by agrupacion.orden ")
											.setParameter("r", recurso)
											.getResultList();
		
		//Para cada agrupacion obtengo los datos a solicitar de la misma.
		for(AgrupacionDato agrupacion: agrupaciones) {

			AgrupacionDato agrupacionDTO = new AgrupacionDato(agrupacion);
			agrupacionDTO.setRecurso(recursoDTO);
			agrupacionesDTO.add(agrupacionDTO);
			
			List<DatoASolicitar> datosDTO = obtenerDatosASolicitar(agrupacionDTO);
			agrupacionDTO.setDatosASolicitar(datosDTO);
			
		}
		
		return agrupacionesDTO;
	}
	
	/**
	 * Retorna el arbol de AgrupacionDato/DatoASolicitar/ValorPosible.
	 * La lista de objetos AgrupacionDato ordenada por el atributo orden.
	 * La lista de objetos DatoASolicitar ordenada por (fila,columna)
	 * Se usa para la consulta de Reservas por datos a Solicitar y actualmente
	 * solo permite traer todos los valores posibles, no los datos
	 * a solicitar ya que esto podría provocar solapamientos en el formulario
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO","RA_AE_FCALL_CENTER","RA_AE_FATENCION"})
	public List<AgrupacionDato> consultarDefCamposTodos(Recurso recurso) throws BusinessException {
		if (recurso == null) {
			throw new BusinessException("AE20084", "El recurso no puede ser nulo");
		}
		
		Recurso recursoDTO = recurso;
		
		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
		List<AgrupacionDato> agrupacionesDTO = new ArrayList<AgrupacionDato>();

		//Obtengo las agrupaciones vivas del recurso
		List<AgrupacionDato> agrupaciones = (List<AgrupacionDato>) entityManager
											.createQuery(
											"from AgrupacionDato agrupacion " +
											"where agrupacion.recurso = :r and agrupacion.fechaBaja is null " +
											"order by agrupacion.orden ")
											.setParameter("r", recurso)
											.getResultList();
		
		//Para cada agrupacion obtengo los datos a solicitar de la misma.
		for(AgrupacionDato agrupacion: agrupaciones) {

			AgrupacionDato agrupacionDTO = new AgrupacionDato(agrupacion);
			agrupacionDTO.setRecurso(recursoDTO);
			agrupacionesDTO.add(agrupacionDTO);
			
			List<DatoASolicitar> datosDTO = obtenerTodosDatosASolicitar(agrupacionDTO);
			agrupacionDTO.setDatosASolicitar(datosDTO);
			
		}
		
		return agrupacionesDTO;
	}
	

	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO","RA_AE_FATENCION"})
	public Boolean mostrarDatosASolicitarEnLlamador(Recurso recurso) throws BusinessException {

		recurso = entityManager.find(Recurso.class, recurso.getId());
		if (recurso == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		
		Long cantAMostrar = (Long) entityManager
				.createQuery(
					"select count(dato) from DatoASolicitar dato " +
					"where dato.recurso = :recurso and " +
					"      dato.fechaBaja is null and " +
					"      dato.incluirEnLlamador = :incluir ")
				.setParameter("recurso", recurso)
				.setParameter("incluir", true)
				.getSingleResult();

		if (cantAMostrar > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private List<DatoASolicitar> obtenerDatosASolicitar(AgrupacionDato agrupacion) throws BusinessException {
		
		List<DatoASolicitar> datosDTO = new ArrayList<DatoASolicitar>();
		
		//Obtengo los datos a solicitar vivos de la agrupacion
		List<DatoASolicitar> datos = (List<DatoASolicitar>) entityManager
									.createQuery(
									"from DatoASolicitar dato " +
									"where dato.agrupacionDato = :agrupacion and " +
									"      dato.fechaBaja is null " +
									"order by dato.fila, dato.columna ")
									.setParameter("agrupacion", agrupacion)
									.getResultList();
		
		//Para cada dato a solicitar que sea del tipo Lista, obtengo los valores posibles.
		for (DatoASolicitar datoASolicitar : datos) {
			
			DatoASolicitar datoASolicitarDTO = new DatoASolicitar(datoASolicitar);
			datoASolicitarDTO.setAgrupacionDato(agrupacion);
			datoASolicitarDTO.setRecurso(agrupacion.getRecurso());
			datosDTO.add(datoASolicitarDTO);
			
			if (datoASolicitarDTO.getTipo() == Tipo.LIST) {
				List<ValorPosible> valoresDTO = obtenerValoresPosibles(datoASolicitar);
				datoASolicitarDTO.setValoresPosibles(valoresDTO);
			}
		}
		
		return datosDTO;
	}

	/**
	 * Obtiene la lista de Datos a Solicitar para armar la consulta de Reserva
	 * por datos de la reserva. No trae los datos a solicitar dados de baja,
	 * debido a que esto podría provocar solapamientos en el formulario dinámico
	 * si recupera todos los valores posibles de los datos a solicitar de tipo
	 * lista aunque ya hayan sido dados de baja.
	 * @param agrupacion
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private List<DatoASolicitar> obtenerTodosDatosASolicitar(AgrupacionDato agrupacion) throws BusinessException {
		
		List<DatoASolicitar> datosDTO = new ArrayList<DatoASolicitar>();
		
		//Obtengo los datos a solicitar vivos de la agrupacion
		List<DatoASolicitar> datos = (List<DatoASolicitar>) entityManager
									.createQuery(
									"from DatoASolicitar dato " +
									"where dato.agrupacionDato = :agrupacion and " +
									"      dato.fechaBaja is null " +
									"order by dato.fila, dato.columna ")
									.setParameter("agrupacion", agrupacion)
									.getResultList();
		
		//Para cada dato a solicitar que sea del tipo Lista, obtengo los valores posibles.
		for (DatoASolicitar datoASolicitar : datos) {
			
			DatoASolicitar datoASolicitarDTO = new DatoASolicitar(datoASolicitar);
			datoASolicitarDTO.setAgrupacionDato(agrupacion);
			datoASolicitarDTO.setRecurso(agrupacion.getRecurso());
			datosDTO.add(datoASolicitarDTO);
			
			if (datoASolicitarDTO.getTipo() == Tipo.LIST) {
				List<ValorPosible> valoresDTO = obtenerTodosValoresPosibles(datoASolicitar);
				datoASolicitarDTO.setValoresPosibles(valoresDTO);
			}
		}
		
		return datosDTO;
	}
	

	
	@SuppressWarnings("unchecked")
	private List<ValorPosible> obtenerValoresPosibles (DatoASolicitar dato) {
		
		List<ValorPosible> valoresDTO = new ArrayList<ValorPosible>(); 
		
		//Obtengo los valores posibles vivos del dato
		List<ValorPosible> valores = (List<ValorPosible>) entityManager
									.createQuery(
									"from ValorPosible valor " +
									"where valor.dato = :d and " +
									"      :ahora >= valor.fechaDesde and " +
									"      (valor.fechaHasta is null or :ahora <= valor.fechaHasta) " +
									"order by valor.orden ")
									.setParameter("d", dato)
									.setParameter("ahora", new Date(), TemporalType.TIMESTAMP)
									.getResultList();
		
		for (ValorPosible valorPosible : valores) {
			valoresDTO.add(new ValorPosible(valorPosible));
		}
		return valoresDTO;
	}

	/**
	 * Obtiene la lista de valores posibles para un dato a solicitar de tipo 
	 * lista sin filtrar por la vigencia, se usa para la consulta de Reserva
	 * por datos de la reserva.
	 * @param dato
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ValorPosible> obtenerTodosValoresPosibles (DatoASolicitar dato) {
		
		List<ValorPosible> valoresDTO = new ArrayList<ValorPosible>(); 
		
		//Obtengo los valores posibles vivos del dato
		List<ValorPosible> valores = (List<ValorPosible>) entityManager
									.createQuery(
									"from ValorPosible valor " +
									"where valor.dato = :d  " +
									"order by valor.orden, valor.fechaDesde ")
									.setParameter("d", dato)
									.getResultList();
		
		for (ValorPosible valorPosible : valores) {
			valoresDTO.add(new ValorPosible(valorPosible));
		}
		return valoresDTO;
	}



	@SuppressWarnings("unchecked")
	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO","RA_AE_FCALL_CENTER","RA_AE_FATENCION"})
	public List<DatoASolicitar> consultarDatosSolicitar(Recurso r) throws ApplicationException{
	
	List<DatoASolicitar> datosDTO = new ArrayList<DatoASolicitar>();
	Map<Integer, AgrupacionDato> agrupacionesDTO = new HashMap<Integer, AgrupacionDato>();
	
	//Obtengo los datos a solicitar vivos de la agrupacion
	List<DatoASolicitar> datos = (List<DatoASolicitar>) entityManager
								.createQuery(
								"from DatoASolicitar dato " +
								"where dato.recurso = :recurso and " +
								"      dato.fechaBaja is null " +
								"order by dato.nombre ")
								.setParameter("recurso", r)
								.getResultList();
	
	//Para cada dato a solicitar que sea del tipo Lista, obtengo los valores posibles.
	for (DatoASolicitar datoASolicitar : datos) {
		
		DatoASolicitar datoASolicitarDTO = new DatoASolicitar(datoASolicitar);
		datoASolicitarDTO.setRecurso(r);
		if (!agrupacionesDTO.containsKey(datoASolicitar.getAgrupacionDato().getId())) {
			AgrupacionDato agrupacionDTO = new AgrupacionDato(datoASolicitar.getAgrupacionDato());
			agrupacionDTO.setRecurso(r);
			agrupacionesDTO.put(agrupacionDTO.getId(), agrupacionDTO);
		}
		datoASolicitarDTO.setAgrupacionDato(agrupacionesDTO.get(datoASolicitar.getAgrupacionDato().getId()));

		datosDTO.add(datoASolicitarDTO);
		if (datoASolicitar.getTipo() == Tipo.LIST) {
//			List<ValorPosible> valoresDTO = obtenerValoresPosibles(datoASolicitar);
			List<ValorPosible> valoresDTO = obtenerTodosValoresPosibles(datoASolicitar);
			datoASolicitarDTO.setValoresPosibles(valoresDTO);
		}
	
	}
	return datosDTO;
	}

	/**
	 * Retorna todos los valores posibles aunque no esten en el rango desde,hasta.
	 */
	public List<ValorPosible> consultarValoresPosibles(DatoASolicitar d)
			throws ApplicationException {
		return this.obtenerTodosValoresPosibles(d);
	}

	private Boolean hayReservasVivas(Recurso r) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(r) FROM Disponibilidad d JOIN d.reservas r " +
							"WHERE d.recurso = :recurso " +
							"  AND d.fecha >= :fecha" +
							"  AND d.horaFin >= :hora" +
							"  AND r.estado IN ('R','P')")
					.setParameter("recurso", r)
					.setParameter("fecha", new Date())
					.setParameter("hora", new Date())
					.getSingleResult();

		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	
	private Boolean hayDisponibilidadesVivas(Recurso r) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(d) FROM Disponibilidad d " +
							"WHERE d.recurso = :recurso " +
							"  AND d.fecha >= :fecha" +
							"  AND d.horaFin >= :hora" +
							"  AND d.fechaBaja is null")
					.setParameter("recurso", r)
					.setParameter("fecha", new Date())
					.setParameter("hora", new Date())
					.getSingleResult();

		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	
	private Boolean hayValidacionesEnRecursoVivos(Recurso r) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(vxr) FROM ValidacionPorRecurso vxr " +
							"WHERE vxr.recurso = :recurso AND vxr.fechaBaja is null")
					.setParameter("recurso", r)
					.getSingleResult();
		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	
	private Boolean hayAccionesEnRecursoVivos(Recurso r) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(axr) FROM AccionPorRecurso axr " +
							"WHERE axr.recurso = :recurso AND axr.fechaBaja is null")
					.setParameter("recurso", r)
					.getSingleResult();
		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	
	private Boolean hayAutocompletadosEnRecursoVivos(Recurso r) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(auxr) FROM ServicioPorRecurso auxr " +
							"WHERE auxr.recurso = :recurso AND auxr.fechaBaja is null")
					.setParameter("recurso", r)
					.getSingleResult();
		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	
	private Boolean hayAgrupacionDatoEnRecursoVivo(Recurso r) throws ApplicationException{
		try {Long cant = (Long) entityManager
					.createQuery("SELECT count(ad) FROM AgrupacionDato ad " +
							"WHERE ad.recurso = :recurso AND ad.fechaBaja is null")
					.setParameter("recurso", r)
					.getSingleResult();
		return (cant > 0);
		}catch (Exception e){
			throw new ApplicationException(e);
		}
	}
	public void copiarRecurso(Recurso r, String nombre, String descripcion) throws BusinessException, ApplicationException, UserException {
		
		r = entityManager.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1", "No se encuentra el recurso indicado");
		}		
		if (r.getFechaBaja() != null) {
			throw new BusinessException("-1", "El recurso ha sido dado de baja");
		}		
		
		copiarRecursoAgenda(r.getAgenda(), r, nombre, descripcion);
	}

	/**
	 * Retorna los servicios de autocompletar asociados al recurso.
	 * @param recurso
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	@RolesAllowed({"RA_AE_ADMINISTRADOR", "RA_AE_PLANIFICADOR","RA_AE_ANONIMO","RA_AE_FCALL_CENTER","RA_AE_FATENCION"})
	public List<ServicioPorRecurso> consultarServicioAutocompletar (Recurso r) throws BusinessException {
		
		List<ServicioPorRecurso> servicios = (List<ServicioPorRecurso>) entityManager
										.createQuery(
										"from ServicioPorRecurso servicio " +
										"where servicio.recurso = :recurso and " +
										"      servicio.fechaBaja is null ")
										.setParameter("recurso", r)
										.getResultList();
		
		for (int i=0; i<servicios.size(); i++){
			servicios.get(i).getAutocompletado().getParametrosAutocompletados().size();
			servicios.get(i).getAutocompletadosPorDato().size();
		}
		
		return servicios;
	}
	
	@Override
	public void copiarRecursoAgenda(Agenda a, Recurso r, String nombre,
			String descripcion) throws BusinessException, ApplicationException,
			UserException {
		// TODO Auto-generated method stub
		r = entityManager.find(Recurso.class, r.getId());
		if (r == null) {
			throw new BusinessException("-1",
					"No se encuentra el recurso indicado");
		}
		if (r.getFechaBaja() != null) {
			throw new BusinessException("-1", "El recurso ha sido dado de baja");
		}

		// 1- Creo un nuevo Recurso y copio los atributos de r.
		// 2- Creo un nuevo TextoRecurso y copio los atributos de
		// r.textoRecurso.
		// 3- Para cada ValidacionPorRecurso de r, creo una copia y la asigno al
		// nuevo recurso
		// 4- Para cada DatoDelRecurso de r, creo una copia y la asigno al nuevo
		// recurso.
		// 5- Para cada AgrupacionDato de r:
		// creo una copia y la asigno al recurso nuevo.
		//
		// 5.1 Para cada DatoASolicitar de la AgrupacionDato:
		// creo una copia y la asigno al recurso nuevo y a la AgrupacionDato
		// nueva.
		//
		// 5.2 Para cada ValorPosible del DatoASolicitar:
		// creo una copia y la asigno al datoASolicitar nuevo.
		//
		// 5.3 Para cada ValidacionPorDato del DatoASolicitar:
		// Creo una ValidacionPorDato nueva y le asigno:
		// el DatoASolicitar nuevo y
		// la ValidacionPorRecurso nueva que se corresponde a la
		// ValidacionPorRecurso original
		// (Para esto usar un Map<ValidacionPorRecursoOrig,
		// ValidacionPorRecursoCopia>

		// 1
		Recurso rCopia = new Recurso(r);
		rCopia.setId(null);
		rCopia.setAgenda(a);
		rCopia.setNombre(nombre);
		rCopia.setDescripcion(descripcion);

		// Controla la unicidad del nombre del recurso nuevo entre todos los
		// recursos vivos (fechaBaja == null) para la misma agenda.
		if (existeRecursoPorNombre(rCopia)) {
			throw new UserException("-1", "Ya existe el recurso de nombre "
					+ rCopia.getNombre());
		}
		if (existeRecursoPorDescripcion(rCopia)) {
			throw new UserException("-1",
					"Ya existe el recurso con descripci�n "
							+ rCopia.getDescripcion());
		}
		entityManager.persist(rCopia);

		// 2
		TextoRecurso trCopia = new TextoRecurso();
		trCopia.setTextoPaso2(r.getTextoRecurso().getTextoPaso2());
		trCopia.setTextoPaso3(r.getTextoRecurso().getTextoPaso3());
		trCopia.setTituloCiudadanoEnLlamador(r.getTextoRecurso()
				.getTituloCiudadanoEnLlamador());
		trCopia.setTituloPuestoEnLlamador(r.getTextoRecurso()
				.getTituloPuestoEnLlamador());
		trCopia.setTicketEtiquetaUno(r.getTextoRecurso().getTicketEtiquetaUno());
		trCopia.setTicketEtiquetaDos(r.getTextoRecurso().getTicketEtiquetaDos());
		trCopia.setValorEtiquetaUno(r.getTextoRecurso().getValorEtiquetaUno());
		trCopia.setValorEtiquetaDos(r.getTextoRecurso().getValorEtiquetaDos());

		trCopia.setRecurso(rCopia);
		entityManager.persist(trCopia);

		// 3
		Map<ValidacionPorRecurso, ValidacionPorRecurso> validacionesDelRecurso = new HashMap<ValidacionPorRecurso, ValidacionPorRecurso>();
		for (ValidacionPorRecurso vxr : r.getValidacionesPorRecurso()) {
			if (vxr.getFechaBaja() == null) {
				ValidacionPorRecurso vxrCopia = new ValidacionPorRecurso();
				vxrCopia.setId(null);
				vxrCopia.setOrdenEjecucion(vxr.getOrdenEjecucion());
				vxrCopia.setRecurso(rCopia);
				vxrCopia.setValidacion(vxr.getValidacion());
				validacionesDelRecurso.put(vxr, vxrCopia);
				entityManager.persist(vxrCopia);
			}
		}

		// 3.1 Autocompletados
		Map<ServicioPorRecurso, ServicioPorRecurso> autocompletadosDelRecurso = new HashMap<ServicioPorRecurso, ServicioPorRecurso>();
		for (ServicioPorRecurso sxr : r.getAutocompletadosPorRecurso()) {
			if (sxr.getFechaBaja() == null) {
				ServicioPorRecurso sxrCopia = new ServicioPorRecurso();
				sxrCopia.setId(null);
				sxrCopia.setRecurso(rCopia);
				sxrCopia.setAutocompletado(sxr.getAutocompletado());
				autocompletadosDelRecurso.put(sxr, sxrCopia);
				entityManager.persist(sxrCopia);
			}
		}

		// 3.2 Acciones

		Map<AccionPorRecurso, AccionPorRecurso> accionesDelRecurso = new HashMap<AccionPorRecurso, AccionPorRecurso>();
		for (AccionPorRecurso axr : r.getAccionesPorRecurso()) {
			if (axr.getFechaBaja() == null) {
				AccionPorRecurso axrCopia = new AccionPorRecurso();
				axrCopia.setId(null);
				axrCopia.setOrdenEjecucion(axr.getOrdenEjecucion());
				axrCopia.setRecurso(rCopia);
				axrCopia.setAccion(axr.getAccion());
				axrCopia.setEvento(axr.getEvento());
				accionesDelRecurso.put(axr, axrCopia);
				entityManager.persist(axrCopia);
			}
		}

		// 4
		for (DatoDelRecurso ddr : r.getDatoDelRecurso()) {

			DatoDelRecurso ddrCopia = new DatoDelRecurso();
			ddrCopia.setOrden(ddr.getOrden());
			ddrCopia.setEtiqueta(ddr.getEtiqueta());
			ddrCopia.setValor(ddr.getValor());
			ddrCopia.setRecurso(rCopia);
			rCopia.getDatoDelRecurso().add(ddrCopia);
			entityManager.persist(ddrCopia);
		}

		// 5
		for (AgrupacionDato agrup : r.getAgrupacionDatos()) {

			if (agrup.getFechaBaja() == null) {
				AgrupacionDato agrupCopia = new AgrupacionDato(agrup);
				agrupCopia.setId(null);
				agrupCopia.setRecurso(rCopia);
				entityManager.persist(agrupCopia);

				// 5.1
				for (DatoASolicitar campo : agrup.getDatosASolicitar()) {

					if (campo.getFechaBaja() == null) {
						DatoASolicitar campoCopia = new DatoASolicitar(campo);
						campoCopia.setId(null);
						campoCopia.setRecurso(rCopia);
						campoCopia.setAgrupacionDato(agrupCopia);

						agrupCopia.getDatosASolicitar().add(campoCopia);
						rCopia.getDatoASolicitar().add(campoCopia);
						entityManager.persist(campoCopia);

						// 5.2
						for (ValorPosible vp : campo.getValoresPosibles()) {
							if (vp.getFechaHasta() == null
									|| vp.getFechaHasta().after(new Date())) {
								ValorPosible vpCopia = new ValorPosible(vp);
								vpCopia.setId(null);
								vpCopia.setDato(campoCopia);
								entityManager.persist(vpCopia);
							}
						}

						// 5.3
						for (ValidacionPorDato vxd : campo
								.getValidacionesPorDato()) {
							if (vxd.getFechaDesasociacion() == null) {
								ValidacionPorDato vxdCopia = new ValidacionPorDato();
								vxdCopia.setNombreParametro(vxd
										.getNombreParametro());
								vxdCopia.setDatoASolicitar(campoCopia);
								ValidacionPorRecurso vxrCopia = validacionesDelRecurso
										.get(vxd.getValidacionPorRecurso());
								vxdCopia.setValidacionPorRecurso(vxrCopia);
								entityManager.persist(vxdCopia);
							}
						}

						// 5.4 ASociamos los datos a solicitar a la accion del
						// recurso
						for (AccionPorDato axd : campo.getAccionesPorDato()) {
							if (axd.getFechaDesasociacion() == null) {
								AccionPorDato axdCopia = new AccionPorDato();
								axdCopia.setNombreParametro(axd
										.getNombreParametro());
								axdCopia.setDatoASolicitar(campoCopia);
								AccionPorRecurso axrCopia = accionesDelRecurso
										.get(axd.getAccionPorRecurso());
								axdCopia.setAccionPorRecurso(axrCopia);
								entityManager.persist(axdCopia);
							}
						}
						// 5.5 Asociamos los datos a solicitar a los
						// autocompletados del recurso
						for (ServicioAutocompletarPorDato saxd : campo
								.getAutocompletadosPorDato()) {
							if (saxd.getFechaDesasociacion() == null) {
								ServicioAutocompletarPorDato saxdCopia = new ServicioAutocompletarPorDato();
								saxdCopia.setNombreParametro(saxd
										.getNombreParametro());
								saxdCopia.setDatoASolicitar(campoCopia);
								ServicioPorRecurso saxrCopia = autocompletadosDelRecurso
										.get(saxd.getServicioPorRecurso());
								saxdCopia.setServicioPorRecurso(saxrCopia);
								entityManager.persist(saxdCopia);
							}
						}

					}

				}// Fin 5.1

			}

		}// Fin 5

	}
	
	
	
	
	
	//Devuelve un nombre de grupo (ex agenda) autogenerado tal que sea único y 
	//cumpla con un formato incremental y amigable estilo GR_01
	//Si no existe ningún grupo devolverá la constante GROUP_NAME_DEFAULT
	private String getNextGroupName() {

		List<String> names = entityManager.createQuery(
			"SELECT a.nombre FROM Agenda a WHERE a.fechaBaja IS NULL and a.nombre like '"+GROUP_NAME_PREFIX+"%'", String.class)
			.getResultList();
		
		return buildNextName(names, GROUP_NAME_PREFIX);
	}

	
	//Devuelve un nombre de agenda (ex recurso) autogenerado tal que sea único y 
	//cumpla con un formato incremetnal y amigable estilo AG_01
	//Generará un nombre único de agenda dentro del grupo de nombre groupName.
	//Si groupName es null o no existe un grupo con ese nombre devolverá siempre AGENDA_NAME_DEFAULT 
	private String getNextRecursoName(String groupName) {

		List<String> names;
		
		if (groupName != null) {
		
			names = entityManager.createQuery(
					"SELECT r.nombre FROM Recurso r WHERE r.fechaBaja IS NULL and r.nombre like '"+AGENDA_NAME_PREFIX+"%' and r.agenda.nombre = :groupName", String.class)
					.setParameter("groupName", groupName)
					.getResultList();
		}
		else  {
			names = new ArrayList<String>();
		}
		
		return buildNextName(names, AGENDA_NAME_PREFIX);
	}

	
	
	private String buildNextName(List<String> names, String prefix) {

		int maxName = 0;
		Pattern extractNumberPattern = Pattern.compile("^"+prefix+"(\\d+)$");

		for (String name : names) {
			Matcher m = extractNumberPattern.matcher(name);
			if (m.matches()) {
				int currentName = Integer.valueOf(m.group(1));
				if (currentName > maxName) {
					maxName = currentName;
				}
			}
		}
		
		maxName ++;
		
		return prefix + maxName;
	}
	
	
}
