/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.
 *
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

package com.sagant.same.web.mbean.administracion.wizzard;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.TextoAgenda;
import uy.gub.imm.sae.entity.TextoRecurso;
import uy.gub.imm.sae.web.common.BaseMBean;

import com.sagant.same.business.api.Wizzard;
import com.sagant.same.web.mbean.administracion.AgendaSelectionMBean;

public class WizzardMBean extends BaseMBean {
	
	private static final String GROUP_DEFAULT_NAME = "Default";
	
	private WIZZARD_STATE state;
	
	//Agenda nueva 
	private Recurso recurso;
	
	//Maneja el estado de la configuración del calendario de disponibilidades
	private Calendar calendar;

	//Maneja el estado del diseño del formulario
	private FormDesign form;
	
	private AgendaSelectionMBean agendaSelection;
	
	private UIComponent contentUI; 
	
	
	@EJB(name="ejb/WizzardBean")
	private Wizzard wizzardEJB;
	
	public enum WIZZARD_STATE {
		BEGIN, CALENDAR, FORM, END;

		public Boolean getIsBegin() {
			return this.equals(BEGIN);
		}

		public Boolean getIsCalendar() {
			return this.equals(CALENDAR);
		}
	
		public Boolean getIsForm() {
			return this.equals(FORM);
		}

		public Boolean getIsEnd() {
			return this.equals(END);
		}
		
		public WIZZARD_STATE next() {
		
			if (this.ordinal() < WIZZARD_STATE.values().length-1) {
				return WIZZARD_STATE.values()[this.ordinal()+1];
				
			} else { 
				return this;
			}
		}

		public WIZZARD_STATE back() {
			
			if (this.ordinal() > 0) {
				return WIZZARD_STATE.values()[this.ordinal()-1];
				
			} else { 
				return this;
			}
		}
		
	};
	
	@PostConstruct
	private void init() {
		
		state = WIZZARD_STATE.BEGIN;
		
		form = new FormDesign("wizzard.form", getI18N());

		calendar = new Calendar(getI18N(), this);

		recurso = buildDefaultAgenda();
	}

	
	public UIComponent getContentUI() {
		return contentUI;
	}

	public void setContentUI(UIComponent contentUI) {
		this.contentUI = contentUI;
	}


	public WIZZARD_STATE getState() {
		return state;
	}

	public int getProgress() {
		switch (state) {
			case BEGIN:		return 0;
			case CALENDAR:	return 30;
			case FORM:		return 70;
			case END:		return 100;
			default: return 0;
		}
	}
	
	public Boolean inProgress(){
		//TODO implementar el reseto o pregunta del wizzard
		return false; //return (!state.equals(WIZZARD_STATE.BEGIN) && !state.equals(WIZZARD_STATE.END));
	}
	
	public String getStepTitle() {
		return getI18N().getText("wizzard."+state.toString().toLowerCase()+".title");
	}
	public void next(ActionEvent event) {

		state = state.next();
	}
	public void back(ActionEvent event) {

		state = state.back();
	}
	public void newWizzard(ActionEvent event) {

		init();
	}
	public FormDesign getForm() {
		return form;
	}
	public Calendar getCalendar() {
		return calendar;
	}
	
	public AgendaSelectionMBean getAgendaSelection() {
		return agendaSelection;
	}

	public void setAgendaSelection(AgendaSelectionMBean agendaSelection) {
		this.agendaSelection = agendaSelection;
	}


	//Con toda la información del wizzard crea una agenda con toda su metadata
	public void createNewAgenda(ActionEvent event) {

		try {
		
			Agenda a;
			//Si hay una agenda (ex recurso) seleccionada, es porque también hay un Grupo(ex agenda) selecionado, 
			//creo la agenda (ex recurso) en dicho grupo, de lo contrario creo la agenda en el grupo Default
			if (agendaSelection.getSelected() != null) {
				a = agendaSelection.getSelected().getAgenda();
			}
			else {
				a = new Agenda();
				a.setTextoAgenda(new TextoAgenda());
				a.getTextoAgenda().setAgenda(a);
				a.getTextoAgenda().setTextoSelecRecurso(getI18N().getText("entity.agenda.textos.etiqueta_recurso.default"));
				a.getTextoAgenda().setTextoPaso1(getI18N().getText("entity.agenda.textos.paso1.default"));		
				a.getTextoAgenda().setTextoPaso2(getI18N().getText("entity.agenda.textos.paso2.default"));		
				a.getTextoAgenda().setTextoPaso3(getI18N().getText("entity.agenda.textos.paso3.default"));		
				a.setNombre(GROUP_DEFAULT_NAME);
				a.setDescripcion(GROUP_DEFAULT_NAME);
			}
			
			recurso.setAgenda(a);
		
			
			//Cargo los datos del recurso ingresados en el wizzard
			if (calendar.getServiceName()!=null && !calendar.getServiceName().isEmpty()) {
				recurso.setDescripcion(calendar.getServiceName());
			}
			recurso.setAgrupacionDatos(form.getSectionList());
			
			
			//Genero el modelo de disponibilidad que pasare celda por celda para cada hora para cada dia del periodo inidicado en el wizzard
			recurso.getDisponibilidades().clear();
			
			java.util.Calendar calDiaInicio = java.util.Calendar.getInstance();
			calDiaInicio.setTime(calendar.getPeriodFrom());
			
			while (calDiaInicio.getTime().compareTo(calendar.getPeriodTo()) <= 0) {
									
				java.util.Calendar calHoraInicio = java.util.Calendar.getInstance();
				calHoraInicio.setTime(calendar.getOpeningFrom());
				
				calDiaInicio.set(java.util.Calendar.HOUR_OF_DAY, calHoraInicio.get(java.util.Calendar.HOUR_OF_DAY));
				calDiaInicio.set(java.util.Calendar.MINUTE, calHoraInicio.get(java.util.Calendar.MINUTE));
				
				for (List<Object> disponibilidades : calendar.getHoursPattern()) {
					
					int cupos = (Integer)disponibilidades.get(getI18N().getWeekdayIndex(calDiaInicio.get(java.util.Calendar.DAY_OF_WEEK)));
					
					if (cupos > 0){
					
						Disponibilidad disponibilidad = new Disponibilidad();
						
						disponibilidad.setFecha(Utiles.time2InicioDelDia(calDiaInicio.getTime()));
						disponibilidad.setHoraInicio(calDiaInicio.getTime());
						calDiaInicio.add(java.util.Calendar.MINUTE, calendar.getAverageServiceTime());
						disponibilidad.setHoraFin(calDiaInicio.getTime());
						
						disponibilidad.setCupo(Integer.valueOf(cupos));
						
						recurso.getDisponibilidades().add(disponibilidad);
					}
				} 
					
				calDiaInicio.add(java.util.Calendar.DAY_OF_MONTH, 1);

			}
			
			Recurso recursoCreado = wizzardEJB.crearAgenda(recurso);
			
			agendaSelection.refreshAgendasDataModel();
			
			recurso.setNombre(recursoCreado.getNombre());
			recurso.getAgenda().setNombre(recursoCreado.getAgenda().getNombre());
			recurso.getAgenda().setDescripcion(recursoCreado.getAgenda().getDescripcion());
			
			state = state.next();
		
		} catch (Exception ex) {
			addErrorMessage(ex, contentUI.getClientId());
		}
	}

	public String gotToBookingPage() {

		FacesContext ctx = FacesContext.getCurrentInstance();
		try {
			ctx.getExternalContext().redirect(getBooking4AgendaURL(recurso));
		} catch (IOException e) {
			addErrorMessage(e, contentUI.getClientId());
			
			//TODO manejar logger
			e.printStackTrace();
		}

		return null;
	}

	public void addErrorMessage (String mensaje) {
		addErrorMessage(mensaje, contentUI.getClientId());
	}
	
	private Recurso buildDefaultAgenda() {
		
		Recurso r = new Recurso();
		
		//Valores por defecto
		r.setCantDiasAGenerar(java.util.Calendar.getInstance().getActualMaximum(java.util.Calendar.DAY_OF_YEAR)); //Se puede generar turnos hasta un año en el futuro
		r.setDiasInicioVentanaInternet(0); //Se puede reservar para el día
		r.setDiasInicioVentanaIntranet(0);
		r.setDiasVentanaInternet(60); //La ventana de turnos a mostrar en internet e intranet es la misma, 2 meses
		r.setDiasVentanaIntranet(60);
		r.setFechaInicio(new Date()); //La agenda esta viva a partir de ahora
		r.setFechaInicioDisp(new Date()); //Se pueden ver las disponibilidades generadas a partir de ahora
		r.setLargoListaEspera(null); //Muestro todas las reservas del dia en la lista de espera
		r.setMostrarNumeroEnLlamador(true); //Se muestra el numero de la reserva en el llamador
		r.setMostrarNumeroEnTicket(true);  //Se muestra el numero de la reserva en el ticket
		r.setReservaMultiple(false);
		r.setSabadoEsHabil(true); //Sabado no es dia habil
		r.setSerie(null); //En principio no usamos la serie
		r.setTextoRecurso(new TextoRecurso());
		r.getTextoRecurso().setRecurso(r);
		r.getTextoRecurso().setTituloCiudadanoEnLlamador(getI18N().getText("entity.recurso.textos.llamador.columna_datos.default"));
		r.getTextoRecurso().setTituloPuestoEnLlamador(getI18N().getText("entity.recurso.textos.llamador.columna_puesto"));
		r.setUsarLlamador(true); //Creo que no se usa! habrá faltado algún merge de la IMM o BPS?
		r.setVentanaCuposMinimos(0);
		r.setVisibleInternet(true);
		
		return r;
	}

}
