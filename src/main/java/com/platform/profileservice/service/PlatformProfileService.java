package com.platform.profileservice.service;

import org.springframework.web.multipart.MultipartFile;

import com.platform.profileservice.exception.CustomerNotExistException;
import com.platform.profileservice.exception.GarageNotExistException;
import com.platform.profileservice.exception.MemberAlreadyExistException;
import com.platform.profileservice.request.CustomerVehicleRequest;
import com.platform.profileservice.request.GarageOwnerRequest;
import com.platform.profileservice.request.GaragePaymentRequest;
import com.platform.profileservice.request.GarageProfileRequest;
import com.platform.profileservice.request.GarageServiceRequest;
import com.platform.profileservice.request.GarageServicesRequest;
import com.platform.profileservice.request.GarageTechnicianRequest;
import com.platform.profileservice.response.GenericResponse;

public interface PlatformProfileService {

	GenericResponse updateGarageProfileDetails(GarageProfileRequest request) throws GarageNotExistException;

	GenericResponse updateGarageOwnerDetails(GarageOwnerRequest request) throws GarageNotExistException;

	GenericResponse updateGarageTechnicianDetails(GarageTechnicianRequest request) throws GarageNotExistException, MemberAlreadyExistException;

	GenericResponse getGarageOwnerDetails(Integer garageId) throws GarageNotExistException;

	GenericResponse getTechnicianDetails(Integer garageId) throws GarageNotExistException;

	GenericResponse updateGarageServicesDetails(GarageServicesRequest request) throws GarageNotExistException;

	GenericResponse getGarageServicesDetails(Integer garageId) throws GarageNotExistException;

	GenericResponse deleteGarageServiceDetails(GarageServiceRequest request) throws GarageNotExistException;

	GenericResponse getGarageLicenseProfileDetails(Integer garageId) throws GarageNotExistException;

	GenericResponse getPlatformServicesLookup();

	GenericResponse activateGarageAndFreeTrail(Integer garageId)throws GarageNotExistException;

	GenericResponse activateGarageForSearchResults();

	GenericResponse registerGaragePaymentDetails(GaragePaymentRequest request) throws GarageNotExistException;

	GenericResponse uploadGarageLicenseDocument(Integer garageId, MultipartFile file) throws GarageNotExistException;

	GenericResponse updateCustomerVehicleDetails(CustomerVehicleRequest request) throws CustomerNotExistException;

	GenericResponse getCustomerVehicleDetails(Integer customerId) throws CustomerNotExistException;

	GenericResponse deleteCustomerVehicleDetails(Integer customerId, Integer vehicleId) throws CustomerNotExistException;

}
