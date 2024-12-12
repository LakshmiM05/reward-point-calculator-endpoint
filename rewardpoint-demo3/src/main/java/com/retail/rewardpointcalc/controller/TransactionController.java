package com.retail.rewardpointcalc.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.retail.rewardpointcalc.entity.Transaction;
import com.retail.rewardpointcalc.model.TransactionRequest;
import com.retail.rewardpointcalc.model.TransactionResponse;
import com.retail.rewardpointcalc.model.TransactionResponseMapper;
import com.retail.rewardpointcalc.repository.TransactionRepository;
import com.retail.rewardpointcalc.service.RewardPointCalcService;

import jakarta.validation.Valid;

@RestController
public class TransactionController {

	@Autowired
	private TransactionRepository repository;

	@Autowired
	private RewardPointCalcService rewardPointCalcService;
	

	@PostMapping("/transaction")
	public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest transactionRequest) throws IllegalArgumentException {

		Transaction transEntity = Transaction.builder().customerId(transactionRequest.getCustomerId())
				.rewardPoints(rewardPointCalcService.getCustomerRewardPoint(transactionRequest.getTransAmt()))
				.transAmt(transactionRequest.getTransAmt()).transDate(new Date(System.currentTimeMillis())).build();
		Transaction transaction = repository.save(transEntity);
		TransactionResponse transactionResponse = new TransactionResponse();
		 transactionResponse = TransactionResponseMapper.mapToTransactionResponse(transaction, transactionResponse);
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
	}

	@GetMapping("/transaction/{customerId}")
	public ResponseEntity<List<TransactionResponse>> getCustomerData(@PathVariable int customerId) {
		List<Transaction> transactionList = repository.findByCustomerId(customerId);
		List<TransactionResponse> transactionResponseList = new ArrayList<TransactionResponse>();

		transactionList.forEach(trans -> {
			TransactionResponse transactionResponse = new TransactionResponse();
			transactionResponse = TransactionResponseMapper.mapToTransactionResponse(trans, transactionResponse);
			transactionResponseList.add(transactionResponse);
		});

		return ResponseEntity.status(HttpStatus.OK).body(transactionResponseList);

	}
}
