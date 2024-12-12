package com.retail.rewardpointcalc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.retail.rewardpointcalc.controller.CustomerController;
import com.retail.rewardpointcalc.controller.TransactionController;
import com.retail.rewardpointcalc.entity.Customer;
import com.retail.rewardpointcalc.entity.Transaction;
import com.retail.rewardpointcalc.model.CustomerRequest;
import com.retail.rewardpointcalc.model.CustomerResponse;
import com.retail.rewardpointcalc.model.TransactionRequest;
import com.retail.rewardpointcalc.model.TransactionResponse;
import com.retail.rewardpointcalc.repository.CustomerRepository;
import com.retail.rewardpointcalc.repository.TransactionRepository;
import com.retail.rewardpointcalc.service.RewardPointCalcService;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

	@InjectMocks
	TransactionController transactionController;

	@Mock
	TransactionRepository transactionRepository;

	@Mock
	// TransactionService transactionService;
	private RewardPointCalcService rewardPointCalcService;

	@InjectMocks
	// TransactionService transactionService;
	private RewardPointCalcService rewardPntCalcService;

	@Test
	public void testCreateCustomer() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		TransactionRequest transreq = new TransactionRequest();
		transreq.setCustomerId(1);
		transreq.setRewardpoints(50);
		transreq.setTransAmt(100);
		transreq.setTransDate(new Date(0));
		transreq.setTransId(1l);

		when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
		when(rewardPointCalcService.getCustomerRewardPoint(any(Integer.class))).thenReturn(any(Integer.class));
		// when(rewardPointCalcService.getCustomerRewardPoint(100)).thenReturn(50);
		ResponseEntity<TransactionResponse> responseEntity = transactionController.createTransaction(transreq);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
		assertThat(responseEntity.getBody().equals(TransactionResponse.class));

	}

	@Test
	public void testRewardPoint() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		TransactionRequest transreq = new TransactionRequest();
		transreq.setCustomerId(1);
		transreq.setRewardpoints(50);
		transreq.setTransAmt(100);
		transreq.setTransDate(new Date(0));
		transreq.setTransId(1l);

		when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

		ResponseEntity<TransactionResponse> responseEntity = transactionController.createTransaction(transreq);
		assertThat(rewardPntCalcService.getCustomerRewardPoint(100)).isEqualTo(50);
		assertThat(responseEntity.getBody().equals(TransactionResponse.class));

	}

	@Test
	public void testRewardPoint1() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		TransactionRequest transreq = new TransactionRequest();
		transreq.setCustomerId(1);
		transreq.setRewardpoints(50);
		transreq.setTransAmt(100);
		transreq.setTransDate(new Date(0));
		transreq.setTransId(1l);

		when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

		ResponseEntity<TransactionResponse> responseEntity = transactionController.createTransaction(transreq);
		assertThat(rewardPntCalcService.getCustomerRewardPoint(120)).isEqualTo(90);
		assertThat(responseEntity.getBody().equals(TransactionResponse.class));

	}

	@Test
	public void testRewardPoint2() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		TransactionRequest transreq = new TransactionRequest();
		transreq.setCustomerId(1);
		transreq.setRewardpoints(50);
		transreq.setTransAmt(100);
		transreq.setTransDate(new Date(0));
		transreq.setTransId(1l);
		when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

		transactionController.createTransaction(transreq);

		Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
			rewardPntCalcService.getCustomerRewardPoint(-100);
		});
		assertEquals(IllegalArgumentException.class, throwable.getClass());
		assertEquals("Parameter 'transAmt' cannot be null or negative", throwable.getMessage());
	}

	@Test
	public void testRewardPoint3() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		TransactionRequest transreq = new TransactionRequest();
		transreq.setCustomerId(1);
		transreq.setRewardpoints(50);
		transreq.setTransAmt(100);
		transreq.setTransDate(new Date(0));
		transreq.setTransId(1l);

		when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

		transactionController.createTransaction(transreq);

		Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
			rewardPntCalcService.getCustomerRewardPoint(null);
		});
		assertEquals(IllegalArgumentException.class, throwable.getClass());
		assertEquals("Parameter 'transAmt' cannot be null or negative", throwable.getMessage());
	}

}
