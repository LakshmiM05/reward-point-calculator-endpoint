package com.retail.rewardpointcalc.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.*;
import java.time.Period;
import java.time.YearMonth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import com.retail.rewardpointcalc.entity.Customer;

import com.retail.rewardpointcalc.model.CustomerRequest;
import com.retail.rewardpointcalc.model.CustomerResponse;
import com.retail.rewardpointcalc.model.CustomerResponseMapper;

import com.retail.rewardpointcalc.model.TransactionResponse;

import com.retail.rewardpointcalc.repository.CustomerRepository;

import jakarta.validation.Valid;

@RestController
public class CustomerController {
	@Autowired
	private CustomerRepository repository;

	CustomerResponse custResponse = null;
	TransactionResponse transResponse = null;
	List<TransactionResponse> list = new ArrayList<TransactionResponse>();
	//List<TransactionResponse> listTrans = null;
	int totalRewardPoints = 0;
	int rewardpointsum3month = 0;

	@PostMapping("/customer")

	public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {

		Customer cutomerEntity = Customer.builder().address(customerRequest.getAddress())
				.customerName(customerRequest.getCustomerName()).emailID(customerRequest.getEmailID())
				.phone(Integer.parseInt(customerRequest.getPhone())).created_at(new Date(System.currentTimeMillis()))
				.created_by(customerRequest.getCustomerName()).build();

		cutomerEntity = repository.save(cutomerEntity);
		CustomerResponse customerResponse = new CustomerResponse();
		customerResponse = CustomerResponseMapper.mapToCustomerResponse(cutomerEntity, customerResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(customerResponse);
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<CustomerResponse> getCustomerData(@PathVariable int customerId) {
		Optional<Customer> customer = repository.findById(customerId);
		CustomerResponse customerResponse = new CustomerResponse();
		if (customer.isPresent()) {
			customerResponse = CustomerResponseMapper.mapToCustomerResponse(customer.get(), customerResponse);
			return ResponseEntity.status(HttpStatus.OK).body(customerResponse);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	@GetMapping("/customer/{customerId}/transactions")
	public ResponseEntity<CustomerResponse> getCustomerAndTransaction(@PathVariable int customerId) throws Exception {
		Optional<Customer> customer = repository.findByCustomerId(customerId);
		totalRewardPoints = 0;
		CustomerResponse customerResponse = new CustomerResponse();
		if (customer.isPresent()) {
			customerResponse = CustomerResponseMapper.mapToCustomerResponse(customer.get(), customerResponse);
			if (customerResponse.getTransList().isPresent()) {
				customerResponse.getTransList().get().forEach(transaction -> {
					totalRewardPoints = calculateTotRewardPoints(transaction.getRewardpoints());
				});

				customerResponse.setTotalRewardPoints(totalRewardPoints);
			} else {
				throw new Exception();
			}

			return ResponseEntity.status(HttpStatus.OK).body(customerResponse);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(customerResponse);
		}
	}

	@GetMapping("/customer/{customerId}/rewardpointsum")
	public ResponseEntity<CustomerResponse> getCustomerWith3MonthPoint(@PathVariable int customerId) {
		totalRewardPoints = 0;
		rewardpointsum3month = 0;
		Optional<Customer> customer = repository.findByCustomerId(customerId);
		CustomerResponse customerResponse = new CustomerResponse();
		if (customer.isPresent()) {
			customerResponse = CustomerResponseMapper.mapToCustomerResponse(customer.get(), customerResponse);
			if (customerResponse.getTransList().isPresent()) {

				customerResponse.getTransList().get().forEach(transaction -> {
					totalRewardPoints = calculateTotRewardPoints(transaction.getRewardpoints());
				});

				LocalDate threeMonthsAgo = LocalDate.now().minus(Period.ofMonths(3));

				List<TransactionResponse> threemonthList = customerResponse.getTransList().get().stream()
						.filter(transaction -> convert(transaction.getTransDate()).isAfter(threeMonthsAgo))
						.collect(Collectors.toList());
				rewardpointsum3month = threemonthList.stream().mapToInt(trans -> trans.getRewardpoints()).sum();
				customerResponse.setRewardpoints_3month(rewardpointsum3month);

				customerResponse.setTotalRewardPoints(totalRewardPoints);
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				Map<Object, List<TransactionResponse>> monthwiseMap = customerResponse.getTransList().get().stream()
						.collect(Collectors.groupingBy(
								transaction -> YearMonth.parse(transaction.getTransDate().toString(), dtf)));
				customerResponse.setMonthWiseRewardPoint(iterateUsingIteratorAndEntrySet(monthwiseMap));

			}
			return ResponseEntity.status(HttpStatus.OK).body(customerResponse);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(customerResponse);
		}

	}

	@GetMapping("/customer/allcustomers")
	public ResponseEntity<List<CustomerResponse>> getAllCustomer() {
		totalRewardPoints = 0;
		rewardpointsum3month = 0;

		List<CustomerResponse> customerResponseList = new ArrayList<CustomerResponse>();

		List<Customer> customerList = repository.findAll();
		if (customerList != null) {
			customerList.forEach(customer -> {
				totalRewardPoints = 0;
				CustomerResponse customerResponse = new CustomerResponse();

				customerResponse = CustomerResponseMapper.mapToCustomerResponse(customer, customerResponse);

				if (customerResponse.getTransList() != null) {

					customerResponse.getTransList().get().forEach(transaction -> {
						totalRewardPoints = calculateTotRewardPoints(transaction.getRewardpoints());
					});

					LocalDate threeMonthsAgo = LocalDate.now().minus(Period.ofMonths(3));

					List<TransactionResponse> threemonthList = customerResponse.getTransList().get().stream()
							.filter(transaction -> convert(transaction.getTransDate()).isAfter(threeMonthsAgo))
							.collect(Collectors.toList());

					rewardpointsum3month = threemonthList.stream().mapToInt(trans -> trans.getRewardpoints()).sum();

					customerResponse.setRewardpoints_3month(rewardpointsum3month);
					customerResponse.setTotalRewardPoints(totalRewardPoints);
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

					Map<Object, List<TransactionResponse>> monthwiseMap = customerResponse.getTransList().get().stream()
							.collect(Collectors.groupingBy(
									transaction -> YearMonth.parse(transaction.getTransDate().toString(), dtf)));
					customerResponse.setMonthWiseRewardPoint(iterateUsingIteratorAndEntrySet(monthwiseMap));
					customerResponseList.add(customerResponse);
				}

			});
			return ResponseEntity.status(HttpStatus.OK).body(customerResponseList);
		}

		else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(customerResponseList);
		}

	}

	Optional<Map<Object, Integer>> iterateUsingIteratorAndEntrySet(Map<Object, List<TransactionResponse>> map) {
		int sum = 0;
		Map<Object, Integer> montwiseRewardPointMap = new HashMap<Object, Integer>();
		if (map != null && map.entrySet() != null) {
			Iterator<Map.Entry<Object, List<TransactionResponse>>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Object, List<TransactionResponse>> list = iterator.next();
				sum = list.getValue().stream().mapToInt(trans -> trans.getRewardpoints()).sum();
				montwiseRewardPointMap.put(list.getKey(), sum);
			}
			return Optional.of(montwiseRewardPointMap);
		}
		return null;
	}

	public LocalDate convert(java.util.Date date) {
		LocalDate ld = new java.sql.Date(date.getTime()).toLocalDate();
		return ld;
	}

	private int calculateTotRewardPoints(int rewardPoints) {
		totalRewardPoints = totalRewardPoints + rewardPoints;
		return totalRewardPoints;
	}

}
