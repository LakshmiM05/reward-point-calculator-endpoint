package com.retail.rewardpointcalc.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.retail.rewardpointcalc.model.RewardPointRequest;
import com.retail.rewardpointcalc.model.RewardPointResponse;

@Service
public class RewardPointCalcService {
	
	
	static final Integer MIN_TRANS_AMT=50;  
	static final Integer MAX_TRANS_AMT=100;  

	public int getCustomerRewardPoint(Integer transAmt) throws IllegalArgumentException {
		Integer rewardPoint = 0;
	
		if(transAmt!=null&&transAmt>=0) {
		if (transAmt >= MIN_TRANS_AMT) {
			// Calculate reward point for amt lesser than 100
			Integer rewardPointBelowMax = calculateRewardPoint(50, 1);
			// Calculate reward point for amt greater than 100
			Integer rewardPointAboveMax = calculateRewardPoint(transAmt - 100, 2);
			rewardPoint = rewardPointBelowMax + rewardPointAboveMax;
		} else if (transAmt > MAX_TRANS_AMT) {
			rewardPoint = calculateRewardPoint(transAmt - MIN_TRANS_AMT, 1);
		}
		}
		else{
			throw new IllegalArgumentException("Parameter 'transAmt' cannot be null or negative");
		}
		return rewardPoint;
	}

	private int calculateRewardPoint(int eligbleAmt, int point) {
		int rewardPoint = eligbleAmt * point;
		return rewardPoint;
	}

	public RewardPointResponse buildRewardPointResponse(RewardPointRequest rewardPointRequest) throws IllegalArgumentException {

		RewardPointResponse rewardPointResponse = new RewardPointResponse();
		rewardPointResponse.setCustomerId(rewardPointRequest.getCustomerId());
		rewardPointResponse.setCustomerName(rewardPointRequest.getCustomerName());
		rewardPointResponse.setMonthlyTotalRewardPoint(getCustomerRewardPoint(rewardPointRequest.getTransActionAmt()));
		
		List<Integer> rewardPointList = new ArrayList<Integer>();
		if(rewardPointRequest.getTransActionAmtList().isEmpty()) {
			throw new IllegalArgumentException("Parameter 'transAmt' cannot be null or negative");
		}
		else{
			rewardPointRequest.getTransActionAmtList().forEach(transAmtValue -> {			
				rewardPointList.add(getCustomerRewardPoint(transAmtValue));			
		});	

		Integer sum = rewardPointList.stream().mapToInt(Integer::intValue).sum();
		rewardPointResponse.setMonthlyTotalRewardPoint(sum);
		}

		return rewardPointResponse;

	}

}
