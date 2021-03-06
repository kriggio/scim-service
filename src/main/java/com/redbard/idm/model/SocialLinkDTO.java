package com.redbard.idm.model;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "socialLinks", itemRelation = "socialLink")
public class SocialLinkDTO {

	private String id;
	private String value;
	private String type;
	
}
