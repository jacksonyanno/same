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

package uy.gub.imm.sae.web.common;

import java.util.Date;
import java.util.List;

import org.richfaces.model.CalendarDataModel;
import org.richfaces.model.CalendarDataModelItem;

public class SAECalendarDataModel implements CalendarDataModel {

	private SAECalendarioDataSource dataSource;
	
	public SAECalendarDataModel(SAECalendarioDataSource dataSource) {

		this.dataSource = dataSource;
	}
	
	public CalendarDataModelItem[] getData(Date[] dates) {
		
		Date desde = dates[0];
		Date hasta = dates[dates.length - 1];
		
		//GregorianCalendar fecha = new GregorianCalendar(desde.getYear() + 1900, desde.getMonth(), desde.getDate()); 
		
		List<Integer> cuposXdia = null;
		cuposXdia = dataSource.obtenerCuposXDia(desde, hasta);
 		CalendarDataModelItem dataItems [] = new SAECalendarDataModelItem[dates.length];
 		
 		if (cuposXdia != null && cuposXdia.size() != dates.length) {
 			System.err.println("La lista de cupos por día no tiene la misma cantidad de elementos que lo que pide el calendario");
 			cuposXdia = null;
 		}
 		
 		for (int i = 0; i < dates.length; i++) {
 			if (cuposXdia != null) {
	 			if (cuposXdia.get(i) < 0) {
	 				dataItems[i] = new SAECalendarDataModelItem();
	 			}
	 			else {
	 				
	 				dataItems[i] = new SAECalendarDataModelItemAgendable(cuposXdia.get(i));
	 			}
 			}
 			else {
 				//Dio algun error al obtener los cuposPorDia
 				dataItems[i] = new SAECalendarDataModelItem();
 			}
 			
 		}
		return dataItems;
	}

	public Object getToolTip(Date arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
