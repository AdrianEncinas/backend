package com.assetstrack.backend.service;

import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistAddRequest;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.model.entity.Watchlist;
import com.assetstrack.backend.repository.UserRepository;
import com.assetstrack.backend.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WatchlistRepository watchlistRepo;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MarketService marketService;

    private User user;
    private Watchlist watchlist;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("johndoe")
                .baseCurrency("USD")
                .build();

        watchlist = Watchlist.builder()
                .id(10L)
                .user(user)
                .ticker("AAPL")
                .companyName("Apple Inc.")
                .build();
    }

    // ── searchTicker ──────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void searchTicker_returnsListFromWebClient() {
        TickerSearchDTO dto = new TickerSearchDTO("AAPL", "Apple Inc.", "NASDAQ");

        // Use RETURNS_DEEP_STUBS on a local spy to handle fluent WebClient chain
        WebClient deepWebClient = mock(WebClient.class, Answers.RETURNS_DEEP_STUBS);
        MarketService serviceUnderTest = new MarketService(deepWebClient, watchlistRepo, userRepository);

        when(deepWebClient.get()
                .uri(any(Function.class))
                .retrieve()
                .bodyToFlux(TickerSearchDTO.class))
                .thenReturn(Flux.just(dto));

        List<TickerSearchDTO> result = serviceUnderTest.searchTicker("AAPL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).symbol()).isEqualTo("AAPL");
    }

    // ── getWatchlist ──────────────────────────────────────────────────────────

    @Test
    void getWatchlist_returnsWatchlistDTOs() {
        when(watchlistRepo.findByUserId(1L)).thenReturn(List.of(watchlist));

        List<WatchlistDTO> result = marketService.getWatchlist(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ticker()).isEqualTo("AAPL");
    }

    @Test
    void getWatchlist_noItems_returnsEmptyList() {
        when(watchlistRepo.findByUserId(99L)).thenReturn(List.of());

        List<WatchlistDTO> result = marketService.getWatchlist(99L);

        assertThat(result).isEmpty();
    }

    // ── addToWatchlist ────────────────────────────────────────────────────────

    @Test
    void addToWatchlist_validUser_savesAndReturnsDTO() {
        WatchlistAddRequest input = new WatchlistAddRequest("GOOGL", "Alphabet Inc.");

        Watchlist saved = Watchlist.builder()
                .id(20L)
                .user(user)
                .ticker("GOOGL")
                .companyName("Alphabet Inc.")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(watchlistRepo.save(any(Watchlist.class))).thenReturn(saved);

        WatchlistDTO result = marketService.addToWatchlist(input, 1L);

        assertThat(result.id()).isEqualTo(20L);
        assertThat(result.ticker()).isEqualTo("GOOGL");
        verify(watchlistRepo).save(any(Watchlist.class));
    }

    @Test
    void addToWatchlist_userNotFound_throwsRuntimeException() {
        WatchlistAddRequest input = new WatchlistAddRequest("GOOGL", "Alphabet Inc.");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marketService.addToWatchlist(input, 99L))
            .isInstanceOf(com.assetstrack.backend.exception.NotFoundException.class)
            .hasMessageContaining("User not found");
    }

    // ── deleteWatchlist ───────────────────────────────────────────────────────

    @Test
    void deleteWatchlist_existingId_deletesEntry() {
        when(watchlistRepo.findById(10L)).thenReturn(Optional.of(watchlist));

        marketService.deleteFromWatchlist(10L, 1L);

        verify(watchlistRepo).delete(watchlist);
    }

    @Test
    void deleteWatchlist_nonExistingId_throwsRuntimeException() {
        when(watchlistRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marketService.deleteFromWatchlist(99L, 1L))
            .isInstanceOf(com.assetstrack.backend.exception.NotFoundException.class)
                .hasMessageContaining("not found");
    }
}
