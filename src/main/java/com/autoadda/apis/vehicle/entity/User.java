package com.autoadda.apis.vehicle.entity;


import lombok.*;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users", indexes = {
		@Index(name = "idx_phone", columnList = "mobile_number", unique = true)
})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "mobile_number",unique = true)
	private String mobileNumber;

	@Column(name = "web_site" )
	private String webSite;

	@Column(name = "company_name" )
	private String companyName;

	@Column(name = "emirates_id" )
	private String vatNumber;

	@Column(name = "account_status" )
	private String accountStatus;

	@Column(name = "is_verified")
	private Boolean isVerified;

	@ManyToMany(fetch = FetchType.EAGER) // or LAZY if you prefer
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;
	
}
