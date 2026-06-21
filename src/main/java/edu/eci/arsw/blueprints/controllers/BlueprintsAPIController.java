package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dto.ApiResult;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @Operation(summary = "Obtener todos los blueprints")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Blueprints obtenidos exitosamente")})
    @GetMapping
    public ResponseEntity<ApiResult<Set<Blueprint>>> getAll() {
        Set<Blueprint> all = services.getAllBlueprints();
        return ResponseEntity.ok(new ApiResult<>(HttpStatus.OK.value(), "Blueprints retrieved", all));
    }

    // GET /api/v1/blueprints/{author}
    @Operation(summary = "Obtener todos los blueprints de un autor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blueprints encontrados"),
            @ApiResponse(responseCode = "404", description = "El autor no tiene blueprints registrados")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResult<Set<Blueprint>>> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> bps = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(new ApiResult<>(HttpStatus.OK.value(),"Blueprints retrieved",bps));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResult<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Obtener un blueprint específico por autor y nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blueprint encontrado"),
            @ApiResponse(responseCode = "404", description = "El blueprint no existe")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResult<Blueprint>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(new ApiResult<>(HttpStatus.OK.value(), "Blueprint found", bp));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResult<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    // POST /api/v1/blueprints
    @Operation(summary = "Crear un nuevo blueprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blueprint creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (author o name vacíos)"),
            @ApiResponse(responseCode = "409", description = "Ya existe un blueprint con ese author y name")
    })
    @PostMapping
    public ResponseEntity<ApiResult<Void>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResult<>(HttpStatus.CREATED.value(), "Blueprint created", null));
        } catch (BlueprintPersistenceException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResult<>(HttpStatus.CONFLICT.value(), e.getMessage(), null));
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un blueprint existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Punto agregado exitosamente"),
            @ApiResponse(responseCode = "404", description = "El blueprint no existe")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResult<Void>> addPoint(@PathVariable String author, @PathVariable String bpname,
                                                    @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResult<>(HttpStatus.ACCEPTED.value(), "Point added", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResult<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
