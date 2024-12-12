package com.retail.rewardpointcalc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.retail.rewardpointcalc.model.RewardPointRequest;
import com.retail.rewardpointcalc.model.RewardPointResponse;
import com.retail.rewardpointcalc.service.RewardPointCalcService;

@RestController
public class RewardPointCalcController {

	@Autowired
	RewardPointCalcService rewardPointCalcService;	
	

	@PostMapping("/rewardpoint")
	public RewardPointResponse findRewardPoint(@RequestBody RewardPointRequest rewardPointRequest) throws IllegalArgumentException {
		return rewardPointCalcService.buildRewardPointResponse(rewardPointRequest);
	}

}
