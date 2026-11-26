package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.LogroService;
import java.util.List;

/**
 * Registro y consulta de logros (M06). El servicio exige rol DOCENTE o ADMIN
 * para registrar (RN-09) y atribuye el logro al actor (RN-14).
 */
public class LogroController {

    private final LogroService logroService;
    private final Usuario actor;

    public LogroController(Usuario actor) {
        this(new LogroService(), actor);
    }

    public LogroController(LogroService logroService, Usuario actor) {
        this.logroService = logroService;
        this.actor = actor;
    }

    public void registrar(int idEstudiante, String tipo, String descripcion, int monedas) {
        logroService.registrar(actor, idEstudiante, tipo, descripcion, monedas);
    }

    public List<DetalleLogro> listarOtorgadosPorMi() {
        return logroService.listarOtorgadosPor(actor);
    }
    
    public List<DetalleLogro> listarTodos() {
        return logroService.listarTodos(actor);
    }
    
}
