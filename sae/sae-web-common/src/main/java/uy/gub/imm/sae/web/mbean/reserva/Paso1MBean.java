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

package uy.gub.imm.sae.web.mbean.reserva;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJBAccessException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BaseException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.RolException;
import uy.gub.imm.sae.common.factories.BusinessLocatorFactory;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.web.common.SAECalendarDataModel;
import uy.gub.imm.sae.web.common.SAECalendarioDataSource;

public class Paso1MBean	extends PasoMBean implements SAECalendarioDataSource {
	
	static Logger logger = Logger.getLogger(Paso1MBean.class);
	private AgendarReservas agendarReservasEJB;

	private Recursos recursosEJB;
		

	/* Será utilizado solamente en casos extermos, como que no tenga permiso para acceder a la agenda, o la misma no sea valida, etc...*/
	private String mensajeError = null;
	
	private List<Recurso> recursos;
	private List<SelectItem> recursosItems;

	private List<DatoDelRecurso> infoRecurso;
	
	public void beforePhase (PhaseEvent phaseEvent) {
		disableBrowserCache(phaseEvent);
		
		if (phaseEvent.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sesionMBean.limpiarPaso2();
		}
	}

	@PostConstruct
	public void init() {

		try {
			
			if(getEsIntranet()){
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getAgendarReservas();
				recursosEJB = BusinessLocatorFactory.getLocatorContextoAutenticado().getRecursos();
			} else {				
				agendarReservasEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getAgendarReservas();
				recursosEJB = BusinessLocatorFactory.getLocatorContextoNoAutenticado().getRecursos();
			}
			
			recursosItems = new ArrayList<SelectItem>();

			//Se selecciona la agenda segun el nombre inidicado en el parametro "agenda" del request: ...xhtml?agenda=XXXX
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String agendaNombre = request.getParameter("agenda");
			//julio 2009  - Se agrega el recurso como parametro 
			String recursoNombre = request.getParameter("recurso");
			String paginaDeRetorno = request.getParameter("pagina_retorno");
			boolean soloCuerpo = Boolean.parseBoolean(request.getParameter("solo_cuerpo"));
			
			if (agendaNombre != null && ! agendaNombre.equals("")) {
				//Se esta indicando a que agenda se desea acceder
				if (sesionMBean.getAgenda() == null || ! sesionMBean.getAgenda().getNombre().equals(agendaNombre)) {
					//Y es distinta de la actualmente seleccionada
					sesionMBean.setPaginaDeRetorno(paginaDeRetorno);
					sesionMBean.seleccionarAgenda(agendaNombre);
					sesionMBean.setSoloCuerpo(soloCuerpo);
				}
			}
		
			
			Recurso recursoDefecto = null;
			//Cargo los recursos
			if (sesionMBean.getAgenda() != null) {
				recursos = agendarReservasEJB.consultarRecursos(sesionMBean.getAgenda());

				for (Recurso recurso : recursos) {
					if (getEsIntranet()){
						SelectItem item = new SelectItem();
						item.setLabel(recurso.getDescripcion());
						item.setValue(recurso.getNombre());
						recursosItems.add(item);
						
						//Si es el recurso que se ingreso por parametro se guarda para seleccionarlo por defecto.
						if (recursoNombre != null && recurso.getNombre().equals(recursoNombre)){
							recursoDefecto = recurso;
						}
					}else if (recurso.getVisibleInternet()){
						SelectItem item = new SelectItem();
						item.setLabel(recurso.getDescripcion());
						item.setValue(recurso.getNombre());
						recursosItems.add(item);
							
						//Si es el recurso que se ingreso por parametro se guarda para seleccionarlo por defecto.
						if (recursoNombre != null && recurso.getNombre().equals(recursoNombre)){
							recursoDefecto = recurso;
						}
					}else if (recursoNombre != null && recurso.getNombre().equals(recursoNombre)){
						throw new RolException(getI18N().getText("message.rolexception"));
					}
				}
			
				//Selecciono el recurso por defecto.
				if (! recursos.isEmpty() ) {
					
					if (recursoDefecto == null ){
						//No se ingreso un recurso en la url, o no existe ese recurso vivo para la agenda.
						//Si hay un recurso seleccionado, me quedo con ese, sino se carga el primero.
						if (sesionMBean.getRecurso() == null){
							sesionMBean.setRecurso(recursos.get(0));
						}
					}
					else {
						//Se ingreso un recurso en la url y se encontro para la agenda.
						sesionMBean.setRecurso(recursoDefecto);
					}
				}
				
				configurarCalendario();
			}

			
		} catch (RolException e1) {
			//El usuario no tiene suficientes privilegios para acceder a la agenda
			setMensajeError(e1.getMessage());
		} catch (EJBAccessException e2) {
			//El usuario no tiene suficientes privilegios para acceder a la agenda
			setMensajeError("ACCESO DENEGADO");
		} catch (ApplicationException e) {
			logger.error("NO SE PUDO OBTENER EJBs");
			logger.error(e);
			redirect(ERROR_PAGE_OUTCOME);
		} catch (BaseException e) {
			logger.error(e);
			redirect(ERROR_PAGE_OUTCOME);
		}
		
	}
	
	
	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}

	
	/**
	 * Es necesario pues debo forzar a que desde cada paso que se ejecute el init de este managed bean, pues es en el donde se 
	 * analizan los parametros del request de donde se saca el solo_cuerpo
	 * @return
	 */
	public Boolean getSoloCuerpo () {
		return sesionMBean.getSoloCuerpo();
	}
	
	public String getAgendaNombre() {

		if (sesionMBean.getAgenda() != null) {
			return sesionMBean.getAgenda().getDescripcion();
		}
		else {
			return null;
		}
	}
	
	public String getRecursoNombre() {
		if (sesionMBean.getRecurso() != null) {
			return  sesionMBean.getRecurso().getNombre();
		}
		else {
			return null;
		}
	}

	public String getRecursoDescripcion() {
		
		String result = null;
		
		if (sesionMBean.getRecurso() != null) {
			result = sesionMBean.getRecurso().getDescripcion();
		}

		return (result == null ? "":result);
	}

	public void setRecursoNombre(String recursoNombre) {
		if (!sesionMBean.getRecurso().getNombre().equals(recursoNombre)) {

			try {
				Boolean encontre = false;
				Iterator<Recurso> iter = recursos.iterator();
				while (iter.hasNext() && ! encontre) {
					Recurso r = iter.next();
					if (r.getNombre().equals(recursoNombre)) {
						sesionMBean.setRecurso(r);
						configurarCalendario();
						encontre = true;
					}
				}
			}
			catch (Exception e) {
				addErrorMessage(e);
			}
		}
	}

	public List<DatoDelRecurso> getInfoRecurso() {

		if (infoRecurso == null) {
			if (sesionMBean.getRecurso() != null) {
				try {
					infoRecurso = recursosEJB.consultarDatosDelRecurso(sesionMBean.getRecurso());
					if (infoRecurso.isEmpty()) {
						infoRecurso = null;
					}
				} catch (Exception e) {
					addErrorMessage(e);
				}
			}
		}
		return infoRecurso;
	}
	
	public List<SelectItem> getRecursosItems() {
		return recursosItems;
	}

	
	public SAECalendarDataModel getCalendario() {
		return sesionMBean.getCalendario();
	}
	
	public Date getCurrentDate() {
		return sesionMBean.getCurrentDate();
	}

	public void setCurrentDate(Date current) {
		sesionMBean.setCurrentDate(current);
	}
	
	public Date getDiaSeleccionado() {

		//Siempre retorno null, asi de esta forma ante una vuelta atras (del paso 2 al 1) con el boton
		//del browser, se redibuja el calendario sin tener dia marcado.
        //Esto lo necesito pues solo se ejecuta el setDiaSeleccionado si se da el evento onchanged
		//en las celdas del calendario. 
		//Por lo tanto si el dia estuviera marcado en una vuelta atrás, un click sobre la celda de este dia
		//no daria el efecto deseado (ejecutar el submit ajax en el evento onchanged)
		return null;
		//return sesionMBean.getDiaSeleccionado();
	}
	
	public void setDiaSeleccionado(Date dia) {
		sesionMBean.setDiaSeleccionado(dia);
	}
	

	public String getDescripcion() {
		if (getMensajeError() != null) return null;
		
		if (sesionMBean.getAgenda().getTextoAgenda() != null) {
			return 	sesionMBean.getAgenda().getTextoAgenda().getTextoPaso1();
		}
		else {
			return null;
		}
	}

	public String getEtiquetaSeleccionDelRecurso() {
		if (getMensajeError() != null) return null;

		if (sesionMBean.getAgenda().getTextoAgenda() != null) {
			return sesionMBean.getAgenda().getTextoAgenda().getTextoSelecRecurso();
		}
		else {
			return null;
		}
	}
	
	
	//Implementacion de la interfaz SAECalendarioDataSource
	public List<Integer> obtenerCuposXDia(Date desde, Date hasta) {
		if (getMensajeError() != null) return null;

		//Si cambio el mes: actualizo.
		if (! sesionMBean.getVentanaMesSeleccionado().getFechaInicial().equals(Utiles.time2InicioDelDia(desde)) ||
			!sesionMBean.getVentanaMesSeleccionado().getFechaFinal().equals(Utiles.time2FinDelDia(hasta))) {
			
			sesionMBean.getVentanaMesSeleccionado().setFechaInicial(Utiles.time2InicioDelDia(desde));
			sesionMBean.getVentanaMesSeleccionado().setFechaFinal(Utiles.time2FinDelDia(hasta));
			
			sesionMBean.setCuposXdiaMesSeleccionado(null);
			
			try {
				cargarCuposADesplegar(sesionMBean.getRecurso(),	sesionMBean.getVentanaMesSeleccionado()	);
				
			} catch (Exception e) {
				addErrorMessage(e);
			}
		}

		return sesionMBean.getCuposXdiaMesSeleccionado();
	}	
	
	private void configurarCalendario() throws RolException, BusinessException {

		Recurso recurso = sesionMBean.getRecurso();

		VentanaDeTiempo ventanaCalendario;
		if(getEsIntranet()){
			ventanaCalendario = agendarReservasEJB.obtenerVentanaCalendarioIntranet(recurso);
		} else {				
			ventanaCalendario = agendarReservasEJB.obtenerVentanaCalendarioInternet(recurso);
		}

		sesionMBean.setVentanaCalendario(ventanaCalendario);
		
		VentanaDeTiempo ventanaMesSeleccionado = new VentanaDeTiempo();
		Calendar cal = Calendar.getInstance();
		cal.setTime(ventanaCalendario.getFechaInicial());
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		ventanaMesSeleccionado.setFechaInicial(Utiles.time2InicioDelDia(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		ventanaMesSeleccionado.setFechaFinal(Utiles.time2FinDelDia(cal.getTime()));
		sesionMBean.setVentanaMesSeleccionado(ventanaMesSeleccionado);
	
		cargarCuposADesplegar(recurso, ventanaMesSeleccionado);

		
		SAECalendarDataModel calendario = new SAECalendarDataModel(this);
		sesionMBean.setCalendario(calendario);

		sesionMBean.setCurrentDate(ventanaCalendario.getFechaInicial());
		
		sesionMBean.setDiaSeleccionado(null);

	}
	
	private void cargarCuposADesplegar(Recurso r, VentanaDeTiempo ventanaMesSeleccionado){
		
		List<Integer> listaCupos=null;
		try {
			listaCupos= agendarReservasEJB.obtenerCuposPorDia(r, ventanaMesSeleccionado);
			//Se carga la fecha inicial 
			Calendar cont = Calendar.getInstance();
			cont.setTime(Utiles.time2InicioDelDia(sesionMBean.getVentanaMesSeleccionado().getFechaInicial()));
			
			Integer i = 0;

			Date inicio_disp = sesionMBean.getVentanaCalendario().getFechaInicial();
			Date fin_disp = sesionMBean.getVentanaCalendario().getFechaFinal();
		
		
			//Recorro la ventana dia a dia y voy generando la lista completa de cupos x dia con -1, 0, >0 según corresponda.
			while (!cont.getTime().after(sesionMBean.getVentanaMesSeleccionado().getFechaFinal())) {
				if ( cont.getTime().before(inicio_disp ) || 
					 cont.getTime().after(fin_disp) ) {
					listaCupos.set(i, -1);
				}
				cont.add(Calendar.DAY_OF_MONTH, 1);
				i++;
			}

			sesionMBean.setCuposXdiaMesSeleccionado( listaCupos	);
		} catch (Exception e) {
			addErrorMessage(e);
		}

	}


}


