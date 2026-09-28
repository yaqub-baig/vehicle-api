package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.CountryRepository;
import com.autoadda.apis.vehicle.dao.StateTerritoryRepository;
import com.autoadda.apis.vehicle.dao.StreetRepository;
import com.autoadda.apis.vehicle.dao.TerritoryAreaRepository;
import com.autoadda.apis.vehicle.entity.Address;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.entity.Street;
import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.AddressRequest;
import com.autoadda.apis.vehicle.request.CarpoolListingRequest;
import com.autoadda.apis.vehicle.response.AddressResponse;
import com.autoadda.apis.vehicle.response.CarpoolListingResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarpoolListingMapper {

    private final AuthClient authClient;
    private final CountryRepository countryRepository;
    private final StateTerritoryRepository stateTerritoryRepository;
    private final TerritoryAreaRepository territoryAreaRepository;
    private final StreetRepository streetRepository;
    private final CountryMapper countryMapper;
    private final StateTerritoryMapper stateTerritoryMapper;
    private final TerritoryAreaMapper territoryAreaMapper;
    private final StreetMapper streetMapper;

    public CarpoolListing toEntity(CarpoolListingRequest request, Integer userId) {
        return CarpoolListing.builder()
                .userId(userId)
                .type(request.getType())
                .originAddress(toAddress(request.getOriginAddress()))
                .destinationAddress(toAddress(request.getDestinationAddress()))
                .travelDateTime(request.getTravelDateTime())
                .budget(request.getBudget())
                .availableSeats(request.getAvailableSeats())
                .carDescription(request.getCarDescription())
                .status(CarpoolListingStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    public void applyUpdate(CarpoolListing entity, CarpoolListingRequest request) {
        entity.setType(request.getType());
        entity.setOriginAddress(toAddress(request.getOriginAddress()));
        entity.setDestinationAddress(toAddress(request.getDestinationAddress()));
        entity.setTravelDateTime(request.getTravelDateTime());
        entity.setBudget(request.getBudget());
        entity.setAvailableSeats(request.getAvailableSeats());
        entity.setCarDescription(request.getCarDescription());
    }

    private Address toAddress(AddressRequest request) {
        Street street = request.getStreetId() != null
                ? streetRepository.findById(request.getStreetId())
                        .orElseThrow(() -> new ResourceNotFoundException("Street not found"))
                : null;

        return Address.builder()
                .country(countryRepository.findById(request.getCountryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Country not found")))
                .stateTerritory(stateTerritoryRepository.findById(request.getStateTerritoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("State/territory not found")))
                .territoryArea(territoryAreaRepository.findById(request.getTerritoryAreaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Territory area not found")))
                .street(street)
                .googleMapsUrl(request.getGoogleMapsUrl())
                .build();
    }

    private AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
                .country(countryMapper.toResponse(address.getCountry()))
                .stateTerritory(stateTerritoryMapper.toResponse(address.getStateTerritory()))
                .territoryArea(territoryAreaMapper.toResponse(address.getTerritoryArea()))
                .street(streetMapper.toResponse(address.getStreet()))
                .googleMapsUrl(address.getGoogleMapsUrl())
                .build();
    }

    public CarpoolListingResponse toResponse(CarpoolListing entity) {
        if (entity == null) {
            return null;
        }

        CarpoolListingResponse.CarpoolListingResponseBuilder builder = CarpoolListingResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .originAddress(toAddressResponse(entity.getOriginAddress()))
                .destinationAddress(toAddressResponse(entity.getDestinationAddress()))
                .travelDateTime(entity.getTravelDateTime())
                .budget(entity.getBudget())
                .availableSeats(entity.getAvailableSeats())
                .carDescription(entity.getCarDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        try {
            UserResponse poster = authClient.getUser(entity.getUserId());
            if (poster != null) {
                builder.posterName(poster.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch poster details for ID {}: {}", entity.getUserId(), e.getMessage());
        }

        return builder.build();
    }

    public List<CarpoolListingResponse> toResponseList(List<CarpoolListing> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}
