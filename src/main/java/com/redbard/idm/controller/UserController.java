package com.redbard.idm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redbard.idm.controller.assembler.UserModelAssembler;
import com.redbard.idm.model.AuthRequestDTO;
import com.redbard.idm.model.UserDTO;
import com.redbard.idm.model.exception.ResourceNotFoundException;
import com.redbard.idm.profiler.Profile;
import com.redbard.idm.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Setter;

@Api(tags = "users")
@RestController
@Setter
@CrossOrigin
public class UserController {

	private UserModelAssembler assembler;
	private UserService userService;

	@Autowired
	public UserController(UserModelAssembler userModelAssembler, UserService userService) {
		this.assembler = userModelAssembler;
		this.userService = userService;
	}

	@Profile("UserController#getAllUsers")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/users")
	@CrossOrigin
	public PagedModel<EntityModel<UserDTO>> getAllUsers(
			@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
			@RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {

		List<UserDTO> userDTOs = userService.getAllUsers(page, size);

		List<EntityModel<UserDTO>> users = new ArrayList<>();

		for (UserDTO user : userDTOs) {
			users.add(assembler.toModel(user));
		}

		return new PagedModel<>(users, new PageMetadata(size, page, userService.getTotalCount()),
				linkTo(methodOn(UserController.class).getAllUsers(page, size)).withSelfRel(),
				linkTo(methodOn(UserController.class).getAllUsers(page + 1, size)).withRel("next"));
	}

	@Profile("UserController#getUserById")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
	@GetMapping("/users/{id}")
	@CrossOrigin
	public EntityModel<UserDTO> getUserById(@PathVariable String id) {

		UserDTO user = userService.getUserById(id);

		if (user == null) {
			throw new ResourceNotFoundException(String.format("%s not found", id));
		}

		return assembler.toModel(user);
	}

	@Profile("UserController#createUser")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/users")
	@CrossOrigin
	public ResponseEntity<EntityModel<UserDTO>> createUser(@RequestBody UserDTO user) {
		EntityModel<UserDTO> entityModel = assembler.toModel(userService.createUser(user));

		return ResponseEntity //
				.created(entityModel.getRequiredLink("self").toUri()).body(entityModel);
	}

	@Profile("UserController#updateUser")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
	@PutMapping("/users/{id}")
	@CrossOrigin
	public EntityModel<UserDTO> updateUser(@PathVariable String id, @RequestBody UserDTO user) {

		UserDTO user2 = userService.updateUser(id, user);

		if (user2 == null) {
			throw new ResourceNotFoundException(String.format("%s not found", id));
		}
		return assembler.toModel(user2);
	}

	@Profile("UserController#signup")
	@PostMapping("/users/signup")
	@ApiOperation(value = "Sign Up")
	@ApiResponses(value = { //
			@ApiResponse(code = 400, message = "Bad request"), //
			@ApiResponse(code = 403, message = "Access denied"), //
			@ApiResponse(code = 422, message = "Username is already in use") })
	@CrossOrigin
	public ResponseEntity<EntityModel<UserDTO>> signup(@ApiParam("Signup User") @RequestBody UserDTO user) {
		return this.createUser(user);
	}
	
	@Profile("UserController#signup")
	@PostMapping("/users/signin")
	@ApiOperation(value = "Sign In")
	@ApiResponses(value = { //
			@ApiResponse(code = 400, message = "Bad request"), //
			@ApiResponse(code = 403, message = "Access denied")})//
	@CrossOrigin
	public EntityModel<UserDTO> signup(@ApiParam("Auth Request") @RequestBody AuthRequestDTO authRequest) {
		return assembler.toModel(userService.authenticateUser(authRequest.getUsername(), authRequest.getPassword()));
	}

}
