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

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.web.mbean.administracion.AccionAsignacionSessionMBean;
import uy.gub.imm.sae.web.mbean.administracion.AutocompletadoAsignacionSessionMBean;
import uy.gub.imm.sae.web.mbean.administracion.ValidacionAsignacionSessionMBean;

public class DatoASolicitarConverter implements Converter {

	public Object getAsObject(FacesContext ctx, UIComponent arg1, String arg2) {
		
		
		if (arg1.getClientId(ctx).contains("Autocompletados")) {
			ELContext elContext = ctx.getELContext();
			ExpressionFactory expFactory = ctx.getApplication().getExpressionFactory();
			String el = "#{autocompletadoAsignacionSessionMBean}"; 
			ValueExpression ve = expFactory.createValueExpression(elContext, el, AutocompletadoAsignacionSessionMBean.class);
			AutocompletadoAsignacionSessionMBean sesion = (AutocompletadoAsignacionSessionMBean) ve.getValue(elContext);
			
			for (DatoASolicitar campo : sesion.getDatosASolicitarDelRecursoCopia()) {
				if (campo.getId().equals(Integer.valueOf(arg2))) {
					return campo;		
				}
			}
			
//			for (ServicioAutocompletar campo : sesion.getAutocompletados()) {
//				if (campo.getId().equals(Integer.valueOf(arg2))) {
//					return campo;		
//				}
//			}
			
		}else if (arg1.getClientId(ctx).contains("Acciones")){
			ELContext elContext = ctx.getELContext();
			ExpressionFactory expFactory = ctx.getApplication().getExpressionFactory();
			String el = "#{accionAsignacionSessionMBean}"; 
			ValueExpression ve = expFactory.createValueExpression(elContext, el, AccionAsignacionSessionMBean.class);
			AccionAsignacionSessionMBean sesion = (AccionAsignacionSessionMBean) ve.getValue(elContext);
			
			for (DatoASolicitar campo : sesion.getDatosASolicitarDelRecursoCopia()) {
				if (campo.getId().equals(Integer.valueOf(arg2))) {
					return campo;		
				}
			}
		}else {
			ELContext elContext = ctx.getELContext();
			ExpressionFactory expFactory = ctx.getApplication().getExpressionFactory();
			String el = "#{validacionAsignacionSessionMBean}"; 
			ValueExpression ve = expFactory.createValueExpression(elContext, el, ValidacionAsignacionSessionMBean.class);
			ValidacionAsignacionSessionMBean sesion = (ValidacionAsignacionSessionMBean) ve.getValue(elContext);
			
			for (DatoASolicitar campo : sesion.getDatosASolicitarDelRecursoCopia()) {
				if (campo.getId().equals(Integer.valueOf(arg2))) {
					return campo;		
				}
			}
		}
		
		return null;
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {

		DatoASolicitar campo = (DatoASolicitar) arg2;
		return campo.getId().toString();
	}
}
	
