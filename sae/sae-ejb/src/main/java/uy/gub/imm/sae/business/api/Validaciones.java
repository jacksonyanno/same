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

package uy.gub.imm.sae.business.api;

import java.util.List;

import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ParametroValidacion;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Validacion;
import uy.gub.imm.sae.entity.ValidacionPorDato;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;

public interface Validaciones {
	
	public Validacion crearValidacion(Validacion v)throws UserException, BusinessException;
	public Validacion modificarValidacion(Validacion v)throws UserException, BusinessException;
	public void eliminarValidacion(Validacion v)throws UserException, BusinessException;
	public List<Validacion> consultarValidaciones()throws ApplicationException;
	public Boolean existeValidacionPorNombre(String nombreValidacion) throws ApplicationException;
	public List<ParametroValidacion> consultarParametrosDeLaValidacion(Validacion v) throws ApplicationException;
	
	//Metodos para manejar la asociacion de validaciones a recursos.
	public List<ValidacionPorRecurso> obtenerValidacionesDelRecurso(Recurso recurso) throws ApplicationException;
	public List<ValidacionPorDato> obtenerAsociacionesValidacionPorDato(ValidacionPorRecurso vr) throws ApplicationException;
	public ValidacionPorDato asociarValidacionPorDato(DatoASolicitar d, ValidacionPorRecurso vr, ValidacionPorDato vd) throws UserException, ApplicationException;
	public void desasociarValidacionPorDato(ValidacionPorDato vd) throws UserException;
	public ValidacionPorRecurso modificarValidacionPorRecurso(ValidacionPorRecurso vr) throws UserException, BusinessException;
	public ValidacionPorRecurso crearValidacionPorRecurso(ValidacionPorRecurso vr) throws BusinessException, UserException;
	public void eliminarValidacionPorRecurso(ValidacionPorRecurso vr) throws BusinessException;
	
}
