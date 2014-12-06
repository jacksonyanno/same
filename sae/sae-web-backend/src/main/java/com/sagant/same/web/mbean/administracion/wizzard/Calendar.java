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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import uy.gub.imm.sae.common.Utiles;

import com.sagant.same.i18n.CurrentLocaleMBean;

public class Calendar {

	private CurrentLocaleMBean i18n;
	private WizzardMBean wizz;
	
	private String serviceName;
	private Date openingFrom;
	private Date openingTo;
	private int averageServiceTime = 30;
	private int desksQuantity = 1;
	private List<List<Object>> hoursPattern;
	private Date periodFrom;
	private Date periodTo;
	private List<SelectItem> opcionesMeses= new ArrayList<SelectItem>();
	private Integer valueMes;
	
	public Calendar(CurrentLocaleMBean i18n, WizzardMBean wizz){
		
		this.i18n = i18n;
		this.wizz = wizz;
		
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.set(java.util.Calendar.HOUR_OF_DAY, 10);
		cal.set(java.util.Calendar.MINUTE, 0);
		cal.set(java.util.Calendar.SECOND, 0);

		openingFrom = cal.getTime();

		cal.set(java.util.Calendar.HOUR_OF_DAY, 17);
		openingTo = cal.getTime();
		
		periodFrom = Utiles.time2InicioDelDia(cal.getTime());
		
		cal.add(java.util.Calendar.MONTH, 1);
		periodTo = Utiles.time2InicioDelDia(cal.getTime());
		
		valueMes = 1;
		cargarOpcionesMeses();
		
		setDefaultHoursPattern();
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public Date getOpeningFrom() {
		return openingFrom;
	}
	public void setOpeningFrom(Date openingFrom) {
		this.openingFrom = openingFrom;
	}
	public Date getOpeningTo() {
		return openingTo;
	}
	public void setOpeningTo(Date openingTo) {
		this.openingTo = openingTo;
	}
	public int getAverageServiceTime() {
		return averageServiceTime;
	}
	public void setAverageServiceTime(int averageServiceTime) {
		this.averageServiceTime = averageServiceTime;
	}
	public int getDesksQuantity() {
		return desksQuantity;
	}
	public void setDesksQuantity(int desksQuantity) {
		this.desksQuantity = desksQuantity;
	}
	public Date getPeriodFrom() {
		return periodFrom;
	}
	public void setPeriodFrom(Date periodFrom) {
		this.periodFrom = periodFrom;
	}
	public Date getPeriodTo() {
		return periodTo;
	}
	public void setPeriodTo(Date periodTo) {
		this.periodTo = periodTo;
	}
	public List<SelectItem> getOpcionesMeses() {
		return opcionesMeses;
	}
	public void setOpcionesMeses(List<SelectItem> opcionesMeses) {
		this.opcionesMeses = opcionesMeses;
	}
	public Integer getValueMes() {
		return valueMes;
	}
	public void setValueMes(Integer valueMes) {
		this.valueMes = valueMes;
	}
	public Converter getTimeConverter() {
		return new Converter() {
			
			@Override
			public String getAsString(FacesContext fctx, UIComponent ui, Object value) {
				if (value != null && Date.class.isAssignableFrom(value.getClass())) {
					Date time = (Date) value;
					
					//TODO usar timezone y locale, pero ambos del tenant/grupo/agenda
					java.util.Calendar cal = java.util.Calendar.getInstance();
					cal.setTime(time);
					int hours = cal.get(java.util.Calendar.HOUR_OF_DAY);
					int minutes = cal.get(java.util.Calendar.MINUTE);
					
					return ((hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes); 
				}
				else{
					return null;
				}
			}
			
			@Override
			public Object getAsObject(FacesContext fctx, UIComponent ui, String value) {
				
				int hours;
				int minutes = 0;
				
				if (value != null && !value.isEmpty()) {
					
					Matcher m = Pattern.compile("^\\D*(\\d{1,2})(\\D+(\\d{1,2})|.*)").matcher(value);
					
					if (m.matches()) {
						
						hours = Integer.valueOf(m.group(1));

						if (m.group(3) != null) {
							minutes = Integer.valueOf(m.group(3));
						}
						else {
							minutes = 0;
						}
						
						if (hours > 23 || minutes > 59) {
							return null;
						}
						
						//TODO usar timezone y locale, pero ambos del tenant/grupo/agenda 
						java.util.Calendar time = java.util.Calendar.getInstance();
						
						if (time.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY){
							time.add(java.util.Calendar.DATE, 1);
						}
						
						time.set(java.util.Calendar.HOUR_OF_DAY, hours);
						time.set(java.util.Calendar.MINUTE, minutes);
						return time.getTime();
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			}
		};
	}
	
	public List<List<Object>> getHoursPattern() {
		return hoursPattern;
	}
	
	public List<String> getWeekdays() {
		
		return i18n.getWeekdays();
	}
		
	public void changeCupos(ActionEvent event) {
		
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
		int rowFrom = Integer.valueOf(params.get("rowFrom"));
		int rowTo   = Integer.valueOf(params.get("rowTo"));
		int colFrom = Integer.valueOf(params.get("colFrom"));
		int colTo   = Integer.valueOf(params.get("colTo"));
		int cupos   = Integer.valueOf(params.get("cupos"));

		for(int row = rowFrom; row <= rowTo ; row++) {
			List<Object> dias = hoursPattern.get(row);
			
			for(int col = colFrom+1; col <= colTo+1; col++) {
				dias.set(col, cupos);
			}
		}
		
		
	}
	
	public void refrescarSeleccionPeriodo(ActionEvent event) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		periodFrom = cal.getTime();
		
		if (valueMes == 0){
			cal.add(java.util.Calendar.MONTH, 1);
		}else{
			cal.add(java.util.Calendar.MONTH, valueMes);			
		}
		
		periodTo = cal.getTime();
	}

	public void validarInicioPeriodo(ActionEvent event) {
		java.util.Calendar calHoy = java.util.Calendar.getInstance();

		if (Utiles.time2InicioDelDia(periodFrom).before(Utiles.time2InicioDelDia(calHoy.getTime()))){
			periodFrom = calHoy.getTime();
			wizz.addErrorMessage(i18n.getText("wizzard.calendar.periodFrom_before_now"));
		}else if (Utiles.time2InicioDelDia(periodFrom).after(Utiles.time2InicioDelDia(periodTo))){
			periodFrom = periodTo;
			wizz.addErrorMessage(i18n.getText("wizzard.calendar.periodFrom_after_periodTo"));
		}
		
	}
	
	public void validarFinPeriodo(ActionEvent event) {
		
		if (Utiles.time2InicioDelDia(periodTo).before(Utiles.time2InicioDelDia(periodFrom))){
			periodTo = periodFrom;
			wizz.addErrorMessage(i18n.getText("wizzard.calendar.periodTo_before_periodFrom"));
		}
	}
	
	public void refrescarTablaDisponibilidades(ActionEvent event) {
		
		setDefaultHoursPattern();
	}
	
	private void setDefaultHoursPattern() {
		
		//Esta lista de horas representa la primer columna de la matriz de horarios semanales
		//y por lo tanto determina la cantidad de filas de la matriz.
		List<Date> horas = getFranjasDeHorarios();
		
		//Matriz de horarios semanales inicializada con cada hora por fila
		hoursPattern = armarHorariosSemanalesVacios(horas);
				
		
	}
	

	//Obtiene todas las horas de inicio según los parámetros ingresados en el wizzard
	private List<Date> getFranjasDeHorarios() {
		
		List<Date> horas = new ArrayList<Date>();
		
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(openingFrom);
		
		while (cal.getTime().before(openingTo)) {
			horas.add(cal.getTime());
			cal.add(java.util.Calendar.MINUTE, averageServiceTime);
		}
		
		return horas;
	}
	
	
	//Crea la matriz de horas x diasDeLaSemana con tantas filas como horas y 7 columnas para cada dia.
	//En la primer columna incluye las horas respectivas
	private List<List<Object>> armarHorariosSemanalesVacios(List<Date> horas) {
		
		List<List<Object>> matriz = new ArrayList<List<Object>>(horas.size());
		for (Date h : horas) {
			List<Object> horarioSemanal = new ArrayList<Object>(8);
			horarioSemanal.add(h);
			for (int i = 0; i < 7; i++) {
				horarioSemanal.add(desksQuantity);
			}
			matriz.add(horarioSemanal);
		}
		return matriz;
	}
	
	private void cargarOpcionesMeses() {
		this.opcionesMeses = new ArrayList<SelectItem>();

		SelectItem s = new SelectItem();
		s.setValue(new Integer(1));
		s.setLabel("1 " + i18n.getText("wizzard.calendar.period.month_singular"));
		this.opcionesMeses.add(s);
		
		s = new SelectItem();
		s.setValue(new Integer(2));
		s.setLabel("2 " + i18n.getText("wizzard.calendar.period.month_plural"));
		this.opcionesMeses.add(s);
		
		s = new SelectItem();
		s.setValue(new Integer(3));
		s.setLabel("3 " + i18n.getText("wizzard.calendar.period.month_plural"));
		this.opcionesMeses.add(s);
		
		s = new SelectItem();
		s.setValue(0);
		s.setLabel(i18n.getText("wizzard.calendar.period.custom"));
		this.opcionesMeses.add(s);
	}


	
}
