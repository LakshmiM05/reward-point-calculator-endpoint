package com.retail.rewardpointcalc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.IOException;

import java.time.YearMonth;
import java.util.ArrayList;

import java.util.List;

import java.util.Optional;

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

import com.fasterxml.jackson.core.StreamReadFeature;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.retail.rewardpointcalc.controller.CustomerController;
import com.retail.rewardpointcalc.entity.Customer;
import com.retail.rewardpointcalc.entity.Transaction;
import com.retail.rewardpointcalc.model.CustomerRequest;
import com.retail.rewardpointcalc.model.CustomerResponse;

import com.retail.rewardpointcalc.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

	@InjectMocks
	CustomerController customerController;

	@Mock
	CustomerRepository customerRepository;

	@Test
	public void testCreateCustomer() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		CustomerRequest customer = new CustomerRequest("customerName", "address", "howtodoinjava@gmail.com",
				"1234567889");
		when(customerRepository.save(any(Customer.class))).thenReturn(new Customer());
		ResponseEntity<CustomerResponse> responseEntity = customerController.createCustomer(customer);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
		assertThat(responseEntity.getBody().equals(CustomerResponse.class));

	}

	@Test
	public void testGetCustomerDataById() {
		/*
		 * CustomerRequest customer = new CustomerRequest("customerName", "address",
		 * "howtodoinjava@gmail.com", "1234567889"); List<TransactionResponse> transList
		 * = new ArrayList<TransactionResponse>();
		 */

		Optional<Customer> custresponse = Optional.of(new Customer(1, "customerName", "address",
				"howtodoinjava@gmail.com", 1234567889, new ArrayList<Transaction>(), null, "customerName", null, null));

		when(customerRepository.findById(1)).thenReturn(custresponse);

		ResponseEntity<CustomerResponse> customerResponse = customerController.getCustomerData(1);
		customerResponse.getBody().getCustomerName();

		assertThat(customerResponse.getBody().getCustomerName()).isEqualTo(custresponse.get().getCustomerName());
		assertThat(customerResponse.getBody().getCustomerId() == (custresponse.get().getCustomerId()));
	}

	@Test
	public void testGetRewardSum() {

		Transaction transAction = new Transaction();
		List<Transaction> transactionList = new ArrayList<Transaction>();
		transAction.setTransId(1);
		transAction.setCustomer(null);
		transAction.setCustomerId(1);
		transAction.setRewardPoints(90);
		transAction.setTransAmt(120);
		transAction.setTransDate(new java.sql.Date(0));
		transactionList.add(transAction);
		Optional<Customer> custresponse = Optional.of(new Customer(1, "customerName", "address",
				"howtodoinjava@gmail.com", 1234567889, transactionList, null, "customerName", null, null));

		when(customerRepository.findByCustomerId(1)).thenReturn(custresponse);

		ResponseEntity<CustomerResponse> customerResponse = customerController.getCustomerWith3MonthPoint(1);
		customerResponse.getBody().getCustomerName();

		assertThat(customerResponse.getBody().getCustomerName()).isEqualTo(custresponse.get().getCustomerName());
		assertThat(customerResponse.getBody().getCustomerId() == (custresponse.get().getCustomerId()));
		// assertThat(customerResponse.getBody().getTransList().get().getRewardpoints()
		// == 90);
	}

	@InjectMocks
	private ObjectMapper objectMapper;
	Integer monthRewardPoint = null;

	@Test
	public void testWithJsonFile() throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(new FileReader("src/test/resources/customer_trans_jpa_resp.json"));

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Customer customer = mapper.treeToValue(jsonNode, Customer.class);

		when(customerRepository.findByCustomerId(1)).thenReturn(Optional.of(customer));

		ResponseEntity<CustomerResponse> customerResponseEntity = customerController.getCustomerWith3MonthPoint(1);
		customerResponseEntity.getBody().getCustomerName();

		assertThat(customerResponseEntity.getBody().getCustomerName()).isEqualTo("Customer1");
		assertThat(customerResponseEntity.getBody().getCustomerId() == 1);

		customerResponseEntity.getBody().getMonthWiseRewardPoint().stream().forEach(map1 -> {
			monthRewardPoint = map1.get(YearMonth.of(2024, 11));

		});

		assertThat(monthRewardPoint == 490);
		assertThat(customerResponseEntity.getBody().getRewardpoints_3month() == 490);
		assertThat(customerResponseEntity.getBody().getTotalRewardPoints() == 580);

	}

	@Test
	public void testWithJsonString() throws IOException {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		String customerString = "{\"customerId\":1,\"customerName\":\"Customer1\",\"address\":\"Address1\",\"emailID\":\"email@gmail.com\",\"phone\":123456789,\"created_at\":\"2024-11-27\",\"created_by\":\"Customer1\",\"updated_at\":null,\"updated_by\":null,\"transActionList\":[{\"transId\":1,\"transDate\":\"2024-11-27\",\"transAmt\":120,\"rewardpoints\":90,\"customerId\":1},{\"transId\":2,\"transDate\":\"2024-11-27\",\"transAmt\":150,\"rewardpoints\":150,\"customerId\":1},{\"transId\":3,\"transDate\":\"2024-06-27\",\"transAmt\":120,\"rewardpoints\":90,\"customerId\":1},{\"transId\":52,\"transDate\":\"2024-11-28\",\"transAmt\":200,\"rewardpoints\":250,\"customerId\":1}]}";

		Customer customer = gson.fromJson(customerString, Customer.class);

		when(customerRepository.findByCustomerId(1)).thenReturn(Optional.of(customer));

		ResponseEntity<CustomerResponse> customerResponseEntity = customerController.getCustomerWith3MonthPoint(1);
		customerResponseEntity.getBody().getCustomerName();

		assertThat(customerResponseEntity.getBody().getCustomerName()).isEqualTo("Customer1");
		assertThat(customerResponseEntity.getBody().getCustomerId() == 1);

		customerResponseEntity.getBody().getMonthWiseRewardPoint().stream().forEach(map1 -> {
			monthRewardPoint = map1.get(YearMonth.of(2024, 11));
		});
		assertThat(monthRewardPoint == 490);
		assertThat(customerResponseEntity.getBody().getRewardpoints_3month() == 490);
		assertThat(customerResponseEntity.getBody().getTotalRewardPoints() == 580);

	}

	@Test
	public void testWithJsonString1() throws IOException {

		// Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// String customerString =
		// "{\"customerId\":1,\"customerName\":\"Customer1\",\"address\":\"Address1\",\"emailID\":\"email@gmail.com\",\"phone\":123456789,\"created_at\":\"2024-11-27\",\"created_by\":\"Customer1\",\"updated_at\":null,\"updated_by\":null,\"transActionList\":[{\"transId\":1,\"transDate\":\"2024-11-27\",\"transAmt\":120,\"rewardpoints\":90,\"customerId\":1},{\"transId\":2,\"transDate\":\"2024-11-27\",\"transAmt\":150,\"rewardpoints\":150,\"customerId\":1},{\"transId\":3,\"transDate\":\"2024-06-27\",\"transAmt\":120,\"rewardpoints\":90,\"customerId\":1},{\"transId\":52,\"transDate\":\"2024-11-28\",\"transAmt\":200,\"rewardpoints\":250,\"customerId\":1}]}";
		String customerString = "\"{\\\"customerId\\\":1,\\\"customerName\\\":\\\"Customer1\\\",\\\"address\\\":\\\"Address1\\\",\\\"emailID\\\":\\\"email@gmail.com\\\",\\\"phone\\\":123456789,\\\"created_at\\\":\\\"2024-11-27\\\",\\\"created_by\\\":\\\"Customer1\\\",\\\"updated_at\\\":null,\\\"updated_by\\\":null,\\\"transActionList\\\":[{\\\"transId\\\":1,\\\"transDate\\\":\\\"2024-11-27\\\",\\\"transAmt\\\":120,\\\"rewardpoints\\\":90,\\\"customerId\\\":1},{\\\"transId\\\":2,\\\"transDate\\\":\\\"2024-11-27\\\",\\\"transAmt\\\":150,\\\"rewardpoints\\\":150,\\\"customerId\\\":1},{\\\"transId\\\":3,\\\"transDate\\\":\\\"2024-06-27\\\",\\\"transAmt\\\":120,\\\"rewardpoints\\\":90,\\\"customerId\\\":1},{\\\"transId\\\":52,\\\"transDate\\\":\\\"2024-11-28\\\",\\\"transAmt\\\":200,\\\"rewardpoints\\\":250,\\\"customerId\\\":1}]}\"";

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonNode = objectMapper.readTree(customerString);

		objectMapper = JsonMapper.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build();

		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Customer customer = objectMapper.readValue(jsonNode.textValue(), Customer.class);

		when(customerRepository.findByCustomerId(1)).thenReturn(Optional.of(customer));

		ResponseEntity<CustomerResponse> customerResponseEntity = customerController.getCustomerWith3MonthPoint(1);
		customerResponseEntity.getBody().getCustomerName();

		assertThat(customerResponseEntity.getBody().getCustomerName()).isEqualTo("Customer1");
		assertThat(customerResponseEntity.getBody().getCustomerId() == 1);

		customerResponseEntity.getBody().getMonthWiseRewardPoint().stream().forEach(map1 -> {
			monthRewardPoint = map1.get(YearMonth.of(2024, 06));
		});
		assertThat(monthRewardPoint == null);
		assertThat(customerResponseEntity.getBody().getRewardpoints_3month() == 490);
		assertThat(customerResponseEntity.getBody().getTotalRewardPoints() == 580);

	}
	
	

}
