package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.NumberPlateInterest;
import com.autoadda.apis.vehicle.entity.NumberPlateListing;
import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRequest;
import com.autoadda.apis.vehicle.response.NumberPlateInterestResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NumberPlateInterestMapper {

    private final AuthClient authClient;
    private final NumberPlateListingMapper listingMapper;

    public NumberPlateInterest toEntity(NumberPlateInterestRequest request, Integer buyerUserId, Integer sellerUserId) {
        return NumberPlateInterest.builder()
                .listingId(request.getListingId())
                .buyerUserId(buyerUserId)
                .sellerUserId(sellerUserId)
                .message(request.getMessage())
                .status(NumberPlateInterestStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    public NumberPlateInterestResponse toResponse(NumberPlateInterest entity, NumberPlateListing listing) {
        if (entity == null) {
            return null;
        }

        NumberPlateInterestResponse.NumberPlateInterestResponseBuilder builder = NumberPlateInterestResponse.builder()
                .id(entity.getId())
                .listingId(entity.getListingId())
                .buyerUserId(entity.getBuyerUserId())
                .sellerUserId(entity.getSellerUserId())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .rejectionReason(entity.getRejectionReason())
                .respondedAt(entity.getRespondedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .listing(listingMapper.toResponse(listing));

        if (entity.getStatus() == NumberPlateInterestStatus.ACCEPTED) {
            try {
                UserResponse buyer = authClient.getUser(entity.getBuyerUserId());
                if (buyer != null) {
                    builder.buyerContactEmail(buyer.getEmail());
                    builder.buyerContactPhone(buyer.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch buyer contact for user {}: {}", entity.getBuyerUserId(), e.getMessage());
            }

            try {
                UserResponse seller = authClient.getUser(entity.getSellerUserId());
                if (seller != null) {
                    builder.sellerContactEmail(seller.getEmail());
                    builder.sellerContactPhone(seller.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch seller contact for user {}: {}", entity.getSellerUserId(), e.getMessage());
            }
        }

        return builder.build();
    }
}
