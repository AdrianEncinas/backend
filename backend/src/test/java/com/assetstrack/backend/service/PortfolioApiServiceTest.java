package com.assetstrack.backend.service;

import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.model.dto.HoldingDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.model.entity.Holding;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.repository.HoldingRepository;
import com.assetstrack.backend.repository.TransactionRepository;
import com.assetstrack.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioApiServiceTest {

    @Mock
    private WebClient webClient;
    @Mock
    private HoldingRepository holdingRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private TransactionRepository transactionRepo;

    @InjectMocks
    private PortfolioApiService portfolioApiService;

    private User user;
    private Holding holding;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("johndoe")
                .baseCurrency("USD")
                .build();

        holding = new Holding();
        holding.setId(10L);
        holding.setUser(user);
        holding.setTicker("AAPL");
        holding.setCompanyName("Apple Inc.");
        holding.setTotalShares(new BigDecimal("10"));
        holding.setAvgPrice(new BigDecimal("150.00"));
    }

    // ── addPosition ───────────────────────────────────────────────────────────

    @Test
    void addPosition_newTicker_savesAndReturnsSuccess() {
        StockPositionDTO position = new StockPositionDTO(
                "GOOGL", "Alphabet Inc.", new BigDecimal("5"), new BigDecimal("2800.00"), 1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(holdingRepo.findByUserId(1L)).thenReturn(List.of(holding)); // AAPL ya existe, GOOGL no
        when(holdingRepo.save(any(Holding.class))).thenReturn(new Holding());

        String result = portfolioApiService.addPosition(position, 1L);

        assertThat(result).isEqualTo("Position added");
        verify(holdingRepo).save(any(Holding.class));
    }

    @Test
    void addPosition_tickerAlreadyExists_returnsAlreadyAddedMessage() {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("5"), new BigDecimal("160.00"), 1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(holdingRepo.findByUserId(1L)).thenReturn(List.of(holding));

        assertThatThrownBy(() -> portfolioApiService.addPosition(position, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
        verify(holdingRepo, never()).save(any());
    }

    @Test
    void addPosition_userNotFound_throwsRuntimeException() {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("5"), new BigDecimal("160.00"), 99L);

        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioApiService.addPosition(position, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── modifyPosition ────────────────────────────────────────────────────────

    @Test
    void modifyPosition_existingTicker_updatesAndReturnsSuccess() {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("20"), new BigDecimal("155.00"), 1L);

        when(holdingRepo.findByUserId(1L)).thenReturn(List.of(holding));
        when(holdingRepo.save(any(Holding.class))).thenReturn(holding);

        String result = portfolioApiService.modifyPosition(position, 1L);

        assertThat(result).isEqualTo("The position has been modified.");
        verify(holdingRepo).save(holding);
    }

    @Test
    void modifyPosition_tickerNotFound_returnsErrorMessage() {
        StockPositionDTO position = new StockPositionDTO(
                "MSFT", "Microsoft", new BigDecimal("5"), new BigDecimal("300.00"), 1L);

        when(holdingRepo.findByUserId(1L)).thenReturn(List.of(holding)); // solo AAPL

        assertThatThrownBy(() -> portfolioApiService.modifyPosition(position, 1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("not found");
        verify(holdingRepo, never()).save(any());
    }

    // ── deletePosition ────────────────────────────────────────────────────────

    @Test
    void deletePosition_existingTicker_deletesAndReturnsSuccess() {
        when(holdingRepo.findByUserId(1L)).thenReturn(List.of(holding));

        String result = portfolioApiService.deletePosition("AAPL", 1L);

        assertThat(result).isEqualTo("The position has been deleted.");
        verify(holdingRepo).delete(holding);
    }

    @Test
    void deletePosition_tickerNotFound_returnsErrorMessage() {
        when(holdingRepo.findByUserId(1L)).thenReturn(List.of(holding)); // solo AAPL

        assertThatThrownBy(() -> portfolioApiService.deletePosition("TSLA", 1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("not found");
        verify(holdingRepo, never()).delete(any());
    }

    // ── syncHoldingManually ───────────────────────────────────────────────────

    @Test
    void syncHoldingManually_existingHolding_updatesSharesAndAvgPrice() {
        when(holdingRepo.findById(10L)).thenReturn(Optional.of(holding));
        when(transactionRepo.save(any())).thenReturn(null);
        when(holdingRepo.save(any(Holding.class))).thenReturn(holding);

        HoldingDTO result = portfolioApiService.syncHoldingManually(
                10L, 1L, new BigDecimal("25"), new BigDecimal("160.00"));

        assertThat(result).isNotNull();
        verify(transactionRepo).save(any());
        verify(holdingRepo).save(holding);
    }

    @Test
    void syncHoldingManually_holdingNotFound_throwsRuntimeException() {
        when(holdingRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                portfolioApiService.syncHoldingManually(99L, 1L, new BigDecimal("10"), new BigDecimal("100")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }
}
