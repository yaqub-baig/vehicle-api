package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// One plate source with its full active type -> code hierarchy, for cascading dropdowns.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateMasterTreeResponse {

    private Long id;
    private String code;
    private String name;
    private MediaFile image;
    private List<PlateTypeNode> plateTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlateTypeNode {
        private Long id;
        private String code;
        private String name;
        private List<PlateCodeNode> plateCodes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlateCodeNode {
        private Long id;
        private String code;
        private String colorHex;
    }
}
