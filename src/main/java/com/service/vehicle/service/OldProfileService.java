package com.service.vehicle.service;

import org.springframework.web.multipart.MultipartFile;

import com.service.vehicle.exception.CustomerNotExistException;
import com.service.vehicle.exception.GarageNotExistException;
import com.service.vehicle.exception.MemberAlreadyExistException;
import com.service.vehicle.request.CustomerVehicleRequest;
import com.service.vehicle.request.GarageOwnerRequest;
import com.service.vehicle.request.GaragePaymentRequest;
import com.service.vehicle.request.GarageProfileRequest;
import com.service.vehicle.request.GarageServiceRequest;
import com.service.vehicle.request.GarageServicesRequest;
import com.service.vehicle.request.GarageTechnicianRequest;
import com.service.vehicle.response.GenericResponse;

public interface OldProfileService {

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
