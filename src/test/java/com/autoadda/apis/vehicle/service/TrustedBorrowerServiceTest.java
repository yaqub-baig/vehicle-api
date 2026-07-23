package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.TrustedBorrowerRepository;
import com.autoadda.apis.vehicle.entity.TrustedBorrower;
import com.autoadda.apis.vehicle.request.AddTrustedBorrowerRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.TrustedBorrowerResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.service.impl.TrustedBorrowerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrustedBorrowerServiceTest {

    @Mock
    private TrustedBorrowerRepository trustedBorrowerRepository;

    @Mock
    private AuthClient authClient;

    private TrustedBorrowerServiceImpl service;

    private static final Integer OWNER_ID = 1;
    private static final Integer BORROWER_ID = 2;

    @BeforeEach
    void setUp() {
        service = new TrustedBorrowerServiceImpl(trustedBorrowerRepository, authClient);
        lenient().when(trustedBorrowerRepository.save(any(TrustedBorrower.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void addTrustedBorrower_newEntry_shouldCreateAndReturn201() {
        when(trustedBorrowerRepository.findByOwnerIdAndBorrowerId(OWNER_ID, BORROWER_ID))
                .thenReturn(Optional.empty());
        when(authClient.getUser(BORROWER_ID)).thenReturn(UserResponse.builder().id(BORROWER_ID).name("Alex").build());

        GenericResponse response = service.addTrustedBorrower(OWNER_ID, new AddTrustedBorrowerRequest(BORROWER_ID));

        assertEquals("success", response.getStatus());
        assertEquals("201", response.getCode());
        TrustedBorrowerResponse body = (TrustedBorrowerResponse) response.getBody();
        assertEquals(BORROWER_ID, body.getBorrowerId());
        assertEquals("Alex", body.getBorrowerName());
    }

    @Test
    void addTrustedBorrower_revivesSoftDeletedEntry() {
        TrustedBorrower deleted = TrustedBorrower.builder()
                .id(5L).ownerId(OWNER_ID).borrowerId(BORROWER_ID).isDeleted(true).build();
        when(trustedBorrowerRepository.findByOwnerIdAndBorrowerId(OWNER_ID, BORROWER_ID))
                .thenReturn(Optional.of(deleted));
        when(authClient.getUser(BORROWER_ID)).thenReturn(UserResponse.builder().id(BORROWER_ID).build());

        GenericResponse response = service.addTrustedBorrower(OWNER_ID, new AddTrustedBorrowerRequest(BORROWER_ID));

        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals(false, deleted.getIsDeleted());
    }

    @Test
    void addTrustedBorrower_alreadyActive_shouldReturn400() {
        TrustedBorrower active = TrustedBorrower.builder()
                .id(5L).ownerId(OWNER_ID).borrowerId(BORROWER_ID).isDeleted(false).build();
        when(trustedBorrowerRepository.findByOwnerIdAndBorrowerId(OWNER_ID, BORROWER_ID))
                .thenReturn(Optional.of(active));
        when(authClient.getUser(BORROWER_ID)).thenReturn(UserResponse.builder().id(BORROWER_ID).build());

        GenericResponse response = service.addTrustedBorrower(OWNER_ID, new AddTrustedBorrowerRequest(BORROWER_ID));

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(trustedBorrowerRepository, never()).save(any());
    }

    @Test
    void addTrustedBorrower_self_shouldReturn400() {
        GenericResponse response = service.addTrustedBorrower(OWNER_ID, new AddTrustedBorrowerRequest(OWNER_ID));

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(trustedBorrowerRepository, never()).save(any());
    }

    @Test
    void addTrustedBorrower_borrowerNotFound_shouldReturn404() {
        when(authClient.getUser(BORROWER_ID)).thenReturn(null);

        GenericResponse response = service.addTrustedBorrower(OWNER_ID, new AddTrustedBorrowerRequest(BORROWER_ID));

        assertEquals("error", response.getStatus());
        assertEquals("404", response.getCode());
        verify(trustedBorrowerRepository, never()).save(any());
    }

    @Test
    void removeTrustedBorrower_existing_shouldSoftDelete() {
        TrustedBorrower active = TrustedBorrower.builder()
                .id(5L).ownerId(OWNER_ID).borrowerId(BORROWER_ID).isDeleted(false).build();
        when(trustedBorrowerRepository.findByOwnerIdAndBorrowerId(OWNER_ID, BORROWER_ID))
                .thenReturn(Optional.of(active));

        GenericResponse response = service.removeTrustedBorrower(OWNER_ID, BORROWER_ID);

        assertEquals("success", response.getStatus());
        assertEquals(true, active.getIsDeleted());
    }

    @Test
    void removeTrustedBorrower_notFound_shouldReturn404() {
        when(trustedBorrowerRepository.findByOwnerIdAndBorrowerId(OWNER_ID, BORROWER_ID))
                .thenReturn(Optional.empty());

        GenericResponse response = service.removeTrustedBorrower(OWNER_ID, BORROWER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("404", response.getCode());
    }

    @Test
    void getTrustedBorrowers_shouldReturnList() {
        TrustedBorrower entry = TrustedBorrower.builder()
                .id(5L).ownerId(OWNER_ID).borrowerId(BORROWER_ID).isDeleted(false).build();
        when(trustedBorrowerRepository.findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(OWNER_ID))
                .thenReturn(List.of(entry));
        when(authClient.getUser(BORROWER_ID)).thenReturn(UserResponse.builder().id(BORROWER_ID).name("Alex").build());

        GenericResponse response = service.getTrustedBorrowers(OWNER_ID);

        assertEquals("success", response.getStatus());
        @SuppressWarnings("unchecked")
        List<TrustedBorrowerResponse> body = (List<TrustedBorrowerResponse>) response.getBody();
        assertEquals(1, body.size());
        assertEquals("Alex", body.get(0).getBorrowerName());
    }
}
