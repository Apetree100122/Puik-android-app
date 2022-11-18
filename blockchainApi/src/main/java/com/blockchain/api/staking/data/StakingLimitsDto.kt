package com.blockchain.api.staking.data

import kotlinx.serialization.Serializable

@Serializable
data class StakingLimitsMapDto(
    val limits: Map<String, StakingLimitsDto>
)

@Serializable
data class StakingLimitsDto(
    val minDepositValue: String,
    val bondingDays: Int,
    val unbondingDays: Int?,
    val disabledWithdrawals: Boolean?
)
