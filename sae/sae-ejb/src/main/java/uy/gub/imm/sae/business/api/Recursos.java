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
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.ValorPosible;

public interface Recursos {
	public Recurso crearRecurso(Agenda a, Recurso r) throws UserException, ApplicationException, BusinessException;
	public void modificarRecurso(Recurso r) throws UserException, BusinessException, ApplicationException;
	public void eliminarRecurso(Recurso r) throws UserException, ApplicationException;
	public Recurso consultarRecurso(Recurso r) throws UserException;	
    //Métodos asociados a DatoDelRecurso
	public DatoDelRecurso agregarDatoDelRecurso(Recurso r, DatoDelRecurso d) throws UserException;
	public void modificarDatoDelRecurso(DatoDelRecurso d) throws UserException;
	public void eliminarDatoDelRecurso(DatoDelRecurso d) throws UserException;
	public List<DatoDelRecurso> consultarDatosDelRecurso(Recurso r) throws ApplicationException, BusinessException;
	//Métodos asociados a AgrupacionDato
	public AgrupacionDato agregarAgrupacionDato(Recurso r, AgrupacionDato a) throws UserException, ApplicationException;
	public void modificarAgrupacionDato(AgrupacionDato a) throws UserException;
	public void eliminarAgrupacionDato(AgrupacionDato a) throws UserException, ApplicationException;
	public List<AgrupacionDato> consultarAgrupacionesDatos(Recurso r) throws ApplicationException;
	public List<AgrupacionDato> consultarDefinicionDeCampos(Recurso recurso) throws BusinessException;
	public List<AgrupacionDato> consultarDefCamposTodos(Recurso recurso) throws BusinessException;
	//Métodos asociados a DatoASolicitar
	public DatoASolicitar agregarDatoASolicitar(Recurso r,AgrupacionDato a, DatoASolicitar d) throws UserException, ApplicationException, BusinessException;
	public void modificarDatoASolicitar(DatoASolicitar d) throws UserException, ApplicationException;
	public void eliminarDatoASolicitar(DatoASolicitar d) throws UserException;
	public List<DatoASolicitar> consultarDatosSolicitar(Recurso r) throws ApplicationException;
	public Boolean mostrarDatosASolicitarEnLlamador(Recurso r) throws BusinessException;
	
	//Métodos asociados a ValorPosible
	public ValorPosible agregarValorPosible(DatoASolicitar d, ValorPosible v) throws UserException, ApplicationException;
	public void modificarValorPosible(ValorPosible v) throws UserException, ApplicationException;
	public void eliminarValorPosible(ValorPosible v) throws UserException;
	public List<ValorPosible> consultarValoresPosibles(DatoASolicitar d) throws ApplicationException;
	
	public void copiarRecurso(Recurso r, String nombre, String descripcion) throws BusinessException, ApplicationException, UserException;
	
	//Métodos asociados a ServicioPorRecurso
	List<ServicioPorRecurso> consultarServicioAutocompletar (Recurso r) throws BusinessException;
	
	public void copiarRecursoAgenda(Agenda a, Recurso r, String nombre, String descripcion) throws BusinessException, ApplicationException, UserException;
		
}
