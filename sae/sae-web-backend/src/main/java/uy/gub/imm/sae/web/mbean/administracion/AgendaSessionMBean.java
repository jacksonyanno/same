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

package uy.gub.imm.sae.web.mbean.administracion;

import java.util.List;

import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;



public class AgendaSessionMBean extends SessionCleanerMBean implements RemovableFromSession {
	

	public static final String MSG_ID = "pantalla";
	
	private int pagAgenDel;
	private int pagAgenCons;
	private int pagAgenUpd;

	private byte[] agendaLogo = null;
	
	public int getPagAgenDel() {
		return pagAgenDel;
	}
	public void setPagAgenDel(int pagAgenDel) {
		this.pagAgenDel = pagAgenDel;
	}
	public int getPagAgenCons() {
		return pagAgenCons;
	}
	public void setPagAgenCons(int pagAgenCons) {
		this.pagAgenCons = pagAgenCons;
	}
	public int getPagAgenUpd() {
		return pagAgenUpd;
	}
	public void setPagAgenUpd(int pagAgenUpd) {
		this.pagAgenUpd = pagAgenUpd;
	}
	public byte[] getAgendaLogo() {
		return agendaLogo;
	}
	public void setAgendaLogo(byte[] agendaLogo) {
		this.agendaLogo = agendaLogo;
	}
	
	
	
}


