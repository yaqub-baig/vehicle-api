package com.autoadda.apis.vehicle.service;

import org.springframework.web.multipart.MultipartFile;

import com.autoadda.apis.vehicle.exception.CustomerNotExistException;
import com.autoadda.apis.vehicle.exception.GarageNotExistException;
import com.autoadda.apis.vehicle.exception.MemberAlreadyExistException;
import com.autoadda.apis.vehicle.request.CustomerVehicleRequest;
import com.autoadda.apis.vehicle.request.GarageOwnerRequest;
import com.autoadda.apis.vehicle.request.GaragePaymentRequest;
import com.autoadda.apis.vehicle.request.GarageProfileRequest;
import com.autoadda.apis.vehicle.request.GarageServiceRequest;
import com.autoadda.apis.vehicle.request.GarageServicesRequest;
import com.autoadda.apis.vehicle.request.GarageTechnicianRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

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
