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

/**
 * 
 */
package uy.gub.imm.sae.web.mbean.reserva;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import uy.gub.imm.sae.web.common.BaseMBean;

import com.google.code.kaptcha.Constants;

/**
 * @author alvaro
 *
 */
public class KaptchaMBean extends BaseMBean {

	private String texto;

	public KaptchaMBean() {
		
	}
	
	public String getTexto() {    
		return texto;  
	}
	public void setTexto(String texto) {
		this.texto = texto;  
	}
	
	public void validarKaptcha (FacesContext context, UIComponent toValidate, Object value) {
	      	
		ExternalContext ext = context.getExternalContext();
		Map<String, Object> session = ext.getSessionMap();
		String kaptchaExpected = (String) session.get(Constants.KAPTCHA_SESSION_KEY);
		if (!value.toString().equalsIgnoreCase(kaptchaExpected)) {
			((UIInput) toValidate).setValid(false);
		    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
		    		getI18N().getText("captcha.required.message"),null);
		    context.addMessage(toValidate.getClientId(context),message);
		}
	} 
	
}
