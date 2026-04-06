package com.assetstrack.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.mapper.Mapper;
import com.assetstrack.backend.model.dto.ChartDTO;
import com.assetstrack.backend.model.dto.HoldingDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.model.entity.Holding;
import com.assetstrack.backend.model.entity.Transaction;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.repository.HoldingRepository;
import com.assetstrack.backend.repository.TransactionRepository;
import com.assetstrack.backend.repository.UserRepository;


@Service
public class PortfolioApiService implements IPortfolioApiService {

    private final WebClient webClient;

    @Autowired
    HoldingRepository holdingRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    TransactionRepository transactionRepo;

    public PortfolioApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Map<String, Object> getPortfolioStatus(Long id) {
        List<HoldingDTO> portfolioData = holdingRepo.findByUserId(id).stream().map(Mapper::toDTO).toList();
        return this.webClient.post()
                .uri("/api/v1/portfolio/status")
                .bodyValue(portfolioData) 
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @Override
    public StockFullDTO getFullStockDetails(String ticker) {
        StockFullDTO stock = this.webClient.get()
                .uri("/api/v1/stock/{ticker}/full", ticker)
                .retrieve()
                .bodyToMono(StockFullDTO.class)
                .block();
        return stock;
    }

    @Override
    @Async
    public CompletableFuture<ChartDTO> getFullChart(String ticker, String period){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/v1/stock/{ticker}/chart")
                    .queryParam("period", period)
                    .build(ticker))
                .retrieve()
                .bodyToMono(ChartDTO.class)
                .toFuture();
    }

    public String addPosition(StockPositionDTO position,Long userId){
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<HoldingDTO> portfolioData = holdingRepo.findByUserId(userId).stream().map(Mapper::toDTO).toList();
        
        for (HoldingDTO holdingDTO : portfolioData) {
            if (holdingDTO.getTicker().contains(position.ticker())) {
                return "The position has already been added.";
            }
        }
        Holding newPosition = new Holding();
        newPosition.setUser(user);
        newPosition.setTicker(position.ticker());
        newPosition.setCompanyName(position.companyName());
        newPosition.setTotalShares(position.shares());
        newPosition.setAvgPrice(position.avgPrice());
        newPosition.setId(null);
        holdingRepo.save(newPosition);
        return "Position added";
    }

    public String modifyPosition(StockPositionDTO position,Long userId){
        List<HoldingDTO> portfolioData = holdingRepo.findByUserId(userId).stream().map(Mapper::toDTO).toList();
        
        for (HoldingDTO holdingDTO : portfolioData) {
            if (holdingDTO.getTicker().contains(position.ticker())) {
                Holding modifiedPosition = holdingRepo.findById(holdingDTO.getId())
                    .orElseThrow(() -> new NotFoundException("Position not found"));
                modifiedPosition.setTotalShares(position.shares());
                modifiedPosition.setAvgPrice(position.avgPrice());
                holdingRepo.save(modifiedPosition);
                return "The position has been modified.";
            }
        }
        return "Error modifying the position";
    }

    @Override
    public String deletePosition(StockPositionDTO position, Long userId) {
        List<HoldingDTO> portfolioData = holdingRepo.findByUserId(userId).stream().map(Mapper::toDTO).toList();

        for (HoldingDTO holdingDTO : portfolioData) {
            if (holdingDTO.getTicker().contains(position.ticker())) {
                Holding deletedPosition = holdingRepo.findById(holdingDTO.getId())
                    .orElseThrow(() -> new NotFoundException("Position not found"));
                holdingRepo.delete(deletedPosition);
                return "The position has been deleted.";
            }
        }
        return "Error deleting the position";
    }

    @Transactional
    public HoldingDTO syncHoldingManually(Long holdingId, BigDecimal targetShares, BigDecimal targetAvgPrice) {
        Holding holding = holdingRepo.findById(holdingId)
            .orElseThrow(() -> new RuntimeException("Posición no encontrada"));

        BigDecimal currentShares = holding.getTotalShares() != null ? holding.getTotalShares() : BigDecimal.ZERO;
        BigDecimal diffShares = targetShares.subtract(currentShares);

        Transaction syncTx = new Transaction();
        syncTx.setHolding(holding);
        syncTx.setShares(diffShares);
        syncTx.setPrice(targetAvgPrice);
        syncTx.setType("SYNC");
        syncTx.setDate(LocalDate.parse("2025-02-20"));
        //syncTx.setDate(LocalDate.now());
        syncTx.setCurrency(holding.getNativeCurrency());
        
        transactionRepo.save(syncTx);

        holding.setTotalShares(targetShares);
        holding.setAvgPrice(targetAvgPrice);

        return Mapper.toDTO(holdingRepo.save(holding));
    }
    
    @Override
    public List<PortfolioPointDTO> getPortfolioHistory(Long userId) {
        List<Transaction> transactions = transactionRepo.findAllByUserIdOrderByDateAsc(userId);
        if (transactions.isEmpty()) return new ArrayList<>();

        String tickers = transactions.stream()
                .map(t -> t.getHolding().getTicker())
                .distinct()
                .collect(Collectors.joining(","));
        
        LocalDate startDate = transactions.get(0).getDate();
        Map<String, Map<String, Double>> priceHistory = fetchPricesFromPython(tickers, startDate);

        List<PortfolioPointDTO> graphPoints = new ArrayList<>();
        Map<String, BigDecimal> currentShares = new HashMap<>();
        BigDecimal totalInvertedCost = BigDecimal.ZERO;
        
        BigDecimal lastValueAdded = BigDecimal.valueOf(-1); 
        BigDecimal lastCostAdded = BigDecimal.valueOf(-1);

        int txIndex = 0;
        LocalDate today = LocalDate.now();

        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            boolean hadTransaction = false;

            while (txIndex < transactions.size() && transactions.get(txIndex).getDate().equals(date)) {
                Transaction tx = transactions.get(txIndex);
                String ticker = tx.getHolding().getTicker();
                
                currentShares.put(ticker, currentShares.getOrDefault(ticker, BigDecimal.ZERO).add(tx.getShares()));
                totalInvertedCost = totalInvertedCost.add(tx.getShares().multiply(tx.getPrice()));
                txIndex++;
                hadTransaction = true;
            }

            BigDecimal dailyMarketValue = BigDecimal.ZERO;
            boolean isMarketOpen = false;

            for (Map.Entry<String, BigDecimal> entry : currentShares.entrySet()) {
                Double price = null;
                if (priceHistory != null && priceHistory.containsKey(entry.getKey())) {
                    price = priceHistory.get(entry.getKey()).get(date.toString());
                }

                if (price != null && !price.isNaN()) {
                    dailyMarketValue = dailyMarketValue.add(entry.getValue().multiply(BigDecimal.valueOf(price)));
                    isMarketOpen = true;
                }
            }

            if (isMarketOpen || hadTransaction) {
                if (dailyMarketValue.compareTo(lastValueAdded) != 0 || totalInvertedCost.compareTo(lastCostAdded) != 0) {
                    graphPoints.add(new PortfolioPointDTO(date.toString(), dailyMarketValue, totalInvertedCost));
                    
                    lastValueAdded = dailyMarketValue;
                    lastCostAdded = totalInvertedCost;
                }
            }
        }
        return graphPoints;
    }

    private Map<String, Map<String, Double>> fetchPricesFromPython(String tickers, LocalDate startDate) {
        try {
            return this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/portfolio/history")
                            .queryParam("tickers", tickers)
                            .queryParam("start_date", startDate.toString())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {})
                    .block();
        } catch (Exception e) {
            System.err.println("Fallo al conectar con Python: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public List<PortfolioPointDTO> getTodayIntraday(Long userId, String period) {
        List<Holding> holdings = holdingRepo.findByUserId(userId);
        if (holdings.isEmpty()) return new ArrayList<>();

        String tickers = holdings.stream()
                .map(Holding::getTicker)
                .collect(Collectors.joining(","));

        Map<String, Object> response = fetchFromPython(tickers, period);
        
        if (response == null || !response.containsKey("prices")) {
            return new ArrayList<>(); 
        }

        Double usdToEurRate = response.get("rate") != null ? ((Number) response.get("rate")).doubleValue() : 0.92;
        
        Map<String, List<Map<String, Object>>> pricesData = (Map<String, List<Map<String, Object>>>) response.get("prices");

        List<PortfolioPointDTO> intradayPoints = new ArrayList<>();
        
        String referenceTicker = pricesData.containsKey("portfolio_index") ? "portfolio_index" : holdings.get(0).getTicker();
        List<Map<String, Object>> referencePoints = pricesData.get(referenceTicker);

        if (referencePoints == null) return new ArrayList<>();

        BigDecimal totalCost = holdings.stream()
                .map(h -> h.getTotalShares().multiply(h.getAvgPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (int i = 0; i < referencePoints.size(); i++) {
            String time = (String) referencePoints.get(i).get("t");
            BigDecimal currentTotalValue = BigDecimal.ZERO;

            for (Holding h : holdings) {
                List<Map<String, Object>> tickerHistory = pricesData.get(h.getTicker());
                
                if (tickerHistory != null && i < tickerHistory.size()) {
                    Double priceUsd = ((Number) tickerHistory.get(i).get("v")).doubleValue();
                    double priceEur = priceUsd * usdToEurRate;
                    
                    currentTotalValue = currentTotalValue.add(
                        BigDecimal.valueOf(priceEur).multiply(h.getTotalShares())
                    );
                }
            }

            if (currentTotalValue.compareTo(BigDecimal.ZERO) > 0) {
                intradayPoints.add(new PortfolioPointDTO(time, currentTotalValue, totalCost));
            }
        }
        
        return intradayPoints;
    }

    private Map<String, Object> fetchFromPython(String tickers, String period) {
        try {
            return this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/portfolio/history")
                            .queryParam("tickers", tickers)
                            .queryParam("period", period)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception e) {
            System.err.println("Error conectando con Python: " + e.getMessage());
            return null;
        }
    }
}