package com.assetstrack.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import com.assetstrack.backend.model.dto.ManualUpdateDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.service.IPortfolioApiService;
import com.assetstrack.backend.service.PortfolioApiService;

@RestController
@RequestMapping("/api/v1/portfolio")
@Validated
@Tag(name = "Portfolio", description = "Operaciones relacionadas con el portafolio de usuario")
public class PortfolioController {

    private final IPortfolioApiService portfolioApiService;
    private final SecurityUtils securityUtils;

    public PortfolioController(PortfolioApiService portfolioApiService, SecurityUtils securityUtils) {
        this.portfolioApiService = portfolioApiService;
        this.securityUtils = securityUtils;
    }


    @Operation(summary = "Obtener dashboard del portafolio", description = "Devuelve el estado actual del portafolio del usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard obtenido correctamente",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return portfolioApiService.getPortfolioStatus(userId);
    }


    @Operation(summary = "Obtener detalles de una acción", description = "Devuelve los detalles completos de una acción específica por su ticker.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalles de la acción obtenidos",
                content = @Content(schema = @Schema(implementation = StockFullDTO.class))),
        @ApiResponse(responseCode = "404", description = "Acción no encontrada", content = @Content)
    })
    @GetMapping("/stocks/{ticker}")
    public StockFullDTO getStockDetails(
            @Parameter(description = "Ticker de la acción", example = "AAPL")
            @PathVariable String ticker) {
        return portfolioApiService.getFullStockDetails(ticker);
    }


    @Operation(summary = "Agregar posición al portafolio", description = "Agrega una nueva posición de acción al portafolio del usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Posición agregada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    @PostMapping("/positions")
    public ResponseEntity<String> addPosition(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la posición a agregar",
                required = true,
                content = @Content(schema = @Schema(implementation = StockPositionDTO.class))
            )
            @Valid @RequestBody StockPositionDTO position) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioApiService.addPosition(position, userId));
    }

    @PutMapping("/positions")
    public ResponseEntity<String> modifyPosition(@Valid @RequestBody StockPositionDTO position) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(portfolioApiService.modifyPosition(position, userId));
    }

    @DeleteMapping("/positions/{ticker}")
    public ResponseEntity<String> deletePosition(@PathVariable String ticker) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(portfolioApiService.deletePosition(ticker, userId));
    }

    @PatchMapping("/holdings/{holdingId}")
    public ResponseEntity<Void> updateHolding(@PathVariable Long holdingId, @RequestBody ManualUpdateDTO dto) {
        Long userId = securityUtils.getAuthenticatedUserId();
        portfolioApiService.syncHoldingManually(holdingId, userId, dto.totalShares(), dto.avgPrice());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/graph")
    public List<PortfolioPointDTO> getGraph(
            @RequestParam(defaultValue = "historic") String mode,
            @RequestParam(defaultValue = "1d") String period) {
        Long userId = securityUtils.getAuthenticatedUserId();
        if ("intraday".equals(mode)) {
            return portfolioApiService.getTodayIntraday(userId, period);
        }
        return portfolioApiService.getPortfolioHistory(userId);
    }
}

