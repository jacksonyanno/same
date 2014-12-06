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
package uy.gub.imm.sae.web.taglib.functions;

import java.text.MessageFormat;

import uy.gub.imm.sae.common.SAEProfile;

public class MessageFormatFunction {
	
	/** Recibe el nombre de una property de SAEProfile y formatea el string respectivo en base a los parametros. */
	public static String propertyFormat(String key, String[] params) {
		return _messageFormat(SAEProfile.getInstance().getProperties().getProperty(key), params);
	}

	/** Recibe un string y los formatea en base a los parametros. */
	public static String messageFormat1(String pattern, String p1 ) {
		return _messageFormat(pattern, p1);
	}

	/** Recibe un string y los formatea en base a los parametros. */
	public static String messageFormat2(String pattern, String p1, String p2 ) {
		return _messageFormat(pattern, p1, p2);
	}

	/** Recibe un string y los formatea en base a los parametros. */
	public static String messageFormat3(String pattern, String p1, String p2, String p3 ) {
		return _messageFormat(pattern, p1, p2, p3);
	}
	
	/** Recibe un string y los formatea en base a los parametros. */
	private static String _messageFormat(String pattern, String... params ) {
		MessageFormat mf = new MessageFormat(pattern);
		return mf.format(params);
	}

}
