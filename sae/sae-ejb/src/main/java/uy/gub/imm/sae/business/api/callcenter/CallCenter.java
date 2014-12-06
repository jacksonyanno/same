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

package uy.gub.imm.sae.business.api.callcenter;

import java.util.Date;
import java.util.Map;

import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.exception.ValidacionException;

/**
 * Servicios brindados al sistema de CallCenter para el acceso al sistema de Agenda Electronica SAE.
 * 
 * Para poder invocar estos servicios se debe contar con el rol RA_AE_FCALL_CENTER/RA_AE_FATENCION y los roles particulares
 * sobre cada agenda a la que se quiera acceder
 * 
 * @author im2716295
 *
 */
public interface CallCenter {

	/**
	 * Ejecuta todas las validaciones definidas para la agenda/recurso y datos indicados: 
	 * campos obligatorios, formatos, validaciones específicas, clave única (reserva previa) 
	 * y warnings.
	 * En caso de que no se pasen todos los datos necesarios para una reserva de un 
	 * determinado recurso de una agenda, se corren las validaciones que aplican solo a 
	 * los datos indicados.  
	 * Para más información sobre los valores de retorno de los códigos de error de las
	 * validaciones referise a la documentación de cada servicio de validacion o preguntarle al
	 * administrador del sistema SAE. </br></br>
	 * <b>MODO DE USO:</b> </br>
	 * Este metodo soporta que se pasen algunos o todos los datos correspondientes 
	 * a una reserva, y en funcion de ellos valida lo que corresponde. La idea es soportar que 
	 * a medida que el usuario vaya ingresando datos de la reserva, los mismos sean validados 
	 * de forma acumulativa. Para esto se deben ir acumulando los datos ingresados por el usuario
	 * e invocar a este servicio de validacion cada vez con el mapa de los datos acumulados, de forma
	 * que se puedan correr validaciones que aplican sobre varios datos (y no solo sobre el o los datos nuevos) 
	 * 
	 * @param nombreAgenda
	 * @param idRecurso
	 * @param datos: Contiene los valores ingresados por el usuario, 
	 * la clave se debe corresponder con el nombre predefinido de algún campo para el recurso indicado.
	 * 
	 * @return Retorna el resultado de la validación, según la siguiente semántica: 
	 * 	Si la validación es exitosa: getOk retorna true y getCodigoError retorna null.
	 *  Si la validación falla: getOk retorna false, getCodigoError retorna uno de 
	 *                          los códigos prefijados en cada caso(Si el error se corresponde 
	 *                          a la falta de algún dato obligatorio y/o error de formato se retorna 
	 *                          el código genérico: 0); y getNombreCampos retorna una lista de 
	 *                          con el nombre de los campos que no pasaron la validación.
	 *  Adicionalemente, si para los datos indicados existe una reserva previa, 
	 *  la misma será devuelta en getReservaPrevia.
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 */
	public ResultadoValidacion validarDatos(String nombreAgenda, Integer idRecurso, Map<String, String> datos) throws ApplicationException, BusinessException;
	
	/**
	 * Retorna una reserva marcada como pendiente para el primer turno con disponibilidad a partir de la fecha y hora indicadas.
	 * En caso de no haber disponibilidades a partir de la fecha indicada se retorna NULL como resultado. 
	 * 
	 * @param nombreAgenda
	 * @param idRecurso
	 * @param fechaHoraDesde: fecha y hora a partir de la cual buscar el siguiente turno disponible.
	 * @return La reserva marcada como pendiente o null si no hay disponibilidad.
	 * @throws ApplicationException 
	 * @throws BusinessException 
	 */
	public ReservaDTO marcarReserva(String nombreAgenda, Integer idRecurso, Date fechaHoraDesde) throws ApplicationException, BusinessException;
	
	
	/**
	 * Desmarca una reserva previamente marcada, es decir borra una reserva que esta en estado pendiente.
	 * 
	 * @param idReserva: Id de la reserva obtenida previamente por marcarReserva(...)
	 * @throws BusinessException 
	 */
	public void desmarcarReserva(Integer idReserva) throws BusinessException;
	
	
	/**
	 * Cancela una reserva existente (confirmada)
	 * @param idReservaPrevia: Id de la reserva obtenida previamente por validarDatos(...)
	 * @throws BusinessException, ApplicationException 
	 */
	public void cancelarReserva(Integer idReservaPrevia) throws BusinessException, ApplicationException;
	
	/**
	 * Confirma la reserva asociando los datos de la misma.
	 * Nota: Se espera que los datos hayan sido validados, de todas formas son validados nuevamente
	 * y si se da algún error de validación dará una excepción de runtime.
	 * 
	 * @param idReserva: Id de la reserva obtenida previamente por marcarReserva(...)
	 * @param datos: Datos ingresados por el usuario, previamente validados por validarDatos(...)
	 * @throws BusinessException 
	 * @throws ApplicationException 
	 * @throws ValidacionException 
	 * @throws AccesoMultipleException 
	 * @throws UserException 
	 */
	public void confirmarReserva(Integer idReserva, Map<String, String> datos) throws BusinessException, ApplicationException, ValidacionException, AccesoMultipleException, UserException;
	
	
}
