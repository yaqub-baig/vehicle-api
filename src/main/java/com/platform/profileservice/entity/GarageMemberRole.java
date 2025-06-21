package com.platform.profileservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Table(name = "garage_member_role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageMemberRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "member_id")
	private Integer memberId;
	
	@Column(name = "member_type")
	private String memberType;

	@Column(name = "role_cde")
    private String roleCde;
	
}
