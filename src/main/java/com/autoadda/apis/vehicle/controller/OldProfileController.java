package com.autoadda.apis.vehicle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.autoadda.apis.vehicle.request.CustomerVehicleRequest;
import com.autoadda.apis.vehicle.request.GarageOwnerRequest;
import com.autoadda.apis.vehicle.request.GaragePaymentRequest;
import com.autoadda.apis.vehicle.request.GarageProfileRequest;
import com.autoadda.apis.vehicle.request.GarageServiceRequest;
import com.autoadda.apis.vehicle.request.GarageServicesRequest;
import com.autoadda.apis.vehicle.request.GarageTechnicianRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.OldProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/profile")
public class OldProfileController {
	
	
	@Autowired
    OldProfileService oldProfileService;
	
	
	
	@Operation(summary = "Get a welcome message")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows welcome message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "Welcome api not found", 
	    content = @Content) })
	@GetMapping("welcome")
	public GenericResponse getGreetingMessage() {
		return GenericResponse.builder().status("sucess").code("200").message("success").body("Welcome to platform profile service..!").build();
	}
	
	@Operation(summary = "Post Garage governament license details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("garage/license")
	public GenericResponse updateGarageDetails(@RequestBody GarageProfileRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.updateGarageProfileDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage license details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "GET garage license details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("garage/license/{garageId}")
	public GenericResponse updateGarageDetails(Integer garageId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.getGarageLicenseProfileDetails(garageId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while get garage license details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "Create a garage owner details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("garage/owner")
	public GenericResponse registerGarageOwnerDetails(@RequestBody GarageOwnerRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.updateGarageOwnerDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage owner details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	
	@Operation(summary = "Post the services that garage is providing")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("garage/service")
	public GenericResponse registerGarageServiceDetails(@RequestBody GarageServicesRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.updateGarageServicesDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "GET services that garage is providing the with category")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@GetMapping("garage/service/{garageId}")
	public GenericResponse getGarageServiceDetails(Integer garageId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.getGarageServicesDetails(garageId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	
	@Operation(summary = "GET The services lookup data, which maintained by platform")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@GetMapping("service/lookup")
	public GenericResponse getServiceLookupDetails() {
		GenericResponse response = null;
		try {
			response = oldProfileService.getPlatformServicesLookup();
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	
	@Operation(summary = "Delete Garage can delete the services, whenever they want to disable")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@DeleteMapping("garage/service")
	public GenericResponse deleteGarageServiceDetails(@RequestBody GarageServiceRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.deleteGarageServiceDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "Post garage owner can register the technician details with temparory auth details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("garage/technician")
	public GenericResponse registerGarageTechnicianDetails(@RequestBody GarageTechnicianRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.updateGarageTechnicianDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "GET garage technician details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@GetMapping("garage/technician/{garageId}")
	public GenericResponse getGarageTechnicianDetails(Integer garageId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.getTechnicianDetails(garageId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	
	@Operation(summary = "GET garage owner details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@GetMapping("garage/owner/{garageId}")
	public GenericResponse getGarageOwnerDetails(Integer garageId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.getGarageOwnerDetails(garageId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "Post Activate garage and enable freetrail subscription on the garage")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("garage/activateFreetrail/{garageId}")
	public GenericResponse activateGarageAndFreeTrail(Integer garageId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.activateGarageAndFreeTrail(garageId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "Post Enable garage for search, whoever is completed their profile update")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "activateGarageForSearchResults api not found", 
	    content = @Content) })
	@PostMapping("garage/enableForSearch")
	public GenericResponse activateGarageForSearchResults() {
		GenericResponse response = null;
		try {
			response = oldProfileService.activateGarageForSearchResults();
			return response;
		} catch (Exception e) {
			log.error("ERROR while activating garage profile for search -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "Post garage payment details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "registerGaragePaymentDetails api not found", 
	    content = @Content) })
	@PostMapping("garage/payment")
	public GenericResponse registerGaragePaymentDetails(@RequestBody GaragePaymentRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.registerGaragePaymentDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update garage payment details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	
	@Operation(summary = "Post garage document details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "uploadGarageLicenseDocument api not found", 
	    content = @Content) })
	@PostMapping("garage/document")
	public GenericResponse uploadGarageLicenseDocument(@RequestParam("garageId") Integer garageId, @RequestParam("file") MultipartFile file) {
		GenericResponse response = null;
		try {
			response = oldProfileService.uploadGarageLicenseDocument(garageId, file);
			return response;
		} catch (Exception e) {
			log.error("ERROR while upload garage document details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	
	@Operation(summary = "Post customer vehicle details with insurance details and registration details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@PostMapping("customer/vehicle")
	public GenericResponse updateCustomerVehicleDetails(@RequestBody CustomerVehicleRequest request) {
		GenericResponse response = null;
		try {
			response = oldProfileService.updateCustomerVehicleDetails(request);
			return response;
		} catch (Exception e) {
			log.error("ERROR while update customer profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "GET customer vehicles and its details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@GetMapping("customer/vehicle/{customerId}")
	public GenericResponse getCustomerVehicleDetails(Integer customerId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.getCustomerVehicleDetails(customerId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while customer vehicle profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	
	@Operation(summary = "Delete customer vehicle and its details")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "If service is running fine, shows success message", 
	    content = { @Content(mediaType = "application/json", 
	      schema = @Schema(implementation = GenericResponse .class)) }),
	  @ApiResponse(responseCode = "404", description = "profile api not found", 
	    content = @Content) })
	@DeleteMapping("customer/vehicle/{customerId}/{vehicleId}")
	public GenericResponse deleteCustomerVehicleDetails(Integer customerId, Integer vehicleId) {
		GenericResponse response = null;
		try {
			response = oldProfileService.deleteCustomerVehicleDetails(customerId, vehicleId);
			return response;
		} catch (Exception e) {
			log.error("ERROR while deleting customer vehicle profile details -> "+e.getMessage());
			return GenericResponse.builder().status("failed").code("500").message(e.getMessage()).build();
		}
	}
	

}
