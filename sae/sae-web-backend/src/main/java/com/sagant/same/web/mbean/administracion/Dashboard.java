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

package com.sagant.same.web.mbean.administracion;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import uy.gub.imm.sae.web.common.BaseMBean;

@ManagedBean
@SessionScoped
public class Dashboard extends BaseMBean implements Serializable {

	private static final long serialVersionUID = 1L;


	private enum COMPONENT { DASHBOARD, WIZARD, AGENDAS, AGENDAS_CONFIG, BOOKING, CALENDAR, CUSTOMER_SERVICE, REPORTS};
	
	private COMPONENT activeComponent = COMPONENT.DASHBOARD;
	

	
	public void show(String component) {
		activeComponent = COMPONENT.valueOf(component);
	}

	public Boolean isActive(COMPONENT component) {
		return (component == null ? false : component.equals(activeComponent));
	}
}
