package com.autoadda.apis.vehicle.transformers;

import org.springframework.stereotype.Component;

import com.autoadda.apis.vehicle.entity.GarageLicenseDetails;
import com.autoadda.apis.vehicle.request.GarageProfileRequest;
import com.autoadda.apis.vehicle.util.DateHelper;

@Component
public class ServiceTransformer {

	public GarageLicenseDetails tranformRequestToLicenseDetails(GarageProfileRequest request) {
		return GarageLicenseDetails.builder()
				.activities(request.getLicenseDetails().getActivities())
				.companyName(request.getLicenseDetails().getCompanyName())
				.dcciNumber(request.getLicenseDetails().getDcciNumber())
				.email(request.getLicenseDetails().getEmail())
				.issueDate(request.getLicenseDetails().getIssueDate())
				.expiryDate(request.getLicenseDetails().getExpiryDate())
				.faxNumber(request.getLicenseDetails().getFaxNumber())
				.garageId(request.getGarageId())
				.insertTms(DateHelper.dbTimeStampp())
				.legalType(request.getLicenseDetails().getLegalType())
				.licenseNumber(request.getLicenseDetails().getLicenseNumber())
				.mainLicenseNumber(request.getLicenseDetails().getMainLicenseNumber())
				.mobileNumber(request.getLicenseDetails().getMobileNumber())
				.name(request.getLicenseDetails().getName())
				.nationality(request.getLicenseDetails().getNationality())
				.numbur(request.getLicenseDetails().getNumbur())
				.parcelId(request.getLicenseDetails().getParcelId())
				.registerNumber(request.getLicenseDetails().getRegisterNumber())
				.remarks(request.getLicenseDetails().getRemarks())
				.role(request.getLicenseDetails().getRole())
				.share(request.getLicenseDetails().getShare())
				.tradeName(request.getLicenseDetails().getTradeName())
				.build();
	}

}
