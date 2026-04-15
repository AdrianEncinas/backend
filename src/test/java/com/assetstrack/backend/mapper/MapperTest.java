package com.assetstrack.backend.mapper;

import com.assetstrack.backend.model.dto.HoldingDTO;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.model.entity.Holding;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.model.entity.Watchlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MapperTest {

    private User user;
    private Holding holding;
    private Watchlist watchlist;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("johndoe")
                .password("secret")
                .baseCurrency("USD")
                .build();

        holding = new Holding();
        holding.setId(10L);
        holding.setUser(user);
        holding.setTicker("AAPL");
        holding.setCompanyName("Apple Inc.");
        holding.setTotalShares(new BigDecimal("10"));
        holding.setAvgPrice(new BigDecimal("150.00"));

        watchlist = Watchlist.builder()
                .id(5L)
                .user(user)
                .ticker("MSFT")
                .companyName("Microsoft")
                .build();
    }

    // ── toDTO(Holding) ────────────────────────────────────────────────────────

    @Test
    void holdingToDTO_mapsAllFields() {
        HoldingDTO dto = Mapper.toDTO(holding);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getUserid()).isEqualTo(1L);
        assertThat(dto.getTicker()).isEqualTo("AAPL");
        assertThat(dto.getCompanyName()).isEqualTo("Apple Inc.");
        assertThat(dto.getTotalShares()).isEqualByComparingTo("10");
        assertThat(dto.getAvgPrice()).isEqualByComparingTo("150.00");
        assertThat(dto.getCurrency()).isEqualTo("USD");
    }

    @Test
    void holdingToDTO_nullInput_returnsNull() {
        assertThat(Mapper.toDTO((Holding) null)).isNull();
    }

    // ── toDTO(User) ───────────────────────────────────────────────────────────

    @Test
    void userToDTO_mapsAllFields() {
        UserDTO dto = Mapper.toDTO(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("johndoe");
        assertThat(dto.getPassword()).isEqualTo("secret");
        assertThat(dto.getBaseCurrency()).isEqualTo("USD");
    }

    @Test
    void userToDTO_nullInput_returnsNull() {
        assertThat(Mapper.toDTO((User) null)).isNull();
    }

    // ── toEntity(UserDTO) ─────────────────────────────────────────────────────

    @Test
    void userToEntity_mapsAllFields() {
        UserDTO dto = UserDTO.builder()
                .username("janedoe")
                .password("pass123")
                .baseCurrency("EUR")
                .build();

        User entity = Mapper.toEntity(dto);

        assertThat(entity.getUsername()).isEqualTo("janedoe");
        assertThat(entity.getPassword()).isEqualTo("pass123");
        assertThat(entity.getBaseCurrency()).isEqualTo("EUR");
    }

    @Test
    void userToEntity_nullInput_returnsNull() {
        assertThat(Mapper.toEntity(null)).isNull();
    }

    // ── toDTO(Watchlist) ──────────────────────────────────────────────────────

    @Test
    void watchlistToDTO_mapsAllFields() {
        WatchlistDTO dto = Mapper.toDTO(watchlist);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.userId()).isEqualTo(1L);
        assertThat(dto.ticker()).isEqualTo("MSFT");
        assertThat(dto.companyName()).isEqualTo("Microsoft");
    }

    @Test
    void watchlistToDTO_nullInput_returnsNull() {
        assertThat(Mapper.toDTO((Watchlist) null)).isNull();
    }
}
