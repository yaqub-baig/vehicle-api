package com.platform.profileservice.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.platform.profileservice.adapter.PlatformNotificationAdaptor;
import com.platform.profileservice.dao.AccountVerificationTrackerRepository;
import com.platform.profileservice.dao.AuthenticationDetailsRepository;
import com.platform.profileservice.dao.CustomerRepository;
import com.platform.profileservice.dao.GarageDocumentDetailsRepository;
import com.platform.profileservice.dao.GarageLicenseDetailsRepository;
import com.platform.profileservice.dao.GarageOwnerRepository;
import com.platform.profileservice.dao.GaragePaymentRepository;
import com.platform.profileservice.dao.GaragePendingActionsRepository;
import com.platform.profileservice.dao.GarageRepository;
import com.platform.profileservice.dao.GarageServiceRepository;
import com.platform.profileservice.dao.GarageSubscriptionRepository;
import com.platform.profileservice.dao.GarageTechnicianRepository;
import com.platform.profileservice.dao.InsuranceDetailsRepository;
import com.platform.profileservice.dao.PlatformServiceRepository;
import com.platform.profileservice.dao.ServiceCategoryRepository;
import com.platform.profileservice.dao.VehicleDetailsRepository;
import com.platform.profileservice.dao.VehicleRegistrationDetailsRepository;
import com.platform.profileservice.entity.AccountVerificationTracker;
import com.platform.profileservice.entity.AuthenticationDetails;
import com.platform.profileservice.entity.Customer;
import com.platform.profileservice.entity.Garage;
import com.platform.profileservice.entity.GarageDocumentDetails;
import com.platform.profileservice.entity.GarageLicenseDetails;
import com.platform.profileservice.entity.GarageOwner;
import com.platform.profileservice.entity.GaragePaymentDetails;
import com.platform.profileservice.entity.GaragePendingActions;
import com.platform.profileservice.entity.GarageServices;
import com.platform.profileservice.entity.GarageSubscription;
import com.platform.profileservice.entity.GarageTechnicians;
import com.platform.profileservice.entity.InsuranceDetails;
import com.platform.profileservice.entity.PlatformService;
import com.platform.profileservice.entity.ServiceCategory;
import com.platform.profileservice.entity.VehicleDetails;
import com.platform.profileservice.entity.VehicleRegistration;
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
import com.platform.profileservice.request.NotificationRequest;
import com.platform.profileservice.response.CustomerVehicleResponse;
import com.platform.profileservice.response.GarageServicesResponse;
import com.platform.profileservice.response.GarageSubscriptionResponse;
import com.platform.profileservice.response.GarageTechniResponse;
import com.platform.profileservice.response.GenericResponse;
import com.platform.profileservice.response.ServiceCategoryResponse;
import com.platform.profileservice.service.PlatformProfileService;
import com.platform.profileservice.transformers.PlatformProfileTransformer;
import com.platform.profileservice.util.AESUtil;
import com.platform.profileservice.util.Constants;
import com.platform.profileservice.util.DateHelper;
import com.platform.profileservice.validator.PlatformProfileValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlatformProfileServiceImpl implements PlatformProfileService {
	
	
	@Autowired
	private PlatformProfileTransformer platformProfileTransformer;
	
	@Autowired
	PlatformProfileValidator platformProfileValidator;
	
	@Autowired
	private GarageLicenseDetailsRepository garageLicenseDetailsRepository;
	
	@Autowired
	private GarageOwnerRepository garageOwnerRepository;
	
	@Autowired
	private GarageTechnicianRepository garageTechnicianRepository;
	
	@Autowired
	private GarageServiceRepository garageServiceRepository;
	
	@Autowired
	private GarageRepository garageRepository;
	
	@Autowired
	private PlatformServiceRepository platformServiceRepository;
	
	@Autowired
	private ServiceCategoryRepository serviceCategoryRepository;
	
	@Autowired
	private AuthenticationDetailsRepository authenticationDetailsRepository;
	
	@Autowired
	private GaragePendingActionsRepository garagePendingActionsRepository;
	
	@Autowired
	private GarageSubscriptionRepository garageSubscriptionRepository;
	
	@Autowired
	private PlatformNotificationAdaptor platformNotificationAdaptor;
	
	@Autowired
	private AccountVerificationTrackerRepository accountVerificationTrackerRepository;
	
	@Autowired
	private GaragePaymentRepository garagePaymentRepository;
	
	@Autowired
	private GarageDocumentDetailsRepository garageDocumentDetailsRepository;
	
	@Value("${authentication.encrypt.secret}")
	private String encryptSecret;
	
	@Value("${platform.auth.service.email.confirm.url}")
	private String emailVerifyUrl;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private VehicleDetailsRepository vehicleDetailsRepository;
	
	@Autowired
	private InsuranceDetailsRepository insuranceDetailsRepository;
	
	@Autowired
	private VehicleRegistrationDetailsRepository vehicleRegistrationDetailsRepository;
	
	
	@Override
	public GenericResponse updateGarageProfileDetails(GarageProfileRequest request) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(request.getGarageId());
			
			GarageLicenseDetails licenseDetails = garageLicenseDetailsRepository.findByGarageId(request.getGarageId());
			if(licenseDetails == null) {
				licenseDetails = platformProfileTransformer.tranformRequestToLicenseDetails(request);
			}
			garageLicenseDetailsRepository.save(licenseDetails);
			
			GaragePendingActions pendingAction = garagePendingActionsRepository.findByGarageId(request.getGarageId());
			pendingAction.setLicenseUpdateInd("Y");
			garagePendingActionsRepository.save(pendingAction);
			
			return GenericResponse.builder().body(licenseDetails).code("200").message("success").status("success").build();
		}catch (GarageNotExistException exec) {
			throw exec;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating garage license info");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	@Override
	public GenericResponse updateGarageOwnerDetails(GarageOwnerRequest request) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(request.getGarageId());
			
			GarageOwner owner = request.getOwnerDetails();
			owner.setAccountStatus("ACTIVE");
			owner.setInsertTms(DateHelper.dbTimeStampp());
			garageOwnerRepository.save(owner);
			
			GaragePendingActions pendingAction = garagePendingActionsRepository.findByGarageId(request.getGarageId());
			pendingAction.setOwnerUpdateInd("Y");
			garagePendingActionsRepository.save(pendingAction);
			
			return GenericResponse.builder().body(owner).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating garage owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
	
	@Override
	public GenericResponse updateGarageTechnicianDetails(GarageTechnicianRequest request) throws GarageNotExistException, MemberAlreadyExistException {
		try {
			platformProfileValidator.validateUsername(request.getTechnicianDetails().getEmailId());
			platformProfileValidator.validateGarage(request.getGarageId());
			
			GarageTechnicians technician = request.getTechnicianDetails();
			technician.setAccountStatus("INACTIVE");
			technician.setInsertTms(DateHelper.dbTimeStampp());
			garageTechnicianRepository.save(technician);
			
			AuthenticationDetails authDetails = prepareTechnicianAuthDetails(request.getTechnicianDetails());
			authenticationDetailsRepository.save(authDetails);
			
			GaragePendingActions pendingAction = garagePendingActionsRepository.findByGarageId(request.getGarageId());
			pendingAction.setTechUpdateInd("Y");
			garagePendingActionsRepository.save(pendingAction);
			
			updateAndsendEmailverificationEmail(technician.getEmailId(), technician.getId(), Constants.GARAGE_TECH);
			
			return GenericResponse.builder().body(GarageTechniResponse.builder().authInfo(authDetails).technician(technician).build()).code("200").message("success").status("success").build();
		}catch (MemberAlreadyExistException mexe) {
			throw mexe;
		}catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating garage owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
	
	
	private AuthenticationDetails prepareTechnicianAuthDetails(GarageTechnicians technicianDetails) {
		return AuthenticationDetails.builder()
				.memberId(technicianDetails.getId())
				.credType("TEMP")
				.effectiveDate(DateHelper.dbTimeStampp())
				.expDate(DateHelper.getDateWith90Days())
				.encryptedPassword(AESUtil.encrypt(technicianDetails.getMobileNumber(), encryptSecret))
				.memberType("GARAGE_TECH")
				.userName(technicianDetails.getEmailId())
				.build();
	}


	@Override
	public GenericResponse getTechnicianDetails(Integer garageId) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(garageId);
			List<GarageTechnicians> technicians = garageTechnicianRepository.findByGarageId(garageId);
			return GenericResponse.builder().body(technicians).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting garage technicians owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
	
	
	@Override
	public GenericResponse getGarageOwnerDetails(Integer garageId) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(garageId);
			List<GarageOwner> owners = garageOwnerRepository.findByGarageId(garageId);
			return GenericResponse.builder().body(owners).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting garage owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	@Override
	public GenericResponse updateGarageServicesDetails(GarageServicesRequest request) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(request.getGarageId());
			for(GarageServices service : request.getServices()) {
				service.setInsertTms(DateHelper.dbTimeStampp());
				garageServiceRepository.save(service);
			}
			GaragePendingActions pendingAction = garagePendingActionsRepository.findByGarageId(request.getGarageId());
			pendingAction.setServiceUpdateInd("Y");
			garagePendingActionsRepository.save(pendingAction);
			return GenericResponse.builder().body(request.getServices()).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while posting garage services");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	
	}


	@Override
	public GenericResponse getGarageServicesDetails(Integer garageId) throws GarageNotExistException {
		GarageServicesResponse response = GarageServicesResponse.builder().build();
		try {
			platformProfileValidator.validateGarage(garageId);
			Garage garage = garageRepository.findById(garageId).get();
			response.setGarage(garage);
			List<GarageServices> garageServices = garageServiceRepository.findByGarageId(garageId);
			if(!CollectionUtils.isEmpty(garageServices)) {
				List<Integer> servicesIds = garageServices.stream().map(GarageServices::getServiceId).collect(Collectors.toList());
				List<PlatformService> services = platformServiceRepository.findByIdIn(servicesIds);
				response.setServices(services);
			}
			return GenericResponse.builder().body(response).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting garage owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	
	}


	@Override
	public GenericResponse deleteGarageServiceDetails(GarageServiceRequest request) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(request.getGarageId());
			GarageServices garageService = garageServiceRepository.findByGarageIdAndServiceId(request.getGarageId(), request.getServiceId());
			garageServiceRepository.delete(garageService);
			return GenericResponse.builder().body("").code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting garage owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	@Override
	public GenericResponse getGarageLicenseProfileDetails(Integer garageId) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(garageId);
			GarageLicenseDetails licenseDetails = garageLicenseDetailsRepository.findByGarageId(garageId);
			
			return GenericResponse.builder().body(licenseDetails).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting garage license info");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	
	}


	@Override
	public GenericResponse getPlatformServicesLookup() {
		List<ServiceCategoryResponse> responseBody = new ArrayList<>();
		try {
			List<ServiceCategory> categories = serviceCategoryRepository.findAll();
			List<PlatformService> services = platformServiceRepository.findAll();
			for (ServiceCategory category : categories) {
				List<PlatformService> cateServices = services.stream().filter(ser -> ser.getCategoryId() == category.getId()).collect(Collectors.toList());
				responseBody.add(ServiceCategoryResponse.builder()
						.id(category.getId())
						.category(category.getCategory())
						.desc(category.getDsc())
						.services(cateServices)
						.build());
			}
			return GenericResponse.builder().body(responseBody).code("200").message("success").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting services lookup");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	@Override
	public GenericResponse activateGarageAndFreeTrail(Integer garageId) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(garageId);
			Garage garage =  garageRepository.findById(garageId).get();
			GarageSubscription subscription = prepareGarageFreeTrailSubscription(garage);
			garageSubscriptionRepository.save(subscription);
			garage.setAccountStatus("ACTIVE");
			garageRepository.save(garage);
			GaragePendingActions pendingAction = garagePendingActionsRepository.findByGarageId(garageId);
			pendingAction.setSubscriptionId(subscription.getSubscriptionId());
			garagePendingActionsRepository.save(pendingAction);
			return GenericResponse.builder().body(GarageSubscriptionResponse.builder().garage(garage).subscription(subscription).build()).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while activating the garage and freetrail");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	private GarageSubscription prepareGarageFreeTrailSubscription(Garage garage) {
		return GarageSubscription.builder()
				.activeInd("Y")
				.effectiveDate(DateHelper.dbTimeStampp())
				.expDate(DateHelper.getDateWith90Days())
				.garageId(garage.getId())
				.insertTms(DateHelper.dbTimeStampp())
				.subscriptionId(1)
				.subscriptionType("FREE_TRAIL")
				.insertTms(DateHelper.dbTimeStampp())
				.build();
	}


	@Override
	public GenericResponse activateGarageForSearchResults() {
		try {
			List<GaragePendingActions> pendingSearchGarages = garagePendingActionsRepository.findByLicenseUpdateIndAndTechUpdateIndAndEmailVerifyIndAndServiceUpdateIndAndSearchInd("Y", "Y", "Y", "Y" , "N");
			for (GaragePendingActions garagePendingActions : pendingSearchGarages) {
				garagePendingActions.setSearchInd("Y");
				garagePendingActionsRepository.save(garagePendingActions);
			}
			return GenericResponse.builder().body("Activated all garages for Search").code("200").message("success").status("success").build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while activating the garage for search");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}
	
	
	private void updateAndsendEmailverificationEmail(String emailId, Integer memberId, String memberType) {
		try {
			String token = UUID.randomUUID().toString();
			String verificationUrl = emailVerifyUrl +"?token=" + token;
			NotificationRequest emailRequest = prepareVerificationEmailRequest(memberId, memberType, 0, emailId, verificationUrl);
			GenericResponse response = platformNotificationAdaptor.callNotificationService(emailRequest);
			if(response.getStatus().equals("success")) {
				AccountVerificationTracker verifyTracker = prepareAccountVerificationTracker(memberId, memberType, token);
				accountVerificationTrackerRepository.save(verifyTracker);
			}
			log.info("Triggering email through SMTP successful");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR while sending the verification email"+memberId);
		}
	}
	
	public NotificationRequest prepareVerificationEmailRequest(Integer memberId, String memberType, int templateId, String emailId,
			String verificationUrl) {
		return NotificationRequest.builder()
				.memberId(memberId)
				.memberType(memberType)
				.templateId(templateId)
				.verifyUrl(verificationUrl)
				.toEmailId(emailId)
				.build();
	}
	
	public AccountVerificationTracker prepareAccountVerificationTracker(int memberId, String memberType, String token) {
		return AccountVerificationTracker.builder()
				.memberId(memberId)
				.memberType(memberType)
				.token(token)
				.status(Constants.NOT_VERIFIED)
				.verifyStartDate(DateHelper.dbTimeStampp())
				.verifyEndDate(DateHelper.getVerifyEndDate())
				.build();
	}


	@Override
	public GenericResponse registerGaragePaymentDetails(GaragePaymentRequest request) throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(request.getGarageId());
			
			String encCardNumber = AESUtil.encrypt(request.getCardNumber(), encryptSecret);
			String encName = AESUtil.encrypt(request.getName(), encryptSecret);
			String encExpDate = AESUtil.encrypt(request.getExpDate(), encryptSecret);
			GaragePaymentDetails paymentDetails = GaragePaymentDetails.builder()
					.cardNumber(encCardNumber)
					.expDate(encExpDate)
					.name(encName)
					.garageId(request.getGarageId())
					.insertTms(DateHelper.dbTimeStampp())
					.build();
			garagePaymentRepository.save(paymentDetails);
			GaragePendingActions pendingAction = garagePendingActionsRepository.findByGarageId(request.getGarageId());
			pendingAction.setPaymentInd("Y");
			garagePendingActionsRepository.save(pendingAction);
			return GenericResponse.builder().body(paymentDetails).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while creating garage owner");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	
	}


	@Override
	public GenericResponse uploadGarageLicenseDocument(Integer garageId, MultipartFile file)
			throws GarageNotExistException {
		try {
			platformProfileValidator.validateGarage(garageId);
			GarageDocumentDetails document = prepareGarageDocumentDetails(garageId, file);
			garageDocumentDetailsRepository.save(document);
			return GenericResponse.builder().body(document).code("200").message("success").status("success").build();
		} catch (GarageNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while uploading the license document");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	private GarageDocumentDetails prepareGarageDocumentDetails(Integer garageId, MultipartFile file) throws IOException {
		return GarageDocumentDetails.builder()
				.fileName(file.getOriginalFilename())
				.garageId(garageId)
				.extension(file.getName())
				.byteStrng(new String(file.getBytes()))
				.insertTms(DateHelper.dbTimeStampp())
				.build();
	}


	@Override
	public GenericResponse updateCustomerVehicleDetails(CustomerVehicleRequest request)
			throws CustomerNotExistException {
		try {
			platformProfileValidator.validateCustomer(request.getCustomerId());
			Customer customer = customerRepository.findById(request.getCustomerId()).get();
			if(request.getVehicle() != null) {
				VehicleDetails vehicleDetails = request.getVehicle();
				if(request.getVehicle().getRegistration() != null) {
					VehicleRegistration registration = request.getVehicle().getRegistration();
					vehicleRegistrationDetailsRepository.save(registration);
					vehicleDetails.setRegistration(registration);
				}
				if(request.getVehicle().getInsurance() != null) {
					InsuranceDetails insurance = request.getVehicle().getInsurance();
					insuranceDetailsRepository.save(insurance);
					vehicleDetails.setInsurance(insurance);	
				}
//				vehicleDetails.setCustomerId(customer.getId());
				vehicleDetailsRepository.save(vehicleDetails);
				return GenericResponse.builder().body(vehicleDetails).code("200").message("success").status("success").build();
			}
		} catch (CustomerNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while adding customer vehicle");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	
	}


	@Override
	public GenericResponse getCustomerVehicleDetails(Integer customerId) throws CustomerNotExistException {
		CustomerVehicleResponse response = CustomerVehicleResponse.builder().build();
		try {
			platformProfileValidator.validateCustomer(customerId);
			Customer customer = customerRepository.findById(customerId).get();
			List<VehicleDetails> vehicles = vehicleDetailsRepository.findByCustomerId(customerId);
			response.setCustomer(customer);
			response.setVehicles(vehicles);
			return GenericResponse.builder().body(response).code("200").message("success").status("success").build();
		} catch (CustomerNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting customer vehicle");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}


	@Override
	public GenericResponse deleteCustomerVehicleDetails(Integer customerId, Integer vehicleId)
			throws CustomerNotExistException {
		try {
			platformProfileValidator.validateCustomer(customerId);
			VehicleDetails vehicles = vehicleDetailsRepository.findById(vehicleId).get();
			vehicleDetailsRepository.delete(vehicles);
			return GenericResponse.builder().body("Vehicle deleted").code("200").message("success").status("success").build();
		} catch (CustomerNotExistException exec) {
			throw exec;
		} catch(Exception e) {
			e.printStackTrace();
			log.error("ERROR while getting customer vehicle");
		}
		return GenericResponse.builder().status("failed").message("failed").code("500").build();
	}

}
