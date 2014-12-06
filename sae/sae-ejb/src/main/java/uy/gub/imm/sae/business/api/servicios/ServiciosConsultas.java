package uy.gub.imm.sae.business.api.servicios;

import java.util.List;
import java.util.Map;

import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.exception.BusinessException;

public interface ServiciosConsultas {
	// Agregada para la validación de la UEPS (umbral de re-agendas)
	public List<ReservaDTO> consultarReservasPorClave(Integer recId, Map<String, String> datosClave) throws BusinessException;

}
