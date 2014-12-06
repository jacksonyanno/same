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

package com.sagant.same.i18n;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import uy.gub.imm.sae.common.SAEProfile;

public class CurrentLocaleMBean {

	public static final String MBEAN_NAME = "i18n";
	
	private Locale locale;
	private List<SelectItem> languages;

	
	@PostConstruct
	public void init() {
		
			locale = getDefaultLocale();
			
			languages = new ArrayList<SelectItem>();
			languages.add(buildLeng("en", "English"));
			languages.add(buildLeng("pt", "Portugues"));
			languages.add(buildLeng("es", "Español"));
			
	}
	private SelectItem buildLeng(String lang, String desc) {
		SelectItem item = new SelectItem();
		item.setLabel(desc);
		item.setValue(lang);
		return item;
	}
	
	
	public Locale getLocale() {
		return locale;
	}

	public String getLanguage() {
		return locale.getLanguage();
	}

	public void setLanguage(String language) {
		
		this.locale = getNewLocale(language);
	}

	public List<SelectItem> getLanguages() {
		return languages;
	}
	
	public String getText(String key) {
		
		ResourceBundle bundle = ResourceBundle.getBundle(SAEProfile.getInstance().getProperties().getProperty("com.sagant.same.i18n.resourcebundle.text.class"), getLocale());
		
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return "???"+key+"???";
		}
	}

	
	/*
	 * Devuelve una lista con los nombres de los dias de la semana segun el Locale actual
	 * El orden de esta lista tambien depende del Locale actual
	 */
	public List<String> getWeekdays() {

		String[] aDias = DateFormatSymbols.getInstance(getLocale()).getWeekdays();

		List<String> lDias = new ArrayList<>(7);
		lDias.add(aDias[Calendar.MONDAY]);
		lDias.add(aDias[Calendar.TUESDAY]);
		lDias.add(aDias[Calendar.WEDNESDAY]);
		lDias.add(aDias[Calendar.THURSDAY]);
		lDias.add(aDias[Calendar.FRIDAY]);
		lDias.add(aDias[Calendar.SATURDAY]);
		lDias.add(aDias[Calendar.SUNDAY]);

		return lDias;
	}

	/*
	 * Devuelve el ordinal (orden en la semana) correspondiente al dia pasado por parámetro según el Local actual
	 * Según el Local puede variar el inicio de la semana, en US la semana comienza en Domingo, mientras que en ES comienza en Lunes.
	 */
	public int getWeekdayIndex(int day) {

		switch (day) {
			case Calendar.MONDAY: 	return 1;
			case Calendar.TUESDAY: 	return 2;
			case Calendar.WEDNESDAY:return 3;
			case Calendar.THURSDAY: return 4;
			case Calendar.FRIDAY: 	return 5;
			case Calendar.SATURDAY: return 6;
			case Calendar.SUNDAY:	return 7;
			default:return 0;
		}
		//int firstDay = Calendar.getInstance(getLocale()).getFirstDayOfWeek();
		//return ((day - firstDay + 1) == 0 ? 7 : (day - firstDay + 1 + 7)%7);
	}
	

	
	protected Locale getNewLocale(String language) {
		return new Locale(language);
	}

	protected Locale getDefaultLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();	
	}

	
}
