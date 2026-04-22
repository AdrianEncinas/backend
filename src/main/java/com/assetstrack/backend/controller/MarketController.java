package com.assetstrack.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistAddRequest;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.service.MarketService;



@RestController
@RequestMapping("/api/v1/market")
@Tag(name = "Market & Watchlist", description = "Operaciones de búsqueda de acciones y gestión de listas de observación")
public class MarketController {

    private final MarketService marketService;
    private final SecurityUtils securityUtils;

    public MarketController(MarketService marketService, SecurityUtils securityUtils){
        this.marketService = marketService;
        this.securityUtils = securityUtils;
    }

    @Operation(summary = "Buscar acciones", description = "Busca acciones por símbolo o nombre de empresa")
    @ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos",
            content = @Content(schema = @Schema(implementation = TickerSearchDTO.class)))
    @GetMapping("/search")
    public List<TickerSearchDTO> getMethodName(
            @Parameter(description = "Término de búsqueda (símbolo o nombre)", example = "AAPL")
            @RequestParam String query) {
        return marketService.searchTicker(query);
    }
    
    @Operation(summary = "Obtener lista de observación", description = "Retorna todas las acciones en la lista de observación del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de observación obtenida",
            content = @Content(schema = @Schema(implementation = WatchlistDTO.class)))
    @GetMapping("/watchlist")
    public List<WatchlistDTO> getWatchlist() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return marketService.getWatchlist(userId);
    }

    @Operation(summary = "Agregar a lista de observación", description = "Añade una acción a la lista de observación del usuario")
    @ApiResponse(responseCode = "200", description = "Acción añadida a la lista",
            content = @Content(schema = @Schema(implementation = WatchlistDTO.class)))
    @PostMapping("/watchlist")
    public WatchlistDTO addToWatchlist(@RequestBody WatchlistAddRequest request) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return marketService.addToWatchlist(request, userId);
    }

    @Operation(summary = "Remover de lista de observación", description = "Elimina una acción de la lista de observación")
    @ApiResponse(responseCode = "204", description = "Acción removida de la lista", content = @Content)
    @DeleteMapping("/watchlist/{watchlistItemId}")
    public ResponseEntity<String> deleteWatchlist(
            @Parameter(description = "ID del elemento en la lista de observación")
            @PathVariable Long watchlistItemId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        marketService.deleteFromWatchlist(watchlistItemId, userId);
        return ResponseEntity.noContent().build();
    }
    
    
    


}
