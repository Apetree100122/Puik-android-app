package piuk.blockchain.android.rating.data.api

import com.blockchain.outcome.Outcome
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import piuk.blockchain.android.rating.data.model.AppRatingApiKeys
import piuk.blockchain.android.rating.data.model.RespondentResponse
import piuk.blockchain.android.rating.data.model.RespondentResponseData
import piuk.blockchain.android.rating.data.model.SurveyBody
import piuk.blockchain.android.rating.domain.model.AppRating

@OptIn(ExperimentalCoroutinesApi::class)
class AppRatingApiTest {
    private val appRatingEndpoints = mockk<AppRatingEndpoints>()

    private val appRatingApi: AppRatingApi = AppRatingApiService(
        appRatingEndpoints = appRatingEndpoints
    )

    private val respondentResponse = mockk<RespondentResponse>()
    private val appRating = AppRating(rating = 3, feedback = "feedback")
    private val surveyBody = SurveyBody.build(appRating)
    private val apiKeys = AppRatingApiKeys(surveyId = "surveyId", masterKey = "masterKey", key = "key")

    @Test
    fun `WHEN postRatingData is called, THEN arguments should be correctly spread to the endpoint`() = runTest {
        coEvery { appRatingEndpoints.postRatingData(any(), any(), any(), any()) } returns
            Outcome.Success(respondentResponse)
        every { respondentResponse.data } returns RespondentResponseData(succeeded = true)

        appRatingApi.postRatingData(apiKeys, appRating)

        coVerify {
            appRatingEndpoints.postRatingData(
                masterKey = apiKeys.masterKey,
                key = apiKeys.key,
                surveyId = apiKeys.surveyId,
                surveyBody = surveyBody
            )
        }
    }
}
