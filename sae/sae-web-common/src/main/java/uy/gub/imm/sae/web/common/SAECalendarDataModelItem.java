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

import org.richfaces.model.CalendarDataModelItem;

public class SAECalendarDataModelItem implements CalendarDataModelItem {

	public SAECalendarDataModelItem() {
	}

	public Object getData() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDay() {
		//No entiendo que deberia hacer este metodo, pero si pongo algo distinto de 0
		//deja de funcionar la customizacion de las celdas de los dias.
		return 0;
	}

	public String getStyleClass() {
		return null;
	}

	public Object getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasToolTip() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnabled() {
		return false;
	}

}
