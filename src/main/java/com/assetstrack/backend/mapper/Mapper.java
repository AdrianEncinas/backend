package com.assetstrack.backend.mapper;

import com.assetstrack.backend.model.dto.HoldingDTO;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.model.entity.Holding;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.model.entity.Watchlist;

public class Mapper {

    public static HoldingDTO toDTO(Holding h){
        if (h == null) return null;

        return HoldingDTO.builder()
                .id(h.getId())
                .ticker(h.getTicker())
                .userid(h.getUser().getId())
                .currency(h.getUser().getBaseCurrency())
                .companyName(h.getCompanyName())
                .totalShares(h.getTotalShares())
                .avgPrice(h.getAvgPrice())
                .build();
    }

    public static UserDTO toDTO(User u){
        if (u == null) return null;

        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .password(u.getPassword())
                .baseCurrency(u.getBaseCurrency())
                .build();
    }

    public static UserResponse toResponse(User u){
        if (u == null) return null;

        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .baseCurrency(u.getBaseCurrency())
                .build();
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setBaseCurrency(dto.getBaseCurrency());
        return user;
    }

    public static WatchlistDTO toDTO(Watchlist watchlist) {
        if (watchlist == null) return null;
        
        return new WatchlistDTO(
            watchlist.getId(),
            watchlist.getUser().getId(),
            watchlist.getTicker(),
            watchlist.getCompanyName()
        );
    }

    
}
