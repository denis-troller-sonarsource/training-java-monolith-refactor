package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResourceTest extends RestResourceTestBase {

    private UserResource resource;

    @BeforeEach
    void createResource() {
        resource = new UserResource(userService);
    }

    private User seedUser() {
        return userService.createUser(new User("john.doe@example.com", "John Doe"));
    }

    @Test
    void listReturnsAllUsers() {
        seedUser();

        assertThat(resource.list()).hasSize(1);
    }

    @Test
    void getReturnsUserWhenFound() {
        User saved = seedUser();

        Response response = resource.get(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(((User) response.getEntity()).getName()).isEqualTo("John Doe");
    }

    @Test
    void getReturns404WhenMissing() {
        Response response = resource.get(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createReturns201WithSavedEntity() {
        Response response = resource.create(new User("jane@example.com", "Jane"));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(((User) response.getEntity()).getId()).isNotNull();
    }

    @Test
    void updateReturnsOkWhenFound() {
        User saved = seedUser();

        Response response = resource.update(saved.getId(), new User("john.doe@example.com", "Johnny"));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(userService.getUser(saved.getId()).getName()).isEqualTo("Johnny");
    }

    @Test
    void updateReturns404WhenMissing() {
        Response response = resource.update(999L, new User("x@x.com", "X"));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteReturnsNoContentWhenFound() {
        User saved = seedUser();

        Response response = resource.delete(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        assertThat(userService.listUsers()).isEmpty();
    }

    @Test
    void deleteReturns404WhenMissing() {
        Response response = resource.delete(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
