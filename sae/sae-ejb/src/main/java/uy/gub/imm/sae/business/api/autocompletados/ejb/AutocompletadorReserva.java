package uy.gub.imm.sae.business.api.autocompletados.ejb;

import java.util.Map;

import uy.gub.imm.sae.business.api.autocompletados.ejb.exception.InvalidParametersException;
import uy.gub.imm.sae.business.api.autocompletados.ejb.exception.UnexpectedAutocompletadoException;

public interface AutocompletadorReserva {
	
	public ResultadoAutocompletado autocompletarDatosReserva(String nombreAutocompletado, Map<String, Object> params) throws UnexpectedAutocompletadoException, InvalidParametersException;

}
