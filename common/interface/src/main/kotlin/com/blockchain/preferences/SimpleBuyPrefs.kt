package com.blockchain.preferences

interface SimpleBuyPrefs {
    fun simpleBuyState(): String?
    fun updateSimpleBuyState(simpleBuyState: String)
    fun clearBuyState()
    fun cardState(): String?
    fun updateCardState(cardState: String)
    fun clearCardState()
    fun updateSupportedCards(cardTypes: String)
    fun getSupportedCardTypes(): String?

    fun getLastAmount(pair: String): String
    fun setLastAmount(pair: String, amount: String)
    fun getLastPaymentMethodId(): String
    fun setLastPaymentMethodId(paymentMethodId: String)

    var hasCompletedAtLeastOneBuy: Boolean
    var buysCompletedCount: Int
    var isFirstTimeBuyer: Boolean
}
