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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ValorPosible;
import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;

public class DatoASSessionMBean extends SessionCleanerMBean implements RemovableFromSession {
	
	public static final String MSG_ID = "pantalla";
	
	//Booleana para saber si se despliega la tabla de Valores Posibles para el dato
	//private Boolean mostrarValor = false;

	//Booleana para saber si se despliega la tabla para agregar Valores Posibles
	private Boolean mostrarAgregarValor = false;
	
	//Booleana para saber si se despliega la tabla para modificar un valor posible
	private Boolean mostrarModifValor = false;
	
	//Booleana para saber si se despliega la tabla de Valores Posibles en la pantalla
	//de consulta de dato a solicitar
	private Boolean mostrarConsultarValor = false;
	

	private int pagValorRUpd;
	private int pagValorRCons;
	private int pagDatoASDel;
	private int pagDatoASCons = 1;
	private int pagDatoASUpd;
	private int pagDatoAgrupUpd;
	private DatoASolicitar datoSeleccionado;
	private List<DatoASolicitar> datosASolicitar;
	private AgrupacionDato agrupacionSeleccionada;
	private List<AgrupacionDato> agrupaciones;
	private ValorPosible valorDelDatoSeleccionado;

	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private SessionMBean sessionMBean; 
	
	
	@PostConstruct
	public void init() {
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
	}
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}
	public int getPagDatoASDel() {
		return pagDatoASDel;
	}
	public void setPagDatoASDel(int pagDatoASDel) {
		this.pagDatoASDel = pagDatoASDel;
	}
	public int getPagDatoASCons() {
		return pagDatoASCons;
	}
	public void setPagDatoASCons(int pagDatoASCons) {
		this.pagDatoASCons = pagDatoASCons;
	}
	public int getPagDatoASUpd() {
		return pagDatoASUpd;
	}
	public void setPagDatoASUpd(int pagDatoASUpd) {
		this.pagDatoASUpd = pagDatoASUpd;
	}
	
	
	public int getPagDatoAgrupUpd() {
		return pagDatoAgrupUpd;
	}
	public void setPagDatoAgrupUpd(int pagDatoAgrupUpd) {
		this.pagDatoAgrupUpd = pagDatoAgrupUpd;
	}
	public DatoASolicitar getDatoSeleccionado() {
		return datoSeleccionado;
	}
	public void setDatoSeleccionado(DatoASolicitar datoSeleccionado) {
		this.datoSeleccionado = datoSeleccionado;
	}
	
	public ValorPosible getValorDelDatoSeleccionado() {
		return valorDelDatoSeleccionado;
	}
	public void setValorDelDatoSeleccionado(ValorPosible valorDelDatoSeleccionado) {
		this.valorDelDatoSeleccionado = valorDelDatoSeleccionado;
	}

	public Boolean getMostrarModifValor() {
		return mostrarModifValor;
	}
	public void setMostrarModifValor(Boolean mostrarModifValor) {
		this.mostrarModifValor = mostrarModifValor;
	}
	
	public Boolean getMostrarConsultarValor() {
		return mostrarConsultarValor;
	}
	public void setMostrarConsultarValor(Boolean mostrarConsultarValor) {
		this.mostrarConsultarValor = mostrarConsultarValor;
	}
	
	public List<ValorPosible> getValoresDelDato() {
		
		if (datoSeleccionado != null ) {
			return new ArrayList<ValorPosible>(datoSeleccionado.getValoresPosibles());
		}
		else {
			return null;
		}
	}
	
	public int getPagValorRUpd() {
		return pagValorRUpd;
	}
	public void setPagValorRUpd(int pagValorRUpd) {
		this.pagValorRUpd = pagValorRUpd;
	}

	public int getPagValorRCons() {
		return pagValorRCons;
	}
	public void setPagValorRCons(int pagValorRCons) {
		this.pagValorRCons = pagValorRCons;
	}
	public AgrupacionDato getAgrupacionSeleccionada() {
		return agrupacionSeleccionada;
	}
	public void setAgrupacionSeleccionada(AgrupacionDato agrupacionSeleccionada) {
		this.agrupacionSeleccionada = agrupacionSeleccionada;
	}

	

	
	
	public List<AgrupacionDato> getAgrupaciones() {
	
		if (sessionMBean.getRecursoMarcado() != null &&
			agrupaciones == null) {
			try {
				agrupaciones = recursosEJB.consultarAgrupacionesDatos(sessionMBean.getRecursoMarcado());
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return agrupaciones;
	}

	public void clearAgrupaciones() {
		this.agrupaciones = null;
	}

	

	
	public Boolean getMostrarValor() {
		
		if (getDatoSeleccionado() != null &&
			getDatoSeleccionado().getTipo() == Tipo.LIST) {
			
			return true;
		}
		else {
			return false;
		}
	}
/*	
	public void setMostrarValor(Boolean mostrarValor) {
		this.mostrarValor = mostrarValor;
	}
*/
	public Boolean getMostrarAgregarValor() {
		return mostrarAgregarValor;
	}
	public void setMostrarAgregarValor(Boolean mostrarAgregarValor) {
		this.mostrarAgregarValor = mostrarAgregarValor;
	}

	public List<DatoASolicitar> getDatosASolicitar() {

		if (datosASolicitar == null) {
			try {
				List<DatoASolicitar> entidades;
				entidades = recursosEJB.consultarDatosSolicitar(sessionMBean.getRecursoMarcado());
				datosASolicitar = new ArrayList<DatoASolicitar>(entidades);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}

		return datosASolicitar;
	}

	public void clearDatosASolicitar() {
		this.datosASolicitar = null;
	}	


}


